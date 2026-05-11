package servlet;

import dao.BidDAOImpl;
import dao.TenderDAOImpl;
import dao.BidDAO;
import dao.TenderDAO;
import model.Bid;
import model.Tender;
import model.User;
import util.SessionValidator;
import service.EmailNotificationService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.logging.Logger;
import util.DBConnectionPool;

/**
 * Award Contract Servlet — PROCUREMENT_OFFICER only, Evaluated status only.
 *
 * Hardened:
 * - Internal role guard
 * - Status guard: tender must be Evaluated
 * - Winning bid must belong to the correct tender (prevents ID injection)
 * - Award + tender-status update wrapped in a single DB transaction
 * - Email notifications are non-blocking (failure doesn't cancel the award)
 */
public class AwardContractServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(AwardContractServlet.class.getName());
    private TenderDAO tenderDAO;
    private BidDAO    bidDAO;

    @Override public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO    = new BidDAOImpl();
    }

    // ── GET: show award form ────────────────────────────────────────────────
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

        if (!"Evaluated".equals(tender.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Tender must be in Evaluated status before awarding"); return;
        }

        List<Bid> rankedBids = bidDAO.getRankedBidsByTender(tenderId);
        if (rankedBids == null || rankedBids.isEmpty()) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=No evaluated bids found for this tender"); return;
        }

        req.setAttribute("tender",     tender);
        req.setAttribute("rankedBids", rankedBids);
        req.getRequestDispatcher("/jsp/awardContract.jsp").forward(req, res);
    }

    // ── POST: process award ─────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!SessionValidator.hasRole(session, "PROCUREMENT_OFFICER")) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }
        User officer = SessionValidator.getLoggedInUser(session);

        int tenderId, winningBidId;
        try {
            tenderId     = Integer.parseInt(req.getParameter("tenderId"));
            winningBidId = Integer.parseInt(req.getParameter("winningBidId"));
        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Invalid form data"); return;
        }

        // Re-verify tender is still Evaluated (guard against concurrent changes)
        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null || !"Evaluated".equals(tender.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Tender is no longer in Evaluated status"); return;
        }

        String awardedValueStr = sanitize(req.getParameter("awardedValue"));
        String justification   = sanitize(req.getParameter("justification"));

        StringBuilder errors = new StringBuilder();
        BigDecimal awardedValue = null;
        try {
            awardedValue = new BigDecimal(awardedValueStr);
            if (awardedValue.compareTo(BigDecimal.ONE) < 0) errors.append("Awarded value must be at least 1 Maloti.<br>");
        } catch (NumberFormatException e) { errors.append("Awarded value must be a valid number.<br>"); }

        if (justification.isEmpty() || justification.length() < 10)
            errors.append("Justification must be at least 10 characters.<br>");
        else if (justification.length() > 1000)
            errors.append("Justification must not exceed 1,000 characters.<br>");

        // Verify the winning bid belongs to THIS tender (ID injection guard)
        Bid winningBid = bidDAO.findById(winningBidId);
        if (winningBid == null || winningBid.getTenderId() != tenderId) {
            errors.append("Invalid bid selection.<br>");
        }

        if (errors.length() > 0) {
            List<Bid> rankedBids = bidDAO.getRankedBidsByTender(tenderId);
            req.setAttribute("errors",        errors.toString());
            req.setAttribute("tender",        tender);
            req.setAttribute("rankedBids",    rankedBids);
            req.setAttribute("selectedBidId", winningBidId);
            req.setAttribute("awardedValue",  awardedValueStr);
            req.setAttribute("justification", justification);
            req.getRequestDispatcher("/jsp/awardContract.jsp").forward(req, res); return;
        }

        // Transactional: create award + update tender status atomically
        boolean success = false;
        Connection conn = null;
        try {
            conn = DBConnectionPool.getConnection();
            conn.setAutoCommit(false);


boolean awardCreated = bidDAO.createAward(
                tenderId, winningBidId, winningBid.getSupplierId(),
                awardedValue, justification, officer.getUserId());
logger.info("Award created: " + awardCreated);

            boolean statusUpdated = awardCreated &&
                tenderDAO.updateTenderStatusWithDate(tenderId, "Awarded", new java.util.Date());
logger.info("Status updated: " + statusUpdated);

            if (awardCreated && statusUpdated) {

                conn.commit();
                success = true;
                logger.info("Contract awarded: tender=" + tenderId +
                    " bid=" + winningBidId + " officer=" + officer.getUsername());
            } else {
                conn.rollback();
                logger.severe("Award transaction rolled back: tender=" + tenderId);
            }
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            logger.severe("Award transaction error: " + e.getMessage());
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception ignored) {}
        }

        if (success) {
            sendEmailNotifications(tenderId, winningBid);
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?success=" +
                java.net.URLEncoder.encode("Contract awarded successfully for " + tender.getReferenceNumber(), "UTF-8"));
        } else {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Failed to award contract. Please try again.");
        }
    }

    private void sendEmailNotifications(int tenderId, Bid winningBid) {
        try {
            new EmailNotificationService().sendAwardNotifications(tenderId, winningBid.getBidId());
        } catch (Exception e) {
            logger.warning("Email notifications failed (award still processed): " + e.getMessage());
        }
    }

    private String sanitize(String v) { return v == null ? "" : v.trim(); }
}
