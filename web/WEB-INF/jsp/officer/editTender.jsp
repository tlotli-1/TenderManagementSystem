<%-- 
    Document   : editTender
    Created on : Apr 18, 2026, 1:58:42 PM
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Edit Tender</title>
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
                <h2>Edit Tender: ${tender.referenceNumber}</h2>
                <p class="warning">Note: Only Draft tenders can be edited.</p>

                <c:if test="${not empty errors}">
                    <div class="error-message">${errors}</div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/officer/editTender" 
                      enctype="multipart/form-data">
                    <input type="hidden" name="tenderId" value="${tender.tenderId}">

                    <fieldset>
                        <legend>Tender Information</legend>

                        <div class="form-group">
                            <label>Reference Number:</label>
                            <input type="text" value="${tender.referenceNumber}" disabled>
                        </div>

                        <div class="form-group">
                            <label>Tender Title:*</label>
                            <input type="text" name="title" value="${tender.title}" required>
                        </div>

                        <div class="form-group">
                            <label>Category:*</label>
                            <select name="category" required>
                                <option value="Construction" ${tender.category == 'Construction' ? 'selected' : ''}>Construction</option>
                                <option value="Roads" ${tender.category == 'Roads' ? 'selected' : ''}>Roads</option>
                                <option value="Electrical" ${tender.category == 'Electrical' ? 'selected' : ''}>Electrical</option>
                                <option value="Plumbing" ${tender.category == 'Plumbing' ? 'selected' : ''}>Plumbing</option>
                                <option value="General Services" ${tender.category == 'General Services' ? 'selected' : ''}>General Services</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label>Description:*</label>
                            <textarea name="description" rows="5" required>${tender.description}</textarea>
                        </div>

                        <div class="form-group">
                            <label>Estimated Value (Maloti):*</label>
                            <input type="number" name="estimatedValue" step="0.01" value="${tender.estimatedValue}" required>
                        </div>

                        <div class="form-group">
                            <label>Submission Deadline:*</label>
                            <input type="datetime-local" name="submissionDeadline" 
                                   value="<fmt:formatDate value='${tender.submissionDeadlineAsDate}' pattern='yyyy-MM-dd'T'HH:mm'/>" required>
                            </div>

                            <div class="form-group">
                                <label>Current Notice File:</label>
                                <c:if test="${not empty tender.noticeFilePath}">
                                    <p><a href="${pageContext.request.contextPath}/download/notice?file=${tender.noticeFilePath}">📄 Download Current PDF</a></p>
                                </c:if>
                                <c:if test="${empty tender.noticeFilePath}">
                                    <p><em>No notice file uploaded yet</em></p>
                                </c:if>
                            </div>

                            <div class="form-group">
                                <label>Replace Notice File (PDF, max 5MB):</label>
                                <input type="file" name="noticeFile" accept=".pdf">
                                <small>Leave empty to keep current file</small>
                            </div>
                        </fieldset>

                        <button type="submit" class="btn-primary">Save Changes</button>
                        <a href="${pageContext.request.contextPath}/officer/tenderList" class="btn-secondary">Cancel</a>
                    </form>
                </div>
            </div>
        </body>
    </html>
