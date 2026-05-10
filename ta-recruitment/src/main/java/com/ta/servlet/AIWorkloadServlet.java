package com.ta.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.dao.TAProfileDAO;
import com.ta.dao.UserDAO;
import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.model.TAProfile;
import com.ta.model.User;
import com.ta.util.AIService;
import com.ta.util.ConfigDAO;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/**
 * US-A02: AI-assisted workload balancing.
 * Separates anomaly detection (always shown) from redistribution recommendations
 * (AI-powered when API key configured, rule-based otherwise).
 */
@WebServlet("/admin/ai-workload")
public class AIWorkloadServlet extends HttpServlet {

    public static class WorkloadRecommendation implements Serializable {
        private final String applicationId;
        private final String fromTaId;
        private final String fromTaName;
        private final String toTaId;
        private final String toTaName;
        private final String jobLabel;
        private final int fromCurrentHours;
        private final int fromNewHours;
        private final int toCurrentHours;
        private final int toNewHours;
        private final int confidence;
        private final String reasoning;

        public WorkloadRecommendation(String applicationId,
                String fromTaId, String fromTaName,
                String toTaId, String toTaName,
                String jobLabel,
                int fromCurrentHours, int fromNewHours,
                int toCurrentHours, int toNewHours,
                int confidence, String reasoning) {
            this.applicationId    = applicationId;
            this.fromTaId         = fromTaId;
            this.fromTaName       = fromTaName;
            this.toTaId           = toTaId;
            this.toTaName         = toTaName;
            this.jobLabel         = jobLabel;
            this.fromCurrentHours = fromCurrentHours;
            this.fromNewHours     = fromNewHours;
            this.toCurrentHours   = toCurrentHours;
            this.toNewHours       = toNewHours;
            this.confidence       = confidence;
            this.reasoning        = reasoning;
        }

