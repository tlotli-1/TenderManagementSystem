package servlet;

import dao.TenderDAOImpl;
import dao.EvaluationDAOImpl;
import dao.TenderDAO;
import dao.EvaluationDAO;
import model.Bid;
import model.Tender;
import model.User;
import util.SessionValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Evaluation Results Servlet — shows the ranked leaderboard.
 *
 * Hardened:
 * - Internal role guard: PROCUREMENT_OFFICER only
 * - Safe tenderId parse
 * - Tender must be Evaluated or Awarded — blocks premature access
 * - Redirects to officer tenderList (not the broken /tenderList)
 */
public class EvaluationResultsServlets extends HttpServlet {

    private static final Logger logger = Logger.getLogger(EvaluationResultsServlets.class.getName());
    private TenderDAO     tenderDAO;
    private EvaluationDAO evaluationDAO;

    @Override public void init() throws ServletException {
        tenderDAO     = new TenderDAOImpl();
        evaluationDAO = new EvaluationDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        if (!SessionValidator.hasRole(session, "PROCUREMENT_OFFICER")) {
            res.sendRedirect(req.getContextPath() + "/login?error=Access denied"); return;
        }

        int tenderId;
        try { tenderId = Integer.parseInt(req.getParameter("tenderId")); }
        catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Invalid tender"); return;
        }

        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=Tender not found"); return;
        }

        // Results only available once evaluation is fully complete
        if (!"Evaluated".equals(tender.getStatus()) && !"Awarded".equals(tender.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/officer/tenderList?error=" +
                java.net.URLEncoder.encode(
                    "Evaluation results are only available once the tender is fully Evaluated (current status: " + tender.getStatus() + ")",
                    "UTF-8")); return;
        }

        List<Bid> rankedBids = evaluationDAO.getBidsWithFinalScores(tenderId);

        req.setAttribute("tender",     tender);
        req.setAttribute("rankedBids", rankedBids);
        req.getRequestDispatcher("/jsp/rankedLeaderboard.jsp").forward(req, res);
    }
}
