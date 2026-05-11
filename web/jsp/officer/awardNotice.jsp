<%-- 
    Document   : awardNotice
    Created on : Apr 18, 2026, 1:59:01 PM
    Author     : legacy
--%>


<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Award Notice</title>
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
                <div class="award-notice">
                    <!-- Ministry Header -->
                    <div class="ministry-header-award">
                        <h1>Ministry of Public Works</h1>
                        <h2>Kingdom of Lesotho</h2>
                        <h3>OFFICIAL TENDER AWARD NOTICE</h3>
                    </div>

                    <!-- Winner Badge -->
                    <c:if test="${isWinner}">
                        <div class="winner-badge">
                            🏆 CONGRATULATIONS! YOUR BID HAS BEEN SELECTED 🏆
                        </div>
                    </c:if>

                    <!-- Tender Information -->
                    <div class="notice-section">
                        <h4>Tender Information</h4>
                        <table class="notice-table">
                            <tr>
                                <th>Tender Reference:</th>
                                <td>${tender.referenceNumber}</td>
                            </tr>
                            <tr>
                                <th>Tender Title:</th>
                                <td>${tender.title}</td>
                            </tr>
                            <tr>
                                <th>Category:</th>
                                <td>${tender.category}</td>
                            </tr>
                            <tr>
                                <th>Description:</th>
                                <td>${tender.description}</td>
                            </tr>
                        </table>
                    </div>

                    <!-- Award Information -->
                    <div class="notice-section">
                        <h4>Award Information</h4>
                        <table class="notice-table">
                            <tr>
                                <th>Winning Supplier:</th>
                                <td><strong>${award.winningSupplierCompany}</strong></td>
                            </tr>
                            <tr>
                                <th>Registration Number:</th>
                                <td>${award.winningSupplierRegNumber}</td>
                            </tr>
                            <tr>
                                <th>Awarded Value:</th>
                                <td><strong><fmt:formatNumber value="${award.awardedValue}" type="number" groupingUsed="true"/> Maloti</strong></td>
                            </tr>
                            <tr>
                                <th>Award Date:</th>
                                <td><fmt:formatDate value="${award.awardDate}" pattern="dd MMMM yyyy"/></td>
                            </tr>
                            <tr>
                                <th>Justification:</th>
                                <td>${award.justification}</td>
                            </tr>
                        </table>
                    </div>

                    <!-- Outcome for Non-Winning Bidders -->
                    <c:if test="${isBidder and not isWinner}">
                        <div class="notice-section loser-message">
                            <h4>Thank You for Your Participation</h4>
                            <p>While your bid was not selected for this tender, the Ministry appreciates your interest and submission.</p>
                            <p>Please continue to monitor ProcureGov for future tender opportunities.</p>
                        </div>
                    </c:if>

                    <!-- Footer -->
                    <div class="notice-footer">
                        <p>This award notice is issued by the Ministry of Public Works, Kingdom of Lesotho.</p>
                        <p>For any inquiries, please contact the Directorate of ICT at procuregov@ministry.gov.ls</p>
                        <p><small>Generated on <fmt:formatDate value="<%= new java.util.Date()%>" pattern="dd MMMM yyyy HH:mm:ss"/></small></p>
                    </div>

                    <div class="notice-actions">
                        <a href="javascript:window.print()" class="btn-primary">Print Notice</a>
                        <a href="${pageContext.request.contextPath}/supplier/dashboard" class="btn-secondary">Back to Dashboard</a>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>          