<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"   %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"       prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ProcureGov – Supplier Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .dash-stats { display:flex; gap:16px; margin-bottom:28px; flex-wrap:wrap; }
        .stat-tile { flex:1; min-width:140px; background:#fff; border:1.5px solid #ddd; border-radius:10px; padding:18px 20px; text-align:center; }
        .stat-tile .num  { font-size:2rem; font-weight:700; color:#000; }
        .stat-tile .lbl  { font-size:.78rem; color:#666; margin-top:4px; text-transform:uppercase; letter-spacing:.5px; }
        .stat-tile.green { border-color:#27ae60; }
        .stat-tile.green .num { color:#27ae60; }
        .stat-tile.amber { border-color:#e67e22; }
        .stat-tile.amber .num { color:#e67e22; }
        .section-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:14px; }
        .section-header h2 { font-size:1.15rem; font-weight:700; }
        .badge-won     { background:#d5f5e3; color:#1e8449; padding:3px 10px; border-radius:12px; font-size:.78rem; font-weight:600; }
        .badge-not-won { background:#f5eef8; color:#6c3483; padding:3px 10px; border-radius:12px; font-size:.78rem; font-weight:600; }
        .badge-pending { background:#fef9e7; color:#b7950b; padding:3px 10px; border-radius:12px; font-size:.78rem; font-weight:600; }
        .deadline-soon { color:#e74c3c; font-weight:600; }
        .info-card { background:#f0f7ff; border:1px solid #bee3f8; border-radius:8px; padding:16px 20px; color:#2471a3; font-size:.9rem; }
    </style>
</head>
<body>
<div class="container">
    <%-- ── Navbar ── --%>
    <div class="navbar">
        <div class="navbar-brand">🏛 ProcureGov</div>
        <div class="navbar-user">
            <span>👤 ${supplier.fullName}</span>
            <span style="opacity:.7;font-size:.85rem">${supplier.companyName} · ${supplier.registrationNumber}</span>
            <a href="${pageContext.request.contextPath}/logout">Logout</a>
        </div>
    </div>

    <div class="content">
        <%-- ── Flash messages ── --%>
        <c:if test="${not empty param.success}">
            <div class="success-message">${param.success}</div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="error-message">${param.error}</div>
        </c:if>

        <%-- ── Summary tiles ── --%>
        <div class="dash-stats">
            <div class="stat-tile">
                <div class="num">${fn:length(openTenders)}</div>
                <div class="lbl">Open Tenders</div>
            </div>
            <div class="stat-tile">
                <div class="num">${fn:length(myBids)}</div>
                <div class="lbl">My Bids</div>
            </div>
            <div class="stat-tile amber">
                <div class="num">${pendingCount}</div>
                <div class="lbl">Awaiting Decision</div>
            </div>
            <div class="stat-tile green">
                <div class="num">${wonCount}</div>
                <div class="lbl">Tenders Won</div>
            </div>
        </div>

        <%-- ══════════════════════════════════════
             SECTION 1 — Open Tenders
        ═══════════════════════════════════════ --%>
        <div class="dashboard-section">
            <div class="section-header">
                <h2>📢 Open Tenders</h2>
                <small style="color:#666">Showing tenders currently accepting bids</small>
            </div>

            <c:choose>
                <c:when test="${empty openTenders}">
                    <div class="info-card">No open tenders are available at this time. Check back later.</div>
                </c:when>
                <c:otherwise>
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Est. Value (M)</th>
                                <th>Deadline</th>
                                <th>Notice</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${openTenders}" var="t">
                                <tr>
                                    <td><strong>${t.referenceNumber}</strong></td>
                                    <td>${t.title}</td>
                                    <td>${t.category}</td>
                                    <td><fmt:formatNumber value="${t.estimatedValue}" type="number" groupingUsed="true"/></td>
                                    <td>
                                        <fmt:formatDate value="${t.submissionDeadlineAsDate}" pattern="dd/MM/yyyy HH:mm"/>
                                    </td>
                                    <td>
                                        <c:if test="${not empty t.noticeFilePath}">
                                            <a href="${pageContext.request.contextPath}/download/notice?tenderId=${t.tenderId}"
                                               class="btn-small" target="_blank">📄 Download</a>
                                        </c:if>
                                        <c:if test="${empty t.noticeFilePath}">
                                            <span style="color:#bbb">—</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/tender/details?tenderId=${t.tenderId}"
                                           class="btn-small btn-primary">View &amp; Bid</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- ══════════════════════════════════════
             SECTION 2 — My Submitted Bids
        ═══════════════════════════════════════ --%>
        <div class="dashboard-section" style="margin-top:32px">
            <div class="section-header">
                <h2>📋 My Submitted Bids</h2>
                <small style="color:#666">Track status and view award notices</small>
            </div>

            <c:choose>
                <c:when test="${empty myBids}">
                    <div class="info-card">You haven't submitted any bids yet. Browse open tenders above to get started.</div>
                </c:when>
                <c:otherwise>
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Tender Title</th>
                                <th>Bid Amount (M)</th>
                                <th>Delivery (Days)</th>
                                <th>Submitted</th>
                                <th>Tender Status</th>
                                <th>Outcome</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${myBids}" var="bid">
                                <tr>
                                    <td><strong>${bid.tenderReference}</strong></td>
                                    <td>${bid.tenderTitle}</td>
                                    <td><fmt:formatNumber value="${bid.bidAmount}" type="number" groupingUsed="true"/></td>
                                    <td>${bid.deliveryTimeline}</td>
                                    <td><fmt:formatDate value="${bid.submissionDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    <td>
                                        <span class="status-badge status-${bid.tenderStatus}">${bid.tenderStatus}</span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${bid.awardOutcome == 'WON'}">
                                                <span class="badge-won">🏆 Won</span>
                                            </c:when>
                                            <c:when test="${bid.awardOutcome == 'NOT_WON'}">
                                                <span class="badge-not-won">Not Selected</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge-pending">⏳ Pending</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/tender/details?tenderId=${bid.tenderId}"
                                           class="btn-small">View Tender</a>
                                        <c:if test="${bid.tenderStatus == 'Awarded'}">
                                            <a href="${pageContext.request.contextPath}/tender/awardNotice?tenderId=${bid.tenderId}"
                                               class="btn-small btn-award">Award Notice</a>
                                        </c:if>
                                        <c:if test="${not empty bid.supportingDocPath}">
                                            <a href="${pageContext.request.contextPath}/download/bidDocument?bidId=${bid.bidId}"
                                               class="btn-small" target="_blank">📎 My Doc</a>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>

    </div><%-- /content --%>
</div><%-- /container --%>
</body>
</html>
