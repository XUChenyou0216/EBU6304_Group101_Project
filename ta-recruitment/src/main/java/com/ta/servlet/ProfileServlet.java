package com.ta.servlet;

import com.ta.dao.TAProfileDAO;
import com.ta.model.TAProfile;
import com.ta.model.User;
import com.ta.util.SessionUtil;
import com.ta.util.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/ta/profile")
public class ProfileServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 设置编码防止中文乱码
        request.setCharacterEncoding("UTF-8");

        User currentUser = SessionUtil.getCurrentUser(request);
        String dataDir = SessionUtil.getDataDir(request);

        // 获取表单参数 [cite: 4, 5, 6, 7, 10]
        String studentId = request.getParameter("studentId");
        String fullName = request.getParameter("fullName");
        String programme = request.getParameter("programme");
        String yearOfStudy = request.getParameter("yearOfStudy");
        String phone = request.getParameter("phone");

        // 1. 调用 Validator 校验必填项
        String error = Validator.validateProfile(studentId, fullName, programme, yearOfStudy);
        if (error == null) {
            error = Validator.validatePhone(phone); // 校验可选的手机号格式
        }

        if (error != null) {
            request.setAttribute("error", error); // 将错误存入 request
            request.getRequestDispatcher("/ta/profile.jsp").forward(request, response);
            return;
        }

        // 2. 调用 DAO 保存数据
        TAProfileDAO profileDAO = new TAProfileDAO(dataDir);
        TAProfile profile = profileDAO.findByUserId(currentUser.getUserId());
        if (profile == null) profile = new TAProfile();

        profile.setUserId(currentUser.getUserId());
        profile.setStudentId(studentId);
        profile.setFullName(fullName);
        profile.setProgramme(programme);
        profile.setYearOfStudy(yearOfStudy);
        profile.setPhone(phone);

        profileDAO.saveOrUpdate(profile);

        // 3. 重定向回页面并附带成功参数
        response.sendRedirect(request.getContextPath() + "/ta/profile.jsp?success=true");
    }
}