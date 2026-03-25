package com.ta;

import com.ta.util.Validator;


/**
 * US-T04 验证脚本 - 终端输出版
 * 目标：验证必填项、格式校验以及数据拦截逻辑
 */
public class ValidatorTest {

    public static void main(String[] args) {
        System.out.println("========== 开始 US-T04 Form Validation 自动化测试 ==========");

        int totalTests = 0;
        int passedTests = 0;

        // 测试案例列表：{测试名, 输入数据, 预期结果(null表示通过, 非null表示错误信息)}
        // 案例格式：[用户名, 邮箱, 密码, 电话, 预期是否报错]

        // 1. 验证必填项 (Acceptance Criteria 1)
        if (runTest("必填项校验 - 用户名为空", Validator.requireNonEmpty("", "Username"), true)) passedTests++;
        totalTests++;

        // 2. 验证数据格式 - 邮箱 (Acceptance Criteria 2)
        if (runTest("格式校验 - 非法邮箱格式", Validator.validateEmail("invalid-email-at-com"), true)) passedTests++;
        totalTests++;

        // 3. 验证数据格式 - 电话 (Acceptance Criteria 2)
        if (runTest("格式校验 - 电话包含字母", Validator.validatePhone("12345abc678"), true)) passedTests++;
        totalTests++;

        if (runTest("格式校验 - 电话长度不足", Validator.validatePhone("123"), true)) passedTests++;
        totalTests++;

        // 4. 验证合法数据 (边界测试)
        if (runTest("合法数据校验 - 标准输入", Validator.validateEmail("test@bupt.edu.cn"), false)) passedTests++;
        totalTests++;

        // 5. 模拟数据拦截逻辑 (Acceptance Criteria 3)
        System.out.println("\n[模拟拦截测试]：尝试模拟无效数据进入写入流程...");
        String dummyUsername = "";
        String dummyEmail = "bad_email";

        String error = Validator.requireNonEmpty(dummyUsername, "Username");
        if (error == null) error = Validator.validateEmail(dummyEmail);

        if (error != null) {
            System.out.println(">>> 拦截成功: 检测到错误 [" + error + "]。数据已被拒绝，不会调用 FileManager 写入。");
            passedTests++;
        } else {
            System.err.println(">>> 拦截失败: 错误数据穿透到了写入层！");
        }
        totalTests++;

        System.out.println("\n========== 测试结束 ==========");
        System.out.printf("总计测试: %d | 通过: %d | 失败: %d%n", totalTests, passedTests, (totalTests - passedTests));

        if (passedTests == totalTests) {
            System.out.println("结论：US-T04 的核心逻辑通过终端验证，数据完整性得到保障。");
        }
    }

    private static boolean runTest(String testName, String result, boolean expectError) {
        boolean actualError = (result != null);
        if (actualError == expectError) {
            System.out.println("[PASS] " + testName + (actualError ? " -> 捕获到预期错误: " + result : " -> 合法数据正常通过"));
            return true;
        } else {
            System.err.println("[FAIL] " + testName + " -> 预期有错但漏报，或预期合法但误报。收到: " + result);
            return false;
        }
    }
}