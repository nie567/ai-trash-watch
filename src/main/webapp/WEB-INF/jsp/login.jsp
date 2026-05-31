<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 垃圾分类监管系统</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background:
                radial-gradient(ellipse at 20% 50%, rgba(59, 130, 246, 0.25) 0%, transparent 60%),
                radial-gradient(ellipse at 80% 20%, rgba(30, 58, 138, 0.2) 0%, transparent 55%),
                radial-gradient(ellipse at 50% 100%, rgba(59, 130, 246, 0.1) 0%, transparent 50%),
                linear-gradient(160deg, #0c0a1a 0%, #120e24 30%, #0f172a 65%, #0a0f1e 100%);
            background-attachment: fixed;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
        }

        /* ===== 粒子背景 ===== */
        .particles {
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            pointer-events: none;
            z-index: 0;
        }
        .particle {
            position: absolute;
            border-radius: 50%;
            background: rgba(59, 130, 246, 0.35);
            animation: particleFloat linear infinite;
        }
        @keyframes particleFloat {
            0%   { transform: translateY(0) scale(1); opacity: 0; }
            10%  { opacity: 1; }
            90%  { opacity: 1; }
            100% { transform: translateY(-100vh) scale(0.3); opacity: 0; }
        }

        /* ===== 网格线条 ===== */
        .grid-lines {
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            pointer-events: none;
            z-index: 0;
            background-image:
                linear-gradient(rgba(59, 130, 246, 0.03) 1px, transparent 1px),
                linear-gradient(90deg, rgba(59, 130, 246, 0.03) 1px, transparent 1px);
            background-size: 60px 60px;
            animation: gridShift 20s linear infinite;
        }
        @keyframes gridShift {
            0%   { background-position: 0 0; }
            100% { background-position: 60px 60px; }
        }

        /* ===== 卡片容器 ===== */
        .card-wrapper {
            position: relative;
            z-index: 1;
        }

        /* ===== 通用卡片样式 ===== */
        .auth-card {
            width: 420px;
            padding: 44px 40px;
            background: rgba(30, 30, 50, 0.85);
            backdrop-filter: blur(12px);
            -webkit-backdrop-filter: blur(12px);
            border: 1px solid rgba(59, 130, 246, 0.2);
            border-radius: 20px;
            box-shadow:
                0 0 30px rgba(59, 130, 246, 0.15),
                0 25px 50px rgba(0, 0, 0, 0.4),
                inset 0 1px 0 rgba(255, 255, 255, 0.05);
            position: relative;
        }

        /* 页面首次加载入场动画（仅一次） */
        .auth-card.initial-enter {
            animation: cardEnter 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
        }

        /* 卡片切换动画（纯水平，不含Y轴） */
        .auth-card.slide-out-left {
            animation: slideOutLeft 0.25s ease forwards !important;
        }
        .auth-card.slide-out-right {
            animation: slideOutRight 0.25s ease forwards !important;
        }
        .auth-card.slide-in-left {
            animation: slideInLeft 0.25s ease forwards !important;
        }
        .auth-card.slide-in-right {
            animation: slideInRight 0.25s ease forwards !important;
        }

        @keyframes cardEnter {
            from { transform: translateY(20px); opacity: 0; }
            to   { transform: translateY(0); opacity: 1; }
        }
        @keyframes slideOutLeft {
            to { transform: translateX(-30px); opacity: 0; }
        }
        @keyframes slideOutRight {
            to { transform: translateX(30px); opacity: 0; }
        }
        @keyframes slideInLeft {
            from { transform: translateX(-30px); opacity: 0; }
            to   { transform: translateX(0); opacity: 1; }
        }
        @keyframes slideInRight {
            from { transform: translateX(30px); opacity: 0; }
            to   { transform: translateX(0); opacity: 1; }
        }

        /* 隐藏的卡片 */
        .auth-card[hidden] {
            display: none;
        }

        /* 卡片顶部装饰光条 */
        .auth-card::before {
            content: '';
            position: absolute;
            top: 0; left: 20%; right: 20%;
            height: 2px;
            background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.6), transparent);
            border-radius: 2px;
        }

        .auth-header {
            text-align: center;
            margin-bottom: 32px;
        }
        .auth-header h1 {
            font-size: 26px;
            font-weight: 700;
            background: linear-gradient(135deg, #60A5FA, #3B82F6, #93C5FD);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 8px;
            animation: titleShimmer 3s ease-in-out infinite alternate;
            background-size: 200% 100%;
        }
        @keyframes titleShimmer {
            0%   { background-position: 0% 50%; }
            100% { background-position: 100% 50%; }
        }
        .auth-header p {
            color: rgba(255, 255, 255, 0.55);
            font-size: 14px;
            letter-spacing: 0.5px;
        }

        .form-group {
            margin-bottom: 20px;
        }
        .form-group label {
            display: block;
            color: rgba(255, 255, 255, 0.55);
            font-size: 14px;
            font-weight: 500;
            margin-bottom: 8px;
            letter-spacing: 0.3px;
        }
        .form-group input[type="text"],
        .form-group input[type="password"],
        .form-group input[type="email"],
        .form-group input[type="tel"] {
            width: 100%;
            padding: 13px 16px;
            background: rgba(255, 255, 255, 0.05);
            border: 1px solid rgba(59, 130, 246, 0.12);
            border-radius: 10px;
            font-size: 14px;
            color: #F1F5F9;
            transition: all 0.3s ease;
        }
        .form-group input:focus {
            outline: none;
            border-color: rgba(59, 130, 246, 0.5);
            background: rgba(255, 255, 255, 0.08);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }
        .form-group input::placeholder {
            color: rgba(255, 255, 255, 0.25);
        }

        .form-group .input-hint {
            font-size: 14px;
            color: rgba(255, 255, 255, 0.3);
            margin-top: 4px;
        }

        .form-row {
            display: flex;
            gap: 12px;
        }
        .form-row .form-group {
            flex: 1;
        }

        .form-group.remember {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 24px;
        }
        .form-group.remember input[type="checkbox"] {
            width: 16px;
            height: 16px;
            accent-color: #3B82F6;
        }
        .form-group.remember label {
            margin-bottom: 0;
            font-size: 14px;
            color: rgba(255, 255, 255, 0.55);
        }

        .btn-auth {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #3B82F6, #2563EB);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
            letter-spacing: 2px;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }
        .btn-auth:hover {
            transform: translateY(-1px);
            box-shadow: 0 6px 25px rgba(59, 130, 246, 0.35);
        }
        .btn-auth:active {
            transform: translateY(0);
        }
        .btn-auth::after {
            content: '';
            position: absolute;
            inset: 0;
            background: linear-gradient(135deg, transparent 30%, rgba(255,255,255,0.1) 50%, transparent 70%);
            transform: translateX(-100%);
            transition: transform 0.5s;
        }
        .btn-auth:hover::after {
            transform: translateX(100%);
        }

        .error-message {
            background: rgba(239, 68, 68, 0.1);
            border: 1px solid rgba(239, 68, 68, 0.3);
            color: #fca5a5;
            padding: 10px 14px;
            border-radius: 8px;
            margin-bottom: 16px;
            font-size: 14px;
            animation: fadeIn 0.3s ease;
        }
        .success-message {
            background: rgba(34, 197, 94, 0.1);
            border: 1px solid rgba(34, 197, 94, 0.3);
            color: #86efac;
            padding: 10px 14px;
            border-radius: 8px;
            margin-bottom: 16px;
            font-size: 14px;
            animation: fadeIn 0.3s ease;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-6px); }
            to   { opacity: 1; transform: translateY(0); }
        }

        .switch-link {
            text-align: center;
            margin-top: 24px;
            color: rgba(255, 255, 255, 0.35);
            font-size: 14px;
        }
        .switch-link a {
            color: #60A5FA;
            cursor: pointer;
            text-decoration: none;
            font-weight: 500;
            transition: color 0.2s;
        }
        .switch-link a:hover {
            color: #93C5FD;
            text-decoration: underline;
        }

        /* ===== 响应式 ===== */
        @media (max-width: 480px) {
            .auth-card {
                width: 92vw;
                padding: 32px 24px;
            }
            .form-row {
                flex-direction: column;
                gap: 0;
            }
        }
    </style>
