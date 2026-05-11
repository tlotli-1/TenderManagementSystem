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
import java.util.Date;

/**
 * Edit Tender Servlet — PROCUREMENT_OFFICER, DRAFT status only.
 * Hardened: internal role + status guard, safe file name, PDF-only, file rollback.
 */
@MultipartConfig(fileSizeThreshold=1024*1024, maxFileSize=1024*1024*5, maxRequestSize=1024*1024*6)
public class EditTenderServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(EditTenderServlet.class.getName());
    private TenderDAO tenderDAO;
    private String uploadDirectory;

    @Override public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        uploadDirectory = getServletContext().getInitParameter("upload.directory");
        if (uploadDirectory == null)
            uploadDirectory = System.getProperty("catalina.base") + File.separator + "procuregov-uploads";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!SessionValidator.hasRole(session, "PROCUREMENT_OFFICER")) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }
        int tenderId;
        try { tenderId = Integer.parseInt(req.getParameter("tenderId")); }
        catch (NumberFormatException e) { res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Invalid tender"); return; }

        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) { res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Tender not found"); return; }
        if (!"Draft".equals(tender.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Only Draft tenders can be edited"); return;
        }
        
        // Pre-format datetime for JSP datetime-local input
        String formattedDeadline = "";
        if (tender.getSubmissionDeadline() != null) {
            formattedDeadline = tender.getSubmissionDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        }
        req.setAttribute("tender", tender);
        req.setAttribute("formattedSubmissionDeadline", formattedDeadline);
        req.getRequestDispatcher("/jsp/editTender.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!SessionValidator.hasRole(session, "PROCUREMENT_OFFICER")) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }

        int tenderId;
        try { tenderId = Integer.parseInt(req.getParameter("tenderId")); }
        catch (NumberFormatException e) { res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Invalid tender"); return; }

        Tender existing = tenderDAO.findById(tenderId);
        if (existing == null || !"Draft".equals(existing.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Cannot edit this tender — not in Draft status"); return;
        }

        String title       = sanitize(req.getParameter("title"));
        String category    = sanitize(req.getParameter("category"));
        String description = sanitize(req.getParameter("description"));
        String valueStr    = sanitize(req.getParameter("estimatedValue"));
        String deadlineStr = sanitize(req.getParameter("submissionDeadline"));

        StringBuilder errors = new StringBuilder();
        if (title.isEmpty() || title.length() < 5) errors.append("Title is required (min 5 characters).<br>");
        else if (title.length() > 200)              errors.append("Title must not exceed 200 characters.<br>");
        if (description.isEmpty())                  errors.append("Description is required.<br>");

        BigDecimal estimatedValue = null;
        try {
            estimatedValue = new BigDecimal(valueStr);
            if (estimatedValue.compareTo(BigDecimal.ONE) < 0) errors.append("Estimated value must be at least 1.<br>");
        } catch (NumberFormatException e) { errors.append("Estimated value must be a valid number.<br>"); }

        LocalDateTime deadline = null;
        try {
            deadline = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            if (!deadline.isAfter(LocalDateTime.now().plusHours(1)))
                errors.append("Deadline must be at least 1 hour in the future.<br>");
        } catch (DateTimeParseException e) { errors.append("Invalid deadline format.<br>"); }

        // Optional file replacement
        Part filePart = req.getPart("noticeFile");
        String filePath = existing.getNoticeFilePath();
        File newSavedFile = null;

        if (filePart != null && filePart.getSize() > 0) {
            String ct  = filePart.getContentType() != null ? filePart.getContentType().toLowerCase() : "";
            String orig = extractFileName(filePart);
            String ext  = orig != null && orig.contains(".") ? orig.substring(orig.lastIndexOf('.')).toLowerCase() : "";
            if (!"application/pdf".equals(ct) || !".pdf".equals(ext))
                errors.append("Replacement notice must be a PDF file.<br>");
            else if (filePart.getSize() > 5L * 1024 * 1024)
                errors.append("Replacement PDF must not exceed 5 MB.<br>");
        }

        if (errors.length() > 0) {
            req.setAttribute("errors", errors.toString()); req.setAttribute("tender", existing);
            req.getRequestDispatcher("/jsp/editTender.jsp").forward(req, res); return;
        }

        if (filePart != null && filePart.getSize() > 0) {
            String orig     = extractFileName(filePart);
            String safeName = System.currentTimeMillis() + "_" + orig.replaceAll("[^a-zA-Z0-9._-]", "_");
            filePath        = "notices/" + safeName;
            newSavedFile    = new File(uploadDirectory, "notices" + File.separator + safeName);
            filePart.write(newSavedFile.getAbsolutePath());
            // Delete old file
            if (existing.getNoticeFilePath() != null) {
                new File(uploadDirectory, existing.getNoticeFilePath().replace("/", File.separator)).delete();
            }
        }

        existing.setTitle(title); existing.setCategory(category); existing.setDescription(description);
        existing.setEstimatedValue(estimatedValue); existing.setSubmissionDeadline(deadline);
        existing.setNoticeFilePath(filePath);

        if (tenderDAO.updateTender(existing)) {
            logger.info("Tender updated: " + existing.getReferenceNumber());
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?success=Tender " + existing.getReferenceNumber() + " updated successfully");
        } else {
            if (newSavedFile != null) newSavedFile.delete();
            req.setAttribute("errors", "Failed to update tender. Please try again.");
            req.setAttribute("tender", existing);
            req.getRequestDispatcher("/jsp/editTender.jsp").forward(req, res);
        }
    }

    private String sanitize(String v) { return v == null ? "" : v.trim(); }
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
