<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.servlet.AIWorkloadServlet" %>
<%@ page import="com.ta.servlet.AdminWorkloadServlet" %>
<%@ page import="java.util.List" %>
<%
    if (request.getAttribute("recommendations") == null) {
        response.sendRedirect(request.getContextPath() + "/admin/ai-workload");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Workload - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .hitl-banner { background: #eef1fb; border: 1px solid #c7d2fe; border-radius: 10px; padding: 16px 20px; display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
        .hitl-banner .title { font-size: 14px; font-weight: 600; color: #2b4acb; }
        .hitl-banner .desc { font-size: 13px; color: #4b5563; }
        .overload-alert { background: #fef2f2; border: 1px solid #fca5a5; border-radius: 10px; padding: 16px 20px; margin-bottom: 20px; }
        .overload-alert .title { font-size: 14px; font-weight: 700; color: #991b1b; margin-bottom: 8px; display: flex; align-items: center; gap: 8px; }
        .overload-alert .ta-list { font-size: 13px; color: #7f1d1d; }
        .overload-alert .ta-item { padding: 4px 0; }
        .no-candidate-notice { background: #fffbeb; border: 1px solid #fde68a; border-radius: 8px; padding: 14px 18px; font-size: 13px; color: #92400e; margin-bottom: 20px; }
        .ai-badge { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 20px; margin-bottom: 20px; }
        .ai-badge.powered { background: #ede9fe; color: #5b21b6; border: 1px solid #c4b5fd; }
        .ai-badge.rule { background: #f3f4f6; color: #6b7280; border: 1px solid #d1d5db; }
        .ai-badge.error { background: #fff7ed; color: #9a3412; border: 1px solid #fed7aa; }
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
        .decrease { color: #16a34a; font-weight: 600; }
        .increase { color: #d97706; font-weight: 600; }
        .impact-line { font-size: 13px; color: #b45309; margin-bottom: 12px; }
        .reasoning-toggle { padding: 10px 14px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; font-size: 13px; color: #717182; cursor: pointer; margin-bottom: 16px; display: flex; justify-content: space-between; user-select: none; }
        .reasoning-content { display: none; padding: 12px 14px; background: #f9fafb; border-radius: 6px; font-size: 13px; color: #717182; margin-bottom: 16px; line-height: 1.6; }
        .suggestion-actions { display: flex; justify-content: flex-end; gap: 12px; }
        .alert-success { background: #ecfdf5; border: 1px solid #6ee7b7; color: #065f46; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; }
        .alert-error-msg { background: #fef2f2; border: 1px solid #fca5a5; color: #991b1b; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; font-size: 14px; }
    </style>
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

<%
    @SuppressWarnings("unchecked")
    List<AdminWorkloadServlet.TaWorkloadRow> overloadedRows =
            (List<AdminWorkloadServlet.TaWorkloadRow>) request.getAttribute("overloadedRows");
    @SuppressWarnings("unchecked")
    List<AIWorkloadServlet.WorkloadRecommendation> recommendations =
            (List<AIWorkloadServlet.WorkloadRecommendation>) request.getAttribute("recommendations");
    boolean aiPowered = Boolean.TRUE.equals(request.getAttribute("aiPowered"));
    boolean hasApiKey = Boolean.TRUE.equals(request.getAttribute("hasApiKey"));
    String  aiError   = (String) request.getAttribute("aiError");
    int workloadLimit = request.getAttribute("workloadLimitHours") != null
            ? (Integer) request.getAttribute("workloadLimitHours")
            : AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS;

    String success = request.getParameter("success");
    String errorParam = request.getParameter("error");
    if (overloadedRows == null) overloadedRows = java.util.Collections.emptyList();
    if (recommendations == null) recommendations = java.util.Collections.emptyList();
%>

    <div class="page-header" style="align-items:center;">
        <div>
            <h1>AI Workload Balancing</h1>
            <p>AI-generated recommendations to optimise TA workload distribution</p>
        </div>
        <a href="<%= request.getContextPath() %>/admin/workload" class="btn btn-secondary" style="white-space:nowrap;">
            View Workload Table
        </a>
    </div>

    <%-- Success / error banners --%>
    <% if ("1".equals(success)) { %>
    <div class="alert-success">Recommendation applied. The assignment has been reassigned.</div>
    <% } else if (errorParam != null) { %>
    <div class="alert-error-msg">Could not apply recommendation. Please try again.</div>
    <% } %>

    <%-- AI / rule-based badge --%>
    <% if (aiPowered) { %>
    <div class="ai-badge powered">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v4l3 3"/></svg>
        Powered by AI
    </div>
    <% } else if (aiError != null) { %>
    <div class="ai-badge error">
        &#9888; AI unavailable — showing rule-based suggestions
        (<a href="<%= request.getContextPath() %>/admin/settings" style="color:inherit;">configure API key</a>)
    </div>
    <% } else if (!hasApiKey) { %>
    <div class="ai-badge rule">
        &#9881; Rule-based &mdash;
        <a href="<%= request.getContextPath() %>/admin/settings" style="color:inherit;margin-left:4px;">configure AI API key in Settings</a>
    </div>
    <% } %>

    <%-- Overload detection alert --%>
    <% if (!overloadedRows.isEmpty()) { %>
    <div class="overload-alert">
        <div class="title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            Workload Anomaly Detected — <%= overloadedRows.size() %> TA(s) at or above the <%= workloadLimit %> hr limit
        </div>
        <div class="ta-list">
            <% for (AdminWorkloadServlet.TaWorkloadRow row : overloadedRows) { %>
            <div class="ta-item">
                &bull; <strong><%= row.getDisplayName() %></strong> —
                <%= row.getEstimatedHours() %> hrs (<%= row.getAcceptedCount() %> assignment(s)):
                <span style="color:#6b7280;"><%= row.getModulesSummary() %></span>
            </div>
            <% } %>
        </div>
    </div>
    <% } %>

    <%-- Human-in-the-Loop Banner --%>
    <div class="hitl-banner">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#2b4acb" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
        <div>
            <div class="title">Human-in-the-Loop Control</div>
            <div class="desc">All AI suggestions require explicit admin approval before any changes are applied. You retain full control over all decisions.</div>
        </div>
    </div>

    <%-- Recommendation cards or empty states --%>
    <% if (overloadedRows.isEmpty()) { %>
        <div class="empty-state" style="padding:60px;">
            <p style="font-size:16px;margin-bottom:8px;">No workload imbalances detected.</p>
            <p style="color:#717182;">All TAs are within the <%= workloadLimit %> hr threshold.</p>
        </div>
    <% } else if (recommendations.isEmpty()) { %>
        <div class="no-candidate-notice">
            <strong>&#9888; Overload detected, but no redistribution is possible.</strong><br>
            All other TAs are already at or near capacity. Consider recruiting additional TAs or adjusting the workload limit in Settings.
        </div>
    <% } else { %>
        <% for (int si = 0; si < recommendations.size(); si++) {
            AIWorkloadServlet.WorkloadRecommendation r = recommendations.get(si);
            int conf = r.getConfidence();
            String confClass = conf >= 90 ? "conf-high" : conf >= 80 ? "conf-med" : "conf-low";
            String divId  = "reasoning-" + si;
            String cardId = "card-" + si;
        %>
        <div class="suggestion-card" id="<%= cardId %>">
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
                    <div class="transfer-label">Move assignment from</div>
                    <div class="transfer-name"><%= r.getFromTaName() %></div>
                    <div class="transfer-detail">
                        <%= r.getFromCurrentHours() %> hrs &rarr; <span class="decrease"><%= r.getFromNewHours() %> hrs</span>
                    </div>
                </div>
                <div style="color:#9ca3af;">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                </div>
                <div class="transfer-side">
                    <div class="transfer-label">To TA</div>
                    <div class="transfer-name"><%= r.getToTaName() %></div>
                    <div class="transfer-detail">
                        <%= r.getToCurrentHours() %> hrs &rarr; <span class="increase"><%= r.getToNewHours() %> hrs</span>
                    </div>
                </div>
            </div>

            <div class="impact-line">
                <strong>Assignment:</strong> <%= r.getJobLabel() %>
                &nbsp;&bull;&nbsp;
                <strong>Impact:</strong> Reduces <%= r.getFromTaName() %>'s hours by <%= AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT %> hrs
                (<%= r.getFromCurrentHours() %> &rarr; <%= r.getFromNewHours() %> hrs).
            </div>

            <div class="reasoning-toggle" onclick="toggleReasoning('<%= divId %>', this)">
                <span><%= aiPowered ? "AI Reasoning" : "Rule-based Reasoning" %> (Explainable Results)</span>
                <span class="toggle-label" style="color:#2b4acb;font-weight:500;">Show &#9660;</span>
            </div>
            <div id="<%= divId %>" class="reasoning-content"><%= r.getReasoning() %></div>

            <div class="suggestion-actions">
                <button type="button" class="btn btn-secondary" onclick="dismissCard('<%= cardId %>')">Reject</button>
                <form method="post" action="<%= request.getContextPath() %>/admin/ai-workload" style="display:inline;">
                    <input type="hidden" name="applicationId" value="<%= r.getApplicationId() %>">
                    <input type="hidden" name="newTaId"       value="<%= r.getToTaId() %>">
                    <input type="hidden" name="fromTaId"      value="<%= r.getFromTaId() %>">
                    <button type="submit" class="btn btn-primary">Approve &amp; Apply</button>
                </form>
            </div>
        </div>
        <% } %>
    <% } %>

<script>
function toggleReasoning(id, toggle) {
    var el = document.getElementById(id);
    var label = toggle.querySelector('.toggle-label');
    if (el.style.display === 'block') {
        el.style.display = 'none';
        label.innerHTML = 'Show &#9660;';
    } else {
        el.style.display = 'block';
        label.innerHTML = 'Hide &#9650;';
    }
}
function dismissCard(cardId) {
    var card = document.getElementById(cardId);
    if (card) {
        card.style.opacity = '0';
        card.style.transition = 'opacity 0.3s';
        setTimeout(function() { card.style.display = 'none'; }, 300);
    }
}
</script>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
