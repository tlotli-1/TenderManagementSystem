package util;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.User;

/**
 * Reusable session validation utility
 * Required by Module 1 - Session check on protected pages
 */
public class SessionValidator {
    
    /**
     * Check if user is logged in
     * @param session HttpSession object
     * @return true if user is logged in
     */
    public static boolean isLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("user") != null;
    }
    
    /**
     * Get logged-in user from session
     * @param session HttpSession object
     * @return User object or null
     */
    public static User getLoggedInUser(HttpSession session) {
        if (isLoggedIn(session)) {
            return (User) session.getAttribute("user");
        }
        return null;
    }
    
    /**
     * Check if logged-in user has required role
     * @param session HttpSession object
     * @param requiredRole The role required (SUPPLIER, PROCUREMENT_OFFICER, EVALUATION_COMMITTEE)
     * @return true if user has the required role
     */
    public static boolean hasRole(HttpSession session, String requiredRole) {
        User user = getLoggedInUser(session);
        return user != null && user.getRole().equals(requiredRole);
    }
    
    /**
     * Check if user has any of the allowed roles
     * @param session HttpSession object
     * @param allowedRoles Array of allowed role names
     * @return true if user has at least one allowed role
     */
    public static boolean hasAnyRole(HttpSession session, String[] allowedRoles) {
        User user = getLoggedInUser(session);
        if (user == null) return false;
        
        for (String role : allowedRoles) {
            if (user.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if user is authenticated (for use with HttpServletRequest)
     * @param request HttpServletRequest
     * @return true if user is logged in
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return isLoggedIn(session);
    }
    
    /**
     * Check role using HttpServletRequest (for filters)
     * @param request HttpServletRequest
     * @param allowedRoles Array of allowed role names
     * @return true if user has at least one allowed role
     */
    public static boolean checkRole(HttpServletRequest request, String[] allowedRoles) {
        HttpSession session = request.getSession(false);
        return hasAnyRole(session, allowedRoles);
    }
    
    /**
     * Validate session and role, redirect if invalid
     * @param request HttpServletRequest (to get context path)
     * @param response HttpServletResponse
     * @param requiredRole The role required
     * @return true if valid, false if redirected
     */
    public static boolean validateSessionAndRole(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   String requiredRole) throws IOException {
        HttpSession session = request.getSession(false);
        
        if (!isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please login first");
            return false;
        }
        
        if (requiredRole != null && !hasRole(session, requiredRole)) {
            response.sendRedirect(request.getContextPath() + "/login?error=Access denied");
            return false;
        }
        
        return true;
    }
}