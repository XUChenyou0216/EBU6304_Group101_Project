<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        body.login-body {
            background: #f0f2f5;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .login-wrapper {
            width: 100%;
            max-width: 460px;
            padding: 0 16px;
        }
        /* Logo + title header */
        .login-header {
            text-align: center;
            margin-bottom: 32px;
        }
        .login-logo {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 48px;
            height: 48px;
            background: #1a3c6e;
            border-radius: 10px;
            margin-bottom: 12px;
        }
        .login-logo svg {
            width: 28px;
            height: 28px;
        }
        .login-header h1 {
            font-size: 22px;
            font-weight: 700;
            color: #1a1a2e;
            margin-bottom: 2px;
        }
        .login-header p {
            font-size: 14px;
            color: #6b7280;
            font-weight: 400;
        }
        /* Card */
        .login-card {
            background: #fff;
            border-radius: 12px;
            padding: 36px 32px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08), 0 8px 24px rgba(0,0,0,0.04);
        }
        .login-card h2 {
            font-size: 22px;
            font-weight: 700;
            color: #1a1a2e;
            margin-bottom: 4px;
        }
        .login-card .subtitle {
            font-size: 14px;
            color: #6b7280;
            margin-bottom: 28px;
        }
        /* Form fields */
        .field-label {
            display: block;
            font-size: 14px;
            font-weight: 500;
            color: #374151;
            margin-bottom: 6px;
        }
        .field-input {
            width: 100%;
            padding: 12px 14px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 15px;
            color: #1a1a2e;
            background: #fff;
            transition: border-color 0.2s, box-shadow 0.2s;
            outline: none;
        }
        .field-input::placeholder {
            color: #9ca3af;
        }
        .field-input:focus {
            border-color: #1a3c6e;
            box-shadow: 0 0 0 3px rgba(26,60,110,0.1);
        }
        .field-group {
            margin-bottom: 20px;
        }
        /* Password field with toggle */
        .password-wrapper {
            position: relative;
        }
        .password-wrapper .field-input {
            padding-right: 44px;
        }
        .password-toggle {
            position: absolute;
            right: 12px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            cursor: pointer;
            color: #9ca3af;
            padding: 4px;
            display: flex;
            align-items: center;
        }
        .password-toggle:hover {
            color: #6b7280;
        }
        /* Forgot password link */
        .forgot-row {
            text-align: right;
            margin-top: -12px;
            margin-bottom: 24px;
        }
        .forgot-row a {
            font-size: 13px;
            color: #1a3c6e;
            text-decoration: none;
            font-weight: 500;
        }
        .forgot-row a:hover {
            text-decoration: underline;
        }
        /* Login button */
        .btn-login {
            width: 100%;
            padding: 13px;
            background: #1a3c6e;
            color: #fff;
            border: none;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s;
        }
        .btn-login:hover {
            background: #15325c;
        }
        /* Register link */
        .register-row {
            text-align: center;
            margin-top: 20px;
            font-size: 14px;
            color: #6b7280;
        }
        .register-row a {
            color: #1a3c6e;
            font-weight: 600;
            text-decoration: none;
        }
        .register-row a:hover {
            text-decoration: underline;
        }
        /* Alert override for login page */
        .login-alert {
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }
        .login-alert-error {
            background: #fef2f2;
            color: #dc2626;
            border: 1px solid #fecaca;
        }
        .login-alert-success {
            background: #f0fdf4;
            color: #16a34a;
            border: 1px solid #bbf7d0;
        }
    </style>
</head>
<body class="login-body">
    <div class="login-wrapper">
        <!-- Logo + Title -->
        <div class="login-header">
            <div class="login-logo">
                <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
                    <line x1="8" y1="21" x2="16" y2="21"></line>
                    <line x1="12" y1="17" x2="12" y2="21"></line>
                </svg>
            </div>
            <h1>TA Recruitment System</h1>
            <p>University Teaching Assistant Portal</p>
        </div>

        <!-- Login Card -->
        <div class="login-card">
            <h2>Welcome back</h2>
            <p class="subtitle">Sign in to your account to continue</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="login-alert login-alert-error">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>

            <% if ("true".equals(request.getParameter("registered"))) { %>
                <div class="login-alert login-alert-success">
                    Registration successful! Please sign in.
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/login" method="post">
                <div class="field-group">
                    <label class="field-label" for="username">Student ID / Email</label>
                    <input class="field-input" type="text" id="username" name="username"
                           required placeholder="Enter your Student ID or Email">
                </div>

                <div class="field-group">
                    <label class="field-label" for="password">Password</label>
                    <div class="password-wrapper">
                        <input class="field-input" type="password" id="password" name="password"
                               required placeholder="Enter your password">
                        <button type="button" class="password-toggle" onclick="togglePassword()">
                            <svg id="eye-icon" width="20" height="20" viewBox="0 0 24 24" fill="none"
                                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                                <circle cx="12" cy="12" r="3"></circle>
                            </svg>
                        </button>
                    </div>
                </div>

                <div class="forgot-row">
                    <a href="${pageContext.request.contextPath}/recover.jsp">Forgot Password?</a>
                </div>

                <button type="submit" class="btn-login">Login</button>
            </form>

            <div class="register-row">
                Don't have an account? <a href="${pageContext.request.contextPath}/register.jsp">Register</a>
            </div>
        </div>
    </div>

    <script>
        function togglePassword() {
            var input = document.getElementById('password');
            var icon = document.getElementById('eye-icon');
            if (input.type === 'password') {
                input.type = 'text';
                icon.innerHTML = '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line>';
            } else {
                input.type = 'password';
                icon.innerHTML = '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle>';
            }
        }
    </script>
</body>
</html>
