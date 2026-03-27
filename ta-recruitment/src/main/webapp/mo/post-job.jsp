<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Post Job - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header">
        <div>
            <h1>Post New TA Position</h1>
            <p>Create a new teaching assistant vacancy for your module</p>
        </div>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= request.getAttribute("error") %></div>
    <% } %>

    <div class="card" style="max-width:640px;">
        <form action="${pageContext.request.contextPath}/mo/post-job" method="post">
            <div class="form-group">
                <label>Module Name *</label>
                <input type="text" name="moduleName" required placeholder="e.g. EBU6304 Software Engineering">
            </div>
            <div class="form-group">
                <label>Job Description *</label>
                <textarea name="description" rows="4" required placeholder="Describe the TA responsibilities..."></textarea>
            </div>
            <div class="form-group">
                <label>Requirements *</label>
                <textarea name="requirements" rows="3" required placeholder="Required skills and qualifications..."></textarea>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label>Number of Vacancies *</label>
                    <input type="number" name="vacancies" min="1" required value="1">
                </div>
                <div class="form-group">
                    <label>Application Deadline *</label>
                    <input type="date" name="deadline" required>
                </div>
            </div>
            <div style="display:flex;gap:12px;margin-top:8px;">
                <button type="submit" class="btn btn-primary btn-lg">Post Job</button>
                <a href="${pageContext.request.contextPath}/mo/jobs.jsp" class="btn btn-secondary btn-lg">Cancel</a>
            </div>
        </form>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
