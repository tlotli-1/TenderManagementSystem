<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>ProcureGov - Supplier Registration</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="register-container">
        <div class="register-box">
            <div class="ministry-header">
                <h1>ProcureGov</h1>
                <p>Supplier Registration</p>
                <p>Ministry of Public Works, Kingdom of Lesotho</p>
            </div>
            
            <!-- Error and Success Messages -->
            <c:if test="${not empty error}">
                <div class="error-message">${error}</div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="success-message">${success}</div>
            </c:if>
            <c:if test="${not empty registrationNumber}">
                <div class="success-message">
                    Registration successful! Your registration number is: <strong>${registrationNumber}</strong>
                    <br>Please login using your username and password.
                </div>
            </c:if>
            
            <!-- Show form only if registration was NOT successful -->
            <c:if test="${empty registrationNumber}">
                <form method="post" action="${pageContext.request.contextPath}/register">
                    <fieldset>
                        <legend>Account Information</legend>
                        
                        <div class="form-group">
                            <label>Username:*</label>
                            <input type="text" name="username" value="${form.username}" required>
                        </div>
                        
                        <div class="form-group">
                            <label>Email:*</label>
                            <input type="email" name="email" value="${form.email}" required>
                        </div>
                        
                        <div class="form-group">
                            <label>Password:*</label>
                            <input type="password" name="password" required minlength="6">
                            <small>Minimum 6 characters</small>
                        </div>
                        
                        <div class="form-group">
                            <label>Confirm Password:*</label>
                            <input type="password" name="confirmPassword" required minlength="6">
                        </div>
                    </fieldset>
                    
                    <fieldset>
                        <legend>Company Information</legend>
                        
                        <div class="form-group">
                            <label>Full Name (Contact Person):*</label>
                            <input type="text" name="fullName" value="${form.fullName}" required>
                        </div>
                        
                        <div class="form-group">
                            <label>Company Name:*</label>
                            <input type="text" name="companyName" value="${form.companyName}" required>
                        </div>
                        
                        <div class="form-group">
                            <label>Contact Number:*</label>
                            <input type="tel" name="contactNumber" value="${form.contactNumber}" required>
                        </div>
                        
                        <div class="form-group">
                            <label>Physical Address:*</label>
                            <textarea name="physicalAddress" rows="3" required>${form.physicalAddress}</textarea>
                        </div>
                    </fieldset>
                    
                    <button type="submit" class="register-btn">Register</button>
                    <a href="${pageContext.request.contextPath}/login" class="cancel-btn">Cancel</a>
                </form>
            </c:if>
            
            <div class="login-link">
                <p>Already registered? <a href="${pageContext.request.contextPath}/login">Login here</a></p>
            </div>
        </div>
    </div>
</body>
</html>