<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, com.ta.util.JobDeadlineUtil, java.util.List, java.util.Collections, java.util.Map, java.util.HashMap, java.util.Set, java.util.HashSet" %>
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

        boolean canReopenJob = false;
        if (job != null) {
            boolean jobPast = JobDeadlineUtil.isPastDeadline(job.getDeadline());
            canReopenJob = "CLOSED".equals(job.getStatus()) && !jobPast && job.getVacancies() > 0
                && acceptedForJob < job.getVacancies();
        }
    %>

    <div class="page-header" style="display:flex;justify-content:space-between;align-items:flex-start;flex-wrap:wrap;gap:12px;">
        <div>
            <h1><%= jobId != null
                ? ("Applicants for: " + (job != null ? job.getModuleName() : "Unknown job"))
                : "All applicants" %></h1>
            <p><%= jobId != null
                ? "Review and manage applicant submissions"
                : "Every submission across your job postings, newest first" %></p>
        </div>
        <div style="display:flex;gap:8px;flex-wrap:wrap;align-items:center;">
            <% if (jobId != null && job != null && canReopenJob) { %>
            <form action="${pageContext.request.contextPath}/mo/reopen-job" method="post" style="margin:0;">
                <input type="hidden" name="jobId" value="<%= jobId %>">
                <input type="hidden" name="returnTo" value="applicants">
                <button type="submit" class="btn btn-primary" title="Posting is closed but has free slots — set back to Active so TAs can apply again.">Refresh &amp; re-open</button>
            </form>
            <% } %>
            <a href="${pageContext.request.contextPath}/mo/jobs.jsp" class="btn btn-secondary">Back to Jobs</a>
        </div>
    </div>

    <% if ("updated".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Application status and/or review note saved.</div>
    <% } %>
    <% if ("batch".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Batch update saved.</div>
    <% } %>
    <% if ("batch".equals(request.getParameter("error"))) { %>
        <div class="alert alert-error">Batch update could not be applied (empty or invalid request).</div>
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
    <% if ("reopened".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Posting re-opened — the job is Active again and TAs can apply.</div>
    <% } %>
    <% if (request.getParameter("error") != null && request.getParameter("error").startsWith("reopen")) {
        String er = request.getParameter("error");
        String msg = "Could not re-open this posting.";
        if ("reopenNotClosed".equals(er)) msg = "Only closed postings can be re-opened this way.";
        else if ("reopenStillFull".equals(er)) msg = "All positions are still filled — free a slot first (e.g. change an accepted applicant to rejected).";
        else if ("reopenDeadline".equals(er)) msg = "The application deadline has passed; extend the deadline on Edit job before re-opening.";
        else if ("reopenNoVacancy".equals(er)) msg = "This posting has zero positions configured.";
        else if ("reopenNotFound".equals(er)) msg = "Job not found or you do not have access.";
    %>
        <div class="alert alert-error"><%= msg %></div>
    <% } %>

    <%-- Sidebar: no jobId → all MO applications --%>
    <% if (jobId == null) { %>
        <% if (myJobs.isEmpty()) { %>
            <div class="empty-state">No jobs posted yet. <a href="${pageContext.request.contextPath}/mo/post-job.jsp">Post one now</a></div>
        <% } else if (applicants.isEmpty()) { %>
            <div class="empty-state">No applications received yet.</div>
        <% } else { %>
            <form id="moApplicantsBatchForm" action="${pageContext.request.contextPath}/mo/batch-update-applications" method="post" class="mo-applicants-batch-form" data-job-id="">
                <input type="hidden" name="jobId" value="">
                <input type="hidden" name="batchPayload" id="moBatchPayload" value="">
                <input type="hidden" name="includeNotes" id="moIncludeNotes" value="false">
                <div class="mo-ar-bulk-bar bulk-actions-bar" style="margin-bottom:12px;">
                    <label class="mo-ar-bulk-check" style="cursor:pointer;"><input type="checkbox" id="moSelectAllCb" onchange="moToggleAllApplicants(this)"> Select all</label>
                    <button type="button" class="btn btn-secondary btn-sm" onclick="moBulkStatus('ACCEPTED')">Accept selected</button>
                    <button type="button" class="btn btn-secondary btn-sm" onclick="moBulkStatus('REJECTED')">Reject selected</button>
                    <button type="button" class="btn btn-primary btn-sm" onclick="moSaveAllApplicantsPage()">Save all rows</button>
                </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width:36px;" title="Bulk select"></th>
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
                        boolean atCapAccept = rowJob != null && rowJob.getVacancies() > 0 && accForJob >= rowJob.getVacancies();
                        boolean allowAcceptOption = "ACCEPTED".equals(apRow.getStatus()) || !atCapAccept;
                    %>
                    <tr class="mo-app-row" data-app-id="<%= apRow.getApplicationId() %>">
                        <td><input type="checkbox" class="mo-batch-cb table-checkbox" value="<%= apRow.getApplicationId() %>"></td>
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
                            <div class="mo-applicant-form">
                                <textarea name="note_<%= apRow.getApplicationId() %>" class="mo-review-note" rows="3" placeholder="Notes for this applicant (saved to applications.csv)"><%= escNote(apRow.getReviewNote()) %></textarea>
                                <div class="mo-applicant-form-actions">
                                    <select class="mo-status-select">
                                        <option value="SUBMITTED" <%= "SUBMITTED".equals(apRow.getStatus())?"selected":"" %>>Submitted</option>
                                        <option value="UNDER_REVIEW" <%= "UNDER_REVIEW".equals(apRow.getStatus())?"selected":"" %>>Under Review</option>
                                        <option value="ACCEPTED" <%= "ACCEPTED".equals(apRow.getStatus())?"selected":"" %> <%= allowAcceptOption ? "" : "disabled" %>>Accepted</option>
                                        <option value="REJECTED" <%= "REJECTED".equals(apRow.getStatus())?"selected":"" %>>Rejected</option>
                                    </select>
                                    <button type="button" class="btn btn-primary btn-sm" onclick="moSaveOneApplicantRow('<%= apRow.getApplicationId() %>')">Save row</button>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
            </form>
        <% } %>
    <% } else if (job == null) { %>
        <div class="alert alert-error">Job not found. Return to <a href="${pageContext.request.contextPath}/mo/jobs.jsp">Job Postings</a>.</div>
    <% } else if (applicants.isEmpty()) { %>
        <div class="empty-state">No applications received yet.</div>
    <% } else { %>
        <form id="moApplicantsBatchForm" action="${pageContext.request.contextPath}/mo/batch-update-applications" method="post" class="mo-applicants-batch-form" data-job-id="<%= jobId %>">
            <input type="hidden" name="jobId" value="<%= jobId %>">
            <input type="hidden" name="batchPayload" id="moBatchPayload" value="">
            <input type="hidden" name="includeNotes" id="moIncludeNotes" value="false">
            <div class="mo-ar-bulk-bar bulk-actions-bar" style="margin-bottom:12px;">
                <label class="mo-ar-bulk-check" style="cursor:pointer;"><input type="checkbox" id="moSelectAllCb" onchange="moToggleAllApplicants(this)"> Select all</label>
                <button type="button" class="btn btn-secondary btn-sm" onclick="moBulkStatus('ACCEPTED')">Accept selected</button>
                <button type="button" class="btn btn-secondary btn-sm" onclick="moBulkStatus('REJECTED')">Reject selected</button>
                <button type="button" class="btn btn-primary btn-sm" onclick="moSaveAllApplicantsPage()">Save all rows</button>
            </div>
        <table class="data-table">
            <thead>
                <tr>
                    <th style="width:36px;" title="Bulk select"></th>
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
                    boolean atCapAccept = job != null && job.getVacancies() > 0 && acceptedForJob >= job.getVacancies();
                    boolean allowAcceptOption = "ACCEPTED".equals(apRow.getStatus()) || !atCapAccept;
                %>
                <tr class="mo-app-row" data-app-id="<%= apRow.getApplicationId() %>">
                    <td><input type="checkbox" class="mo-batch-cb table-checkbox" value="<%= apRow.getApplicationId() %>"></td>
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
                        <div class="mo-applicant-form">
                            <textarea name="note_<%= apRow.getApplicationId() %>" class="mo-review-note" rows="3" placeholder="Notes for this applicant (saved to applications.csv)"><%= escNote(apRow.getReviewNote()) %></textarea>
                            <div class="mo-applicant-form-actions">
                                <select class="mo-status-select">
                                    <option value="SUBMITTED" <%= "SUBMITTED".equals(apRow.getStatus())?"selected":"" %>>Submitted</option>
                                    <option value="UNDER_REVIEW" <%= "UNDER_REVIEW".equals(apRow.getStatus())?"selected":"" %>>Under Review</option>
                                    <option value="ACCEPTED" <%= "ACCEPTED".equals(apRow.getStatus())?"selected":"" %> <%= allowAcceptOption ? "" : "disabled" %>>Accepted</option>
                                    <option value="REJECTED" <%= "REJECTED".equals(apRow.getStatus())?"selected":"" %>>Rejected</option>
                                </select>
                                <button type="button" class="btn btn-primary btn-sm" onclick="moSaveOneApplicantRow('<%= apRow.getApplicationId() %>')">Save row</button>
                            </div>
                        </div>
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
        </form>
    <% } %>

<script>
(function() {
    var ctx = '${pageContext.request.contextPath}';
    window.moToggleAllApplicants = function(master) {
        document.querySelectorAll('.mo-batch-cb').forEach(function(cb) { cb.checked = master.checked; });
    };
    window.moBulkStatus = function(st) {
        var parts = [];
        document.querySelectorAll('.mo-batch-cb:checked').forEach(function(cb) {
            parts.push(cb.value + ',' + st);
        });
        if (parts.length === 0) {
            alert('Please select at least one applicant.');
            return;
        }
        var f = document.createElement('form');
        f.method = 'POST';
        f.action = ctx + '/mo/batch-update-applications';
        var pay = document.createElement('input');
        pay.type = 'hidden'; pay.name = 'batchPayload'; pay.value = parts.join(';');
        f.appendChild(pay);
        var frm = document.getElementById('moApplicantsBatchForm');
        if (frm) {
            var jid = frm.getAttribute('data-job-id');
            if (jid) {
                var j = document.createElement('input');
                j.type = 'hidden'; j.name = 'jobId'; j.value = jid;
                f.appendChild(j);
            }
        }
        document.body.appendChild(f);
        f.submit();
    };
    window.moSaveAllApplicantsPage = function() {
        var parts = [];
        document.querySelectorAll('tr.mo-app-row').forEach(function(tr) {
            var id = tr.getAttribute('data-app-id');
            var sel = tr.querySelector('.mo-status-select');
            if (id && sel) { parts.push(id + ',' + sel.value); }
        });
        if (parts.length === 0) { return; }
        var pay = document.getElementById('moBatchPayload');
        var inc = document.getElementById('moIncludeNotes');
        var frm = document.getElementById('moApplicantsBatchForm');
        if (!pay || !frm) { return; }
        pay.value = parts.join(';');
        inc.value = 'true';
        frm.submit();
    };
    window.moSaveOneApplicantRow = function(appId) {
        var tr = document.querySelector('tr.mo-app-row[data-app-id="' + appId + '"]');
        if (!tr) { return; }
        var sel = tr.querySelector('.mo-status-select');
        if (!sel) { return; }
        var pay = document.getElementById('moBatchPayload');
        var inc = document.getElementById('moIncludeNotes');
        var frm = document.getElementById('moApplicantsBatchForm');
        if (!pay || !frm) { return; }
        pay.value = appId + ',' + sel.value;
        inc.value = 'true';
        frm.submit();
    };
})();
</script>
<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
