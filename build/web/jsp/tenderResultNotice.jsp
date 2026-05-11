<%-- 
    Document   : tenderResultNotice
    Created on : Apr 27, 2026
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>ProcureGov - Tender Result Notice</title>
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
            <h2>Tender Result Notice</h2>
            
            <div class="detail-section">
                <h3>Tender Information</h3>
                <table class="detail-table">
                    <tr><th>Reference Number:</th><td><strong>${tender.referenceNumber}</strong></td></tr>
                    <tr><th>Title:</th><td>${tender.title}</td></tr>
                    <tr><th>Category:</th><td>${tender.category}</td></tr>
                    <tr><th>Status:</th>
                        <td>
                            <span class="status-badge status-${tender.status}">${tender.status}</span>
                        </td>
                    </tr>
                    <tr><th>Estimated Value:</th><td><fmt:formatNumber value="${tender.estimatedValue}" type="number" groupingUsed="true"/> Maloti</td></tr>
                    <tr><th>Submission Deadline:</th>
                        <td>
                            <fmt:formatDate value="${tender.submissionDeadlineAsDate}" pattern="dd MMMM yyyy 'at' HH:mm"/>
                        </td>
                    </tr>
                </table>
            </div>
            
            <div class="detail-section">
                <h3>Bid Evaluation Results</h3>
                <p style="color: #666; font-style: italic; margin-bottom: 15px;">
                    Below is the ranking of all bids submitted for this tender, based on evaluation scores.
                </p>
                
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>Supplier</th>
                            <th>Bid Amount (M)</th>
                            <th>Delivery Timeline (Days)</th>
                            <th>Price Score</th>
                            <th>Technical Score</th>
                            <th>Delivery Score</th>
                            <th>Final Score</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${rankedBids}" var="bid" varStatus="status">
                            <c:choose>
                                <c:when test="${status.index == 0}">
                                    <tr style="background: linear-gradient(135deg, #fff3cd 0%, #ffe6b3 100%); border: 2px solid #ffc107;">
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                </c:otherwise>
                            </c:choose>
                                <td><strong>${status.index + 1}</strong></td>
                                <td>${bid.supplierCompany}</td>
                                <td><fmt:formatNumber value="${bid.bidAmount}" type="number" groupingUsed="true"/></td>
                                <td>${bid.deliveryTimeline}</td>
                                <td><strong><fmt:formatNumber value="${bid.priceScore}" type="number" maxFractionDigits="2"/></strong></td>
                                <td><strong><fmt:formatNumber value="${bid.technicalScore}" type="number" maxFractionDigits="2"/></strong></td>
                                <td><strong><fmt:formatNumber value="${bid.deliveryScore}" type="number" maxFractionDigits="2"/></strong></td>
                                <td>
                                    <strong style="font-size: 16px; color: #000;">
                                        <fmt:formatNumber value="${bid.finalWeightedScore}" type="number" maxFractionDigits="2"/>
                                    </strong>
                                    <c:if test="${status.index == 0}">
                                        <span style="color: #856404; font-weight: bold; display: block; font-size: 11px;">WINNER</span>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            
            <div class="detail-section">
                <h3>Scoring Formula</h3>
                <div class="formula-info">
                    <p><strong>Final Score = (Price Score × 40%) + (Technical Score × 35%) + (Delivery Score × 25%)</strong></p>
                </div>
            </div>
            
            <div class="detail-section">
                <h3>Notice Distribution</h3>
                <p style="color: #666; margin-bottom: 15px;">
                    Click the button below to send result notices to all suppliers who submitted bids.
                    Notices will include ranking information and evaluation feedback.
                </p>
                
                <form method="post" action="${pageContext.request.contextPath}/officer/resultNotice" style="display: inline;">
                    <input type="hidden" name="tenderId" value="${tender.tenderId}">
                    <input type="hidden" name="action" value="sendNotices">
                    <button type="submit" class="btn-primary" onclick="return confirm('Send result notices to all ${fn:length(rankedBids)} bidding suppliers?')">
                        📧 Send Result Notices
                    </button>
                </form>
                
                <a href="${pageContext.request.contextPath}/officer/tenderList" class="btn-secondary">
                    ← Back to Tender List
                </a>
            </div>
        </div>
    </div>
</body>
</html>
