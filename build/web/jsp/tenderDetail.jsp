<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"   %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"       prefix="fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ProcureGov – ${tender.referenceNumber}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .detail-card { background:#fff; border:1.5px solid #ddd; border-radius:10px; padding:24px 28px; margin-bottom:22px; }
        .detail-card h3 { font-size:1rem; font-weight:700; border-bottom:2px solid #000; padding-bottom:8px; margin-bottom:16px; }
        .detail-grid { display:grid; grid-template-columns:200px 1fr; gap:10px 16px; font-size:.9rem; }
        .detail-grid dt { font-weight:600; color:#555; }
        .detail-grid dd { color:#222; margin:0; }
        .bid-card { background:#fffef5; border:2px solid #000; border-radius:10px; padding:28px; }
        .bid-card h3 { font-size:1.1rem; font-weight:700; margin-bottom:18px; }
        .form-group { margin-bottom:18px; }
        .form-group label { display:block; font-size:.85rem; font-weight:600; margin-bottom:6px; color:#333; }
        .form-group input, .form-group textarea {
            width:100%; padding:10px 12px; border:1.5px solid #ccc; border-radius:7px; font-size:.9rem;
        }
        .form-group input:focus, .form-group textarea:focus { outline:none; border-color:#000; }
        .form-group small { font-size:.75rem; color:#888; margin-top:4px; display:block; }
        .form-group textarea { resize:vertical; min-height:100px; }
        .char-counter { font-size:.73rem; color:#888; text-align:right; }
        .btn-submit { background:#000; color:#f5f5dc; border:none; padding:12px 32px; border-radius:7px; font-size:.95rem; font-weight:600; cursor:pointer; }
        .btn-submit:hover { background:#222; }
        .already-bid { background:#f0fff4; border:2px solid #27ae60; border-radius:10px; padding:22px; }
        .already-bid .bid-row { display:grid; grid-template-columns:160px 1fr; gap:8px; font-size:.9rem; margin-top:12px; }
        .already-bid .bid-row dt { font-weight:600; color:#555; }
        .notice-box { background:#eaf4fd; border:1px solid #aed6f1; border-radius:8px; padding:14px 18px; margin-top:10px; }
        .closed-box  { background:#fdf0f0; border:1px solid #e74c3c; border-radius:8px; padding:14px 18px; color:#c0392b; }
        .badge-won    { background:#d5f5e3; color:#1e8449; padding:4px 12px; border-radius:12px; font-size:.82rem; font-weight:600; }
        .badge-not-won{ background:#f5eef8; color:#6c3483; padding:4px 12px; border-radius:12px; font-size:.82rem; font-weight:600; }
        .badge-pending{ background:#fef9e7; color:#b7950b; padding:4px 12px; border-radius:12px; font-size:.82rem; font-weight:600; }
        .breadcrumb   { margin-bottom:14px; font-size:.85rem; }
        .breadcrumb a { color:#555; }
    </style>
</head>
<body>
<div class="container">
    <div class="navbar">
        <div class="navbar-brand">🏛 ProcureGov</div>
        <div class="navbar-user">
            <span>${sessionScope.user.fullName}</span>
            <span style="opacity:.7;font-size:.85rem">${sessionScope.user.role}</span>
            <a href="${pageContext.request.contextPath}/logout">Logout</a>
        </div>
    </div>

    <div class="content">
        <div class="breadcrumb">
            <c:choose>
                <c:when test="${userRole == 'SUPPLIER'}">
                    <a href="${pageContext.request.contextPath}/supplier/dashboard">← Back to Dashboard</a>
                </c:when>
                <c:when test="${userRole == 'PROCUREMENT_OFFICER'}">
                    <a href="${pageContext.request.contextPath}/officer/tenderList">← Back to Tender List</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/evaluation/panel">← Back to Evaluation Panel</a>
                </c:otherwise>
            </c:choose>
        </div>


        <%-- Flash error from redirect (e.g. bid validation failure) --%>
        <c:if test="${not empty param.error}">
            <div class="error-message">${param.error}</div>
        </c:if>

        <h2 style="margin-bottom:22px">${tender.referenceNumber} — ${tender.title}</h2>

        <%-- ── Tender information ── --%>
        <div class="detail-card">
            <h3>Tender Information</h3>
            <dl class="detail-grid">
                <dt>Reference:</dt>   <dd><strong>${tender.referenceNumber}</strong></dd>
                <dt>Title:</dt>       <dd>${tender.title}</dd>
                <dt>Category:</dt>    <dd>${tender.category}</dd>
                <dt>Status:</dt>      <dd><span class="status-badge status-${tender.status}">${tender.status}</span></dd>
                <dt>Description:</dt> <dd>${tender.description}</dd>
            </dl>
        </div>

        <div class="detail-card">
            <h3>Financial &amp; Timeline</h3>
            <dl class="detail-grid">
                <dt>Estimated Value:</dt>
                <dd><fmt:formatNumber value="${tender.estimatedValue}" type="number" groupingUsed="true"/> Maloti</dd>
                <dt>Submission Deadline:</dt>
                <dd>
                    <fmt:formatDate value="${tender.submissionDeadlineAsDate}" pattern="dd MMMM yyyy 'at' HH:mm"/>
                    <c:if test="${not isOpen}"> <span style="color:#e74c3c;font-weight:600;">(CLOSED)</span></c:if>
                </dd>
            </dl>
        </div>

        <%-- Tender notice download --%>
        <div class="detail-card">
            <h3>Tender Notice Document</h3>
            <c:if test="${not empty tender.noticeFilePath}">
                <div class="notice-box">
                    📄 A tender notice PDF is available for this tender.
                    <a href="${pageContext.request.contextPath}/download/notice?tenderId=${tender.tenderId}"
                       class="btn-small" target="_blank" style="margin-left:12px">Download Tender Notice (PDF)</a>
                </div>
            </c:if>
            <c:if test="${empty tender.noticeFilePath}">
                <p style="color:#999;font-style:italic">No notice document has been attached to this tender.</p>
            </c:if>
        </div>

        <%-- ══════════════════════════════════════
             BID SECTION — Suppliers only
        ═══════════════════════════════════════ --%>
        <c:if test="${userRole == 'SUPPLIER'}">

            <%-- Already submitted — show their bid details --%>
            <c:if test="${hasSubmittedBid and not empty myBid}">
                <div class="already-bid">
                    <strong>✅ You have already submitted a bid for this tender.</strong>
                    <p style="color:#555;margin:6px 0 12px">Only one bid per tender is allowed. Here is a summary of your submission:</p>
                    <dl class="bid-row">
                        <dt>Bid Amount:</dt>
                        <dd><fmt:formatNumber value="${myBid.bidAmount}" type="number" groupingUsed="true"/> Maloti</dd>
                        <dt>Delivery Timeline:</dt>
                        <dd>${myBid.deliveryTimeline} days</dd>
                        <dt>Submitted On:</dt>
                        <dd><fmt:formatDate value="${myBid.submissionDate}" pattern="dd MMMM yyyy HH:mm"/></dd>
                        <dt>Technical Statement:</dt>
                        <dd>${myBid.technicalStatement}</dd>
                        <dt>Supporting Document:</dt>
                        <dd>
                            <c:if test="${not empty myBid.supportingDocPath}">
                                <a href="${pageContext.request.contextPath}/download/bidDocument?bidId=${myBid.bidId}"
                                   target="_blank">📎 Download my document</a>
                            </c:if>
                        </dd>
                        <dt>Outcome:</dt>
                        <dd>
                            <c:choose>
                                <c:when test="${myBid.awardOutcome == 'WON'}">
                                    <span class="badge-won">🏆 WON — Contract Awarded</span>
                                    <a href="${pageContext.request.contextPath}/tender/awardNotice?tenderId=${tender.tenderId}"
                                       class="btn-small btn-award" style="margin-left:10px">View Award Notice</a>
                                </c:when>
                                <c:when test="${myBid.awardOutcome == 'NOT_WON'}">
                                    <span class="badge-not-won">Not Selected</span>
                                    <a href="${pageContext.request.contextPath}/tender/awardNotice?tenderId=${tender.tenderId}"
                                       class="btn-small" style="margin-left:10px">View Award Notice</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge-pending">⏳ Pending evaluation</span>
                                </c:otherwise>
                            </c:choose>
                        </dd>
                    </dl>
                </div>
            </c:if>

            <%-- Tender closed — cannot bid --%>
            <c:if test="${not hasSubmittedBid and not canSubmitBid}">
                <div class="closed-box">
                    ⚠️ This tender is not open for bidding.
                    The submission deadline has passed or the tender status is <strong>${tender.status}</strong>.
                </div>
            </c:if>

            <%-- Can submit — show bid form --%>
            <c:if test="${canSubmitBid}">
                <div class="bid-card">
                    <h3>Submit Your Bid</h3>
                    <p style="color:#666;margin-bottom:20px;font-size:.88rem;">
                        All fields are required. You may only submit <strong>one bid</strong> per tender — review carefully before submitting.
                    </p>

                    <form method="post"
                          action="${pageContext.request.contextPath}/supplier/submitBid"
                          enctype="multipart/form-data"
                          onsubmit="return confirmSubmit()">
                        <input type="hidden" name="tenderId" value="${tender.tenderId}">

                        <div class="form-group">
                            <label for="bidAmount">Bid Amount (Maloti) <span style="color:#c0392b">*</span></label>
                            <input type="number" id="bidAmount" name="bidAmount"
                                   step="0.01" min="1" max="999999999"
                                   required placeholder="Enter your proposed price">
                            <small>Your total proposed price in Maloti to complete this tender.</small>
                        </div>

                        <div class="form-group">
                            <label for="technicalStatement">Technical Compliance Statement <span style="color:#c0392b">*</span></label>
                            <textarea id="technicalStatement" name="technicalStatement"
                                      minlength="10" maxlength="600" required
                                      oninput="updateCount(this)"
                                      placeholder="Describe how your company meets the technical requirements of this tender..."></textarea>
                            <div class="char-counter"><span id="techCount">0</span> / 600 characters</div>
                            <small>Minimum 10, maximum 600 characters.</small>
                        </div>

                        <div class="form-group">
                            <label for="deliveryTimeline">Proposed Delivery Timeline (Days) <span style="color:#c0392b">*</span></label>
                            <input type="number" id="deliveryTimeline" name="deliveryTimeline"
                                   min="1" max="3650" required
                                   placeholder="Number of days to complete the project">
                            <small>Working days from contract award date to project completion.</small>
                        </div>

                        <div class="form-group">
                            <label for="supportingDoc">Supporting Document (PDF only, max 5 MB) <span style="color:#c0392b">*</span></label>
                            <input type="file" id="supportingDoc" name="supportingDoc"
                                   accept=".pdf" required
                                   onchange="validateFile(this)">
                            <small id="fileLabel">Upload your technical proposal, company certificates, or other supporting material. PDF only.</small>
                        </div>

                        <div style="background:#fff3cd;border:1px solid #ffc107;border-radius:6px;padding:12px 16px;margin-bottom:18px;font-size:.85rem;">
                            ⚠️ <strong>Once submitted, your bid cannot be modified or withdrawn.</strong>
                            Ensure all information is correct before submitting.
                        </div>

                        <button type="submit" class="btn-submit">Submit Bid →</button>
                    </form>
                </div>
            </c:if>

        </c:if><%-- /SUPPLIER --%>

    </div>
</div>

<script>
    function updateCount(el) {
        document.getElementById('techCount').textContent = el.value.length;
    }

    function validateFile(input) {
        const label = document.getElementById('fileLabel');
        if (!input.files.length) return;
        const file = input.files[0];
        const maxBytes = 5 * 1024 * 1024;
        const ext = file.name.toLowerCase().split('.').pop();

        if (ext !== 'pdf') {
            label.textContent = '❌ Only PDF files are accepted.';
            label.style.color = '#c0392b';
            input.value = '';
            return;
        }
        if (file.size > maxBytes) {
            label.textContent = '❌ File size exceeds 5 MB. Please compress or reduce the file size.';
            label.style.color = '#c0392b';
            input.value = '';
            return;
        }
        const kb = (file.size / 1024).toFixed(0);
        label.textContent = '✅ ' + file.name + ' (' + kb + ' KB)';
        label.style.color = '#27ae60';
    }

    function confirmSubmit() {
        return confirm('Submit your bid now?\n\nThis action cannot be undone — you cannot edit or withdraw a bid once submitted.');
    }
</script>
</body>
</html>
