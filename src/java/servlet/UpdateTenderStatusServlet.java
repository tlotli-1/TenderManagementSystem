package servlet;

import dao.TenderDAOImpl;
import dao.TenderDAO;
import model.Tender;
import util.SessionValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Update Tender Status Servlet — enforces the strict lifecycle:
 * Draft → Open → Closed → Under_Evaluation → Evaluated → Awarded
 *
 * Hardened: internal role guard, safe ID parse, validated transitions only.
 * Awarded status is final — no further changes allowed.
 */
public class UpdateTenderStatusServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(UpdateTenderStatusServlet.class.getName());
    private TenderDAO tenderDAO;

    @Override public void init() throws ServletException { tenderDAO = new TenderDAOImpl(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!SessionValidator.hasRole(session, "PROCUREMENT_OFFICER")) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }

        int tenderId;
        try { tenderId = Integer.parseInt(req.getParameter("tenderId")); }
        catch (NumberFormatException e) { res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Invalid tender ID"); return; }

        String newStatus = sanitize(req.getParameter("status"));

        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) { res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Tender not found"); return; }

        // Strict lifecycle enforcement
        String error = validateTransition(tender.getStatus(), newStatus);
        if (error != null) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=" +
                java.net.URLEncoder.encode(error, "UTF-8")); return;
        }

        boolean success;
        Date now = new Date();
        switch (newStatus) {
            case "Closed": case "Evaluated": case "Awarded":
                success = tenderDAO.updateTenderStatusWithDate(tenderId, newStatus, now); break;
            default:
                success = tenderDAO.updateTenderStatus(tenderId, newStatus);
        }

        if (success) {
            logger.info("Tender " + tender.getReferenceNumber() + ": " + tender.getStatus() + " → " + newStatus);
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?success=" +
                java.net.URLEncoder.encode("Tender " + tender.getReferenceNumber() + " moved to " + newStatus, "UTF-8"));
        } else {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Failed to update tender status");
        }
    }

    /**
     * Enforces the one-way lifecycle. Only the exact next step is allowed.
     */
    private String validateTransition(String current, String requested) {
        switch (current) {
            case "Draft":            if ("Open".equals(requested))             return null; break;
            case "Open":             if ("Closed".equals(requested))           return null; break;
            case "Closed":           if ("Under_Evaluation".equals(requested)) return null; break;
            case "Under_Evaluation": if ("Evaluated".equals(requested))        return null; break;
            case "Evaluated":        if ("Awarded".equals(requested))          return null; break;
            case "Awarded": return "Awarded tenders are final and cannot be changed.";
            default: return "Unknown current status: " + current;
        }
        return "Invalid transition: " + current + " → " + requested +
               ". The correct lifecycle is: Draft → Open → Closed → Under_Evaluation → Evaluated → Awarded.";
    }

    private String sanitize(String v) { return v == null ? "" : v.trim(); }
}
