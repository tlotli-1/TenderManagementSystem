<%-- 
    Document   : genericError
    Created on : Apr 18, 2026, 2:01:29 PM
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - System Error</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    </head>
    <body>
        <div class="error-container">
            <div class="error-box">
                <div class="ministry-header">
                    <h1>ProcureGov</h1>
                    <p>Ministry of Public Works</p>
                    <p>Kingdom of Lesotho</p>
                </div>

                <div class="error-icon">⚠️</div>
                <h2>System Error</h2>

                <div class="error-message-box">
                    <p>We apologize for the inconvenience. An unexpected system error has occurred.</p>
                    <p>Our technical team has been notified and is working to resolve the issue.</p>
                </div>

                <div class="error-actions">
                    <a href="${pageContext.request.contextPath}/login" class="btn-primary">Return to Login</a>
                    <button onclick="history.back()" class="btn-secondary">Go Back</button>
                </div>

                <!-- Error details (only shown in development - hidden in production via web.xml) -->
                <c:if test="${not empty pageContext.exception}">
                    <div class="error-details">
                        <details>
                            <summary>Technical Details (for administrators)</summary>
                            <pre>${pageContext.exception.message}</pre>
                        </details>
                    </div>
                </c:if>

                <div class="contact-info">
                    <p>If the problem persists, please contact:</p>
                    <p><strong>Directorate of ICT</strong><br>
                        Ministry of Public Works<br>
                        Email: procuregov@ministry.gov.ls</p>
                </div>
            </div>
        </div>

        <style>
            .error-container {
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
                padding: 20px;
            }
            .error-box {
                background: white;
                border-radius: 10px;
                padding: 40px;
                max-width: 500px;
                text-align: center;
                box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            }
            .error-icon {
                font-size: 64px;
                margin: 20px 0;
            }
            .error-message-box {
                background: #f8d7da;
                color: #721c24;
                padding: 15px;
                border-radius: 5px;
                margin: 20px 0;
            }
            .error-actions {
                margin: 20px 0;
            }
            .error-details {
                margin-top: 20px;
                text-align: left;
                background: #f5f5f5;
                padding: 10px;
                border-radius: 5px;
            }
            .error-details pre {
                font-size: 11px;
                overflow-x: auto;
            }
            .contact-info {
                margin-top: 20px;
                padding-top: 20px;
                border-top: 1px solid #ddd;
                font-size: 12px;
                color: #666;
            }
            .btn-primary, .btn-secondary {
                display: inline-block;
                padding: 10px 20px;
                margin: 0 10px;
                border-radius: 5px;
                text-decoration: none;
            }
            .btn-primary {
                background: #1e3c72;
                color: white;
            }
            .btn-primary:hover {
                background: #2a5298;
            }
            .btn-secondary {
                background: #6c757d;
                color: white;
            }
            .btn-secondary:hover {
                background: #5a6268;
            }
        </style>
    </body>
</html>
