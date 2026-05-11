package servlet;

import dao.TenderDAOImpl;
import dao.BidDAOImpl;
import dao.EvaluationDAOImpl;
import dao.TenderDAO;
import dao.BidDAO;
import dao.EvaluationDAO;
import model.Evaluation;
import model.Tender;
import model.Bid;
import model.User;
import util.SessionValidator;
import service.ScoreCalculationService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

/**
 * Score Entry Servlet — displays bids for an evaluator to score.
 *
 * Hardened:
 * - Internal role guard
 * - Tender must be Closed or Under_Evaluation (not Open or Draft)
 * - Pre-fills existing scores if evaluator already scored a bid
 * - Uses isActive flag to indicate pending (true) vs already scored (false)
 */
public class ScoreEntryServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ScoreEntryServlet.class.getName());
    private TenderDAO          tenderDAO;
    private BidDAO             bidDAO;
    private EvaluationDAO      evaluationDAO;
    private ScoreCalculationService scoreService;

    @Override public void init() throws ServletException {
        tenderDAO     = new TenderDAOImpl();
        bidDAO        = new BidDAOImpl();
        evaluationDAO = new EvaluationDAOImpl();
        scoreService  = new ScoreCalculationService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (!SessionValidator.isLoggedIn(session)) {
            res.sendRedirect(req.getContextPath() + "/login?error=Please login first"); return;
        }

        User user = SessionValidator.getLoggedInUser(session);
        String role = user.getRole();

        if (!"PROCUREMENT_OFFICER".equals(role) && !"EVALUATION_COMMITTEE".equals(role)) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }

        int tenderId;
        try { tenderId = Integer.parseInt(req.getParameter("tenderId")); }
        catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/evaluation/panel?error=Invalid tender"); return;
        }

        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            res.sendRedirect(req.getContextPath() + "/evaluation/panel?error=Tender not found"); return;
        }

        // Strict status check — only Closed or Under_Evaluation tenders are scoreable
        if (!"Closed".equals(tender.getStatus()) && !"Under_Evaluation".equals(tender.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/evaluation/panel?error=" +
                java.net.URLEncoder.encode(
                    "Tender " + tender.getReferenceNumber() + " is not available for evaluation (status: " + tender.getStatus() + ")",
                    "UTF-8")); return;
        }

        List<Bid> bids = bidDAO.getBidsByTender(tenderId);
        if (bids.isEmpty()) {
            res.sendRedirect(req.getContextPath() + "/evaluation/panel?error=No bids found for this tender"); return;
        }

        BigDecimal lowestBid        = bidDAO.getLowestBidAmount(tenderId);
        int        shortestTimeline = bidDAO.getShortestDeliveryTimeline(tenderId);

        for (Bid bid : bids) {
            bid.setPriceScore(scoreService.calculatePriceScore(lowestBid, bid.getBidAmount()));
            bid.setDeliveryScore(scoreService.calculateDeliveryScore(shortestTimeline, bid.getDeliveryTimeline()));

            boolean alreadyScored = evaluationDAO.hasEvaluatorSubmitted(bid.getBidId(), user.getUserId());
            bid.setActive(!alreadyScored); // active=true means PENDING (needs scoring)

            if (alreadyScored) {
                Evaluation ev = evaluationDAO.findByBidAndEvaluator(bid.getBidId(), user.getUserId());
                if (ev != null) bid.setTechnicalScore(ev.getTechnicalScore());
            }
        }

        req.setAttribute("tender",          tender);
        req.setAttribute("bids",            bids);
        req.setAttribute("lowestBidAmount", lowestBid);
        req.setAttribute("shortestTimeline",shortestTimeline);
        req.setAttribute("userId",          user.getUserId());

        req.getRequestDispatcher("/jsp/scoreEntry.jsp").forward(req, res);
    }
}
