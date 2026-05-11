package filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import util.SessionValidator;

/**
 * Role-based access control filter.
 * Module 1 & Non-functional: Protects pages based on user roles.
 * 
 * Usage in web.xml:
 * <filter-mapping>
 *     <filter-name>RoleFilter</filter-name>
 *     <url-pattern>/officer/*</url-pattern>
 *     <init-param>
 *         <param-name>roles</param-name>
 *         <param-value>PROCUREMENT_OFFICER</param-value>
 *     </init-param>
 * </filter-mapping>
 */
public class RoleFilter implements Filter {
    
    private String[] allowedRoles;
    private String filterName;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        filterName = filterConfig.getFilterName();
        String roles = filterConfig.getInitParameter("roles");
        
        if (roles != null && !roles.trim().isEmpty()) {
            allowedRoles = roles.split(",");
            // Trim whitespace from each role
            for (int i = 0; i < allowedRoles.length; i++) {
                allowedRoles[i] = allowedRoles[i].trim();
            }
            System.out.println("[RoleFilter] " + filterName + " initialized for roles: " + roles);
        } else {
            System.out.println("[RoleFilter] " + filterName + " initialized with no role restriction");
        }
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        // Check if user is authenticated
        if (!SessionValidator.isAuthenticated(req)) {
            // User not logged in - redirect to login with message
            resp.sendRedirect(req.getContextPath() + "/login?error=Please login to access this page");
            return;
        }
        
        // Check role restrictions if any
        if (allowedRoles != null && allowedRoles.length > 0) {
            boolean hasRequiredRole = SessionValidator.checkRole(req, allowedRoles);
            
            if (!hasRequiredRole) {
                // User logged in but doesn't have required role
                // Redirect to access denied page
                String accessDeniedPath = req.getContextPath() + "/jsp/accessDenied.jsp";
                resp.sendRedirect(accessDeniedPath);
                return;
            }
        }
        
        // All checks passed - proceed to requested resource
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
       
        System.out.println("[RoleFilter] " + filterName + " destroyed");
    }
}