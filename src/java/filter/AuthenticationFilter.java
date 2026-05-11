
package filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Authentication Filter - Checks if user is logged in
 * Protects all restricted pages
 * Module 1: Protected pages security requirement
 */

public class AuthenticationFilter implements Filter {
    
    private List<String> excludedUrls;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // URLs that don't require authentication
        excludedUrls = Arrays.asList(
            "/login", "/register", "/logout", "/css/", "/js/"
        );
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());
        
        // Check if current URL is excluded from authentication
        boolean isExcluded = false;
        for (String excluded : excludedUrls) {
            if (path.startsWith(excluded)) {
                isExcluded = true;
                break;
            }
        }
        
        if (isExcluded) {
            // Allow access to login, register, and static resources
            chain.doFilter(request, response);
            return;
        }
        
        // Check if user is logged in
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);
        
        if (isLoggedIn) {
            // Prevent caching of authenticated pages (helps block Back-button viewing after logout)
            httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setDateHeader("Expires", 0);
            httpResponse.setHeader("ETag", "authenticated");

            chain.doFilter(request, response);
        } else {
            // Not authenticated - redirect to login with access denied message
            httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setDateHeader("Expires", 0);
            httpResponse.sendRedirect(contextPath + "/login?error=Please login to access this page");
        }
    }
    
    @Override
    public void destroy() {
        
    }
}