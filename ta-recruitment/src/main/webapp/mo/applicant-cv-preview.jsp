<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, com.ta.util.CvMimeUtil, java.net.URLEncoder, java.nio.charset.StandardCharsets" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CV Preview - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .cv-preview-frame { width:100%; min-height:72vh; border:1px solid var(--border-solid); border-radius:var(--radius); background:#fff; }
        .cv-toolbar { display:flex; flex-wrap:wrap; gap:10px; align-items:center; margin-bottom:16px; }
    </style>
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>
<%
    String jobId = request.getParameter("jobId");
    String taUserId = request.getParameter("taUserId");
    String dataDir = SessionUtil.getDataDir(request);
    User u = SessionUtil.getCurrentUser(request);
    Job job = jobId != null ? new JobDAO(dataDir).findById(jobId) : null;
    TAProfile p = taUserId != null ? new TAProfileDAO(dataDir).findByUserId(taUserId) : null;
    boolean allowed = u != null && "MO".equalsIgnoreCase(u.getRole())
        && job != null && job.getMoUserId().equals(u.getUserId())
        && taUserId != null
        && new ApplicationDAO(dataDir).findByJob(jobId).stream()
            .anyMatch(a -> a.getTaUserId().equals(taUserId));
    String cvPath = p != null ? p.getCvFilePath() : null;
    String ctx = request.getContextPath();
    String serveBase = ctx + "/cv/serve?action=applicant&jobId="
        + URLEncoder.encode(jobId != null ? jobId : "", StandardCharsets.UTF_8)
        + "&taUserId=" + URLEncoder.encode(taUserId != null ? taUserId : "", StandardCharsets.UTF_8);
    String previewUrl = serveBase + "&disposition=inline";
    String downloadUrl = serveBase + "&disposition=attachment";
    boolean pdf = cvPath != null && CvMimeUtil.isPdf(cvPath);
%>
    <div class="page-header">
        <div>
            <h1>Applicant CV</h1>
            <p><%= job != null ? job.getModuleName() : "" %> — <%= p != null ? p.getFullName() : taUserId %></p>
        </div>
        <div class="cv-toolbar">
            <a href="<%= ctx %>/mo/applicants<%= jobId != null && !jobId.isEmpty() ? "?jobId=" + jobId : "" %>" class="btn btn-secondary">Back to applicants</a>
            <% if (allowed && cvPath != null && !cvPath.isEmpty()) { %>
                <a href="<%= downloadUrl %>" class="btn btn-primary">Download CV</a>
            <% } %>
        </div>
    </div>

    <% if (!allowed) { %>
        <div class="alert alert-error">You do not have permission to view this CV.</div>
    <% } else if (cvPath == null || cvPath.isEmpty()) { %>
        <div class="empty-state">No CV uploaded for this applicant.</div>
    <% } else if (pdf) { %>
        <iframe class="cv-preview-frame" title="CV preview" src="<%= previewUrl %>"></iframe>
    <% } else { %>
        <div class="card" style="max-width:640px;">
            <p class="mb-4">In-browser preview is not available for Word documents. Use download to open the file locally.</p>
            <a href="<%= downloadUrl %>" class="btn btn-primary">Download CV</a>
        </div>
    <% } %>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
