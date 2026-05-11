<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="model.User" %>
<c:if test="${not empty sessionScope.user}">
<nav class="navbar">
  <ul>
    <li><a href="${pageContext.request.contextPath}/">ProcureGov</a></li>
    <c:choose>
      <c:when test="${sessionScope.user.role == 'PROCUREMENT_OFFICER'}">
        <li><a href="${pageContext.request.contextPath}/officer/tenderList">Tenders</a></li>
        <li><a href="${pageContext.request.contextPath}/logout">Logout (${sessionScope.user.fullName})</a></li>
      </c:when>
      <c:when test="${sessionScope.user.role == 'SUPPLIER'}">
        <li><a href="${pageContext.request.contextPath}/supplier/dashboard">Dashboard</a></li>
        <li><a href="${pageContext.request.contextPath}/logout">Logout (${sessionScope.user.fullName})</a></li>
      </c:when>
      <c:otherwise>
        <li><a href="${pageContext.request.contextPath}/evaluation/panel">Evaluate</a></li>
        <li><a href="${pageContext.request.contextPath}/logout">Logout (${sessionScope.user.fullName})</a></li>
      </c:otherwise>
    </c:choose>
  </ul>
</nav>
</c:if>
