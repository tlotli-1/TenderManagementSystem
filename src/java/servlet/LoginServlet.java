package servlet;

import dao.UserDAOImpl;
import dao.UserDAO;
import model.User;
import util.PasswordHasher;
import util.SessionValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Login Servlet — Module 1: Authentication & Session Management.
 *
 * Hardened:
 *  - Session fixation protection: old session invalidated, new one created on login
 *  - Session timeout set to 30 minutes
 *  - Both session-level AND DB-level lockout enforced
 *  - Input sanitised (trim, null-safe)
 *  - Already-logged-in users redirected to their dashboard immediately
 *  - Account lockout message is generic to avoid username enumeration
 */
public class LoginServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());
    private static final int MAX_ATTEMPTS = 3;
    private UserDAO userDAO;

    @Override public void init() throws ServletException { userDAO = new UserDAOImpl(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (SessionValidator.isLoggedIn(session)) {
            redirectToDashboard(SessionValidator.getLoggedInUser(session), req, res); return;
        }
        req.getRequestDispatcher("/jsp/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String username = sanitize(req.getParameter("username"));
        String password = req.getParameter("password"); // never trim passwords

        if (username.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("error", "Username and password are required");
            req.getRequestDispatcher("/jsp/login.jsp").forward(req, res); return;
        }

        // Use a NEW session to count session-level attempts (avoids fixation)
        HttpSession session = req.getSession(true);

        Integer sessionAttempts = (Integer) session.getAttribute("failedAttempts");
        if (sessionAttempts == null) sessionAttempts = 0;

        Boolean sessionLocked = (Boolean) session.getAttribute("accountLocked");
        if (Boolean.TRUE.equals(sessionLocked)) {
            req.setAttribute("error", "Too many failed attempts. Please close your browser and try again.");
            req.getRequestDispatcher("/jsp/login.jsp").forward(req, res); return;
        }

        User user = userDAO.findByUsername(username);

        // Unknown username — increment session counter (generic message, no username enumeration)
        if (user == null) {
            sessionAttempts++;
            session.setAttribute("failedAttempts", sessionAttempts);
            if (sessionAttempts >= MAX_ATTEMPTS) session.setAttribute("accountLocked", true);
            req.setAttribute("error", buildAttemptMessage(sessionAttempts));
            req.getRequestDispatcher("/jsp/login.jsp").forward(req, res); return;
        }

        // DB-level lockout (time-based unlock)
        if (user.isAccountLocked()) {
            java.util.Date lockedUntil = userDAO.getLockedUntil(user.getUserId());
            // If lockedUntil exists and is in the past, auto-unlock
            if (lockedUntil != null && System.currentTimeMillis() > lockedUntil.getTime()) {
                userDAO.setAccountLockWithUnlockTime(user.getUserId(), false, null);
                userDAO.resetFailedAttempts(user.getUserId());
            } else {
                long remainingSeconds = 0;
                if (lockedUntil != null) {
                    long diffMs = lockedUntil.getTime() - System.currentTimeMillis();
                    remainingSeconds = Math.max(0, (diffMs + 999) / 1000); // ceil
                }

                if (lockedUntil != null && remainingSeconds > 0) {
                    req.setAttribute("error", "Your account is temporarily locked. It will be unlocked after " + remainingSeconds + " second(s). Please try again.");
                } else {
                    req.setAttribute("error", "Your account is locked. Please contact the system administrator.");
                }
                req.getRequestDispatcher("/jsp/login.jsp").forward(req, res); return;
            }
        }



        if (PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
            // ── SUCCESSFUL LOGIN ──────────────────────────────────────────────
            // Session fixation protection: invalidate old session, create fresh one
            session.invalidate();
            HttpSession newSession = req.getSession(true);
            newSession.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Reset failure counters
            userDAO.resetFailedAttempts(user.getUserId());
            userDAO.updateLastLogin(user.getUserId());

            // Populate session
            newSession.setAttribute("user",     user);
            newSession.setAttribute("role",     user.getRole());
            newSession.setAttribute("userId",   user.getUserId());
            newSession.setAttribute("username", user.getUsername());

            logger.info("Login: " + username + " [" + user.getRole() + "]");
            redirectToDashboard(user, req, res);

        } else {
            // ── FAILED PASSWORD ───────────────────────────────────────────────
            sessionAttempts++;
            session.setAttribute("failedAttempts", sessionAttempts);

            int dbAttempts = user.getFailedAttempts() + 1;
            userDAO.updateFailedAttempts(user.getUserId(), dbAttempts);

            if (dbAttempts >= MAX_ATTEMPTS || sessionAttempts >= MAX_ATTEMPTS) {
                // Temporarily lock the account for 2 minutes (per assessment requirement)
                long unlockAtMillis = System.currentTimeMillis() + (2L * 60L * 1000L);
                java.util.Date unlockAt = new java.util.Date(unlockAtMillis);

                userDAO.setAccountLockWithUnlockTime(
                        user.getUserId(),
                        true,
                        unlockAt
                );

                session.setAttribute("accountLocked", true);
                req.setAttribute("error", "Too many failed attempts. Your account is temporarily locked for 2 minute(s).");
            } else {
                req.setAttribute("error", buildAttemptMessage(sessionAttempts));
            }
            req.getRequestDispatcher("/jsp/login.jsp").forward(req, res);
        }
    }

    private void redirectToDashboard(User user, HttpServletRequest req, HttpServletResponse res) throws IOException {
        String cp = req.getContextPath();
        switch (user.getRole()) {
            case "SUPPLIER":             res.sendRedirect(cp + "/supplier/dashboard");  break;
            case "PROCUREMENT_OFFICER":  res.sendRedirect(cp + "/officer/tenderList");  break;
            case "EVALUATION_COMMITTEE": res.sendRedirect(cp + "/evaluation/panel");    break;
            default:                     res.sendRedirect(cp + "/login");
        }
    }

    private String buildAttemptMessage(int attempts) {
        int remaining = MAX_ATTEMPTS - attempts;
        if (remaining <= 0) return "Account locked due to too many failed attempts.";
        return "Invalid username or password. " + remaining + " attempt(s) remaining before lockout.";
    }

    private String sanitize(String v) { return v == null ? "" : v.trim(); }
}
