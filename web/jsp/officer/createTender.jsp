<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>ProcureGov - Create Tender</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <div class="navbar">
            <div class="navbar-brand">ProcureGov</div>
            <div class="navbar-user">
                <span>Welcome, ${sessionScope.user.fullName}</span>
                <span>Role: ${sessionScope.user.role}</span>
                <a href="${pageContext.request.contextPath}/logout">Logout</a>
            </div>
        </div>
        
        <div class="content">
            <h2>Create New Tender</h2>
            
            <c:if test="${not empty errors}">
                <div class="error-message">${errors}</div>
            </c:if>
            
            <form method="post" action="${pageContext.request.contextPath}/officer/createTender" 
                  enctype="multipart/form-data">
                <fieldset>
                    <legend>Tender Information</legend>
                    
                    <div class="form-group">
                        <label>Tender Title:*</label>
                        <input type="text" name="title" value="${title}" required>
                    </div>
                    
                    <div class="form-group">
                        <label>Category:*</label>
                        <select name="category" required>
                            <option value="">Select Category</option>
                            <option value="Construction" ${category == 'Construction' ? 'selected' : ''}>Construction</option>
                            <option value="Roads" ${category == 'Roads' ? 'selected' : ''}>Roads</option>
                            <option value="Electrical" ${category == 'Electrical' ? 'selected' : ''}>Electrical</option>
                            <option value="Plumbing" ${category == 'Plumbing' ? 'selected' : ''}>Plumbing</option>
                            <option value="General Services" ${category == 'General Services' ? 'selected' : ''}>General Services</option>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label>Description:*</label>
                        <textarea name="description" rows="5" required>${description}</textarea>
                    </div>
                    
                    <div class="form-group">
                        <label>Estimated Value (Maloti):*</label>
                        <input type="number" name="estimatedValue" step="0.01" value="${estimatedValue}" required>
                    </div>
                    
                    <div class="form-group">
                        <label>Submission Deadline:*</label>
                        <input type="datetime-local" name="submissionDeadline" value="${submissionDeadline}" required>
                    </div>
                    
                    <div class="form-group">
                        <label>Tender Notice (PDF, max 5MB):*</label>
                        <input type="file" name="noticeFile" accept=".pdf" required>
                    </div>
                </fieldset>
                
                <button type="submit" class="btn-primary">Create Tender</button>
                <a href="${pageContext.request.contextPath}/officer/tenderList" class="btn-secondary">Cancel</a>
            </form>
        </div>
    </div>
</body>
</html>