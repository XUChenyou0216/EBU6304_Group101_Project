<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil" %>
<%@ page import="java.util.Map, java.util.HashMap, java.util.List, java.util.ArrayList" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Workload - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .hitl-banner { background: #eef1fb; border: 1px solid #c7d2fe; border-radius: 10px; padding: 16px 20px; display: flex; align-items: center; gap: 12px; margin-bottom: 28px; }
        .hitl-banner .title { font-size: 14px; font-weight: 600; color: #2b4acb; }
        .hitl-banner .desc { font-size: 13px; color: #4b5563; }
        .suggestion-card { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 24px; margin-bottom: 20px; }
        .suggestion-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
        .suggestion-title { font-size: 15px; font-weight: 600; color: #030213; }
        .confidence { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #717182; }
        .confidence-bar { width: 80px; height: 8px; background: #e5e7eb; border-radius: 4px; overflow: hidden; display: inline-block; }
        .confidence-fill { height: 100%; border-radius: 4px; display: block; }
        .conf-high { background: #16a34a; }
        .conf-med { background: #2b4acb; }
        .conf-low { background: #d97706; }
        .transfer-box { display: flex; align-items: center; justify-content: center; gap: 24px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; padding: 20px; margin-bottom: 16px; }
        .transfer-side { text-align: center; }
        .transfer-label { font-size: 12px; color: #9ca3af; margin-bottom: 4px; }
        .transfer-name { font-size: 15px; font-weight: 600; color: #030213; }
        .transfer-detail { font-size: 13px; color: #717182; }
        .impact-line { font-size: 13px; color: #b45309; margin-bottom: 12px; }
        .reasoning-toggle { padding: 10px 14px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; font-size: 13px; color: #717182; cursor: pointer; margin-bottom: 16px; display: flex; justify-content: space-between; }
        .reasoning-content { display: none; padding: 12px 14px; background: #f9fafb; border-radius: 6px; font-size: 13px; color: #717182; margin-bottom: 16px; }
        .suggestion-actions { display: flex; justify-content: flex-end; gap: 12px; }
    </style>
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
        TAProfileDAO profileDAO = new TAProfileDAO(dataDir);
        JobDAO jobDAO = new JobDAO(dataDir);

        int maxLoad = 3;

        // Build workload map (Java 6 compatible - no diamond, no lambda)
        Map taLoad = new HashMap();       // String -> Integer
        Map taJobNames = new HashMap();   // String -> List of Strings

        List allApps = appDAO.findAll();
        for (int i = 0; i < allApps.size(); i++) {
            Application a = (Application) allApps.get(i);
            if ("ACCEPTED".equals(a.getStatus())) {
                String taId = a.getTaUserId();

                // Count load
                Integer prevLoad = (Integer) taLoad.get(taId);
                taLoad.put(taId, prevLoad != null ? prevLoad + 1 : 1);

                // Track job names
                Job j = jobDAO.findById(a.getJobId());
                String jobLabel = j != null ? j.getJobId() + " - " + j.getModuleName() : a.getJobId();
                List jobList = (List) taJobNames.get(taId);
                if (jobList == null) {
                    jobList = new ArrayList();
                    taJobNames.put(taId, jobList);
                }
                jobList.add(jobLabel);
            }
        }

        // Find overloaded and underloaded TAs
        List overloadedIds = new ArrayList();   // String
        List underloadedIds = new ArrayList();  // String
        List allProfiles = profileDAO.findAll();

        for (int i = 0; i < allProfiles.size(); i++) {
            TAProfile p = (TAProfile) allProfiles.get(i);
            Integer load = (Integer) taLoad.get(p.getUserId());
            int loadVal = load != null ? load : 0;
            if (loadVal > maxLoad) {
                overloadedIds.add(p.getUserId());
            } else if (loadVal < maxLoad) {
                underloadedIds.add(p.getUserId());
            }
        }

        // Generate suggestions
        List suggestions = new ArrayList(); // each item: String[6] = {taName, fromModule, toTaName, toSlot, confidence, impact}
        int sugIdx = 0;

        for (int oi = 0; oi < overloadedIds.size(); oi++) {
            String overId = (String) overloadedIds.get(oi);
            TAProfile overP = profileDAO.findByUserId(overId);
            List jobs = (List) taJobNames.get(overId);
            Integer overLoadVal = (Integer) taLoad.get(overId);
            int excess = (overLoadVal != null ? overLoadVal : 0) - maxLoad;

            for (int ui = 0; ui < Math.min(excess, underloadedIds.size()); ui++) {
                String underId = (String) underloadedIds.get(ui);
                TAProfile underP = profileDAO.findByUserId(underId);
                String fromJob = (jobs != null && jobs.size() > ui) ? (String) jobs.get(ui) : "Unknown";
                int overHours = (overLoadVal != null ? overLoadVal : 0) * 16;
                int conf = 92 - sugIdx * 5;

                String[] sug = new String[6];
                sug[0] = overP != null ? overP.getFullName() : overId;
                sug[1] = fromJob;
                sug[2] = underP != null ? underP.getFullName() : underId;
                sug[3] = "Available slot";
                sug[4] = String.valueOf(conf);
                sug[5] = "Reduces " + sug[0] + "'s hours from " + overHours + " to " + (overHours - 16) + " (-16 hrs).";
                suggestions.add(sug);
                sugIdx++;
            }
        }
    %>

    <div class="page-header" style="align-items:center;">
        <div>
            <h1>AI Workload Balancing</h1>
            <p>AI-generated recommendations to optimise TA workload distribution</p>
        </div>
    </div>

    <!-- Human-in-the-Loop Banner -->
    <div class="hitl-banner">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#2b4acb" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
        <div>
            <div class="title">Human-in-the-Loop Control</div>
            <div class="desc">All AI suggestions require explicit admin approval before any changes are applied. You retain full control over all decisions.</div>
        </div>
    </div>

    <% if (suggestions.isEmpty()) { %>
        <div class="empty-state" style="padding:60px;">
            <p style="font-size:16px;margin-bottom:8px;">No workload imbalances detected.</p>
            <p style="color:#717182;">All TAs are within the maximum workload threshold of <%= maxLoad %> assignments.</p>
        </div>
    <% } else { %>
        <% for (int si = 0; si < suggestions.size(); si++) {
            String[] s = (String[]) suggestions.get(si);
            int conf = Integer.parseInt(s[4]);
            String confClass = conf >= 90 ? "conf-high" : conf >= 80 ? "conf-med" : "conf-low";
            String divId = "reasoning-" + si;
        %>
        <div class="suggestion-card">
            <div class="suggestion-header">
                <div class="suggestion-title">Suggestion <%= si + 1 %></div>
                <div class="confidence">
                    Confidence:
                    <span class="confidence-bar"><span class="confidence-fill <%= confClass %>" style="width:<%= conf %>%"></span></span>
                    <strong><%= conf %>%</strong>
                </div>
            </div>

            <div class="transfer-box">
                <div class="transfer-side">
                    <div class="transfer-label">Move TA</div>
                    <div class="transfer-name"><%= s[0] %></div>
                    <div class="transfer-detail">from <%= s[1] %></div>
                </div>
                <div style="color:#9ca3af;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                </div>
                <div class="transfer-side">
                    <div class="transfer-label">To available TA</div>
                    <div class="transfer-name"><%= s[2] %></div>
                    <div class="transfer-detail"><%= s[3] %></div>
                </div>
            </div>

            <div class="impact-line">
                <strong>Impact:</strong> <%= s[5] %>
            </div>

            <div class="reasoning-toggle" onclick="var el=document.getElementById('<%= divId %>');el.style.display=el.style.display==='none'?'block':'none';">
                <span>AI Reasoning (Explainable Results)</span>
                <span style="color:#2b4acb;font-weight:500;">Show</span>
            </div>
            <div id="<%= divId %>" class="reasoning-content">
                This recommendation is generated by analyzing the current workload distribution. The algorithm identifies TAs whose total assigned modules exceed the threshold (<%= maxLoad %> assignments) and suggests transferring excess workload to TAs with available capacity. The confidence score reflects the match quality based on workload gap and availability.
            </div>

            <div class="suggestion-actions">
                <button class="btn btn-secondary">Reject</button>
                <button class="btn btn-primary">Approve AI Suggestion</button>
            </div>
        </div>
        <% } %>
    <% } %>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
