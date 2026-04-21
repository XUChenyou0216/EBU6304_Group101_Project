<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.model.Job" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Job - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

<% 
    Job job = (Job) request.getAttribute("job"); 
    if (job == null) {
        job = new Job(); // Prevent null pointer exceptions
    }
%>

    <div class="breadcrumb" style="font-size:14px;color:var(--text-secondary);margin-bottom:24px;">
        Job Postings &rarr; <span style="font-weight:600;color:var(--text-dark);">Edit Job</span>
    </div>

    <form action="${pageContext.request.contextPath}/mo/post-job" method="post">
        <input type="hidden" name="jobId" value="<%= job.getJobId() != null ? job.getJobId() : "" %>">
        
        <div class="page-header" style="align-items:flex-start;">
            <div>
                <h1>Edit TA Job</h1>
                <p>Update the Teaching Assistant position details for your module</p>
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
                        <input type="text" name="moduleCode" required placeholder="e.g., CS301" value="<%= job.getModuleCode() != null ? job.getModuleCode().replace("\"", "&quot;") : "" %>">
                    </div>
                    <div class="form-group" style="margin-bottom:0;">
                        <label>Module Name <span style="color:#2b4acb;">*</span></label>
                        <input type="text" name="moduleName" required placeholder="e.g., Advanced Algorithms" value="<%= job.getModuleName() != null ? job.getModuleName().replace("\"", "&quot;") : "" %>">
                    </div>
                </div>
            </div>

            <!-- Job Details -->
            <div class="card" style="padding: 32px 32px; border-radius: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
                <h4 style="margin-bottom: 24px; font-size: 16px; font-weight: 700;">Job Details</h4>
                <div class="form-group" style="margin-bottom:24px;">
                    <label>Job Title <span style="color:#2b4acb;">*</span></label>
                    <input type="text" name="jobTitle" required placeholder="e.g., Teaching Assistant - Advanced Algorithms" value="<%= job.getJobTitle() != null ? job.getJobTitle().replace("\"", "&quot;") : "" %>">
                </div>
                <div class="form-group" style="margin-bottom:24px;">
                    <label>Job Description <span style="color:#2b4acb;">*</span></label>
                    <textarea name="description" rows="5" required placeholder="Provide a brief overview of the role and its purpose..."><%= job.getDescription() != null ? job.getDescription() : "" %></textarea>
                </div>
                <div style="display:grid; grid-template-columns: 1fr 1fr 1fr; gap: 24px;">
                    <div class="form-group" style="margin-bottom:0;">
                        <label>Number of Vacancies <span style="color:#2b4acb;">*</span></label>
                        <input type="number" name="vacancies" min="1" required placeholder="e.g., 2" value="<%= job.getVacancies() > 0 ? job.getVacancies() : "" %>">
                    </div>
                    <div class="form-group" style="margin-bottom:0;">
                        <label>Application Deadline <span style="color:#2b4acb;">*</span></label>
                        <input type="date" name="deadline" required value="<%= job.getDeadline() != null ? job.getDeadline() : "" %>">
                    </div>
                    <div class="form-group" style="margin-bottom:0;">
                        <label>Working Period <span style="color:#2b4acb;">*</span></label>
                        <input type="text" name="workingPeriod" required placeholder="e.g., Jan 2026 - May 2026" value="<%= job.getWorkingPeriod() != null ? job.getWorkingPeriod().replace("\"", "&quot;") : "" %>">
                    </div>
                </div>
            </div>

            <!-- Duties and Responsibilities -->
            <div class="card" style="padding: 32px 32px; border-radius: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
                <h4 style="margin-bottom: 24px; font-size: 16px; font-weight: 700;">Duties and Responsibilities</h4>
                <div class="form-group" style="margin-bottom:0;">
                    <label>Key Duties <span style="color:#2b4acb;">*</span></label>
                    <textarea name="keyDuties" rows="5" required placeholder="List the main responsibilities and tasks..."><%= job.getKeyDuties() != null ? job.getKeyDuties() : "" %></textarea>
                </div>
            </div>

            <!-- Required Skills and Qualifications -->
            <div class="card" style="padding: 32px 32px; border-radius: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
                <h4 style="font-size: 16px; font-weight: 700; padding-bottom: 20px; border-bottom: 1px solid var(--border-solid); margin-bottom: 24px;">Required Skills and Qualifications</h4>
                <div class="form-group" style="margin-bottom:0;">
                    <label>Required Skills <span style="color:#2b4acb;">*</span></label>
                    <textarea name="requiredSkills" rows="4" required placeholder="List the skills and qualifications required..."><%= job.getRequiredSkills() != null ? job.getRequiredSkills() : "" %></textarea>
                    <div style="font-size:12px; color:var(--text-secondary); margin-top:8px;">Include both technical skills and soft skills</div>
                </div>
            </div>

            <!-- Eligibility Requirements -->
            <div class="card" style="padding: 32px 32px; border-radius: 12px; box-shadow: 0 1px 2px rgba(0,0,0,0.05);">
                <h4 style="font-size: 16px; font-weight: 700; padding-bottom: 20px; border-bottom: 1px solid var(--border-solid); margin-bottom: 24px;">Eligibility Requirements</h4>
                <div class="form-group" style="margin-bottom:0;">
                    <label>Eligibility Restrictions <span style="color:#2b4acb;">*</span></label>
                    <textarea name="eligibility" rows="4" required placeholder="Specify any eligibility criteria..."><%= job.getEligibility() != null ? job.getEligibility() : "" %></textarea>
                    <div style="font-size:12px; color:var(--text-secondary); margin-top:8px;">Include year of study, GPA requirements, prerequisite courses, etc.</div>
                </div>
            </div>

            <div style="padding-top: 24px; border-top: 1px solid var(--border-solid); display:flex; justify-content:space-between; align-items:center; margin-top: 8px;">
                <a href="${pageContext.request.contextPath}/mo/jobs.jsp" style="color:var(--text-dark);font-weight:600;text-decoration:none;font-size:15px;padding-left:10px;">Cancel</a>
                <div style="display:flex;gap:12px;">
                    <!-- We'll treat save draft differently if we had an action for it, but let's assume it's just frontend placeholder or we don't have status DRAFT in requirements -->
                    <!-- <button type="button" class="btn btn-outline-primary" style="padding:10px 24px;font-weight:600;border-radius:8px;background:#fff;">Save Draft</button> -->
                    <button type="submit" class="btn btn-primary" style="background:#2b4acb;padding:10px 24px;font-weight:600;border-radius:8px;">Save Changes</button>
                </div>
            </div>

        </div>
    </form>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
