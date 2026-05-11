<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="util.SessionValidator" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>ProcureGov - Secure Government Procurement Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
    <div class="login-wrapper">
        <!-- Hero Section -->
        <div class="hero-section">
            <div class="hero-content">
                <div class="hero-logo">
                    <div class="logo-icon">🏛️</div>
                    <h1>ProcureGov</h1>
                    <p class="hero-subtitle">Ministry of Public Works</p>
                    <p class="hero-location">Kingdom of Lesotho</p>
                </div>

                <div class="hero-features">
                    <div class="feature-item">
                        <div class="feature-icon">🔒</div>
                        <div class="feature-text">
                            <h3>Secure & Transparent</h3>
                            <p>End-to-end encrypted procurement process</p>
                        </div>
                    </div>
                    <div class="feature-item">
                        <div class="feature-icon">⚡</div>
                        <div class="feature-text">
                            <h3>Efficient Workflow</h3>
                            <p>Streamlined tender management system</p>
                        </div>
                    </div>
                    <div class="feature-item">
                        <div class="feature-icon">📊</div>
                        <div class="feature-text">
                            <h3>Data-Driven</h3>
                            <p>Comprehensive reporting and analytics</p>
                        </div>
                    </div>
                </div>

                <div class="hero-quote">
                    <blockquote>
                        "Building Lesotho's future through fair and transparent procurement practices"
                    </blockquote>
                    <cite>— Ministry of Public Works</cite>
                </div>
            </div>
        </div>

        <!-- Login Section -->
        <div class="login-section">
            <div class="login-card">
                <div class="login-header">
                    <h2>Welcome Back</h2>
                    <p>Access your procurement dashboard</p>
                </div>

                <c:if test="${not empty param.loggedout}">
                    <div class="message message-success">
                        <div class="message-icon">✅</div>
                        <div class="message-content">
                            <strong>Logged out successfully</strong>
                            <p>Thank you for using ProcureGov</p>
                        </div>
                    </div>
                </c:if>

                <c:if test="${not empty param.error or not empty error}">
                    <div class="message message-error">
                        <div class="message-icon">⚠️</div>
                        <div class="message-content">
                            <strong>Login Failed</strong>
                            <p>${param.error}${error}</p>
                        </div>
                    </div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/login" class="login-form">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <div class="input-wrapper">
                            <div class="input-icon">👤</div>
                            <input type="text" id="username" name="username" placeholder="Enter your username" required autofocus>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="password">Password</label>
                        <div class="input-wrapper">
                            <div class="input-icon">🔑</div>
                            <input type="password" id="password" name="password" placeholder="Enter your password" required>
                        </div>
                    </div>

                    <button type="submit" class="login-btn">
                        <span class="btn-text">Sign In</span>
                        <div class="btn-icon">→</div>
                    </button>
                </form>

                <div class="login-footer">
                    <div class="register-prompt">
                        <p>New to ProcureGov?</p>
                        <a href="${pageContext.request.contextPath}/register" class="register-link">
                            Register as Supplier
                        </a>
                    </div>
                </div>

                    </div>
                </div>
            </div>

            <!-- Footer -->
            <div class="login-footer-info">
                <p>&copy; 2026 Ministry of Public Works, Kingdom of Lesotho. All rights reserved.</p>
                <div class="footer-links">
                    <a href="#">Privacy Policy</a>
                    <a href="#">Terms of Service</a>
                    <a href="#">Support</a>
                </div>
            </div>
        </div>
    </div>

    <script>
        function toggleCredentials() {
            const content = document.getElementById('credentialsContent');
            const toggle = document.querySelector('.credentials-toggle');
            const icon = document.querySelector('.toggle-icon');

            if (content.style.display === 'none' || content.style.display === '') {
                content.style.display = 'block';
                toggle.classList.add('active');
                icon.textContent = '▲';
            } else {
                content.style.display = 'none';
                toggle.classList.remove('active');
                icon.textContent = '▼';
            }
        }
    </script>
</body>
</html>