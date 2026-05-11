<%-- 
    Document   : databaseError
    Created on : Apr 19, 2026, 4:23:29 PM
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Database Error</title>
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

                <div class="error-icon">🗄️</div>
                <h2>Database Connection Error</h2>

                <div class="error-message-box">
                    <p>Unable to connect to the database. The system is temporarily unavailable.</p>
                    <p>Please try again later or contact the system administrator.</p>
                </div>

                <div class="error-actions">
                    <a href="${pageContext.request.contextPath}/login" class="btn-primary">Return to Login</a>
                </div>

                <div class="contact-info">
                    <p><strong>Possible causes:</strong></p>
                    <ul style="text-align: left;">
                        <li>MySQL service is not running</li>
                        <li>Database connection pool is exhausted</li>
                        <li>Network connectivity issues</li>
                    </ul>
                    <p>Contact: procuregov@ministry.gov.ls</p>
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
            .contact-info {
                margin-top: 20px;
                padding-top: 20px;
                border-top: 1px solid #ddd;
                font-size: 12px;
                color: #666;
                text-align: left;
            }
            .btn-primary {
                display: inline-block;
                padding: 10px 20px;
                background: #1e3c72;
                color: white;
                text-decoration: none;
                border-radius: 5px;
            }
            .btn-primary:hover {
                background: #2a5298;
            }
        </style>
    </body>
</html>
