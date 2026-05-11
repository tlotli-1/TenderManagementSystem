<%-- 
    Document   : tenderDetail
    Created on : Apr 18, 2026, 1:57:27 PM
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Tender Details</title>
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
                <div class="breadcrumb">
                    <a href="${pageContext.request.contextPath}/supplier/dashboard">← Back to Dashboard</a>
                </div>

                <h2>Tender Details</h2>

                <!-- Tender Information -->
                <div class="tender-details">
                    <div class="detail-section">
                        <h3>Basic Information</h3>
                        <table class="detail-table">
                            <tr><th>Reference Number:</th><td><strong>${tender.referenceNumber}</strong></td></tr>
                            <tr><th>Title:</th><td>${tender.title}</td></tr>
                            <tr><th>Category:</th><td>${tender.category}</td></tr>
                            <tr><th>Status:</th>
                                <td>
                                    <span class="status-badge status-${tender.status}">${tender.status}</span>
                                </td>
                            </tr>
                        </table>
                    </div>

                    <div class="detail-section">
                        <h3>Financial & Timeline</h3>
                        <table class="detail-table">
                            <tr><th>Estimated Value:</th><td><fmt:formatNumber value="${tender.estimatedValue}" type="number" groupingUsed="true"/> Maloti</td></tr>
                            <tr><th>Submission Deadline:</th>
                                <td>
                                    <fmt:formatDate value="${tender.submissionDeadlineAsDate}" pattern="dd MMMM yyyy 'at' HH:mm"/>
                                    <c:if test="${not isOpen}">
                                        <span class="warning">(CLOSED)</span>
                                    </c:if>
                                </td>
                            </tr>
                        </table>
                    </div>

                    <div class="detail-section">
                        <h3>Description</h3>
                        <p>${tender.description}</p>
                    </div>

                    <div class="detail-section">
                        <h3>Tender Notice</h3>
                        <c:if test="${not empty tender.noticeFilePath}">
                            <a href="${pageContext.request.contextPath}/download/notice?tenderId=${tender.tenderId}" class="btn-small">📄 Download Tender Notice (PDF)</a>
                        </c:if>
                        <c:if test="${empty tender.noticeFilePath}">
                            <p><em>No notice file available</em></p>
                        </c:if>
                    </div>
                </div>

                <!-- Bid Submission Section (Only for Suppliers) -->
                <c:if test="${userRole == 'SUPPLIER'}">
                    <div class="bid-section">
                        <c:choose>
                            <c:when test="${hasSubmittedBid}">
                                <div class="info-message">
                                    ✅ You have already submitted a bid for this tender.
                                    <br>You cannot submit another bid.
                                </div>
                            </c:when>
                            <c:when test="${canSubmitBid}">
                                <div class="bid-form-container">
                                    <h3>Submit Your Bid</h3>
                                    <form method="post" action="${pageContext.request.contextPath}/supplier/submitBid" 
                                          enctype="multipart/form-data">
                                        <input type="hidden" name="tenderId" value="${tender.tenderId}">

                                        <div class="form-group">
                                            <label>Bid Amount (Maloti):*</label>
                                            <input type="number" name="bidAmount" step="0.01" required>
                                            <small>Your proposed price for completing the tender</small>
                                        </div>

                                        <div class="form-group">
                                            <label>Technical Compliance Statement:*</label>
                                            <textarea name="technicalStatement" rows="4" maxlength="600" required></textarea>
                                            <small>Maximum 600 characters. Describe how you will meet the requirements.</small>
                                        </div>

                                        <div class="form-group">
                                            <label>Proposed Delivery Timeline (Days):*</label>
                                            <input type="number" name="deliveryTimeline" min="1" required>
                                            <small>Number of days to complete the project after contract award</small>
                                        </div>

                                        <div class="form-group">
                                            <label>Supporting Document (PDF/DOCX, max 10MB):*</label>
                                            <input type="file" name="supportingDoc" accept=".pdf,.docx" required>
                                            <small>Upload your technical proposal, certifications, or other supporting documents</small>
                                        </div>

                                        <button type="submit" class="btn-primary" onclick="return confirm('Submit your bid? You cannot modify it after submission.')">
                                            Submit Bid
                                        </button>
                                    </form>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="warning-message">
                                    ⚠️ This tender is not open for bidding.
                                    <c:if test="${not isOpen}">
                                        <br>The submission deadline has passed or the tender is no longer in Open status.
                                    </c:if>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
            </div>
        </div>
    </body>
</html>