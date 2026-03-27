<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Update Job - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="breadcrumb" style="font-size:14px;color:var(--text-secondary);margin-bottom:24px;">
        Job Postings &rarr; <span style="font-weight:600;color:var(--text-dark);">Edit Job</span>
    </div>

    <form action="${pageContext.request.contextPath}/mo/post-job" method="post">
        
        <div class="page-header" style="align-items:flex-start;">
            <div>
                <h1>Post New TA Job</h1>
                <p>Create a new Teaching Assistant position for your module</p>
            </div>
            <div style="display:flex;gap:12px;">
                <button type="button" class="btn btn-secondary">Save Draft</button>
                <button type="submit" class="btn btn-primary" style="background:#2b4acb;">Publish Job</button>
            </div>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error"><%= request.getAttribute("error") %></div>
        <% } %>

        <div style="max-width:800px; display:flex; flex-direction:column; gap:24px; padding-bottom:40px;">

            <!-- Module Information -->
            <div class="card" style="padding: 32px 32px; border-radius: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
                <h4 style="margin-bottom: 24px; font-size: 16px; font-weight: 700;">Module Information</h4>
                <div style="display:grid; grid-template-columns: 1fr 1fr; gap: 24px;">
                    <div class="form-group" style="margin-bottom:0;">
                        <label>Module Code <span style="color:#2b4acb;">*</span></label>
                        <input type="text" name="moduleCode" required placeholder="e.g., CS301">
                    </div>
                    <div class="form-group" style="margin-bottom:0;">
                        <label>Module Name <span style="color:#2b4acb;">*</span></label>
                        <input type="text" name="moduleName" required placeholder="e.g., Advanced Algorithms">
                    </div>
                </div>
            </div>

            <!-- Job Details -->
            <div class="card" style="padding: 32px 32px; border-radius: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
                <h4 style="margin-bottom: 24px; font-size: 16px; font-weight: 700;">Job Details</h4>
                <div class="form-group" style="margin-bottom:24px;">
                    <label>Job Title <span style="color:#2b4acb;">*</span></label>
                    <input type="text" name="jobTitle" required placeholder="e.g., Teaching Assistant - Advanced Algorithms">
                </div>
                <div class="form-group" style="margin-bottom:24px;">
                    <label>Job Description <span style="color:#2b4acb;">*</span></label>
                    <textarea name="description" rows="5" required placeholder="Provide a brief overview of the role and its purpose..."></textarea>
                </div>
                <div style="display:grid; grid-template-columns: 1fr 1fr 1fr; gap: 24px;">
                    <div class="form-group" style="margin-bottom:0;">
                        <label>Number of Vacancies <span style="color:#2b4acb;">*</span></label>
                        <input type="number" name="vacancies" min="1" required placeholder="e.g., 2">
                    </div>
                    <div class="form-group" style="margin-bottom:0;">
                        <label>Application Deadline <span style="color:#2b4acb;">*</span></label>
                        <input type="date" name="deadline" required>
                    </div>
                    <div class="form-group" style="margin-bottom:0;">
                        <label>Working Period <span style="color:#2b4acb;">*</span></label>
                        <input type="text" name="workingPeriod" required placeholder="e.g., Jan 2026 - May 2026">
                    </div>
                </div>
            </div>

            <!-- Duties and Responsibilities -->
            <div class="card" style="padding: 32px 32px; border-radius: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
                <h4 style="margin-bottom: 24px; font-size: 16px; font-weight: 700;">Duties and Responsibilities</h4>
                <div class="form-group" style="margin-bottom:0;">
                    <label>Key Duties <span style="color:#2b4acb;">*</span></label>
                    <textarea name="keyDuties" rows="5" required placeholder="List the main responsibilities and tasks..."></textarea>
                </div>
            </div>

        </div>
    </form>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
