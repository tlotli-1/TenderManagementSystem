<%--
    Document   : tenderList
    Module 2   : Tender Management - Role-aware list view
    Roles      : PROCUREMENT_OFFICER, SUPPLIER, EVALUATION_COMMITTEE
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
            <span>Role: ${sessionScope.user.role}</span>
            <a href="${pageContext.request.contextPath}/logout">Logout</a>
        </div>
    </div>

    <div class="content">
        <h2>
            <c:choose>
                <c:when test="${role == 'SUPPLIER'}">Available Tenders</c:when>
                <c:when test="${role == 'EVALUATION_COMMITTEE'}">Tenders for Evaluation</c:when>
                <c:otherwise>Tender Management</c:otherwise>
            </c:choose>
        </h2>

        <c:if test="${not empty param.success}">
            <div class="success-message">${param.success}</div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="error-message">${param.error}</div>
        </c:if>

        <c:if test="${role == 'PROCUREMENT_OFFICER'}">
            <div class="stats-row">
                <div class="stat-card">Draft: ${draftCount}</div>
                <div class="stat-card">Open: ${openCount}</div>
                <div class="stat-card">Closed: ${closedCount}</div>
                <div class="stat-card">Under Evaluation: ${underEvaluationCount}</div>
                <div class="stat-card">Evaluated: ${evaluatedCount}</div>
                <div class="stat-card">Awarded: ${awardedCount}</div>
            </div>
            <div class="actions">
                <a href="${pageContext.request.contextPath}/officer/createTender" class="btn-primary">+ Create New Tender</a>
            </div>
        </c:if>

        <div class="filters">
            <form method="get" action="${pageContext.request.contextPath}/${role == 'PROCUREMENT_OFFICER' ? 'officer/tenderList' : role == 'SUPPLIER' ? 'supplier/tenders' : 'evaluation/tenders'}">
                <c:if test="${role == 'PROCUREMENT_OFFICER'}">
                    <label>Status:</label>
                    <select name="status">
                        <option value="">All</option>
                        <option value="Draft"            ${statusFilter == 'Draft'            ? 'selected' : ''}>Draft</option>
                        <option value="Open"             ${statusFilter == 'Open'             ? 'selected' : ''}>Open</option>
                        <option value="Closed"           ${statusFilter == 'Closed'           ? 'selected' : ''}>Closed</option>
                        <option value="Under_Evaluation" ${statusFilter == 'Under_Evaluation' ? 'selected' : ''}>Under Evaluation</option>
                        <option value="Evaluated"        ${statusFilter == 'Evaluated'        ? 'selected' : ''}>Evaluated</option>
                        <option value="Awarded"          ${statusFilter == 'Awarded'          ? 'selected' : ''}>Awarded</option>
                    </select>
                </c:if>
                <label>Category:</label>
                <select name="category">
                    <option value="">All Categories</option>
                    <option value="Construction"     ${categoryFilter == 'Construction'     ? 'selected' : ''}>Construction</option>
                    <option value="Roads"            ${categoryFilter == 'Roads'            ? 'selected' : ''}>Roads</option>
                    <option value="Electrical"       ${categoryFilter == 'Electrical'       ? 'selected' : ''}>Electrical</option>
                    <option value="Plumbing"         ${categoryFilter == 'Plumbing'         ? 'selected' : ''}>Plumbing</option>
                    <option value="General Services" ${categoryFilter == 'General Services' ? 'selected' : ''}>General Services</option>
                </select>
                <button type="submit" class="btn-secondary">Filter</button>
                <a href="${pageContext.request.contextPath}/${role == 'PROCUREMENT_OFFICER' ? 'officer/tenderList' : role == 'SUPPLIER' ? 'supplier/tenders' : 'evaluation/tenders'}" class="btn-secondary">Clear</a>
            </form>
        </div>

        <table class="data-table">
            <thead>
                <tr>
                    <th>Ref No.</th>
                    <th>Title</th>
                    <th>Category</th>
                    <th>Estimated Value (M)</th>
                    <th>Deadline</th>
                    <th>Status</th>
                    <c:if test="${role == 'PROCUREMENT_OFFICER'}"><th>Notice</th></c:if>
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
                        <td><fmt:formatDate value="${tender.submissionDeadlineAsDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                        <td><span class="status-badge status-${tender.status}">${tender.status}</span></td>
                        <c:if test="${role == 'PROCUREMENT_OFFICER'}">
                            <td>
                                <c:choose>
                                    <c:when test="${not empty tender.noticeFilePath}">
                                        <a href="${pageContext.request.contextPath}/download/notice?file=${tender.noticeFilePath}" class="btn-small">PDF</a>
                                    </c:when>
                                    <c:otherwise><span style="color:#999;">-</span></c:otherwise>
                                </c:choose>
                            </td>
                        </c:if>
                        <td class="actions-cell">
                            <a href="${pageContext.request.contextPath}/tender/details?tenderId=${tender.tenderId}" class="btn-small">View</a>
                            <c:if test="${role == 'PROCUREMENT_OFFICER'}">
                                <c:if test="${tender.status == 'Draft'}">
                                    <a href="${pageContext.request.contextPath}/officer/editTender?tenderId=${tender.tenderId}" class="btn-small btn-edit">Edit</a>
                                    <a href="${pageContext.request.contextPath}/officer/updateStatus?tenderId=${tender.tenderId}&status=Open"
                                       class="btn-small btn-publish" onclick="return confirm('Publish this tender to suppliers?')">Publish</a>
                                </c:if>
                                <c:if test="${tender.status == 'Open'}">
                                    <a href="${pageContext.request.contextPath}/officer/updateStatus?tenderId=${tender.tenderId}&status=Closed"
                                       class="btn-small btn-close" onclick="return confirm('Force close this tender? No more bids will be accepted.')">Force Close</a>
                                </c:if>
                                <c:if test="${tender.status == 'Closed'}">
                                    <a href="${pageContext.request.contextPath}/officer/updateStatus?tenderId=${tender.tenderId}&status=Under_Evaluation"
                                       class="btn-small btn-evaluate">Start Evaluation</a>
                                </c:if>
                                <c:if test="${tender.status == 'Evaluated'}">
                                    <a href="${pageContext.request.contextPath}/evaluation/results?tenderId=${tender.tenderId}" class="btn-small btn-results">View Results</a>
                                    <a href="${pageContext.request.contextPath}/officer/awardContract?tenderId=${tender.tenderId}" class="btn-small btn-award">Award Contract</a>
                                    <a href="${pageContext.request.contextPath}/officer/resultNotice?tenderId=${tender.tenderId}" class="btn-small btn-notice">Result Notice</a>
                                </c:if>
                                <c:if test="${tender.status == 'Awarded'}">
                                    <a href="${pageContext.request.contextPath}/tender/awardNotice?tenderId=${tender.tenderId}" class="btn-small btn-view">View Award</a>
                                    <a href="${pageContext.request.contextPath}/officer/resultNotice?tenderId=${tender.tenderId}" class="btn-small btn-notice">Result Notice</a>
                                </c:if>
                            </c:if>
                            <c:if test="${role == 'EVALUATION_COMMITTEE'}">
                                <c:if test="${tender.status == 'Closed' || tender.status == 'Under_Evaluation'}">
                                    <a href="${pageContext.request.contextPath}/evaluation/scoreEntry?tenderId=${tender.tenderId}" class="btn-small btn-evaluate">Score Bids</a>
                                </c:if>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty tenders}">
                    <tr>
                        <td colspan="8" style="text-align:center; padding:30px; color:#666;">
                            <c:choose>
                                <c:when test="${role == 'SUPPLIER'}">No open tenders available at this time.</c:when>
                                <c:when test="${role == 'EVALUATION_COMMITTEE'}">No tenders awaiting evaluation.</c:when>
                                <c:otherwise>No tenders found. <a href="${pageContext.request.contextPath}/officer/createTender">Create one?</a></c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
