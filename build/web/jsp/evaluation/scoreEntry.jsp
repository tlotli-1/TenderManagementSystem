<%-- 
    Document   : scoreEntry
    Created on : Apr 18, 2026, 1:59:43 PM
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Score Entry</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
        <style>
            .bid-card {
                border: 1px solid #ddd;
                border-radius: 8px;
                margin-bottom: 20px;
                padding: 20px;
                background: white;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }
            .bid-card.evaluated {
                background: #f0f8f0;
                border-left: 5px solid #28a745;
            }
            .bid-card.pending {
                border-left: 5px solid #ffc107;
            }
            .score-display {
                display: inline-block;
                padding: 5px 10px;
                border-radius: 5px;
                font-weight: bold;
            }
            .auto-score {
                background: #e9ecef;
                padding: 8px;
                border-radius: 5px;
                margin: 5px 0;
            }
            .formula-badge {
                font-size: 12px;
                color: #666;
                margin-left: 10px;
            }
            .technical-input {
                width: 100px;
                padding: 8px;
                border: 1px solid #ddd;
                border-radius: 4px;
            }
            .submit-btn {
                background: #28a745;
                color: white;
                padding: 10px 20px;
                border: none;
                border-radius: 5px;
                cursor: pointer;
                margin-top: 10px;
            }
            .submit-btn:hover {
                background: #218838;
            }
            .progress-bar {
                background: #e9ecef;
                border-radius: 10px;
                height: 20px;
                margin: 20px 0;
                overflow: hidden;
            }
            .progress-fill {
                background: #28a745;
                height: 100%;
                color: white;
                text-align: center;
                line-height: 20px;
                font-size: 12px;
                transition: width 0.3s;
            }
            .info-badge {
                background: #17a2b8;
                color: white;
                padding: 2px 8px;
                border-radius: 12px;
                font-size: 12px;
                margin-left: 10px;
            }
        </style>
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
                <!-- Breadcrumb -->
                <div class="breadcrumb">
                    <a href="${pageContext.request.contextPath}/evaluation/panel">← Back to Evaluation Panel</a>
                </div>

                <h2>Bid Evaluation</h2>
                <h3>${tender.referenceNumber} - ${tender.title}</h3>

                <!-- Success/Error Messages -->
                <c:if test="${not empty param.success}">
                    <div class="success-message">${param.success}</div>
                </c:if>
                <c:if test="${not empty param.error}">
                    <div class="error-message">${param.error}</div>
                </c:if>

                <!-- Progress Indicator -->
                <c:set var="totalBids" value="${bids.size()}"/>
                <c:set var="evaluatedCount" value="0"/>
                <c:forEach items="${bids}" var="bid">
                    <c:if test="${not bid.active}"><c:set var="evaluatedCount" value="${evaluatedCount + 1}"/></c:if>
                </c:forEach>
                <c:set var="progressPercent" value="${evaluatedCount * 100 / totalBids}"/>

                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${progressPercent}%;">
                        ${evaluatedCount}/${totalBids} Evaluated
                    </div>
                </div>

                <!-- Scoring Formula Reference -->
                <div class="auto-score" style="margin-bottom: 20px;">
                    <strong>📊 Scoring Formula:</strong>
                    <span class="formula-badge">Price Score = (Lowest Bid / This Bid) × 100 (40% weight)</span>
                    <span class="formula-badge">Technical Score = Your input (35% weight)</span>
                    <span class="formula-badge">Delivery Score = (Shortest Timeline / This Timeline) × 100 (25% weight)</span>
                    <br>
                    <small>Lowest bid amount: <strong><fmt:formatNumber value="${lowestBidAmount}" type="number" groupingUsed="true"/> M</strong></small>
                    |
                    <small>Shortest timeline: <strong>${shortestTimeline} days</strong></small>
                </div>

                <!-- Bids List -->
                <c:forEach items="${bids}" var="bid" varStatus="status">
                    <div class="bid-card ${bid.active ? 'pending' : 'evaluated'}">
                        <h3>
                            Bid #${status.index + 1}: ${bid.supplierCompany}
                            <c:if test="${not bid.active}">
                                <span class="info-badge">✓ Already Evaluated</span>
                            </c:if>
                        </h3>

                        <table style="width: 100%; margin-bottom: 15px;">
                            <tr>
                                <td style="width: 150px;"><strong>Bid Amount:</strong></td>
                                <td><fmt:formatNumber value="${bid.bidAmount}" type="number" groupingUsed="true"/> Maloti</td>
                            </tr>
                            <tr>
                                <td><strong>Technical Statement:</strong></td>
                                <td>${bid.technicalStatement}</td>
                            </tr>
                            <tr>
                                <td><strong>Proposed Timeline:</strong></td>
                                <td>${bid.deliveryTimeline} days</td>
                            </tr>
                            <tr>
                                <td><strong>Supporting Document:</strong></td>
                                <td>
                                    <c:if test="${not empty bid.supportingDocPath}">
                                        <a href="${pageContext.request.contextPath}/download/bidDocument?bidId=${bid.bidId}" target="_blank">
                                            📄 View Document
                                        </a>
                                    </c:if>
                                    <c:if test="${empty bid.supportingDocPath}">
                                        <span style="color: #999;">-</span>
                                    </c:if>
                                </td>
                            </tr>
                        </table>

                        <!-- Auto-calculated Scores -->
                        <div class="auto-score">
                            <strong>📈 Auto-Calculated Scores:</strong><br>
                            Price Score: <fmt:formatNumber value="${bid.priceScore}" maxFractionDigits="2"/> / 100 
                            (Weight: <fmt:formatNumber value="${bid.priceScore * 0.40}" maxFractionDigits="2"/>)
                            <br>
                            Delivery Score: <fmt:formatNumber value="${bid.deliveryScore}" maxFractionDigits="2"/> / 100 
                            (Weight: <fmt:formatNumber value="${bid.deliveryScore * 0.25}" maxFractionDigits="2"/>)
                        </div>

                        <!-- Technical Score Entry Form -->
                        <c:choose>
                            <c:when test="${bid.active}">
                                <form method="post" action="${pageContext.request.contextPath}/evaluation/submitScores" 
                                      onsubmit="return validateScore(this, ${bid.bidId})">
                                    <input type="hidden" name="tenderId" value="${tender.tenderId}">
                                    <input type="hidden" name="bidId" value="${bid.bidId}">

                                    <div style="margin-top: 15px;">
                                        <label><strong>📝 Technical Compliance Score (0-100):</strong></label>
                                        <input type="number" 
                                               name="technicalScore" 
                                               id="techScore_${bid.bidId}"
                                               class="technical-input" 
                                               step="0.01" 
                                               min="0" 
                                               max="100" 
                                               required>
                                        <span style="margin-left: 10px; font-size: 12px; color: #666;">
                                            Weight: 35%
                                        </span>
                                    </div>

                                    <!-- Live Preview of Weighted Total -->
                                    <div id="preview_${bid.bidId}" style="margin-top: 10px; font-size: 14px; color: #666;">
                                        <em>Will calculate: 
                                            Price(<fmt:formatNumber value="${bid.priceScore}" maxFractionDigits="2"/>×0.40) + 
                                            Tech(?×0.35) + 
                                            Delivery(<fmt:formatNumber value="${bid.deliveryScore}" maxFractionDigits="2"/>×0.25)
                                        </em>
                                    </div>

                                    <button type="submit" class="submit-btn">
                                        ✓ Submit Scores for ${bid.supplierCompany}
                                    </button>
                                </form>

                                <script>
                                    function validateScore(form, bidId) {
                                        var techInput = document.getElementById('techScore_' + bidId);
                                        var techScore = parseFloat(techInput.value);

                                        if (isNaN(techScore)) {
                                            alert('Please enter a technical score between 0 and 100');
                                            return false;
                                        }
                                        if (techScore < 0 || techScore > 100) {
                                            alert('Technical score must be between 0 and 100');
                                            return false;
                                        }

                                        var priceScore = ${bid.priceScore};
                                        var deliveryScore = ${bid.deliveryScore};
                                        var weightedTotal = (priceScore * 0.40) + (techScore * 0.35) + (deliveryScore * 0.25);

                                        return confirm('Submit score of ' + techScore + ' for ' + '${bid.supplierCompany}' +
                                                '?\n\nWeighted Total Score will be: ' + weightedTotal.toFixed(2) +
                                                '\n\nThis action cannot be undone.');
                                    }

                                    // Live preview update
                                    document.getElementById('techScore_${bid.bidId}').addEventListener('input', function () {
                                        var techScore = parseFloat(this.value) || 0;
                                        var priceScore = ${bid.priceScore};
                                        var deliveryScore = ${bid.deliveryScore};
                                        var weightedTotal = (priceScore * 0.40) + (techScore * 0.35) + (deliveryScore * 0.25);
                                        var previewDiv = document.getElementById('preview_${bid.bidId}');
                                        previewDiv.innerHTML = '<strong>Preview Weighted Total:</strong> ' + weightedTotal.toFixed(2) +
                                                ' (Price: ' + (priceScore * 0.40).toFixed(2) +
                                                ' + Tech: ' + (techScore * 0.35).toFixed(2) +
                                                ' + Delivery: ' + (deliveryScore * 0.25).toFixed(2) + ')';
                                    });
                                </script>
                            </c:when>
                            <c:otherwise>
                                <!-- Already evaluated - show existing scores -->
                                <div class="auto-score" style="background: #d4edda;">
                                    <strong>✅ Already Submitted Scores:</strong><br>
                                    Technical Score: <fmt:formatNumber value="${bid.technicalScore}" maxFractionDigits="2"/> / 100
                                    (Weight: <fmt:formatNumber value="${bid.technicalScore * 0.35}" maxFractionDigits="2"/>)
                                    <br>
                                    <strong>Weighted Total:</strong> 
                                    <fmt:formatNumber value="${bid.priceScore * 0.40 + bid.technicalScore * 0.35 + bid.deliveryScore * 0.25}" 
                                                      maxFractionDigits="2"/> / 100
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:forEach>

                <!-- No Bids Message -->
                <c:if test="${empty bids}">
                    <div class="info-message">No bids have been submitted for this tender.</div>
                </c:if>

                <!-- Navigation Buttons -->
                <div style="margin-top: 30px; text-align: center;">
                    <a href="${pageContext.request.contextPath}/evaluation/panel" class="btn-secondary">Back to Evaluation Panel</a>
                    <c:if test="${evaluatedCount == totalBids && totalBids > 0}">
                        <a href="${pageContext.request.contextPath}/evaluation/results?tenderId=${tender.tenderId}" class="btn-primary">
                            View Ranked Results →
                        </a>
                    </c:if>
                </div>
            </div>
        </div>
    </body>
</html>
