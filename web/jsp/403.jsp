<%-- 
    Document   : 403
    Created on : Apr 19, 2026, 4:18:47 PM
    Author     : legacy
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>ProcureGov - Access Denied</title>
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

                <div class="error-icon">403</div>
                <h2>Access Denied</h2>

                <div class="error-message-box">
                    <p>You do not have permission to access this page.</p>
                    <p>Please login with appropriate credentials or contact your administrator.</p>
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
                background: #f8d7da;
                color: #721c24;
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