</head>
<body>
    <div class="grid-lines"></div>
    <div class="particles" id="particles"></div>

    <div class="card-wrapper">
        <!-- ========== 登录卡片 ========== -->
        <div class="auth-card login-card initial-enter" id="loginCard" <c:if test="${not empty showRegister or not empty regError}">hidden</c:if>>
            <div class="auth-header">
                <h1>垃圾分类监管系统</h1>
                <p>请登录您的账号</p>
            </div>

            <c:if test="${not empty success}">
                <div class="success-message">${success}</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="error-message">${error}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/login">
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input type="text" id="username" name="username"
                           value="${cookie.rememberUser.value}"
                           placeholder="请输入用户名" required autofocus>
                </div>

                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password"
                           placeholder="请输入密码" required>
                </div>

                <div class="form-group remember">
                    <input type="checkbox" id="rememberMe" name="rememberMe">
                    <label for="rememberMe">记住用户名</label>
                </div>

                <button type="submit" class="btn-auth">登 录</button>
            </form>

            <div class="switch-link">
                还没有账号？<a onclick="switchToRegister()">立即注册</a>
            </div>
        </div>

        <!-- ========== 注册卡片 ========== -->
        <div class="auth-card register-card initial-enter" id="registerCard" <c:if test="${empty showRegister and empty regError}">hidden</c:if>>
            <div class="auth-header">
                <h1>垃圾分类监管系统</h1>
                <p>创建新账号</p>
            </div>

            <c:if test="${not empty regError}">
                <div class="error-message">${regError}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/register" id="registerForm">
                <div class="form-group">
                    <label for="regUsername">用户名</label>
                    <input type="text" id="regUsername" name="username"
                           value="${regUsername}"
                           placeholder="3-20位字母、数字或下划线" required
                           minlength="3" maxlength="20" pattern="[a-zA-Z0-9_]+">
                    <div class="input-hint">3-20位字母、数字或下划线</div>
                </div>

                <div class="form-group">
                    <label for="regPassword">密码</label>
                    <input type="password" id="regPassword" name="password"
                           placeholder="请输入密码" required
                           minlength="8" maxlength="64">
                    <div class="input-hint">至少8位，需包含字母和数字</div>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">确认密码</label>
                    <input type="password" id="confirmPassword" name="confirmPassword"
                           placeholder="请再次输入密码" required>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="regEmail">邮箱 <span style="opacity:0.5">(选填)</span></label>
                        <input type="email" id="regEmail" name="email"
                               value="${regEmail}"
                               placeholder="example@mail.com">
                    </div>
                    <div class="form-group">
                        <label for="regPhone">手机 <span style="opacity:0.5">(选填)</span></label>
                        <input type="tel" id="regPhone" name="phone"
                               value="${regPhone}"
                               placeholder="13800138000">
                    </div>
                </div>

                <button type="submit" class="btn-auth">注 册</button>
            </form>

            <div class="switch-link">
                已有账号？<a onclick="switchToLogin()">返回登录</a>
            </div>
        </div>
    </div>

    <script>
        // 纯 CSS 粒子需要 JS 初始化 DOM，动画由 CSS @keyframes 驱动
        (function() {
            var container = document.getElementById('particles');
            if (!container) return;
            var count = 25;
            for (var i = 0; i < count; i++) {
                var p = document.createElement('div');
                p.className = 'particle';
                var size = Math.random() * 3 + 1;
                var left = Math.random() * 100;
                var duration = Math.random() * 15 + 10;
                var delay = Math.random() * 20;
                p.style.width = size + 'px';
                p.style.height = size + 'px';
                p.style.left = left + '%';
                p.style.bottom = '-10px';
                p.style.animationDuration = duration + 's';
                p.style.animationDelay = delay + 's';
                p.style.opacity = '0';
                container.appendChild(p);
            }
        })();

        // 登录/注册卡片切换（纯水平滑动，不含Y轴抖动）
        var switching = false;
        function switchToRegister() {
            if (switching) return;
            switching = true;
            var login = document.getElementById('loginCard');
            var register = document.getElementById('registerCard');
            login.classList.add('slide-out-left');
            login.addEventListener('animationend', function handler() {
                login.removeEventListener('animationend', handler);
                login.hidden = true;
                login.classList.remove('slide-out-left', 'initial-enter');
                register.classList.remove('initial-enter');
                register.hidden = false;
                register.classList.add('slide-in-right');
                register.addEventListener('animationend', function h2() {
                    register.removeEventListener('animationend', h2);
                    register.classList.remove('slide-in-right');
                    switching = false;
                    var el = document.getElementById('regUsername');
                    if (el) el.focus();
                });
            });
        }
        function switchToLogin() {
            if (switching) return;
            switching = true;
            var login = document.getElementById('loginCard');
            var register = document.getElementById('registerCard');
            register.classList.add('slide-out-right');
            register.addEventListener('animationend', function handler() {
                register.removeEventListener('animationend', handler);
                register.hidden = true;
                register.classList.remove('slide-out-right', 'initial-enter');
                login.classList.remove('initial-enter');
                login.hidden = false;
                login.classList.add('slide-in-left');
                login.addEventListener('animationend', function h2() {
                    login.removeEventListener('animationend', h2);
                    login.classList.remove('slide-in-left');
                    switching = false;
                    var el = document.getElementById('username');
                    if (el) el.focus();
                });
            });
        }

        // 前端密码一致性校验
        (function() {
            var form = document.getElementById('registerForm');
            if (!form) return;
            form.addEventListener('submit', function(e) {
                var pwd = document.getElementById('regPassword').value;
                var confirm = document.getElementById('confirmPassword').value;
                if (pwd !== confirm) {
                    e.preventDefault();
                    var existing = form.querySelector('.error-message');
                    if (existing) existing.remove();
                    var div = document.createElement('div');
                    div.className = 'error-message';
                    div.textContent = '两次输入的密码不一致';
                    form.insertBefore(div, form.firstChild);
                    document.getElementById('confirmPassword').focus();
                }
            });
        })();
    </script>
</body>
</html>
