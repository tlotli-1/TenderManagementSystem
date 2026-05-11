/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Logout Servlet - Invalidates session and redirects to login
 * Module 1: Session Management requirement
 */
public class LogoutServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(LogoutServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            String username = session.getAttribute("username") != null ? 
                              session.getAttribute("username").toString() : "Unknown";
            logger.info("User logging out: " + username);
            
            // Invalidate the session
            session.invalidate();
        }
        
        // Prevent caching after logout (helps stop Back button from showing protected pages)
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        // Also handle clients that rely on the Expires / ETag semantics
        response.setHeader("ETag", "logout");


        // Redirect to login page with logout message
        response.sendRedirect(request.getContextPath() + "/login?loggedout=true");
    }
}