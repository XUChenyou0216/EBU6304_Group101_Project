package com.ta;

import com.ta.util.Validator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * US-T04 验证脚本 - JUnit 4 自动化版
 * 适配原始 pom.xml 中的 junit:junit:4.13.2
 */
public class ValidatorTest {

    @Test
    public void testRequiredFields() {
        // Acceptance Criteria 1: 验证必填项
        String error = Validator.requireNonEmpty("", "Username");
        assertNotNull("用户名为空时应当返回错误信息", error);
        assertTrue("错误信息应包含字段名", error.contains("Username"));
    }

    @Test
    public void testInvalidEmailFormat() {
        // Acceptance Criteria 2: 验证邮箱格式
        String error = Validator.validateEmail("invalid-email-at-com");
        assertNotNull("非法的邮箱格式应当被拦截", error);
    }

    @Test
    public void testInvalidPhoneFormat() {
        // Acceptance Criteria 2: 验证电话格式
        assertNotNull("电话包含字母应当被拦截", Validator.validatePhone("12345abc678"));
        assertNotNull("电话长度不足应当被拦截", Validator.validatePhone("123"));
    }

    @Test
    public void testValidData() {
        // 验证合法数据边界
        assertNull("标准的 BUPT 邮箱应当通过校验", Validator.validateEmail("test@bupt.edu.cn"));
    }

    @Test
    public void testInterceptionLogic() {
        // Acceptance Criteria 3: 模拟数据拦截逻辑
        String dummyUsername = "";
        String dummyEmail = "bad_email";

        String error = Validator.requireNonEmpty(dummyUsername, "Username");
        if (error == null) {
            error = Validator.validateEmail(dummyEmail);
        }

        assertNotNull("包含无效数据的流程应当被拦截，不应进入写入层", error);
    }

    @Test
    public void testJobPostingValidation() {
        // 职位发布校验 - 异常流程 (Acceptance Criteria 2)
        assertNotNull("名额包含字母应当报错", Validator.validateJob("Software Engineering", "abc", "2026-06-01"));
        assertNotNull("名额为负数应当报错", Validator.validateJob("Computer Vision", "-5", "2026-06-01"));
        assertNotNull("模块名为空应当报错", Validator.validateJob("", "10", "2026-06-01"));
    }

    @Test
    public void testValidJobPosting() {
        // 职位发布校验 - 合法流程
        assertNull("合法职位数据应当返回 null", Validator.validateJob("AI Basics", "3", "2026-05-20"));
    }
}