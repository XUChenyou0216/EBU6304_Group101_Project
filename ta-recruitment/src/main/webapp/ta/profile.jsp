<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.TAProfileDAO, com.ta.model.TAProfile, com.ta.model.User, com.ta.util.SessionUtil" %>
<!DOCTYPE html>
<html>
<head>
    <title>My Profile - TA System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .profile-grid { display: flex; align-items: stretch; gap: 24px; margin-top: 20px; }
        .card { flex: 1; display: flex; flex-direction: column; background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); border: 1px solid #eee; }
        .card:first-child { flex: 1.2; } .card:last-child { flex: 0.8; }
        .cv-box { border: 1px solid #e2e8f0; padding: 16px; border-radius: 12px; margin-bottom: 20px; display: flex; align-items: center; gap: 12px; background: #f8fafc; }
        .cv-icon { background: #eef2ff; color: #4f46e5; width: 40px; height: 40px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 20px; }
        .cv-info { flex-grow: 1; min-width: 0; }
        .cv-filename { font-weight: 600; font-size: 14px; color: #2563eb; text-decoration: none; display: flex; align-items: center; gap: 5px; }
        .status-check { color: #10b981; font-weight: bold; }
        .cv-meta { font-size: 12px; color: #64748b; margin-top: 2px; }
        .upload-zone { border: 2px dashed #cbd5e1; padding: 40px 20px; text-align: center; border-radius: 12px; background: #fdfdfd; cursor: pointer; transition: all 0.3s; margin-top: auto; display: block; }
        .upload-zone:hover { border-color: #3b82f6; background: #eff6ff; }
        .form-group { margin-bottom: 18px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 14px; color: #334155; }
        .form-group input, .form-group select { width: 100%; padding: 11px; border: 1px solid #cbd5e1; border-radius: 6px; box-sizing: border-box; font-size: 14px; }
        .mandatory-hint { color: #ef4444; font-size: 12px; margin-top: 5px; display: flex; align-items: center; gap: 4px; }
    </style>
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

<%
    String dataDir = SessionUtil.getDataDir(request);
    TAProfile profile = null;
    if (currentUser != null) {
        profile = new TAProfileDAO(dataDir).findByUserId(currentUser.getUserId());
    } else {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>

<div class="container" style="max-width: 1100px; margin: 0 auto; padding: 20px;">
    <div class="page-header">
        <h1 style="font-size: 24px; color: #1e293b; margin-bottom: 4px;">Profile & CV Upload</h1>
        <p style="color: #64748b; margin-bottom: 24px;">Manage your profile information and upload your CV</p>
    </div>

    <div class="profile-grid">
        <div class="card">
            <h3 style="margin: 0 0 20px 0; font-size: 18px;">Personal Information</h3>
            <form action="${pageContext.request.contextPath}/ta/profile" method="post" style="flex: 1; display: flex; flex-direction: column;">

                <%-- 1. Student ID --%>
                <div class="form-group">
                    <label>Student ID</label>
                    <input type="text" name="studentId" value="<%= profile!=null?profile.getStudentId():"" %>" placeholder="e.g. 2024CS1042" required>
                </div>

                <%-- 2. Full Name (修复: 添加这个必填字段) --%>
                <div class="form-group">
                    <label>Full Name <span style="color:red">*</span></label>
                    <input type="text" name="fullName" value="<%= profile!=null?profile.getFullName():"" %>" placeholder="Enter your full name" required>
                    <% if (profile == null || profile.getFullName() == null || profile.getFullName().isEmpty()) { %>
                        <div class="mandatory-hint">❌ Full Name is required</div>
                    <% } %>
                </div>

                <%-- 3. Programme --%>
                <div class="form-group">
                    <label>Programme</label>
                    <input type="text" name="programme" value="<%= (profile!=null && profile.getProgramme()!=null) ? profile.getProgramme() : "" %>" placeholder="e.g. BSc Computer Science">
                </div>

                <%-- 4. Year of Study --%>
                <div class="form-group">
                    <label>Year of Study</label>
                    <select name="yearOfStudy">
                        <option value="Year 1" <%= profile!=null&&"Year 1".equals(profile.getYearOfStudy())?"selected":"" %>>Year 1</option>
                        <option value="Year 2" <%= profile!=null&&"Year 2".equals(profile.getYearOfStudy())?"selected":"" %>>Year 2</option>
                        <option value="Year 3" <%= profile!=null&&"Year 3".equals(profile.getYearOfStudy())?"selected":"" %>>Year 3</option>
                        <option value="Year 4" <%= profile!=null&&"Year 4".equals(profile.getYearOfStudy())?"selected":"" %>>Year 4</option>
                    </select>
                </div>

                <%-- 5. Contact Details --%>
                <div class="form-group">
                    <label>Contact Details</label>
                    <input type="email" value="<%= currentUser.getEmail() %>" disabled style="background:#f8fafc; color: #64748b; cursor: not-allowed;">
                </div>

                <div style="margin-top: auto; padding-top: 10px;">
                    <button type="submit" class="btn btn-primary" style="background: #1e3a8a; padding: 12px 24px; border: none; border-radius: 6px; color: white; font-weight: 600; cursor: pointer;">Save Profile</button>
                </div>
            </form>
        </div>

        <div class="card">
            <h3 style="margin: 0 0 20px 0; font-size: 18px;">CV Upload</h3>

            <%-- 已上传显示 --%>
            <% if (profile != null && profile.getCvFilePath() != null && !profile.getCvFilePath().isEmpty()) {
                String filePath = profile.getCvFilePath();
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            %>
                <div class="cv-box">
                    <div class="cv-icon">📄</div>
                    <div class="cv-info">
                        <a href="${pageContext.request.contextPath}/<%= filePath %>" target="_blank" class="cv-filename">
                            <%= fileName %> <span class="status-check">✓</span>
                        </a>
                        <div class="cv-meta">Uploaded on system</div>
                    </div>
                    <button type="button" onclick="document.getElementById('cvFile').click()" style="background: white; border: 1px solid #cbd5e1; padding: 6px 12px; border-radius: 6px; cursor: pointer; font-size: 12px;">🔄 Replace File</button>
                </div>
            <% } %>

            <%-- 成功显示：当操作成功时显示 --%>
            <% if (request.getAttribute("success") != null || "true".equals(request.getParameter("success"))) { %>
                <div style="background: #f0fdf4; color: #15803d; padding: 14px; border-radius: 8px; font-size: 13px; margin-bottom: 20px; border: 1px solid #dcfce7;">
                    <div style="font-weight: bold; margin-bottom: 4px;">✅ Successed</div>
                    Your CV has been uploaded and updated successfully.
                </div>
            <% } %>

            <%-- 错误显示：此处显示由 Servlet 返回的 error 消息 --%>
            <% if (request.getAttribute("error") != null) { %>
                <div style="background: #fef2f2; color: #b91c1c; padding: 14px; border-radius: 8px; font-size: 13px; margin-bottom: 20px; border: 1px solid #fee2e2;">
                    <div style="font-weight: bold; margin-bottom: 4px;">⚠️ Upload Failed</div>
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/ta/upload-cv" method="post" enctype="multipart/form-data" style="flex: 1; display: flex; flex-direction: column;">
                <label for="cvFile" class="upload-zone">
                    <div style="font-size: 32px; margin-bottom: 12px;">📤</div>
                    <div style="font-weight: 700; font-size: 16px; color: #1e293b;">Upload your CV</div>
                    <div style="font-size: 13px; color: #64748b; margin: 6px 0;">Drag & drop your file here, or click to browse</div>
                    <div style="font-size: 11px; color: #94a3b8; font-weight: 500;">PDF, DOC, DOCX, JPG, PNG (max 10MB)</div>
                </label>
                <input type="file" id="cvFile" name="cvFile" style="display: none;" accept=".pdf,.doc,.docx,.jpg,.png" onchange="this.form.submit()">
            </form>
        </div>
    </div>
</div>
</body>
</html>