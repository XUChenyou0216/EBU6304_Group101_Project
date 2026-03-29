<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.JobDAO, com.ta.model.Job, com.ta.util.SessionUtil, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Available Jobs - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header">
        <div>
            <h1>Available TA Positions</h1>
            <p>Browse and apply for teaching assistant opportunities</p>
        </div>
    </div>

    <%
        /* For frontend demonstration based on the prototype screenshot, we will use mock data so it perfectly matches the image.
           The backend can be ignored.
        */
        boolean useMock = true;
    %>

    <% if (request.getParameter("error") != null) { %>
        <div class="alert alert-error">
            <% if ("duplicate".equals(request.getParameter("error"))) { %>
                You have already applied for this position.
            <% } else if ("noprofile".equals(request.getParameter("error"))) { %>
                Please complete your profile before applying. <a href="${pageContext.request.contextPath}/ta/profile.jsp">Go to Profile</a>
            <% } else { %>
                This position is no longer available.
            <% } %>
        </div>
    <% } %>

    <div class="job-grid">
        <!-- Card 1 -->
        <div class="job-card">
            <div class="job-card-top">
                <span class="badge badge-open">Open</span>
                <span class="deadline">
                    <svg style="vertical-align:text-bottom;margin-right:4px;" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                    Deadline: 2026-03-30
                </span>
            </div>
            <h3 style="margin-bottom:8px;font-size:18px;">CS301 - Advanced Algorithms</h3>
            <p class="job-desc" style="color:var(--text-secondary);font-size:14px;flex-grow:1;margin-bottom:20px;">
                Strong understanding of algorithm design, data structures, and complexity analysis. Minimum 2:1 grade.
            </p>
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
                <div style="display:flex; align-items:center; gap:6px; color:var(--text-secondary); font-size:13px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                    <span>Vacancies: <strong>2</strong> / 4 filled</span>
                </div>
                <span class="badge-remaining">2 remaining</span>
            </div>
            <div style="text-align:right;">
                <a href="${pageContext.request.contextPath}/ta/apply?jobId=1" class="btn btn-primary" style="background:#2b4acb;border-radius:6px;font-weight:600;padding:8px 16px;">View Details</a>
            </div>
        </div>

        <!-- Card 2 -->
        <div class="job-card">
            <div class="job-card-top">
                <span class="badge badge-open">Open</span>
                <span class="deadline">
                    <svg style="vertical-align:text-bottom;margin-right:4px;" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                    Deadline: 2026-03-25
                </span>
            </div>
            <h3 style="margin-bottom:8px;font-size:18px;">CS205 - Database Systems</h3>
            <p class="job-desc" style="color:var(--text-secondary);font-size:14px;flex-grow:1;margin-bottom:20px;">
                Experience with SQL, relational databases, and ER modelling. Prior TA experience preferred.
            </p>
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
                <div style="display:flex; align-items:center; gap:6px; color:var(--text-secondary); font-size:13px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                    <span>Vacancies: <strong>1</strong> / 3 filled</span>
                </div>
                <span class="badge-remaining">2 remaining</span>
            </div>
            <div style="text-align:right;">
                <a href="${pageContext.request.contextPath}/ta/apply?jobId=2" class="btn btn-primary" style="background:#2b4acb;border-radius:6px;font-weight:600;padding:8px 16px;">View Details</a>
            </div>
        </div>

        <!-- Card 3 -->
        <div class="job-card">
            <div class="job-card-top">
                <span class="badge badge-open">Open</span>
                <span class="deadline">
                    <svg style="vertical-align:text-bottom;margin-right:4px;" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                    Deadline: 2026-04-05
                </span>
            </div>
            <h3 style="margin-bottom:8px;font-size:18px;">CS150 - Web Development</h3>
            <p class="job-desc" style="color:var(--text-secondary);font-size:14px;flex-grow:1;margin-bottom:20px;">
                Proficiency in HTML, CSS, JavaScript, and React. Good communication skills required.
            </p>
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
                <div style="display:flex; align-items:center; gap:6px; color:var(--text-secondary); font-size:13px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                    <span>Vacancies: <strong>4</strong> / 5 filled</span>
                </div>
                <span class="badge-remaining" style="color:#dc2626;background:#fee2e2;">1 remaining</span>
            </div>
            <div style="text-align:right;">
                <a href="${pageContext.request.contextPath}/ta/apply?jobId=3" class="btn btn-primary" style="background:#2b4acb;border-radius:6px;font-weight:600;padding:8px 16px;">View Details</a>
            </div>
        </div>

        <!-- Card 4 -->
        <div class="job-card">
            <div class="job-card-top">
                <span class="badge badge-open">Open</span>
                <span class="deadline">
                    <svg style="vertical-align:text-bottom;margin-right:4px;" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                    Deadline: 2026-04-15
                </span>
            </div>
            <h3 style="margin-bottom:8px;font-size:18px;">CS401 - Machine Learning</h3>
            <p class="job-desc" style="color:var(--text-secondary);font-size:14px;flex-grow:1;margin-bottom:20px;">
                Strong background in statistics, Python, and ML frameworks (PyTorch/TensorFlow).
            </p>
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
                <div style="display:flex; align-items:center; gap:6px; color:var(--text-secondary); font-size:13px;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                    <span>Vacancies: <strong>0</strong> / 3 filled</span>
                </div>
                <span class="badge-remaining">3 remaining</span>
            </div>
            <div style="text-align:right;">
                <a href="${pageContext.request.contextPath}/ta/apply?jobId=4" class="btn btn-primary" style="background:#2b4acb;border-radius:6px;font-weight:600;padding:8px 16px;">View Details</a>
            </div>
        </div>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
