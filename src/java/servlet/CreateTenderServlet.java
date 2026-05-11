package servlet;

import dao.TenderDAOImpl;
import dao.TenderDAO;
import model.Tender;
import model.User;
import util.SessionValidator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

/**
 * Create Tender Servlet — PROCUREMENT_OFFICER only.
 * Hardened: internal role guard, PDF-only, path traversal prevention,
 * file rollback on DB failure, deadline must be at least 24h in future.
 */
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize       = 1024 * 1024 * 5,
    maxRequestSize    = 1024 * 1024 * 6
)
public class CreateTenderServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(CreateTenderServlet.class.getName());
    private TenderDAO tenderDAO;
    private String uploadDirectory;

    @Override public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        uploadDirectory = getServletContext().getInitParameter("upload.directory");
        if (uploadDirectory == null)
            uploadDirectory = System.getProperty("catalina.base") + File.separator + "procuregov-uploads";
        new File(uploadDirectory, "notices").mkdirs();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!SessionValidator.hasRole(req.getSession(false), "PROCUREMENT_OFFICER")) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }
        req.getRequestDispatcher("/jsp/createTender.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!SessionValidator.hasRole(session, "PROCUREMENT_OFFICER")) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }
        User officer = SessionValidator.getLoggedInUser(session);

        String title       = sanitize(req.getParameter("title"));
        String category    = sanitize(req.getParameter("category"));
        String description = sanitize(req.getParameter("description"));
        String valueStr    = sanitize(req.getParameter("estimatedValue"));
        String deadlineStr = sanitize(req.getParameter("submissionDeadline"));

        StringBuilder errors = new StringBuilder();

        if (title.isEmpty() || title.length() < 5)    errors.append("Title is required (min 5 characters).<br>");
        else if (title.length() > 200)                 errors.append("Title must not exceed 200 characters.<br>");

        String[] validCats = {"Construction","Roads","Electrical","Plumbing","General Services"};
        if (!isValidCategory(category, validCats))    errors.append("Please select a valid category.<br>");

        if (description.isEmpty() || description.length() < 20) errors.append("Description is required (min 20 characters).<br>");
        else if (description.length() > 2000)          errors.append("Description must not exceed 2,000 characters.<br>");

        BigDecimal estimatedValue = null;
        try {
            estimatedValue = new BigDecimal(valueStr);
            if (estimatedValue.compareTo(BigDecimal.ONE) < 0)
                errors.append("Estimated value must be at least 1 Maloti.<br>");
            if (estimatedValue.compareTo(new BigDecimal("9999999999")) > 0)
                errors.append("Estimated value is unreasonably large.<br>");
        } catch (NumberFormatException e) { errors.append("Estimated value must be a valid number.<br>"); }

        LocalDateTime deadline = null;
        try {
            deadline = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            if (!deadline.isAfter(LocalDateTime.now().plusHours(24)))
                errors.append("Submission deadline must be at least 24 hours in the future.<br>");
        } catch (DateTimeParseException e) { errors.append("Invalid deadline format.<br>"); }

        // File validation
        Part filePart = req.getPart("noticeFile");
        String filePath = null;
        File savedFile  = null;

        if (filePart == null || filePart.getSize() == 0) {
            errors.append("Tender notice PDF is required.<br>");
        } else {
            String ct  = filePart.getContentType() != null ? filePart.getContentType().toLowerCase() : "";
            String orig = extractFileName(filePart);
            String ext  = orig != null && orig.contains(".") ? orig.substring(orig.lastIndexOf('.')).toLowerCase() : "";
            if (!"application/pdf".equals(ct) || !".pdf".equals(ext))
                errors.append("Only PDF files are accepted for the tender notice.<br>");
            else if (filePart.getSize() > 5L * 1024 * 1024)
                errors.append("Notice PDF must not exceed 5 MB.<br>");
        }

        if (errors.length() > 0) {
            req.setAttribute("errors", errors.toString());
            req.setAttribute("title", title); req.setAttribute("category", category);
            req.setAttribute("description", description); req.setAttribute("estimatedValue", valueStr);
            req.setAttribute("submissionDeadline", deadlineStr);
            req.getRequestDispatcher("/jsp/createTender.jsp").forward(req, res); return;
        }

        // Save file
        String orig     = extractFileName(filePart);
        String safeName = System.currentTimeMillis() + "_" + orig.replaceAll("[^a-zA-Z0-9._-]", "_");
        filePath        = "notices" + File.separator + safeName;
        savedFile       = new File(uploadDirectory, filePath);
        filePart.write(savedFile.getAbsolutePath());

        Tender tender = new Tender();
        tender.setReferenceNumber(tenderDAO.generateReferenceNumber());
        tender.setTitle(title); tender.setCategory(category); tender.setDescription(description);
        tender.setEstimatedValue(estimatedValue); tender.setSubmissionDeadline(deadline);
        tender.setStatus("Draft");
        tender.setNoticeFilePath("notices/" + safeName);
        tender.setCreatedBy(officer.getUserId());

        if (tenderDAO.createTender(tender)) {
            logger.info("Tender created: " + tender.getReferenceNumber() + " by " + officer.getUsername());
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?success=Tender " + tender.getReferenceNumber() + " created successfully");
        } else {
            if (savedFile != null) savedFile.delete(); // rollback
            req.setAttribute("errors", "Failed to save tender. Please try again.");
            req.setAttribute("title", title); req.setAttribute("category", category);
            req.setAttribute("description", description); req.setAttribute("estimatedValue", valueStr);
            req.setAttribute("submissionDeadline", deadlineStr);
            req.getRequestDispatcher("/jsp/createTender.jsp").forward(req, res);
        }
    }

    private String sanitize(String v) { return v == null ? "" : v.trim(); }
    private boolean isValidCategory(String cat, String[] valid) {
        for (String v : valid) if (v.equals(cat)) return true; return false;
    }
    private String extractFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return "notice.pdf";
        for (String t : cd.split(";")) {
            if (t.trim().startsWith("filename")) {
                String n = t.substring(t.indexOf('=') + 1).trim().replace("\"", "");
                int s = Math.max(n.lastIndexOf('/'), n.lastIndexOf('\\'));
                return s >= 0 ? n.substring(s + 1) : n;
            }
        }
        return "notice.pdf";
    }
}
