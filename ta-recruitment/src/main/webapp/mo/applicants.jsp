<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List, java.util.Collections, java.util.Map, java.util.HashMap, java.util.Set, java.util.HashSet" %>
<%!
    /** Escape for textarea body (avoid breaking HTML / XSS). */
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
    <title>Review Applicants - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/applicants-review.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <%
        String jobId = request.getParameter("jobId");
        if (jobId != null) {
            jobId = jobId.trim();
            if (jobId.isEmpty()) jobId = null;
        }
        String dataDir = SessionUtil.getDataDir(request);
        JobDAO jobDAO = new JobDAO(dataDir);
        ApplicationDAO appDao = new ApplicationDAO(dataDir);
        TAProfileDAO profileDAO = new TAProfileDAO(dataDir);

        Job job = null;
        List<Application> applicants = Collections.emptyList();
        long acceptedForJob = 0L;
        List<Job> myJobs = Collections.emptyList();
        Map<String, Job> jobById = new HashMap<String, Job>();
        Map<String, Long> acceptedCountByJob = new HashMap<String, Long>();

        if (jobId != null) {
            job = jobDAO.findById(jobId);
            if (job != null) {
                applicants = appDao.findByJob(jobId);
                acceptedForJob = appDao.countAcceptedForJob(jobId);
            }
        } else {
            myJobs = jobDAO.findByMo(currentUser.getUserId());
            Set<String> moJobIds = new HashSet<String>();
            for (Job j : myJobs) {
                moJobIds.add(j.getJobId());
                jobById.put(j.getJobId(), j);
            }
            for (String jid : moJobIds) {
                acceptedCountByJob.put(jid, appDao.countAcceptedForJob(jid));
            }
            applicants = appDao.findByJobIdsSortedByAppliedDateDesc(moJobIds);
        }
    %>

    <div class="page-header">
        <div>
            <h1><%= jobId != null
                ? ("Applicants for: " + (job != null ? job.getModuleName() : "Unknown job"))
                : "All applicants" %></h1>
            <p><%= jobId != null
                ? "Review and manage applicant submissions"
                : "Every submission across your job postings, newest first" %></p>
        </div>
        <a href="${pageContext.request.contextPath}/mo/jobs.jsp" class="btn btn-secondary">Back to Jobs</a>
    </div>

    <% if ("updated".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Application status and/or review note saved.</div>
    <% } %>
    <% if ("invalid".equals(request.getParameter("error")) || "notfound".equals(request.getParameter("error"))) { %>
        <div class="alert alert-error">Could not update that application. Please try again.</div>
    <% } %>
    <% if ("capacity".equals(request.getParameter("error"))) { %>
        <div class="alert alert-error">This job already has the maximum number of accepted TAs (see &quot;positions&quot; on the posting). You cannot accept more applicants.</div>
    <% } %>
    <% if ("capacity".equals(request.getParameter("warning"))) { %>
        <div class="alert alert-info">Some rows were not set to Accepted because the vacancy limit was reached.</div>
    <% } %>

    <%-- Sidebar: no jobId → all MO applications --%>
    <% if (jobId == null) { %>
        <% if (myJobs.isEmpty()) { %>
            <div class="empty-state">No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job.jsp">Post one now</a></div>
        <% } else if (applicants.isEmpty()) { %>
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
                    <% for (Application apRow : applicants) {
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
                                <input type="hidden" name="returnTo" value="all">
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
    <% } else if (job == null) { %>
        <div class="alert alert-error">Job not found. Return to <a href="${pageContext.request.contextPath}/mo/jobs.jsp">Job Postings</a>.</div>
    <% } else if (applicants.isEmpty()) { %>
        <div class="empty-state">No applications received yet.</div>
    <% } else { %>
        <table class="data-table">
            <thead>
                <tr>
                    <th>Applicant</th>
                    <th>Student ID</th>
                    <th>Programme</th>
                    <th>CV</th>
                    <th>Status</th>
                    <th style="min-width:280px;">Review note &amp; status</th>
                </tr>
            </thead>
            <tbody>
                <% for (Application apRow : applicants) {
                    TAProfile prof = profileDAO.findByUserId(apRow.getTaUserId());
                    boolean atCapAccept = job != null && acceptedForJob >= job.getVacancies();
                    boolean allowAcceptOption = "ACCEPTED".equals(apRow.getStatus()) || !atCapAccept;
                %>
                <tr>
                    <td><strong><%= prof!=null?prof.getFullName():apRow.getTaUserId() %></strong></td>
                    <td><%= prof!=null?prof.getStudentId():"-" %></td>
                    <td><%= prof!=null?prof.getProgramme():"-" %></td>
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
                            <input type="hidden" name="jobId" value="<%= jobId %>">
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

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
