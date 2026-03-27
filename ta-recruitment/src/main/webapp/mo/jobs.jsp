<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Job Posts - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header" style="align-items: center;">
        <div>
            <h1>Job Posts</h1>
            <p>Manage all your TA position postings</p>
        </div>
    </div>

    <% if ("posted".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">Job posted successfully!</div>
    <% } %>

    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 24px;">
        <div class="filter-tabs" style="margin-bottom: 0;">
            <span class="filter-tab active">All</span>
            <span class="filter-tab">Active</span>
            <span class="filter-tab">Closed</span>
        </div>
        <a href="${pageContext.request.contextPath}/mo/post-job.jsp" class="btn btn-primary" style="padding: 8px 20px; border-radius: 20px;">+ Create New Job</a>
    </div>

    <p class="text-sm text-muted mb-4">Showing <strong>7</strong> total job posts</p>

    <div style="background:var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius); overflow: hidden;">
        <table class="data-table" style="border: none; border-radius: 0;">
            <thead>
                <tr>
                    <th>MODULE</th>
                    <th>JOB TITLE</th>
                    <th>POSTED</th>
                    <th>DEADLINE</th>
                    <th>POSITIONS</th>
                    <th>APPLICANTS</th>
                    <th>STATUS</th>
                    <th>ACTIONS</th>
                </tr>
            </thead>
            <tbody>
                <!-- Mock Data matched to screenshot -->
                <tr>
                    <td><span class="module-code">CS301</span></td>
                    <td><strong>TA - Advanced Algorithms</strong></td>
                    <td>2026-03-10</td>
                    <td>2026-03-30</td>
                    <td>2</td>
                    <td><a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=1" style="color:var(--primary);font-weight:700;">12</a></td>
                    <td><span class="badge badge-active">Active</span></td>
                    <td style="display:flex;gap:8px;">
                        <a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=1" class="btn btn-primary btn-sm" style="border-radius:20px;padding:6px 16px;">View</a>
                        <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=1" class="btn btn-outline-primary btn-sm" style="border-radius:20px;padding:6px 16px;">Edit</a>
                    </td>
                </tr>
                <tr>
                    <td><span class="module-code">CS205</span></td>
                    <td><strong>TA - Database Systems</strong></td>
                    <td>2026-03-08</td>
                    <td>2026-03-25</td>
                    <td>3</td>
                    <td><a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=2" style="color:var(--primary);font-weight:700;">8</a></td>
                    <td><span class="badge badge-active">Active</span></td>
                    <td style="display:flex;gap:8px;">
                        <a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=2" class="btn btn-primary btn-sm" style="border-radius:20px;padding:6px 16px;">View</a>
                        <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=2" class="btn btn-outline-primary btn-sm" style="border-radius:20px;padding:6px 16px;">Edit</a>
                    </td>
                </tr>
                <tr>
                    <td><span class="module-code">CS150</span></td>
                    <td><strong>TA - Web Development</strong></td>
                    <td>2026-03-05</td>
                    <td>2026-04-05</td>
                    <td>2</td>
                    <td><a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=3" style="color:var(--primary);font-weight:700;">15</a></td>
                    <td><span class="badge badge-active">Active</span></td>
                    <td style="display:flex;gap:8px;">
                        <a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=3" class="btn btn-primary btn-sm" style="border-radius:20px;padding:6px 16px;">View</a>
                        <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=3" class="btn btn-outline-primary btn-sm" style="border-radius:20px;padding:6px 16px;">Edit</a>
                    </td>
                </tr>
                <tr>
                    <td><span class="module-code">CS401</span></td>
                    <td><strong>TA - Machine Learning</strong></td>
                    <td>2026-02-20</td>
                    <td>2026-03-10</td>
                    <td>1</td>
                    <td><a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=4" style="color:var(--primary);font-weight:700;">18</a></td>
                    <td><span class="badge badge-closed">Closed</span></td>
                    <td style="display:flex;gap:8px;">
                        <a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=4" class="btn btn-primary btn-sm" style="border-radius:20px;padding:6px 16px;">View</a>
                        <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=4" class="btn btn-outline-primary btn-sm" style="border-radius:20px;padding:6px 16px;">Edit</a>
                    </td>
                </tr>
                <tr>
                    <td><span class="module-code">CS202</span></td>
                    <td><strong>TA - Data Structures</strong></td>
                    <td>2026-02-15</td>
                    <td>2026-03-05</td>
                    <td>3</td>
                    <td><a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=5" style="color:var(--primary);font-weight:700;">22</a></td>
                    <td><span class="badge badge-closed">Closed</span></td>
                    <td style="display:flex;gap:8px;">
                        <a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=5" class="btn btn-primary btn-sm" style="border-radius:20px;padding:6px 16px;">View</a>
                        <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=5" class="btn btn-outline-primary btn-sm" style="border-radius:20px;padding:6px 16px;">Edit</a>
                    </td>
                </tr>
                <tr>
                    <td><span class="module-code">CS350</span></td>
                    <td><strong>TA - Operating Systems</strong></td>
                    <td>2026-03-12</td>
                    <td>2026-04-01</td>
                    <td>2</td>
                    <td><a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=6" style="color:var(--primary);font-weight:700;">10</a></td>
                    <td><span class="badge badge-active">Active</span></td>
                    <td style="display:flex;gap:8px;">
                        <a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=6" class="btn btn-primary btn-sm" style="border-radius:20px;padding:6px 16px;">View</a>
                        <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=6" class="btn btn-outline-primary btn-sm" style="border-radius:20px;padding:6px 16px;">Edit</a>
                    </td>
                </tr>
                <tr>
                    <td><span class="module-code">CS320</span></td>
                    <td><strong>TA - Computer Networks</strong></td>
                    <td>2026-02-10</td>
                    <td>2026-03-01</td>
                    <td>2</td>
                    <td><a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=7" style="color:var(--primary);font-weight:700;">14</a></td>
                    <td><span class="badge badge-closed">Closed</span></td>
                    <td style="display:flex;gap:8px;">
                        <a href="${pageContext.request.contextPath}/mo/applicants.jsp?jobId=7" class="btn btn-primary btn-sm" style="border-radius:20px;padding:6px 16px;">View</a>
                        <a href="${pageContext.request.contextPath}/mo/edit-job?jobId=7" class="btn btn-outline-primary btn-sm" style="border-radius:20px;padding:6px 16px;">Edit</a>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
