package servlet;

import dao.TenderDAOImpl;
import dao.BidDAOImpl;
import dao.EvaluationDAOImpl;
import dao.TenderDAO;
import dao.BidDAO;
import dao.EvaluationDAO;
import model.Bid;
import model.Evaluation;
import model.Tender;
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
import java.util.logging.Logger;

/**
 * Submit Scores Servlet — hardened.
 *
 * Rules enforced server-side:
 *  1. Role guard (EVALUATION_COMMITTEE or PROCUREMENT_OFFICER)
 *  2. Safe ID parse for tenderId and bidId
 *  3. Tender must be Closed or Under_Evaluation — not Open, Draft, etc.
 *  4. Bid must belong to the tender supplied (ID injection guard)
 *  5. Duplicate submission blocked — evaluator cannot re-score the same bid
 *  6. Technical score validated 0–100 via ScoreCalculationService
 *  7. Auto-transition: Closed → Under_Evaluation on first score
 *  8. Auto-transition: Under_Evaluation → Evaluated when all bids fully scored
 */
public class SubmitScoresServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SubmitScoresServlet.class.getName());
    private TenderDAO tenderDAO;
    private BidDAO    bidDAO;
    private EvaluationDAO evaluationDAO;
    private ScoreCalculationService scoreService;

    @Override public void init() throws ServletException {
        tenderDAO     = new TenderDAOImpl();
        bidDAO        = new BidDAOImpl();
        evaluationDAO = new EvaluationDAOImpl();
        scoreService  = new ScoreCalculationService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        // 1. Role guard
        if (!SessionValidator.isLoggedIn(session)) {
            res.sendRedirect(req.getContextPath() + "/login?error=Please login first"); return;
        }
        User user = SessionValidator.getLoggedInUser(session);
        String role = user.getRole();
        if (!"EVALUATION_COMMITTEE".equals(role) && !"PROCUREMENT_OFFICER".equals(role)) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }

        // 2. Safe ID parse
        int tenderId, bidId;
        try {
            tenderId = Integer.parseInt(req.getParameter("tenderId"));
            bidId    = Integer.parseInt(req.getParameter("bidId"));
        } catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/evaluation/panel?error=Invalid request"); return;
        }

        // 3. Tender status guard
        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            res.sendRedirect(req.getContextPath() + "/evaluation/panel?error=Tender not found"); return;
        }
        if (!"Closed".equals(tender.getStatus()) && !"Under_Evaluation".equals(tender.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/evaluation/scoreEntry?tenderId=" + tenderId +
                "&error=" + enc("This tender is not open for evaluation (status: " + tender.getStatus() + ")")); return;
        }

        // 4. Bid belongs to this tender
        Bid bid = bidDAO.findById(bidId);
        if (bid == null || bid.getTenderId() != tenderId) {
            res.sendRedirect(req.getContextPath() + "/evaluation/scoreEntry?tenderId=" + tenderId +
                "&error=Invalid bid"); return;
        }

        // 5. Duplicate submission guard
        if (evaluationDAO.hasEvaluatorSubmitted(bidId, user.getUserId())) {
            res.sendRedirect(req.getContextPath() + "/evaluation/scoreEntry?tenderId=" + tenderId +
                "&error=" + enc("You have already submitted scores for " + bid.getSupplierCompany() + ". Scores cannot be changed.")); return;
        }

        // 6. Technical score validation
        BigDecimal technicalScore = scoreService.parseTechnicalScore(req.getParameter("technicalScore"));
        if (technicalScore == null) {
            res.sendRedirect(req.getContextPath() + "/evaluation/scoreEntry?tenderId=" + tenderId +
                "&error=Technical score must be a number between 0 and 100"); return;
        }

        // Calculate all scores via the service (single source of truth)
        BigDecimal lowestBid       = bidDAO.getLowestBidAmount(tenderId);
        int        shortestTimeline = bidDAO.getShortestDeliveryTimeline(tenderId);
        BigDecimal priceScore      = scoreService.calculatePriceScore(lowestBid, bid.getBidAmount());
        BigDecimal deliveryScore   = scoreService.calculateDeliveryScore(shortestTimeline, bid.getDeliveryTimeline());
        BigDecimal weightedTotal   = scoreService.calculateWeightedTotal(priceScore, technicalScore, deliveryScore);

Evaluation evaluation = new Evaluation();
        evaluation.setTenderId(tenderId);
        evaluation.setBidId(bidId);
        evaluation.setEvaluatorId(user.getUserId());
        evaluation.setPriceScore(priceScore);
        evaluation.setTechnicalScore(technicalScore);
        evaluation.setDeliveryScore(deliveryScore);
        evaluation.setWeightedTotal(weightedTotal);

        if (!evaluationDAO.saveEvaluation(evaluation)) {
            res.sendRedirect(req.getContextPath() + "/evaluation/scoreEntry?tenderId=" + tenderId +
                "&error=Failed to save scores. Please try again."); return;
        }

        logger.info("Score saved: tender=" + tenderId + " bid=" + bidId +
            " evaluator=" + user.getUserId() + " score=" + weightedTotal);

        // 7. Auto: Closed → Under_Evaluation on first score submission
        if ("Closed".equals(tender.getStatus())) {
            tenderDAO.updateTenderStatus(tenderId, "Under_Evaluation");
            logger.info("Tender " + tenderId + " auto-moved to Under_Evaluation");
        }

        // 8. Auto: Under_Evaluation → Evaluated when all bids scored by all evaluators
        int totalEvaluators = Math.max(evaluationDAO.getTotalEvaluatorCount(), 1);
        if (evaluationDAO.isAllBidsFullyEvaluated(tenderId, totalEvaluators)) {
            evaluationDAO.calculateAndStoreFinalScores(tenderId);
            tenderDAO.updateTenderStatus(tenderId, "Evaluated");
            logger.info("Tender " + tenderId + " auto-transitioned to Evaluated");
            res.sendRedirect(req.getContextPath() + "/evaluation/scoreEntry?tenderId=" + tenderId +
                "&success=" + enc("All bids scored! Tender " + tender.getReferenceNumber() +
                    " has been marked Evaluated. The Procurement Officer can now award the contract.")); return;
        }

        int completed = evaluationDAO.getCompletedEvaluatorCount(tenderId);
        res.sendRedirect(req.getContextPath() + "/evaluation/scoreEntry?tenderId=" + tenderId +
            "&success=" + enc("Score submitted for " + bid.getSupplierCompany() +
                ". (" + completed + "/" + totalEvaluators + " evaluators completed)"));
    }

    private String enc(String s) throws java.io.UnsupportedEncodingException {
        return java.net.URLEncoder.encode(s, "UTF-8");
    }
}
