package servlet;

import dao.UserDAOImpl;
import dao.UserDAO;
import model.User;
import util.PasswordHasher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Registration Servlet — Supplier account creation.
 * Hardened: input sanitization, strong validation, duplicate checks.
 */
public class RegistrationServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RegistrationServlet.class.getName());
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect already-logged-in users
        if (request.getSession(false) != null && request.getSession(false).getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/supplier/dashboard");
            return;
        }
        request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Sanitize: trim all strings, never null
        String username        = sanitize(request.getParameter("username"));
        String email           = sanitize(request.getParameter("email")).toLowerCase();
        String password        = request.getParameter("password");        // never trim passwords
        String confirmPassword = request.getParameter("confirmPassword");
        String fullName        = sanitize(request.getParameter("fullName"));
        String companyName     = sanitize(request.getParameter("companyName"));
        String contactNumber   = sanitize(request.getParameter("contactNumber"));
        String physicalAddress = sanitize(request.getParameter("physicalAddress"));

        // Preserve form values for re-display on error
        Map<String, String> form = new HashMap<>();
        form.put("username",        username);
        form.put("email",           email);
        form.put("fullName",        fullName);
        form.put("companyName",     companyName);
        form.put("contactNumber",   contactNumber);
        form.put("physicalAddress", physicalAddress);

        StringBuilder errors = new StringBuilder();

        // --- Username ---
        if (username.isEmpty()) {
            errors.append("Username is required.<br>");
        } else if (username.length() < 3 || username.length() > 50) {
            errors.append("Username must be 3–50 characters.<br>");
        } else if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
            errors.append("Username may only contain letters, numbers, underscores, hyphens and dots.<br>");
        } else if (userDAO.usernameExists(username)) {
            errors.append("Username already taken. Please choose another.<br>");
        }

        // --- Email ---
        if (email.isEmpty()) {
            errors.append("Email is required.<br>");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.append("Invalid email format.<br>");
        } else if (email.length() > 100) {
            errors.append("Email must not exceed 100 characters.<br>");
        } else if (userDAO.emailExists(email)) {
            errors.append("Email already registered. Please use a different email.<br>");
        }

        // --- Password ---
        if (password == null || password.isEmpty()) {
            errors.append("Password is required.<br>");
        } else if (password.length() < 8) {
            errors.append("Password must be at least 8 characters.<br>");
        } else if (!password.matches(".*[A-Z].*")) {
            errors.append("Password must contain at least one uppercase letter.<br>");
        } else if (!password.matches(".*[0-9].*")) {
            errors.append("Password must contain at least one number.<br>");
        } else if (!password.equals(confirmPassword)) {
            errors.append("Passwords do not match.<br>");
        }

        // --- Full name ---
        if (fullName.isEmpty()) {
            errors.append("Full name is required.<br>");
        } else if (fullName.length() < 2 || fullName.length() > 100) {
            errors.append("Full name must be 2–100 characters.<br>");
        }

        // --- Company name ---
        if (companyName.isEmpty()) {
            errors.append("Company name is required.<br>");
        } else if (companyName.length() < 2 || companyName.length() > 150) {
            errors.append("Company name must be 2–150 characters.<br>");
        }

        // --- Contact number ---
        if (contactNumber.isEmpty()) {
            errors.append("Contact number is required.<br>");
        } else if (!contactNumber.matches("^[+]?[0-9]{8,15}$")) {
            errors.append("Invalid contact number. Use 8–15 digits, optional + prefix.<br>");
        }

        // --- Physical address ---
        if (physicalAddress.isEmpty()) {
            errors.append("Physical address is required.<br>");
        } else if (physicalAddress.length() < 5 || physicalAddress.length() > 300) {
            errors.append("Physical address must be 5–300 characters.<br>");
        }

        // Return errors
        if (errors.length() > 0) {
            request.setAttribute("error", errors.toString());
            request.setAttribute("form", form);
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
            return;
        }

        // Build user
        String registrationNumber = userDAO.generateRegistrationNumber();
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(PasswordHasher.hashPassword(password));
        newUser.setFullName(fullName);
        newUser.setRole("SUPPLIER");
        newUser.setRegistrationNumber(registrationNumber);
        newUser.setCompanyName(companyName);
        newUser.setContactNumber(contactNumber);
        newUser.setPhysicalAddress(physicalAddress);
        newUser.setAccountLocked(false);
        newUser.setFailedAttempts(0);

        if (userDAO.createUser(newUser)) {
            logger.info("Supplier registered: " + username + " | " + registrationNumber + " | " + companyName);
            request.setAttribute("success", "Registration successful!");
            request.setAttribute("registrationNumber", registrationNumber);
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
        } else {
            logger.severe("DB error registering: " + username);
            request.setAttribute("error", "Registration failed due to a system error. Please try again.");
            request.setAttribute("form", form);
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
        }
    }

    /** Trim and return empty string if null */
    private String sanitize(String value) {
        return value == null ? "" : value.trim();
    }
}
