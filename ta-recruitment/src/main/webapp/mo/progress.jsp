<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, com.ta.util.JobDeadlineUtil, java.util.List, java.util.Map, java.util.HashMap, java.util.Set, java.util.HashSet" %>
<%!
    private static String escNote(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') sb.append("&amp;");
            else if (c == '<') sb.append("&lt;");
            else if (c == '>') sb.append("&gt;");
            else sb.append(c);
        }
        return sb.toString();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recruitment Progress - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/applicants-review.css">
    <style>
        .deadline-past { color: #dc2626; font-weight: 600; }
    </style>
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <%
        String dataDir = SessionUtil.getDataDir(request);
        JobDAO jobDAO = new JobDAO(dataDir);
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
        List<Job> myJobs = jobDAO.findByMo(currentUser.getUserId());
        Set<String> moJobIds = new HashSet<String>();
        Map<String, Job> jobById = new HashMap<String, Job>();
        for (Job j : myJobs) {
            moJobIds.add(j.getJobId());
            jobById.put(j.getJobId(), j);
        }
        List<Application> allMoApplications = appDAO.findByJobIdsSortedByAppliedDateDesc(moJobIds);
        TAProfileDAO profileDAO = new TAProfileDAO(dataDir);
        Map<String, Long> acceptedCountByJob = new HashMap<String, Long>();
        for (String jid : moJobIds) {
            acceptedCountByJob.put(jid, appDAO.countAcceptedForJob(jid));
        }

        int totalApps = 0, accepted = 0, rejected = 0, underReview = 0, submitted = 0;
        for (Job j : myJobs) {
            List<Application> apps = appDAO.findByJob(j.getJobId());
            for (Application a : apps) {
                totalApps++;
                String s = a.getStatus();
                if ("ACCEPTED".equals(s)) accepted++;
                else if ("REJECTED".equals(s)) rejected++;
                else if ("UNDER_REVIEW".equals(s)) underReview++;
                else submitted++;
            }
        }
    %>

    <div class="page-header">
        <div>
            <h1>Recruitment Progress</h1>
            <p>Track the overall progress of your TA recruitment</p>
        </div>
    </div>

    <div class="stats-row">
        <div class="stat-card">
            <div class="stat-label">Total Applications</div>
            <div class="stat-value"><%= totalApps %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Accepted</div>
            <div class="stat-value" style="color:#16a34a;"><%= accepted %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Under Review</div>
            <div class="stat-value" style="color:#d97706;"><%= underReview %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Submitted</div>
            <div class="stat-value" style="color:#2563eb;"><%= submitted %></div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Rejected</div>
            <div class="stat-value" style="color:#dc2626;"><%= rejected %></div>
        </div>
    </div>

    <div style="margin-top:32px;">
        <h3 style="margin-bottom:16px;">All applications</h3>
        <p style="color:var(--text-muted); margin-bottom:16px;">Every submission across your job postings, newest first.</p>

        <% if ("updated".equals(request.getParameter("success"))) { %>
            <div class="alert alert-success">Application status and/or review note saved.</div>
        <% } %>
        <% if ("invalid".equals(request.getParameter("error")) || "notfound".equals(request.getParameter("error"))) { %>
            <div class="alert alert-error">Could not update that application. Please try again.</div>
        <% } %>
        <% if ("capacity".equals(request.getParameter("error"))) { %>
            <div class="alert alert-error">This job already has the maximum number of accepted TAs. You cannot accept more applicants.</div>
        <% } %>

        <% if (myJobs.isEmpty()) { %>
            <div class="empty-state">No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job.jsp">Post one now</a></div>
        <% } else if (allMoApplications.isEmpty()) { %>
            <div class="empty-state">No applications received yet.</div>
        <% } else { %>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Applicant</th>
                        <th>Student ID</th>
                        <th>Programme</th>
                        <th>Job</th>
                        <th>Applied</th>
                        <th>CV</th>
                        <th>Status</th>
                        <th style="min-width:280px;">Review note &amp; status</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Application apRow : allMoApplications) {
                        TAProfile prof = profileDAO.findByUserId(apRow.getTaUserId());
                        Job rowJob = jobById.get(apRow.getJobId());
                        String jobLabel = rowJob != null
                            ? (rowJob.getJobTitle() != null && !rowJob.getJobTitle().isEmpty()
                                ? rowJob.getJobTitle() : rowJob.getModuleName())
                            : (apRow.getJobId() != null ? apRow.getJobId() : "-");
                        Long accCt = acceptedCountByJob.get(apRow.getJobId());
                        long accForJob = accCt != null ? accCt.longValue() : 0L;
                        boolean atCapAccept = rowJob != null && accForJob >= rowJob.getVacancies();
                        boolean allowAcceptOption = "ACCEPTED".equals(apRow.getStatus()) || !atCapAccept;
                    %>
                    <tr>
                        <td><strong><%= prof!=null?prof.getFullName():apRow.getTaUserId() %></strong></td>
                        <td><%= prof!=null?prof.getStudentId():"-" %></td>
                        <td><%= prof!=null?prof.getProgramme():"-" %></td>
                        <td><%= jobLabel %></td>
                        <td><%= apRow.getAppliedDate() != null && !apRow.getAppliedDate().isEmpty() ? apRow.getAppliedDate() : "-" %></td>
                        <td>
                            <% if (prof!=null && prof.getCvFilePath()!=null && !prof.getCvFilePath().isEmpty()) { %>
                                <a href="${pageContext.request.contextPath}/<%= prof.getCvFilePath() %>" target="_blank" class="btn btn-ghost btn-sm">View CV</a>
                            <% } else { %>
                                <span class="text-muted">No CV</span>
                            <% } %>
                        </td>
                        <td><span class="badge badge-<%= apRow.getStatus() != null ? apRow.getStatus().toLowerCase() : "unknown" %>"><%= apRow.getStatus() != null ? apRow.getStatus().replace("_"," ") : "-" %></span></td>
                        <td style="vertical-align:top;">
                            <form action="${pageContext.request.contextPath}/mo/update-status" method="post" class="mo-applicant-form">
                                <input type="hidden" name="applicationId" value="<%= apRow.getApplicationId() %>">
                                <input type="hidden" name="jobId" value="<%= apRow.getJobId() %>">
                                <input type="hidden" name="returnTo" value="progress">
                                <textarea name="reviewNote" class="mo-review-note" rows="3" placeholder="Notes for this applicant (saved to applications.csv)"><%= escNote(apRow.getReviewNote()) %></textarea>
                                <div class="mo-applicant-form-actions">
                                    <select name="newStatus" class="mo-status-select">
                                        <option value="SUBMITTED" <%= "SUBMITTED".equals(apRow.getStatus())?"selected":"" %>>Submitted</option>
                                        <option value="UNDER_REVIEW" <%= "UNDER_REVIEW".equals(apRow.getStatus())?"selected":"" %>>Under Review</option>
                                        <option value="ACCEPTED" <%= "ACCEPTED".equals(apRow.getStatus())?"selected":"" %> <%= allowAcceptOption ? "" : "disabled" %>>Accepted</option>
                                        <option value="REJECTED" <%= "REJECTED".equals(apRow.getStatus())?"selected":"" %>>Rejected</option>
                                    </select>
                                    <button type="submit" class="btn btn-primary btn-sm">Save</button>
                                </div>
                            </form>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        <% } %>
    </div>

    <div style="margin-top:32px;">
        <h3 style="margin-bottom:16px;">Progress by Job</h3>

        <% if (myJobs.isEmpty()) { %>
            <div class="empty-state">No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job.jsp">Post one now</a></div>
        <% } else { %>
            <div style="background:var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius); overflow: hidden;">
                <table class="data-table" style="border: none; border-radius: 0;">
                    <thead>
                        <tr>
                            <th>JOB</th>
                            <th>MODULE</th>
                            <th>POSITIONS</th>
                            <th>APPLICANTS</th>
                            <th>ACCEPTED</th>
                            <th>UNDER REVIEW</th>
                            <th>REJECTED</th>
                            <th>FILL RATE</th>
                            <th>STATUS</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Job job : myJobs) {
                            List<Application> apps = appDAO.findByJob(job.getJobId());
                            int jAccepted = 0, jReview = 0, jRejected = 0;
                            for (Application a : apps) {
                                String s = a.getStatus();
                                if ("ACCEPTED".equals(s)) jAccepted++;
                                else if ("UNDER_REVIEW".equals(s)) jReview++;
                                else if ("REJECTED".equals(s)) jRejected++;
                            }
                            int pct = job.getVacancies() > 0 ? (int) Math.round(jAccepted * 100.0 / job.getVacancies()) : 0;
                            if (pct > 100) pct = 100;
                            boolean past = JobDeadlineUtil.isPastDeadline(job.getDeadline());
                            boolean open = "OPEN".equals(job.getStatus());
                            String statusBadgeClass;
                            String statusLabel;
                            if (open) {
                                statusBadgeClass = "active";
                                statusLabel = "Active";
                            } else if (past) {
                                statusBadgeClass = "expired";
                                statusLabel = "Expired";
                            } else {
                                statusBadgeClass = "closed";
                                statusLabel = "Closed";
                            }
                        %>
                        <tr>
                            <td><strong><%= job.getJobTitle() != null && !job.getJobTitle().isEmpty() ? job.getJobTitle() : job.getModuleName() %></strong></td>
                            <td><%= job.getModuleName() %></td>
                            <td><%= job.getVacancies() %></td>
                            <td><%= apps.size() %></td>
                            <td style="color:#16a34a;font-weight:600;"><%= jAccepted %></td>
                            <td style="color:#d97706;font-weight:600;"><%= jReview %></td>
                            <td style="color:#dc2626;font-weight:600;"><%= jRejected %></td>
                            <td>
                                <div style="display:flex;align-items:center;gap:8px;">
                                    <div style="flex:1;background:#e5e7eb;border-radius:4px;height:8px;overflow:hidden;">
                                        <div style="width:<%= pct %>%;background:<%= pct >= 100 ? "#16a34a" : "#2563eb" %>;height:100%;border-radius:4px;"></div>
                                    </div>
                                    <span style="font-size:13px;font-weight:600;min-width:36px;"><%= pct %>%</span>
                                </div>
                            </td>
                            <td><span class="badge badge-<%= statusBadgeClass %>"><%= statusLabel %></span></td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
