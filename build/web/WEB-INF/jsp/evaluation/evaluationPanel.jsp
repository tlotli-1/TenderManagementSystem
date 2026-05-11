<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Evaluation Panel</title>
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
                <h2>Bid Evaluation Panel</h2>

                <c:if test="${not empty param.success}">
                    <div class="success-message">${param.success}</div>
                </c:if>
                <c:if test="${not empty param.error}">
                    <div class="error-message">${param.error}</div>
                </c:if>

                <div class="filters" style="margin-bottom:20px; display:flex; gap:12px; flex-wrap:wrap; align-items:center;">
                    <form method="get" action="${pageContext.request.contextPath}/evaluation/panel" style="display:flex; gap:12px; flex-wrap:wrap; align-items:center;">
                        <label>
                            Status:
                            <select name="status">
                                <option value="" ${statusFilter == null || statusFilter == '' ? 'selected' : ''}>All</option>
                                <option value="Closed" ${statusFilter == 'Closed' ? 'selected' : ''}>Ready for Evaluation</option>
                                <option value="Under_Evaluation" ${statusFilter == 'Under_Evaluation' ? 'selected' : ''}>Under Evaluation</option>
                            </select>
                        </label>
                        <label>
                            Category:
                            <select name="category">
                                <option value="" ${categoryFilter == null || categoryFilter == '' ? 'selected' : ''}>All</option>
                                <option value="Construction" ${categoryFilter == 'Construction' ? 'selected' : ''}>Construction</option>
                                <option value="Roads" ${categoryFilter == 'Roads' ? 'selected' : ''}>Roads</option>
                                <option value="Electrical" ${categoryFilter == 'Electrical' ? 'selected' : ''}>Electrical</option>
                                <option value="Plumbing" ${categoryFilter == 'Plumbing' ? 'selected' : ''}>Plumbing</option>
                                <option value="General Services" ${categoryFilter == 'General Services' ? 'selected' : ''}>General Services</option>
                            </select>
                        </label>
                        <button type="submit" class="btn-secondary">Filter</button>
                    </form>
                </div>

                <!-- Tenders Ready for Evaluation (Closed) -->
                <div class="evaluation-section">
                    <h3>📋 Tenders Ready for Evaluation</h3>
                    <p>Tenders that have closed and are ready for scoring.</p>

                    <c:choose>
                        <c:when test="${empty closedTenders}">
                            <div class="info-message">No tenders are currently ready for evaluation.</div>
                        </c:when>
                        <c:otherwise>
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Reference</th>
                                        <th>Title</th>
                                        <th>Category</th>
                                        <th>Bids Received</th>
                                        <th>Deadline</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${closedTenders}" var="tender">
                                        <tr>
                                            <td>${tender.referenceNumber}</td>
                                            <td>${tender.title}</td>
                                            <td>${tender.category}</td>
                                            <td><c:out value="${bidCounts[tender.tenderId] != null ? bidCounts[tender.tenderId] : 0}"/></td>
                                            <td><fmt:formatDate value="${tender.submissionDeadlineAsDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/evaluation/scoreEntry?tenderId=${tender.tenderId}" 
                                                   class="btn-small btn-primary">Start Evaluation</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Tenders Under Evaluation -->
                <div class="evaluation-section">
                    <h3>🔄 Tenders Under Evaluation</h3>
                    <p>Evaluation in progress. Continue scoring bids.</p>

                    <c:choose>
                        <c:when test="${empty underEvaluationTenders}">
                            <div class="info-message">No tenders are currently under evaluation.</div>
                        </c:when>
                        <c:otherwise>
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Reference</th>
                                        <th>Title</th>
                                        <th>Category</th>
                                        <th>Your Progress</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${underEvaluationTenders}" var="tender">
                                        <tr>
                                            <td>${tender.referenceNumber}</td>
                                            <td>${tender.title}</td>
                                            <td>${tender.category}</td>
                                            <td>
                                                <!-- Progress would be calculated -->
                                                In Progress
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/evaluation/scoreEntry?tenderId=${tender.tenderId}" 
                                                   class="btn-small">Continue Evaluation</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- For Procurement Officer: View Results -->
                <c:if test="${userRole == 'PROCUREMENT_OFFICER'}">
                    <div class="evaluation-section">
                        <h3>🏆 Completed Evaluations</h3>
                        <p>Tenders that have been fully evaluated and are ready for award.</p>

                        <c:if test="${not empty evaluatedTenders}">
                            <table class="data-table">
                                <thead>
                                    <tr><th>Reference</th><th>Title</th><th>Action</th></tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${evaluatedTenders}" var="tender">
                                        <tr>
                                            <td>${tender.referenceNumber}</td>
                                            <td>${tender.title}</td>
                                            <td><a href="${pageContext.request.contextPath}/evaluation/results?tenderId=${tender.tenderId}" class="btn-small">View Results</a></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </div>
    </body>
</html>