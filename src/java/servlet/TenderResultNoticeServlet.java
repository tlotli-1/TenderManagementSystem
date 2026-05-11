package servlet;

import dao.TenderDAOImpl;
import dao.BidDAOImpl;
import dao.EvaluationDAOImpl;
import dao.TenderDAO;
import dao.BidDAO;
import dao.EvaluationDAO;
import model.Bid;
import model.Tender;
import util.SessionValidator;
import service.EmailNotificationService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Tender Result Notice Servlet — PROCUREMENT_OFFICER only.
 *
 * GET: Shows the ranked bid table before sending notices.
 * POST: Sends result notice emails to all bidding suppliers.
 *
 * Hardened:
 *  - Internal role guard on both GET and POST
 *  - Safe tenderId parse
 *  - Tender must be Evaluated or Awarded
 *  - action parameter validated against whitelist
 *  - Email failures are logged but don't crash the response
 *  - Scoring formula in JSP matches the actual formula used in ScoreCalculationService
 */
public class TenderResultNoticeServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(TenderResultNoticeServlet.class.getName());
    private TenderDAO     tenderDAO;
    private BidDAO        bidDAO;
    private EvaluationDAO evaluationDAO;

    @Override public void init() throws ServletException {
        tenderDAO     = new TenderDAOImpl();
        bidDAO        = new BidDAOImpl();
        evaluationDAO = new EvaluationDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!SessionValidator.hasRole(session, "PROCUREMENT_OFFICER")) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }

        Tender tender = loadAndVerify(req, res); if (tender == null) return;

        List<Bid> rankedBids = evaluationDAO.getBidsWithFinalScores(tender.getTenderId());
        req.setAttribute("tender",     tender);
        req.setAttribute("rankedBids", rankedBids);
        req.getRequestDispatcher("/jsp/tenderResultNotice.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!SessionValidator.hasRole(session, "PROCUREMENT_OFFICER")) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }

        Tender tender = loadAndVerify(req, res); if (tender == null) return;

        // Validate action parameter against whitelist
        String action = req.getParameter("action");
        if (!"sendNotices".equals(action)) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Invalid action"); return;
        }

        List<Bid> allBids = bidDAO.getBidsByTender(tender.getTenderId());
        EmailNotificationService emailService = new EmailNotificationService();
        int sent = 0;
        for (Bid bid : allBids) {
            try {
                if (emailService.sendTenderResultNotice(tender.getTenderId(), bid.getSupplierId())) sent++;
            } catch (Exception e) {
                logger.warning("Result notice failed for supplier " + bid.getSupplierId() + ": " + e.getMessage());
            }
        }
        logger.info("Result notices sent: " + sent + "/" + allBids.size() + " for tender " + tender.getTenderId());
        res.sendRedirect(req.getContextPath() + "/officer/tenderList?success=" +
            java.net.URLEncoder.encode(sent + " of " + allBids.size() + " result notices sent successfully", "UTF-8"));
    }

    /** Shared load-and-verify logic for GET and POST */
    private Tender loadAndVerify(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int tenderId;
        try { tenderId = Integer.parseInt(req.getParameter("tenderId")); }
        catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Invalid tender"); return null;
        }
        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Tender not found"); return null;
        }
        if (!"Evaluated".equals(tender.getStatus()) && !"Awarded".equals(tender.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=" +
                java.net.URLEncoder.encode("Result notices can only be sent after evaluation completes (status: " + tender.getStatus() + ")", "UTF-8")); return null;
        }
        return tender;
    }
}
