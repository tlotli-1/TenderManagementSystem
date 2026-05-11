package servlet;

import dao.TenderDAOImpl;
import dao.TenderDAO;
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
import java.util.stream.Collectors;

/**
 * Tender List Servlet — role-aware tender list.
 *
 * URL mappings:
 *   /officer/tenderList  → PROCUREMENT_OFFICER  → /jsp/officer/tenderList.jsp (all tenders, full actions)
 *   /supplier/tenders    → SUPPLIER             → /jsp/tenderList.jsp         (Open only)
 *   /evaluation/tenders  → EVALUATION_COMMITTEE → redirects to /evaluation/panel (dedicated view)
 *
 * Auto-closes expired tenders on every page load.
 */
public class TenderListServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(TenderListServlet.class.getName());
    private TenderDAO tenderDAO;

    @Override public void init() throws ServletException { tenderDAO = new TenderDAOImpl(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (!SessionValidator.isLoggedIn(session)) {
            res.sendRedirect(req.getContextPath() + "/login?error=Please login first"); return;
        }

        User user = SessionValidator.getLoggedInUser(session);
        String role = user.getRole();

        // Auto-close expired tenders on every visit
        int autoClose = tenderDAO.autoCloseExpiredTenders();
        if (autoClose > 0) logger.info("Auto-closed " + autoClose + " expired tender(s)");

        // EVALUATION_COMMITTEE has its own dedicated servlet and JSP — redirect there
        if ("EVALUATION_COMMITTEE".equals(role)) {
            res.sendRedirect(req.getContextPath() + "/evaluation/panel"); return;
        }

        String statusFilter   = req.getParameter("status");
        String categoryFilter = req.getParameter("category");

        List<Tender> tenders;
        if ("PROCUREMENT_OFFICER".equals(role)) {
            tenders = tenderDAO.findAllTenders();
        } else if ("SUPPLIER".equals(role)) {
            tenders = tenderDAO.findOpenTenders();
        } else {
            res.sendRedirect(req.getContextPath() + "/login"); return;
        }

        // In-memory filter
        if ((statusFilter != null && !statusFilter.isEmpty()) ||
            (categoryFilter != null && !categoryFilter.isEmpty())) {
            final String sf = statusFilter, cf = categoryFilter;
            tenders = tenders.stream()
                .filter(t -> sf == null || sf.isEmpty() || t.getStatus().equals(sf))
                .filter(t -> cf == null || cf.isEmpty() || t.getCategory().equals(cf))
                .collect(Collectors.toList());
        }

        req.setAttribute("tenders",       tenders);
        req.setAttribute("role",          role);
        req.setAttribute("statusFilter",  statusFilter);
        req.setAttribute("categoryFilter",categoryFilter);

        // Stats for Officer dashboard tiles
        if ("PROCUREMENT_OFFICER".equals(role)) {
            req.setAttribute("draftCount",          tenderDAO.countTendersByStatus("Draft"));
            req.setAttribute("openCount",           tenderDAO.countTendersByStatus("Open"));
            req.setAttribute("closedCount",         tenderDAO.countTendersByStatus("Closed"));
            req.setAttribute("underEvaluationCount",tenderDAO.countTendersByStatus("Under_Evaluation"));
            req.setAttribute("evaluatedCount",      tenderDAO.countTendersByStatus("Evaluated"));
            req.setAttribute("awardedCount",        tenderDAO.countTendersByStatus("Awarded"));
            req.getRequestDispatcher("/jsp/officer/tenderList.jsp").forward(req, res);
        } else {
            // Supplier
            req.getRequestDispatcher("/jsp/tenderList.jsp").forward(req, res);
        }
    }
}
