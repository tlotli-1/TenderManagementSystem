package servlet;

import dao.TenderDAOImpl;
import dao.EvaluationDAOImpl;
import dao.BidDAOImpl;
import dao.TenderDAO;
import dao.EvaluationDAO;
import dao.BidDAO;
import model.Tender;
import model.User;
import util.SessionValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Evaluation Panel Servlet — shows Closed and Under_Evaluation tenders.
 *
 * Hardened:
 * - Internal role guard (EVALUATION_COMMITTEE and PROCUREMENT_OFFICER only)
 * - Bid counts populated correctly using bidCounts map
 * - Evaluated tenders passed for Officer's "Completed Evaluations" section
 * - Suppliers cannot reach this URL even if they bypass the filter
 */
public class EvaluationPanelServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(EvaluationPanelServlet.class.getName());
    private TenderDAO     tenderDAO;
    private EvaluationDAO evaluationDAO;
    private BidDAO        bidDAO;

    @Override public void init() throws ServletException {
        tenderDAO     = new TenderDAOImpl();
        evaluationDAO = new EvaluationDAOImpl();
        bidDAO        = new BidDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (!SessionValidator.isLoggedIn(session)) {
            res.sendRedirect(req.getContextPath() + "/login?error=Please login first"); return;
        }

        User user = SessionValidator.getLoggedInUser(session);
        String role = user.getRole();

        // Internal guard — suppliers must never reach this
        if (!"PROCUREMENT_OFFICER".equals(role) && !"EVALUATION_COMMITTEE".equals(role)) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }

        String categoryFilter = sanitize(req.getParameter("category"));

        List<Tender> closedTenders         = tenderDAO.findTendersByStatus("Closed");
        List<Tender> underEvalTenders      = tenderDAO.findTendersByStatus("Under_Evaluation");
        List<Tender> evaluatedTenders      = tenderDAO.findTendersByStatus("Evaluated"); // Officer only

        // Apply category filter
        if (!categoryFilter.isEmpty()) {
            closedTenders    = closedTenders.stream().filter(t -> categoryFilter.equals(t.getCategory())).collect(Collectors.toList());
            underEvalTenders = underEvalTenders.stream().filter(t -> categoryFilter.equals(t.getCategory())).collect(Collectors.toList());
            evaluatedTenders = evaluatedTenders.stream().filter(t -> categoryFilter.equals(t.getCategory())).collect(Collectors.toList());
        }

        // Bid counts for each tender
        Map<Integer, Integer> bidCounts = new HashMap<>();
        for (Tender t : closedTenders)    bidCounts.put(t.getTenderId(), bidDAO.getBidsByTender(t.getTenderId()).size());
        for (Tender t : underEvalTenders) bidCounts.put(t.getTenderId(), bidDAO.getBidsByTender(t.getTenderId()).size());

        // Per-evaluator progress: how many bids in each tender this user has already scored
        Map<Integer, Integer> scoredCounts = new HashMap<>();
        if ("EVALUATION_COMMITTEE".equals(role)) {
            for (Tender t : underEvalTenders) {
                int scored = evaluationDAO.getBidCountForTender(t.getTenderId()) > 0
                    ? evaluationDAO.getEvaluationsByEvaluator(user.getUserId()).stream()
                          .filter(e -> bidDAO.findById(e.getBidId()) != null &&
                                       bidDAO.findById(e.getBidId()).getTenderId() == t.getTenderId())
                          .mapToInt(e -> 1).sum()
                    : 0;
                scoredCounts.put(t.getTenderId(), scored);
            }
        }

        req.setAttribute("closedTenders",         closedTenders);
        req.setAttribute("underEvaluationTenders", underEvalTenders);
        req.setAttribute("evaluatedTenders",       evaluatedTenders);
        req.setAttribute("bidCounts",              bidCounts);
        req.setAttribute("scoredCounts",           scoredCounts);
        req.setAttribute("categoryFilter",         categoryFilter);
        req.setAttribute("userRole",               role);
        req.setAttribute("userId",                 user.getUserId());

        req.getRequestDispatcher("/jsp/evaluation/evaluationPanel.jsp").forward(req, res);
    }

    private String sanitize(String v) { return v == null ? "" : v.trim(); }
}
