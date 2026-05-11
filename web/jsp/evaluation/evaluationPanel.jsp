<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"       prefix="fmt"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ProcureGov – Evaluation Panel</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .eval-info-box { background:#eaf4fd; border:1px solid #aed6f1; border-radius:8px;
                         padding:14px 18px; margin-bottom:22px; font-size:.9rem; color:#1a5276; }
        .eval-info-box strong { display:block; margin-bottom:4px; }
        .section-card { background:#fff; border:1.5px solid #ddd; border-radius:10px;
                        padding:22px 24px; margin-bottom:28px; }
        .section-card h3 { font-size:1.05rem; font-weight:700; margin-bottom:4px; }
        .section-card p  { font-size:.85rem; color:#666; margin-bottom:14px; }
        .progress-pill { display:inline-block; background:#e9ecef; border-radius:10px;
                         padding:2px 10px; font-size:.78rem; font-weight:600; color:#555; }
        .progress-pill.done  { background:#d5f5e3; color:#1e8449; }
        .progress-pill.part  { background:#fef9e7; color:#b7950b; }
        .bids-pill { display:inline-block; background:#eef; border-radius:10px;
                     padding:2px 10px; font-size:.78rem; font-weight:600; color:#444; }
        .empty-state { text-align:center; padding:24px; color:#888; font-style:italic;
                       background:#fafafa; border-radius:8px; border:1px dashed #ddd; }
        .stats-row { display:flex; gap:14px; margin-bottom:24px; flex-wrap:wrap; }
        .stat-tile { flex:1; min-width:120px; background:#fff; border:1.5px solid #ddd;
                     border-radius:9px; padding:14px 16px; text-align:center; }
        .stat-tile .num { font-size:1.8rem; font-weight:700; color:#000; }
        .stat-tile .lbl { font-size:.75rem; color:#666; text-transform:uppercase; letter-spacing:.5px; }
        .stat-tile.amber { border-color:#e67e22; }
        .stat-tile.amber .num { color:#e67e22; }
        .stat-tile.blue  { border-color:#2980b9; }
        .stat-tile.blue  .num { color:#2980b9; }
    </style>
</head>
<body>
<div class="container">
    <div class="navbar">
        <div class="navbar-brand">🏛 ProcureGov</div>
        <div class="navbar-user">
            <span>Welcome, ${sessionScope.user.fullName}</span>
            <span style="opacity:.7;font-size:.85rem">${sessionScope.user.role}</span>
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

        

        <%-- Summary tiles ──────────────────────────────────────────────────── --%>
        <div class="stats-row">
            <div class="stat-tile amber">
                <div class="num">${closedTenders.size()}</div>
                <div class="lbl">Ready to Score</div>
            </div>
            <div class="stat-tile blue">
                <div class="num">${underEvaluationTenders.size()}</div>
                <div class="lbl">In Progress</div>
            </div>
            <c:if test="${userRole == 'PROCUREMENT_OFFICER'}">
                <div class="stat-tile">
                    <div class="num">${evaluatedTenders.size()}</div>
                    <div class="lbl">Fully Evaluated</div>
                </div>
            </c:if>
        </div>

        <%-- Category filter ─────────────────────────────────────────────────── --%>
        <div class="filters" style="margin-bottom:22px">
            <form method="get" action="${pageContext.request.contextPath}/evaluation/panel">
                <label>Category:</label>
                <select name="category">
                    <option value="">All Categories</option>
                    <option value="Construction"     ${categoryFilter == 'Construction'     ? 'selected':''}>Construction</option>
                    <option value="Roads"            ${categoryFilter == 'Roads'            ? 'selected':''}>Roads</option>
                    <option value="Electrical"       ${categoryFilter == 'Electrical'       ? 'selected':''}>Electrical</option>
                    <option value="Plumbing"         ${categoryFilter == 'Plumbing'         ? 'selected':''}>Plumbing</option>
                    <option value="General Services" ${categoryFilter == 'General Services' ? 'selected':''}>General Services</option>
                </select>
                <button type="submit" class="btn-secondary">Filter</button>
                <a href="${pageContext.request.contextPath}/evaluation/panel" class="btn-secondary">Clear</a>
            </form>
        </div>

        <%-- ═══ SECTION 1 — Ready for Evaluation (Closed) ═══ --%>
        <div class="section-card">
            <h3>📋 Ready for Evaluation — Closed Tenders</h3>
            <p>Bidding has ended. These tenders are waiting for the first evaluation score to be entered.</p>

            <c:choose>
                <c:when test="${empty closedTenders}">
                    <div class="empty-state">No tenders are currently closed and awaiting evaluation.</div>
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
                                    <td><strong>${tender.referenceNumber}</strong></td>
                                    <td>${tender.title}</td>
                                    <td>${tender.category}</td>
                                    <td>
                                        <span class="bids-pill">
                                            ${bidCounts[tender.tenderId]} bid(s)
                                        </span>
                                    </td>
                                    <td><fmt:formatDate value="${tender.submissionDeadlineAsDate}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${bidCounts[tender.tenderId] > 0}">
                                                <a href="${pageContext.request.contextPath}/evaluation/scoreEntry?tenderId=${tender.tenderId}"
                                                   class="btn-small btn-primary">Start Scoring</a>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color:#bbb;font-size:.82rem">No bids submitted</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- ═══ SECTION 2 — Under Evaluation ═══ --%>
        <div class="section-card">
            <h3>🔄 Evaluation In Progress</h3>
            <p>Scoring has begun. Continue evaluating the remaining bids.</p>

            <c:choose>
                <c:when test="${empty underEvaluationTenders}">
                    <div class="empty-state">No tenders are currently under evaluation.</div>
                </c:when>
                <c:otherwise>
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Reference</th>
                                <th>Title</th>
                                <th>Category</th>
                                <th>Total Bids</th>
                                <th>Your Progress</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${underEvaluationTenders}" var="tender">
                                <c:set var="total"  value="${bidCounts[tender.tenderId]}"/>
                                <c:set var="scored" value="${scoredCounts[tender.tenderId]}"/>
                                <tr>
                                    <td><strong>${tender.referenceNumber}</strong></td>
                                    <td>${tender.title}</td>
                                    <td>${tender.category}</td>
                                    <td><span class="bids-pill">${total} bid(s)</span></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${userRole == 'EVALUATION_COMMITTEE'}">
                                                <c:choose>
                                                    <c:when test="${scored >= total and total > 0}">
                                                        <span class="progress-pill done">✅ All ${total} scored</span>
                                                    </c:when>
                                                    <c:when test="${scored > 0}">
                                                        <span class="progress-pill part">${scored}/${total} scored</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="progress-pill">Not started</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="progress-pill part">In Progress</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/evaluation/scoreEntry?tenderId=${tender.tenderId}"
                                           class="btn-small">
                                            <c:choose>
                                                <c:when test="${scored >= total and total > 0}">Review Scores</c:when>
                                                <c:otherwise>Continue Scoring</c:otherwise>
                                            </c:choose>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </div>

        <%-- ═══ SECTION 3 — Completed Evaluations (Officer only) ═══ --%>
        <c:if test="${userRole == 'PROCUREMENT_OFFICER'}">
            <div class="section-card">
                <h3>🏆 Fully Evaluated — Ready for Award</h3>
                <p>All scores have been submitted. Review results and award the contract.</p>

                <c:choose>
                    <c:when test="${empty evaluatedTenders}">
                        <div class="empty-state">No tenders have completed evaluation yet.</div>
                    </c:when>
                    <c:otherwise>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Reference</th>
                                    <th>Title</th>
                                    <th>Category</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${evaluatedTenders}" var="tender">
                                    <tr>
                                        <td><strong>${tender.referenceNumber}</strong></td>
                                        <td>${tender.title}</td>
                                        <td>${tender.category}</td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/evaluation/results?tenderId=${tender.tenderId}"
                                               class="btn-small btn-results">View Results</a>
                                            <a href="${pageContext.request.contextPath}/officer/awardContract?tenderId=${tender.tenderId}"
                                               class="btn-small btn-award">Award Contract</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

    </div><%-- /content --%>
</div><%-- /container --%>
</body>
</html>