        public String getApplicationId()  { return applicationId; }
        public String getFromTaId()       { return fromTaId; }
        public String getFromTaName()     { return fromTaName; }
        public String getToTaId()         { return toTaId; }
        public String getToTaName()       { return toTaName; }
        public String getJobLabel()       { return jobLabel; }
        public int getFromCurrentHours()  { return fromCurrentHours; }
        public int getFromNewHours()      { return fromNewHours; }
        public int getToCurrentHours()    { return toCurrentHours; }
        public int getToNewHours()        { return toNewHours; }
        public int getConfidence()        { return confidence; }
        public String getReasoning()      { return reasoning; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User current = SessionUtil.getCurrentUser(req);
        if (current == null || !"ADMIN".equalsIgnoreCase(current.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        String dataDir = SessionUtil.getDataDir(req);
        UserDAO userDAO = new UserDAO(dataDir);
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
        JobDAO jobDAO = new JobDAO(dataDir);
        TAProfileDAO profileDAO = new TAProfileDAO(dataDir);

        // ── 1. Build workload maps ────────────────────────────────────────────
        Map<String, List<Application>> taApps  = new LinkedHashMap<>();
        Map<String, Integer>           taHours = new LinkedHashMap<>();
        List<User> allTas = new ArrayList<>();

        for (User u : userDAO.findAll()) {
            if ("TA".equalsIgnoreCase(u.getRole())) {
                allTas.add(u);
                taApps.put(u.getUserId(), new ArrayList<>());
                taHours.put(u.getUserId(), 0);
            }
        }
        for (Application a : appDAO.findAll()) {
            if ("ACCEPTED".equalsIgnoreCase(a.getStatus()) && taApps.containsKey(a.getTaUserId())) {
                taApps.get(a.getTaUserId()).add(a);
                taHours.put(a.getTaUserId(),
                        taHours.get(a.getTaUserId()) + AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT);
            }
        }

        // ── 2. Detect overloaded TAs (always, regardless of candidates) ───────
        // Uses > so TAs exactly at limit are also flagged
        List<AdminWorkloadServlet.TaWorkloadRow> overloadedRows = new ArrayList<>();
        for (User ta : allTas) {
            int hrs = taHours.get(ta.getUserId());
            if (hrs <= AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS) continue;
            TAProfile p = profileDAO.findByUserId(ta.getUserId());
            Set<String> labels = new LinkedHashSet<>();
            for (Application a : taApps.get(ta.getUserId())) {
                labels.add(buildJobLabel(jobDAO.findById(a.getJobId()), a.getJobId()));
            }
            overloadedRows.add(new AdminWorkloadServlet.TaWorkloadRow(
                    ta.getUserId(), ta.getUsername(), displayName(p, ta),
                    taApps.get(ta.getUserId()).size(),
                    labels.isEmpty() ? "—" : String.join(", ", labels),
                    hrs, hrs > AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS));
        }

        // ── 3. Generate recommendations ───────────────────────────────────────
        ConfigDAO config = new ConfigDAO(dataDir);
        String apiKey    = config.get("ai.api.key");
        boolean aiPowered = false;
        String  aiError   = null;
        List<WorkloadRecommendation> recommendations = new ArrayList<>();

        // Sort TAs by hours desc for processing
        List<User> sortedTas = new ArrayList<>(allTas);
        sortedTas.sort((a, b) -> taHours.get(b.getUserId()) - taHours.get(a.getUserId()));

        if (apiKey != null && !apiKey.trim().isEmpty() && !overloadedRows.isEmpty()) {
            try {
                String prompt = buildPrompt(allTas, taHours, taApps, jobDAO, profileDAO);
                String aiText = AIService.call(config, prompt);
                recommendations = parseAIRecommendations(aiText, appDAO, taHours, taApps,
                        jobDAO, profileDAO, allTas);
                aiPowered = true;
            } catch (Exception e) {
                aiError = e.getMessage();
                recommendations = buildRuleBasedRecs(sortedTas, taHours, taApps, jobDAO, profileDAO);
            }
        } else {
            recommendations = buildRuleBasedRecs(sortedTas, taHours, taApps, jobDAO, profileDAO);
        }

        req.setAttribute("overloadedRows",   overloadedRows);
        req.setAttribute("recommendations",  recommendations);
        req.setAttribute("aiPowered",        aiPowered);
        req.setAttribute("aiError",          aiError);
        req.setAttribute("hasApiKey",        apiKey != null && !apiKey.trim().isEmpty());
        req.setAttribute("workloadLimitHours", AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS);
        req.getRequestDispatcher("/admin/ai-workload.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User current = SessionUtil.getCurrentUser(req);
        if (current == null || !"ADMIN".equalsIgnoreCase(current.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        String applicationId = req.getParameter("applicationId");
        String newTaId       = req.getParameter("newTaId");
        String fromTaId      = req.getParameter("fromTaId");

        if (applicationId == null || applicationId.trim().isEmpty()
                || newTaId == null || newTaId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/ai-workload?error=invalid");
            return;
        }

        String dataDir = SessionUtil.getDataDir(req);
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
        Application app = appDAO.findById(applicationId.trim());

        if (app == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/ai-workload?error=notfound");
            return;
        }

        String prevTaId = (fromTaId != null && !fromTaId.trim().isEmpty())
                ? fromTaId.trim() : app.getTaUserId();
        app.setTaUserId(newTaId.trim());
        app.setReviewNote("Reassigned from TA " + prevTaId
                + " by AI workload balancing on " + LocalDate.now());
        appDAO.update(app);

        resp.sendRedirect(req.getContextPath() + "/admin/ai-workload?success=1");
    }

    // ── AI integration ────────────────────────────────────────────────────────

    private String buildPrompt(List<User> allTas, Map<String, Integer> taHours,
            Map<String, List<Application>> taApps, JobDAO jobDAO, TAProfileDAO profileDAO) {
        int limit = AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS;
        int hpa   = AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT;
        StringBuilder sb = new StringBuilder();
        sb.append("You are an AI assistant for a TA (Teaching Assistant) recruitment system.\n");
        sb.append("Policy: each accepted assignment = ").append(hpa)
          .append(" hrs estimated work. The limit is ").append(limit).append(" hrs per TA.\n\n");

        sb.append("OVERLOADED TAs (above ").append(limit).append(" hrs) — need reassignment:\n");
        for (User ta : allTas) {
            int hrs = taHours.get(ta.getUserId());
            if (hrs <= limit) continue;
            TAProfile p = profileDAO.findByUserId(ta.getUserId());
            sb.append("- ").append(displayName(p, ta)).append(" [").append(ta.getUserId())
              .append("]: ").append(hrs).append(" hrs\n  Assignments:\n");
            for (Application a : taApps.get(ta.getUserId())) {
                Job j = jobDAO.findById(a.getJobId());
                sb.append("    - applicationId=").append(a.getApplicationId())
                  .append(", job=").append(buildJobLabel(j, a.getJobId())).append("\n");
            }
        }

        sb.append("\nAVAILABLE TAs (can take one more assignment without exceeding ").append(limit).append(" hrs):\n");
        boolean anyAvailable = false;
        for (User ta : allTas) {
            int hrs = taHours.get(ta.getUserId());
            if (hrs + hpa > limit) continue;
            TAProfile p = profileDAO.findByUserId(ta.getUserId());
            sb.append("- ").append(displayName(p, ta)).append(" [").append(ta.getUserId())
              .append("]: ").append(hrs).append(" hrs, ").append(limit - hrs)
              .append(" hrs spare\n");
            anyAvailable = true;
        }
        if (!anyAvailable) sb.append("(none — consider recruiting additional TAs)\n");

        sb.append("\nRecommend which assignments to transfer to reduce overloaded TAs to below the limit.\n");
        sb.append("Return ONLY a valid JSON array (no markdown, no extra text):\n");
        sb.append("[{\"applicationId\":\"APP001\",\"toTaId\":\"U004\","
                + "\"reasoning\":\"One sentence explaining the benefit.\"}]\n");
        sb.append("If no redistribution is possible, return: []\n");
        return sb.toString();
    }

    private List<WorkloadRecommendation> parseAIRecommendations(String aiText,
            ApplicationDAO appDAO, Map<String, Integer> taHours,
            Map<String, List<Application>> taApps,
            JobDAO jobDAO, TAProfileDAO profileDAO, List<User> allTas) {

        Map<String, User> userById = new HashMap<>();
        for (User u : allTas) userById.put(u.getUserId(), u);

        List<WorkloadRecommendation> result = new ArrayList<>();
        try {
            int s = aiText.indexOf('['), e = aiText.lastIndexOf(']');
            String json = (s >= 0 && e > s) ? aiText.substring(s, e + 1) : "[]";
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();

            for (JsonElement elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                String appId    = obj.has("applicationId") ? obj.get("applicationId").getAsString() : null;
                String toTaId   = obj.has("toTaId")        ? obj.get("toTaId").getAsString()        : null;
                String reasoning = obj.has("reasoning")    ? obj.get("reasoning").getAsString()     : "";
                if (appId == null || toTaId == null) continue;

                Application app = appDAO.findById(appId);
                if (app == null) continue;
                User toTa   = userById.get(toTaId);
                User fromTa = userById.get(app.getTaUserId());
                if (toTa == null || fromTa == null) continue;

                TAProfile fromP = profileDAO.findByUserId(fromTa.getUserId());
                TAProfile toP   = profileDAO.findByUserId(toTa.getUserId());
                Job job = jobDAO.findById(app.getJobId());
                String jobLabel = buildJobLabel(job, app.getJobId());

                int curFrom = taHours.getOrDefault(fromTa.getUserId(), 0);
                int curTo   = taHours.getOrDefault(toTa.getUserId(), 0);
                int newFrom = curFrom - AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT;
                int newTo   = curTo   + AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT;
                int conf    = Math.max(70, 95 - result.size() * 5);

                result.add(new WorkloadRecommendation(appId,
                        fromTa.getUserId(), displayName(fromP, fromTa),
                        toTaId, displayName(toP, toTa),
                        jobLabel, curFrom, newFrom, curTo, newTo, conf, reasoning));

                taHours.put(fromTa.getUserId(), newFrom);
                taHours.put(toTa.getUserId(), newTo);
            }
        } catch (Exception ignored) {}
        return result;
    }

    // ── Rule-based fallback ───────────────────────────────────────────────────

    private List<WorkloadRecommendation> buildRuleBasedRecs(List<User> sortedTas,
            Map<String, Integer> taHours, Map<String, List<Application>> taApps,
            JobDAO jobDAO, TAProfileDAO profileDAO) {
        List<WorkloadRecommendation> result = new ArrayList<>();
        List<User> allTas = new ArrayList<>(sortedTas);

        for (User fromTa : sortedTas) {
            if (taHours.get(fromTa.getUserId()) < AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS) break;
            TAProfile fromP = profileDAO.findByUserId(fromTa.getUserId());
            String fromName = displayName(fromP, fromTa);

            for (Application app : taApps.get(fromTa.getUserId())) {
                if (taHours.get(fromTa.getUserId()) < AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS) break;

                User toTa = findCandidate(allTas, taHours, taApps, fromTa.getUserId(), app.getJobId());
                if (toTa == null) continue;

                Job job = jobDAO.findById(app.getJobId());
                String jobLabel = buildJobLabel(job, app.getJobId());

                TAProfile toP = profileDAO.findByUserId(toTa.getUserId());
                String toName = displayName(toP, toTa);

                int curFrom = taHours.get(fromTa.getUserId());
                int curTo   = taHours.get(toTa.getUserId());
                int newFrom = curFrom - AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT;
                int newTo   = curTo   + AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT;
                int conf    = Math.max(65, 92 - result.size() * 5);

                result.add(new WorkloadRecommendation(app.getApplicationId(),
                        fromTa.getUserId(), fromName, toTa.getUserId(), toName,
                        jobLabel, curFrom, newFrom, curTo, newTo, conf,
                        buildRuleReasoning(fromName, toName, jobLabel, curFrom, newFrom, curTo, newTo)));

                taHours.put(fromTa.getUserId(), newFrom);
                taHours.put(toTa.getUserId(), newTo);
                taApps.get(toTa.getUserId()).add(app);
            }
        }
        return result;
    }

    private User findCandidate(List<User> allTas, Map<String, Integer> taHours,
            Map<String, List<Application>> taApps, String excludeTaId, String jobId) {
        User best = null;
        int bestCapacity = -1;
        for (User u : allTas) {
            if (u.getUserId().equals(excludeTaId)) continue;
            int hrs = taHours.get(u.getUserId());
            if (hrs + AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT
                    > AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS) continue;
            boolean alreadyOnJob = false;
            for (Application a : taApps.get(u.getUserId())) {
                if (jobId.equals(a.getJobId())) { alreadyOnJob = true; break; }
            }
            if (alreadyOnJob) continue;
            int capacity = AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS - hrs;
            if (capacity > bestCapacity) { bestCapacity = capacity; best = u; }
        }
        return best;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String displayName(TAProfile profile, User user) {
        if (profile != null && profile.getFullName() != null
                && !profile.getFullName().trim().isEmpty())
            return profile.getFullName().trim();
        return user.getUsername();
    }

    private String buildJobLabel(Job job, String fallbackJobId) {
        if (job == null) return fallbackJobId;
        String code = job.getModuleCode() != null ? job.getModuleCode().trim() : "";
        String name = job.getModuleName() != null ? job.getModuleName().trim() : "";
        if (!code.isEmpty() && !name.isEmpty()) return code + " — " + name;
        if (!name.isEmpty()) return name;
        if (!code.isEmpty()) return code;
        return fallbackJobId;
    }

    private String buildRuleReasoning(String fromName, String toName, String jobLabel,
            int curFrom, int newFrom, int curTo, int newTo) {
        int limit = AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS;
        int hpa   = AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT;
        return fromName + " holds " + (curFrom / hpa) + " assignment(s) (est. " + curFrom
                + " hrs), at or above the " + limit + " hr limit. "
                + toName + " has " + (limit - curTo) + " hrs spare. "
                + "Transferring [" + jobLabel + "] reduces " + fromName + "'s load from "
                + curFrom + " to " + newFrom + " hrs and " + toName + "'s from "
                + curTo + " to " + newTo + " hrs.";
    }
}
