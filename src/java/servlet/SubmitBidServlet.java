package servlet;

import dao.TenderDAOImpl;
import dao.BidDAOImpl;
import dao.TenderDAO;
import dao.BidDAO;
import model.Tender;
import model.Bid;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Submit Bid Servlet — fully hardened.
 *
 * Security rules enforced server-side (not trusting the client):
 *  1. SUPPLIER role check (internal, not just filter)
 *  2. Tender must be Open and deadline not passed
 *  3. One bid per supplier per tender (DB + Java layer)
 *  4. Bid amount > 0 and within reasonable range
 *  5. Technical statement: 10–600 chars, not empty
 *  6. Delivery timeline: 1–3650 days
 *  7. File: PDF only (content-type + extension), max 5 MB
 *  8. Safe file name (no path traversal)
 */
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,       // 1 MB
    maxFileSize       = 1024 * 1024 * 5,   // 5 MB — stricter than before
    maxRequestSize    = 1024 * 1024 * 6    // 6 MB
)
public class SubmitBidServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SubmitBidServlet.class.getName());

    // Allowed MIME types and extensions
    private static final Set<String> ALLOWED_MIME = new HashSet<>(Arrays.asList(
        "application/pdf"
    ));
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
        ".pdf"
    ));
    private static final long MAX_FILE_BYTES = 5L * 1024 * 1024; // 5 MB

    private TenderDAO tenderDAO;
    private BidDAO    bidDAO;
    private String    uploadDirectory;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO    = new BidDAOImpl();

        uploadDirectory = getServletContext().getInitParameter("upload.directory");
        if (uploadDirectory == null) {
            uploadDirectory = System.getProperty("catalina.base") + File.separator + "procuregov-uploads";
        }

        File bidDir = new File(uploadDirectory, "bids");
        if (!bidDir.exists()) bidDir.mkdirs();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // ── 1. Internal role guard ──────────────────────────────────────────
        if (!SessionValidator.hasRole(session, "SUPPLIER")) {
            response.sendRedirect(request.getContextPath() + "/login?error=Access denied");
            return;
        }
        User supplier = SessionValidator.getLoggedInUser(session);

        // ── 2. Safe tender ID parse ─────────────────────────────────────────
        int tenderId;
        try {
            tenderId = Integer.parseInt(request.getParameter("tenderId"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/supplier/dashboard?error=Invalid tender");
            return;
        }

        // ── 3. Tender status + deadline enforcement ─────────────────────────
        Tender tender = tenderDAO.findById(tenderId);

        if (tender == null) {
            response.sendRedirect(request.getContextPath() + "/supplier/dashboard?error=Tender not found");
            return;
        }
        if (!"Open".equals(tender.getStatus())) {
            response.sendRedirect(request.getContextPath() + "/supplier/dashboard?error=This tender is no longer open for bidding");
            return;
        }
        if (LocalDateTime.now().isAfter(tender.getSubmissionDeadline())) {
            // Auto-close the expired tender as a side effect
            tenderDAO.autoCloseExpiredTenders();
            response.sendRedirect(request.getContextPath() + "/supplier/dashboard?error=The submission deadline has passed");
            return;
        }

        // ── 4. One-bid-per-tender enforcement ──────────────────────────────
        if (bidDAO.hasSupplierBid(tenderId, supplier.getUserId())) {
            response.sendRedirect(request.getContextPath() + "/tender/details?tenderId=" + tenderId +
                                  "&error=You have already submitted a bid for this tender");
            return;
        }

        // ── 5. Collect and validate form fields ────────────────────────────
        String bidAmountStr        = sanitize(request.getParameter("bidAmount"));
        String technicalStatement  = sanitize(request.getParameter("technicalStatement"));
        String deliveryTimelineStr = sanitize(request.getParameter("deliveryTimeline"));
        Part   filePart            = request.getPart("supportingDoc");

        StringBuilder errors = new StringBuilder();

        // Bid amount
        BigDecimal bidAmount = null;
        try {
            bidAmount = new BigDecimal(bidAmountStr);
            if (bidAmount.compareTo(BigDecimal.ONE) < 0) {
                errors.append("Bid amount must be at least 1 Maloti.<br>");
            } else if (bidAmount.compareTo(new BigDecimal("999999999")) > 0) {
                errors.append("Bid amount is unreasonably large.<br>");
            }
        } catch (NumberFormatException e) {
            errors.append("Bid amount is required and must be a valid number.<br>");
        }

        // Technical statement
        if (technicalStatement.isEmpty()) {
            errors.append("Technical compliance statement is required.<br>");
        } else if (technicalStatement.length() < 10) {
            errors.append("Technical statement must be at least 10 characters.<br>");
        } else if (technicalStatement.length() > 600) {
            errors.append("Technical statement must not exceed 600 characters.<br>");
        }

        // Delivery timeline
        int deliveryTimeline = 0;
        try {
            deliveryTimeline = Integer.parseInt(deliveryTimelineStr);
            if (deliveryTimeline < 1) {
                errors.append("Delivery timeline must be at least 1 day.<br>");
            } else if (deliveryTimeline > 3650) {
                errors.append("Delivery timeline cannot exceed 3,650 days (10 years).<br>");
            }
        } catch (NumberFormatException e) {
            errors.append("Delivery timeline is required and must be a whole number of days.<br>");
        }

        // Supporting document — PDF only, max 5 MB
        String safeFileName = null;
        String filePath     = null;

        if (filePart == null || filePart.getSize() == 0) {
            errors.append("A supporting document (PDF) is required.<br>");
        } else {
            if (filePart.getSize() > MAX_FILE_BYTES) {
                errors.append("File size must not exceed 5 MB.<br>");
            }

            String contentType = filePart.getContentType() != null
                    ? filePart.getContentType().toLowerCase() : "";
            String originalName = extractFileName(filePart);
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase() : "";

            if (!ALLOWED_MIME.contains(contentType) || !ALLOWED_EXT.contains(ext)) {
                errors.append("Only PDF files are accepted for supporting documents.<br>");
            } else {
                // Build a safe, unique file name — no original path segments
                String baseName = originalName != null
                        ? originalName.replaceAll("[^a-zA-Z0-9._-]", "_") : "doc";
                safeFileName = System.currentTimeMillis() + "_" + supplier.getUserId() + "_" + baseName;
                filePath     = "bids" + File.separator + safeFileName;
            }
        }

        // ── 6. If any errors, bounce back to tender detail ──────────────────
        if (errors.length() > 0) {
            response.sendRedirect(request.getContextPath() + "/tender/details?tenderId=" + tenderId +
                                  "&error=" + java.net.URLEncoder.encode(errors.toString(), "UTF-8"));
            return;
        }

        // ── 7. Save file (only after all validation passes) ─────────────────
        File dest = new File(uploadDirectory, filePath);
        dest.getParentFile().mkdirs();
        filePart.write(dest.getAbsolutePath());
        logger.info("Bid document saved: " + dest.getAbsolutePath());

        // ── 8. Persist bid ──────────────────────────────────────────────────
        Bid bid = new Bid();
        bid.setTenderId(tenderId);
        bid.setSupplierId(supplier.getUserId());
        bid.setBidAmount(bidAmount);
        bid.setTechnicalStatement(technicalStatement);
        bid.setDeliveryTimeline(deliveryTimeline);
        bid.setSupportingDocPath(filePath);
        bid.setActive(true);

        if (bidDAO.createBid(bid)) {
            logger.info("Bid submitted: tender=" + tenderId + " supplier=" + supplier.getUsername());
            response.sendRedirect(request.getContextPath() +
                    "/supplier/dashboard?success=Your bid was submitted successfully for " + tender.getReferenceNumber());
        } else {
            // Roll back saved file if DB write fails
            dest.delete();
            logger.severe("DB error saving bid: tender=" + tenderId + " supplier=" + supplier.getUsername());
            response.sendRedirect(request.getContextPath() + "/tender/details?tenderId=" + tenderId +
                                  "&error=Failed to save your bid. Please try again.");
        }
    }

    private String sanitize(String value) {
        return value == null ? "" : value.trim();
    }

    private String extractFileName(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return "document.pdf";
        for (String token : cd.split(";")) {
            if (token.trim().startsWith("filename")) {
                String name = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                // Strip any directory components
                int lastSep = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
                return lastSep >= 0 ? name.substring(lastSep + 1) : name;
            }
        }
        return "document.pdf";
    }
}
