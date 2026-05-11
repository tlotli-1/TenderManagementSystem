<%-- 
    Document   : rankedLeaderboard
    Created on : Apr 18, 2026, 2:00:13 PM
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Evaluation Results</title>
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
                <h2>Evaluation Results</h2>
                <h3>${tender.referenceNumber} - ${tender.title}</h3>

                <div class="leaderboard">
                    <h3>🏆 Ranked Leaderboard (by Final Score)</h3>

                    <c:choose>
                        <c:when test="${empty rankedBids}">
                            <div class="info-message">No bids found for this tender.</div>
                        </c:when>
                        <c:otherwise>
                            <table class="leaderboard-table">
                                <thead>
                                    <tr>
                                        <th>Rank</th>
                                        <th>Supplier</th>
                                        <th>Bid Amount (M)</th>
                                        <th>Delivery (Days)</th>
                                        <th>Price Score</th>
                                        <th>Tech Score</th>
                                        <th>Delivery Score</th>
                                        <th>FINAL SCORE</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${rankedBids}" var="bid" varStatus="status">
                                        <tr class="${status.index == 0 ? 'winner-row' : ''}">
                                            <td class="rank-cell">
                                                <c:choose>
                                                    <c:when test="${status.index == 0}">🥇 1st</c:when>
                                                    <c:when test="${status.index == 1}">🥈 2nd</c:when>
                                                    <c:when test="${status.index == 2}">🥉 3rd</c:when>
                                                    <c:otherwise>${status.index + 1}th</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><strong>${bid.supplierCompany}</strong></td>
                                            <td><fmt:formatNumber value="${bid.bidAmount}" type="number" groupingUsed="true"/></td>
                                            <td>${bid.deliveryTimeline}</td>
                                            <td><fmt:formatNumber value="${bid.priceScore}" maxFractionDigits="2"/></td>
                                            <td><fmt:formatNumber value="${bid.technicalScore}" maxFractionDigits="2"/></td>
                                            <td><fmt:formatNumber value="${bid.deliveryScore}" maxFractionDigits="2"/></td>
                                            <td class="score-cell">
                                                <strong><fmt:formatNumber value="${bid.finalWeightedScore}" maxFractionDigits="2"/></strong>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>

                            <div class="formula-info">
                                <h4>Scoring Formula:</h4>
                                <p>Final Score = (Price Score × 0.40) + (Technical Score × 0.35) + (Delivery Score × 0.25)</p>
                                <p><em>Price Score = (Lowest Bid / This Bid) × 100 | Delivery Score = (Shortest Timeline / This Timeline) × 100</em></p>
                            </div>

                            <div class="action-buttons">
                                <a href="${pageContext.request.contextPath}/officer/awardContract?tenderId=${tender.tenderId}" 
                                   class="btn-primary">Proceed to Award Contract</a>
                                <a href="${pageContext.request.contextPath}/officer/tenderList" class="btn-secondary">Back to Tenders</a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </body>
</html>