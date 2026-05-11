<%--
    Document   : awardContract
    Module 2   : Tender Management - Award Contract
    Role       : PROCUREMENT_OFFICER only
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>ProcureGov - Award Contract</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .award-section { background: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin-bottom: 20px; }
        .bid-option { border: 2px solid #ddd; border-radius: 8px; padding: 15px; margin-bottom: 15px; cursor: pointer; transition: all 0.2s; }
        .bid-option:hover { border-color: #007bff; background: #f0f7ff; }
        .bid-option.selected { border-color: #28a745; background: #f0fff4; }
        .bid-option input[type=radio] { margin-right: 10px; }
        .rank-1 { border-left: 5px solid #ffc107; }
        .rank-2 { border-left: 5px solid #adb5bd; }
        .rank-3 { border-left: 5px solid #cd7f32; }
        .winner-indicator { color: #28a745; font-weight: bold; font-size: 18px; float: right; }
        .score-pill { display: inline-block; background: #007bff; color: white; padding: 3px 10px; border-radius: 12px; font-size: 13px; font-weight: bold; }
        .award-form-section { background: #f8f9fa; border: 1px solid #dee2e6; border-radius: 8px; padding: 20px; margin-top: 20px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; font-weight: 600; margin-bottom: 5px; }
        .form-group input, .form-group textarea { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px; font-size: 14px; }
        .form-group textarea { height: 120px; resize: vertical; }
        .formula-info { background: #e9ecef; border-radius: 5px; padding: 12px; margin-top: 10px; font-size: 13px; color: #555; }
        .btn-award-submit { background: #28a745; color: white; padding: 12px 30px; border: none; border-radius: 5px; font-size: 16px; cursor: pointer; font-weight: bold; }
        .btn-award-submit:hover { background: #218838; }
        .char-counter { font-size: 12px; color: #666; text-align: right; }
    </style>
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
        <h2>Award Contract</h2>

        <c:if test="${not empty errors}">
            <div class="error-message">${errors}</div>
        </c:if>

        <!-- Tender Summary -->
        <div class="award-section">
            <h3>Tender Details</h3>
            <table class="detail-table">
                <tr><th>Reference:</th><td><strong>${tender.referenceNumber}</strong></td></tr>
                <tr><th>Title:</th><td>${tender.title}</td></tr>
                <tr><th>Category:</th><td>${tender.category}</td></tr>
                <tr><th>Estimated Value:</th><td><fmt:formatNumber value="${tender.estimatedValue}" type="number" groupingUsed="true"/> Maloti</td></tr>
            </table>
        </div>

        <!-- Ranked Bids -->
        <div class="award-section">
            <h3>🏆 Ranked Bids — Select the Winning Supplier</h3>
            <p style="color:#666; margin-bottom:15px;">Bids are ranked by final evaluation score. The highest-ranked bid is recommended. You must select a bid and provide a written justification.</p>

            <div class="formula-info">
                <strong>Scoring Formula:</strong> Final Score = (Price Score × 40%) + (Technical Score × 35%) + (Delivery Score × 25%)
            </div>

            <form method="post" action="${pageContext.request.contextPath}/officer/awardContract" id="awardForm">
                <input type="hidden" name="tenderId" value="${tender.tenderId}">

                <div style="margin-top: 20px;">
                    <c:forEach items="${rankedBids}" var="bid" varStatus="loop">
                        <div class="bid-option rank-${loop.index + 1} ${selectedBidId == bid.bidId ? 'selected' : ''}"
                             onclick="selectBid(${bid.bidId}, ${bid.bidAmount})">
                            <label style="cursor:pointer; display:block;">
                                <input type="radio" name="winningBidId" value="${bid.bidId}"
                                       ${selectedBidId == bid.bidId ? 'checked' : ''}
                                       onchange="selectBid(${bid.bidId}, ${bid.bidAmount})" required>

                                <strong>
                                    <c:choose>
                                        <c:when test="${loop.index == 0}">🥇 Rank 1 (Recommended)</c:when>
                                        <c:when test="${loop.index == 1}">🥈 Rank 2</c:when>
                                        <c:when test="${loop.index == 2}">🥉 Rank 3</c:when>
                                        <c:otherwise>Rank ${loop.index + 1}</c:otherwise>
                                    </c:choose>
                                </strong>
                                — ${bid.supplierCompany}

                                <span class="winner-indicator">
                                    <c:if test="${loop.index == 0}">⭐ Top Ranked</c:if>
                                </span>
                            </label>

                            <table style="width:100%; margin-top:10px; font-size:14px;">
                                <tr>
                                    <td><strong>Bid Amount:</strong></td>
                                    <td><fmt:formatNumber value="${bid.bidAmount}" type="number" groupingUsed="true"/> Maloti</td>
                                    <td><strong>Delivery:</strong></td>
                                    <td>${bid.deliveryTimeline} days</td>
                                    <td><strong>Final Score:</strong></td>
                                    <td><span class="score-pill"><fmt:formatNumber value="${bid.finalWeightedScore}" maxFractionDigits="2"/></span></td>
                                </tr>
                                <tr>
                                    <td><strong>Price Score:</strong></td>
                                    <td><fmt:formatNumber value="${bid.priceScore}" maxFractionDigits="2"/></td>
                                    <td><strong>Technical Score:</strong></td>
                                    <td><fmt:formatNumber value="${bid.technicalScore}" maxFractionDigits="2"/></td>
                                    <td><strong>Delivery Score:</strong></td>
                                    <td><fmt:formatNumber value="${bid.deliveryScore}" maxFractionDigits="2"/></td>
                                </tr>
                            </table>
                        </div>
                    </c:forEach>
                </div>

                <!-- Award Form -->
                <div class="award-form-section">
                    <h4>Award Details</h4>

                    <div class="form-group">
                        <label for="awardedValue">Awarded Contract Value (Maloti) *</label>
                        <input type="number" id="awardedValue" name="awardedValue" step="0.01" min="1"
                               value="${not empty awardedValue ? awardedValue : ''}"
                               placeholder="Enter the final contract value" required>
                        <small style="color:#666;">Usually matches the winning bid amount but may differ after negotiation.</small>
                    </div>

                    <div class="form-group">
                        <label for="justification">Award Justification * (minimum 10 characters)</label>
                        <textarea id="justification" name="justification" maxlength="1000"
                                  placeholder="Explain why this supplier was selected. Include key evaluation criteria, score comparison, value for money assessment, etc."
                                  oninput="updateCounter(this)">${not empty justification ? justification : ''}</textarea>
                        <div class="char-counter"><span id="charCount">0</span>/1000 characters</div>
                    </div>

                    <div style="background:#fff3cd; border:1px solid #ffc107; border-radius:5px; padding:12px; margin-bottom:15px;">
                        <strong>⚠️ Important:</strong> Once awarded, this decision cannot be reversed. Ensure you have selected the correct supplier and provided a complete justification. All bidding suppliers will be notified of the outcome.
                    </div>

                    <button type="submit" class="btn-award-submit"
                            onclick="return confirm('Are you sure you want to award this contract? This action cannot be undone.')">
                        ✅ Award Contract
                    </button>
                    <a href="${pageContext.request.contextPath}/officer/tenderList" class="btn-secondary" style="margin-left:15px;">Cancel</a>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    function selectBid(bidId, bidAmount) {
        document.querySelectorAll('.bid-option').forEach(el => el.classList.remove('selected'));
        const radio = document.querySelector('input[value="' + bidId + '"]');
        if (radio) {
            radio.checked = true;
            radio.closest('.bid-option').classList.add('selected');
        }
        const awardField = document.getElementById('awardedValue');
        if (!awardField.value) {
            awardField.value = bidAmount.toFixed(2);
        }
    }

    function updateCounter(el) {
        document.getElementById('charCount').textContent = el.value.length;
    }

    // Init counter and pre-select first bid if none chosen
    window.onload = function() {
        const justEl = document.getElementById('justification');
        if (justEl) updateCounter(justEl);
        const radios = document.querySelectorAll('input[name=winningBidId]');
        let anyChecked = Array.from(radios).some(r => r.checked);
        if (!anyChecked && radios.length > 0) {
            radios[0].checked = true;
            radios[0].closest('.bid-option').classList.add('selected');
        }
    };
</script>
</body>
</html>
