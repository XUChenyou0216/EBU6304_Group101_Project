package com.ta.servlet;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.dao.TAProfileDAO;
import com.ta.dao.UserDAO;
import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.model.TAProfile;
import com.ta.model.User;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Admin servlet for US-A01: aggregated view of TA workload based on accepted applications.
 * <p>
 * URL mapping: {@code /admin/workload} via {@link WebServlet}.
 * Hours estimate and limit align with the admin dashboard workload section.
 * </p>
 *
 * @see AdminWorkloadServlet#HOURS_PER_ACCEPTED_ASSIGNMENT
 * @see AdminWorkloadServlet#WORKLOAD_LIMIT_HOURS
 */
@WebServlet("/admin/workload")
public class AdminWorkloadServlet extends HttpServlet {

    /** Estimated hours per accepted TA–job assignment (same as dashboard.jsp). */
    public static final int HOURS_PER_ACCEPTED_ASSIGNMENT = 16;
    /** Maximum recommended total hours before a TA is flagged (same as dashboard.jsp). */
    public static final int WORKLOAD_LIMIT_HOURS = 48;

    /**
     * View model row representing one TA's workload summary for the admin workload table.
     */
    public static class TaWorkloadRow implements Serializable {
        private final String userId;
        private final String username;
        private final String displayName;
        private final int acceptedCount;
        private final String modulesSummary;
        private final int estimatedHours;
        private final boolean exceeded;

        /**
         * Constructs a workload summary row for display.
         *
         * @param userId          the TA user identifier
         * @param username        the TA login username
         * @param displayName     the TA full name or username fallback
         * @param acceptedCount   number of accepted applications
         * @param modulesSummary  comma-separated module labels for accepted jobs
         * @param estimatedHours  estimated total hours ({@code acceptedCount * HOURS_PER_ACCEPTED_ASSIGNMENT})
         * @param exceeded        {@code true} if {@code estimatedHours} exceeds {@link #WORKLOAD_LIMIT_HOURS}
         */
        public TaWorkloadRow(String userId, String username, String displayName,
                             int acceptedCount, String modulesSummary,
                             int estimatedHours, boolean exceeded) {
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.acceptedCount = acceptedCount;
            this.modulesSummary = modulesSummary;
            this.estimatedHours = estimatedHours;
            this.exceeded = exceeded;
        }

        /** @return the TA user identifier */
        public String getUserId() { return userId; }
        /** @return the TA login username */
        public String getUsername() { return username; }
        /** @return the display name shown in the UI */
        public String getDisplayName() { return displayName; }
        /** @return the number of accepted assignments */
        public int getAcceptedCount() { return acceptedCount; }
        /** @return a summary of module codes/names for accepted jobs */
        public String getModulesSummary() { return modulesSummary; }
        /** @return estimated workload hours for this TA */
        public int getEstimatedHours() { return estimatedHours; }
        /** @return whether estimated hours exceed the configured limit */
        public boolean isExceeded() { return exceeded; }
    }

    /**
     * Builds workload rows for all TAs and forwards to the admin workload JSP.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response; forwards to {@code /admin/workload.jsp} on success
     * @throws ServletException if the request dispatcher fails
     * @throws IOException      if forwarding or sending a forbidden error fails
     */
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

        List<Application> accepted = new ArrayList<>();
        for (Application a : appDAO.findAll()) {
            if ("ACCEPTED".equalsIgnoreCase(a.getStatus())) {
                accepted.add(a);
            }
        }

        List<User> tas = new ArrayList<>();
        for (User u : userDAO.findAll()) {
            if ("TA".equalsIgnoreCase(u.getRole())) {
                tas.add(u);
            }
        }
        tas.sort(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));

        List<TaWorkloadRow> rows = new ArrayList<>();
        for (User ta : tas) {
            String taId = ta.getUserId();
            int acceptedCount = 0;
            Set<String> moduleLabels = new LinkedHashSet<>();
            for (Application a : accepted) {
                if (!taId.equals(a.getTaUserId())) {
                    continue;
                }
                acceptedCount++;
                Job j = jobDAO.findById(a.getJobId());
                if (j != null) {
                    String code = j.getModuleCode() != null ? j.getModuleCode().trim() : "";
                    String name = j.getModuleName() != null ? j.getModuleName().trim() : "";
                    if (!code.isEmpty() && !name.isEmpty()) {
                        moduleLabels.add(code + " — " + name);
                    } else if (!name.isEmpty()) {
                        moduleLabels.add(name);
                    } else if (!code.isEmpty()) {
                        moduleLabels.add(code);
                    } else {
                        moduleLabels.add(j.getJobId());
                    }
                } else {
                    moduleLabels.add(a.getJobId());
                }
            }

            TAProfile profile = profileDAO.findByUserId(taId);
            String display = profile != null && profile.getFullName() != null
                    && !profile.getFullName().trim().isEmpty()
                    ? profile.getFullName().trim()
                    : ta.getUsername();

            String modulesSummary = moduleLabels.isEmpty() ? "—" : String.join(", ", moduleLabels);
            int estimatedHours = acceptedCount * HOURS_PER_ACCEPTED_ASSIGNMENT;
            boolean exceeded = estimatedHours > WORKLOAD_LIMIT_HOURS;

            rows.add(new TaWorkloadRow(
                    taId,
                    ta.getUsername(),
                    display,
                    acceptedCount,
                    modulesSummary,
                    estimatedHours,
                    exceeded
            ));
        }

        Collections.sort(rows, Comparator
                .comparing(TaWorkloadRow::isExceeded).reversed()
                .thenComparing(TaWorkloadRow::getDisplayName, String.CASE_INSENSITIVE_ORDER));

        req.setAttribute("workloadRows", rows);
        req.setAttribute("workloadLimitHours", WORKLOAD_LIMIT_HOURS);
        req.setAttribute("hoursPerAssignment", HOURS_PER_ACCEPTED_ASSIGNMENT);
        req.getRequestDispatcher("/admin/workload.jsp").forward(req, resp);
    }
}
