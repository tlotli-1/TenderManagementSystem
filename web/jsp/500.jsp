<%-- 
    Document   : 500
    Created on : Apr 18, 2026, 2:00:37 PM
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Server Error</title>
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

                <div class="error-icon">500</div>
                <h2>Internal Server Error</h2>

                <div class="error-message-box">
                    <p>Something went wrong on our end. Please try again later.</p>
                </div>

                <div class="error-actions">
                    <a href="${pageContext.request.contextPath}/login" class="btn-primary">Return to Login</a>
                </div>

                <div class="contact-info">
                    <p>If the problem persists, please contact:</p>
                    <p>procuregov@ministry.gov.ls</p>
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
                font-size: 72px;
                font-weight: bold;
                color: #dc3545;
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