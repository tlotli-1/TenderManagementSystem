<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="util.SessionValidator" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>ProcureGov - Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="login-container">
        <div class="login-box">
            <div class="ministry-header">
                <h1>ProcureGov</h1>
                <p>Ministry of Public Works</p>
                <p>Kingdom of Lesotho</p>
            </div>
            
            <c:if test="${not empty param.loggedout}">
                <div class="success-message">Logged out successfully.</div>
            </c:if>
            
            <c:if test="${not empty param.error}">
                <div class="error-message">${param.error}</div>
            </c:if>
            
            <c:if test="${not empty error}">
                <div class="error-message">${error}</div>
            </c:if>
            
            <form method="post" action="${pageContext.request.contextPath}/login">
                <div class="form-group">
                    <label>Username:</label>
                    <input type="text" name="username" required>
                </div>
                
                <div class="form-group">
                    <label>Password:</label>
                    <input type="password" name="password" required>
                </div>
                
                <button type="submit" class="login-btn">Login</button>
            </form>
            
            <div class="register-link">
                <p><a href="${pageContext.request.contextPath}/register">Register as Supplier</a></p>
            </div>
            
            <div class="test-credentials">
                <h3>Test Credentials:</h3>
                <p><strong>Procurement Officer:</strong> peter.mokhosi / password123</p>
                <p><strong>Evaluation Committee:</strong> thabo.masilo / password123</p>
                <p><strong>Supplier:</strong> john.supplier / password123</p>
            </div>
        </div>
    </div>
</body>
</html>