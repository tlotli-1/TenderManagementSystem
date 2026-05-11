<%-- 
    Document   : 404
    Created on : Apr 18, 2026, 2:00:24 PM
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Page Not Found</title>
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

                <div class="error-icon">404</div>
                <h2>Page Not Found</h2>

                <div class="error-message-box">
                    <p>The page you are looking for does not exist or has been moved.</p>
                </div>

                <div class="error-actions">
                    <a href="${pageContext.request.contextPath}/login" class="btn-primary">Return to Login</a>
                    <a href="javascript:history.back()" class="btn-secondary">Go Back</a>
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
                background: #fff3cd;
                color: #856404;
                padding: 15px;
                border-radius: 5px;
                margin: 20px 0;
            }
            .error-actions {
                margin: 20px 0;
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