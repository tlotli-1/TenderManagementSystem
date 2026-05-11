5
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Supplier Dashboard</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    </head>
    <body>
        <div class="container">
            <div class="navbar">
                <div class="navbar-brand">ProcureGov</div>
                <div class="navbar-user">
                    <span>Welcome, ${sessionScope.user.fullName}</span>
                    <span>Company: ${supplier.companyName}</span>
                    <span>Reg No: ${supplier.registrationNumber}</span>
                    <a href="${pageContext.request.contextPath}/logout">Logout</a>
                </div>
            </div>

            <div class="content">
                <!-- Success/Error Messages -->
                <c:if test="${not empty param.success}">
                    <div class="success-message">${param.success}</div>
                </c:if>
                <c:if test="${not empty param.error}">
                    <div class="error-message">${param.error}</div>
                </c:if>

                <!-- Section 1: Open Tenders -->
                <div class="dashboard-section">
                    <h2>📢 Open Tenders</h2>
                    <p>Tenders currently accepting bids. Click "View Details" to submit your bid.</p>

                    <c:choose>
                        <c:when test="${empty openTenders}">
                            <div class="info-message">No open tenders available at this time.</div>
                        </c:when>
                        <c:otherwise>
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Reference</th>
                                        <th>Title</th>
                                        <th>Category</th>
                                        <th>Estimated Value (M)</th>
                                        <th>Deadline</th>
                                        <th>Notice</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${openTenders}" var="tender">
                                        <tr>
                                            <td>${tender.referenceNumber}</td>
                                            <td>${tender.title}</td>
                                            <td>${tender.category}</td>
                                            <td><fmt:formatNumber value="${tender.estimatedValue}" type="number" groupingUsed="true"/></td>
                                            <td><fmt:formatDate value="${tender.submissionDeadlineAsDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                            <td>
                                                <c:if test="${not empty tender.noticeFilePath}">
                                                    <a href="${pageContext.request.contextPath}/download/notice?tenderId=${tender.tenderId}" class="btn-small">📄 Download PDF</a>
                                                </c:if>
                                                <c:if test="${empty tender.noticeFilePath}">
                                                    <span style="color: #999;">-</span>
                                                </c:if>
                                            </td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/tender/details?tenderId=${tender.tenderId}" class="btn-small">View Details</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>

                <!-- Section 2: My Submitted Bids -->
                <div class="dashboard-section">
                    <h2>📋 My Submitted Bids</h2>
                    <p>Track the status of your bids.</p>

                    <c:choose>
                        <c:when test="${empty myBids}">
                            <div class="info-message">You haven't submitted any bids yet.</div>
                        </c:when>
                        <c:otherwise>
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Tender Reference</th>
                                        <th>Title</th>
                                        <th>Bid Amount (M)</th>
                                        <th>Delivery Timeline</th>
                                        <th>Submission Date</th>
                                        <th>Tender Status</th>
                                        <th>Award Outcome</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${myBids}" var="bid">
                                        <tr>
                                            <td>${bid.tenderReference}</td>
                                            <td>${bid.tenderTitle}</td>
                                            <td><fmt:formatNumber value="${bid.bidAmount}" type="number" groupingUsed="true"/></td>
                                            <td>${bid.deliveryTimeline} days</td>
                                            <td><fmt:formatDate value="${bid.submissionDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                            <td>
                                                <span class="status-badge status-${bid.tenderStatus}">
                                                    ${bid.tenderStatus}
                                                </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${bid.awardOutcome == 'WON'}">
                                                        <span class="winner-badge-small">🏆 WON</span>
                                                    </c:when>
                                                    <c:when test="${bid.awardOutcome == 'NOT_WON'}">
                                                        <span class="loser-badge-small">Not Won</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="pending-badge">Pending</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:if test="${bid.tenderStatus == 'Awarded'}">
                                                    <a href="${pageContext.request.contextPath}/tender/awardNotice?tenderId=${bid.tenderId}" class="btn-small">View Award Notice</a>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </body>
</html>