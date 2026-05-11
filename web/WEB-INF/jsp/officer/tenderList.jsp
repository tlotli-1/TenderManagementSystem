<%-- 
    Document   : tenderList
    Created on : Apr 18, 2026, 1:57:51 PM
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>ProcureGov - Tender Management</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <div class="navbar">
            <div class="navbar-brand">ProcureGov</div>
            <div class="navbar-user">
                <span>Welcome, ${sessionScope.user.fullName}</span>
                <span>Role: Procurement Officer</span>
                <a href="${pageContext.request.contextPath}/logout">Logout</a>
            </div>
        </div>
        
        <div class="content">
            <h2>Tender Management</h2>
            
            <c:if test="${not empty param.success}">
                <div class="success-message">${param.success}</div>
            </c:if>
            
            <!-- Stats Dashboard -->
            <div class="stats-row">
                <div class="stat-card">Draft: ${draftCount}</div>
                <div class="stat-card">Open: ${openCount}</div>
                <div class="stat-card">Closed: ${closedCount}</div>
                <div class="stat-card">Under Evaluation: ${underEvaluationCount}</div>
                <div class="stat-card">Evaluated: ${evaluatedCount}</div>
                <div class="stat-card">Awarded: ${awardedCount}</div>
            </div>
            
            <!-- Actions -->
            <div class="actions">
                <a href="${pageContext.request.contextPath}/createTender" class="btn-primary">+ Create New Tender</a>
            </div>
            
            <!-- Filters -->
            <div class="filters">
                <form method="get" action="${pageContext.request.contextPath}/tenderList">
                    <label>Status:</label>
                    <select name="status">
                        <option value="">All</option>
                        <option value="Draft" ${statusFilter == 'Draft' ? 'selected' : ''}>Draft</option>
                        <option value="Open" ${statusFilter == 'Open' ? 'selected' : ''}>Open</option>
                        <option value="Closed" ${statusFilter == 'Closed' ? 'selected' : ''}>Closed</option>
                        <option value="Under_Evaluation" ${statusFilter == 'Under_Evaluation' ? 'selected' : ''}>Under Evaluation</option>
                        <option value="Evaluated" ${statusFilter == 'Evaluated' ? 'selected' : ''}>Evaluated</option>
                        <option value="Awarded" ${statusFilter == 'Awarded' ? 'selected' : ''}>Awarded</option>
                    </select>
                    
                    <label>Category:</label>
                    <select name="category">
                        <option value="">All</option>
                        <option value="Construction" ${categoryFilter == 'Construction' ? 'selected' : ''}>Construction</option>
                        <option value="Roads" ${categoryFilter == 'Roads' ? 'selected' : ''}>Roads</option>
                        <option value="Electrical" ${categoryFilter == 'Electrical' ? 'selected' : ''}>Electrical</option>
                        <option value="Plumbing" ${categoryFilter == 'Plumbing' ? 'selected' : ''}>Plumbing</option>
                        <option value="General Services" ${categoryFilter == 'General Services' ? 'selected' : ''}>General Services</option>
                    </select>
                    
                    <button type="submit" class="btn-secondary">Filter</button>
                </form>
            </div>
            
            <!-- Tender Table -->
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Ref No.</th>
                        <th>Title</th>
                        <th>Category</th>
                        <th>Estimated Value (M)</th>
                        <th>Deadline</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${tenders}" var="tender">
                        <tr>
                            <td>${tender.referenceNumber}</td>
                            <td>${tender.title}</td>
                            <td>${tender.category}</td>
                            <td><fmt:formatNumber value="${tender.estimatedValue}" type="number" groupingUsed="true"/></td>
                            <td><fmt:formatDate value="${tender.submissionDeadline}" pattern="dd/MM/yyyy HH:mm"/></td>
                            <td>
                                <span class="status-badge status-${tender.status}">
                                    ${tender.status}
                                </span>
                            </td>
                            <td class="actions-cell">
                                <a href="${pageContext.request.contextPath}/details?tenderId=${tender.tenderId}" class="btn-small">View</a>
                                
                                <c:if test="${tender.status == 'Draft'}">
                                    <a href="${pageContext.request.contextPath}/editTender?tenderId=${tender.tenderId}" class="btn-small btn-edit">Edit</a>
                                    <a href="${pageContext.request.contextPath}/updateStatus?tenderId=${tender.tenderId}&status=Open" 
                                       class="btn-small btn-publish" onclick="return confirm('Publish this tender? Suppliers will be able to bid.')">Publish</a>
                                </c:if>
                                
                                <c:if test="${tender.status == 'Open'}">
                                    <a href="${pageContext.request.contextPath}/updateStatus?tenderId=${tender.tenderId}&status=Closed" 
                                       class="btn-small btn-close" onclick="return confirm('Close this tender? No more bids will be accepted.')">Force Close</a>
                                </c:if>
                                
                                <c:if test="${tender.status == 'Closed'}">
                                    <a href="${pageContext.request.contextPath}updateStatus?tenderId=${tender.tenderId}&status=Under_Evaluation" 
                                       class="btn-small btn-evaluate">Start Evaluation</a>
                                </c:if>
                                
                                <c:if test="${tender.status == 'Evaluated'}">
                                    <a href="${pageContext.request.contextPath}/results?tenderId=${tender.tenderId}" class="btn-small btn-results">View Results</a>
                                    <a href="${pageContext.request.contextPath}/awardContract?tenderId=${tender.tenderId}" class="btn-small btn-award">Award Contract</a>
                                </c:if>
                                
                                <c:if test="${tender.status == 'Awarded'}">
                                    <a href="${pageContext.request.contextPath}/tender/awardNotice?tenderId=${tender.tenderId}" class="btn-small btn-view">View Award</a>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    
                    <c:if test="${empty tenders}">
                        <tr>
                            <td colspan="7" style="text-align: center;">No tenders found</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>