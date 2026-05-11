package servlet;

import dao.BidDAOImpl;
import dao.TenderDAOImpl;
import dao.BidDAO;
import dao.TenderDAO;
import model.Award;
import model.Tender;
import model.User;
import util.SessionValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Award Notice Servlet — displays the official award notice.
 *
 * Access rules:
 *  - PROCUREMENT_OFFICER: can view any award notice
 *  - EVALUATION_COMMITTEE: can view any award notice
 *  - SUPPLIER: can only view if they submitted a bid on that tender
 *
 * Hardened:
 *  - Internal login check
 *  - Safe tenderId parse
 *  - Tender must be Awarded
 *  - Supplier access checked against bid records, not just session role
 *  - Authenticated-but-unauthorised → dashboard with error (not login page)
 */
public class AwardNoticeServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(AwardNoticeServlet.class.getName());
    private TenderDAO tenderDAO;
    private BidDAO    bidDAO;

    @Override public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO    = new BidDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (!SessionValidator.isLoggedIn(session)) {
            res.sendRedirect(req.getContextPath() + "/login?error=Please login first"); return;
        }
        User user = SessionValidator.getLoggedInUser(session);

        int tenderId;
        try { tenderId = Integer.parseInt(req.getParameter("tenderId")); }
        catch (NumberFormatException e) {
            res.sendRedirect(req.getContextPath() + "/supplier/dashboard?error=Invalid tender"); return;
        }

        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null) {
            res.sendRedirect(req.getContextPath() + "/supplier/dashboard?error=Tender not found"); return;
        }

        // Tender must be Awarded before the notice is visible
        if (!"Awarded".equals(tender.getStatus())) {
            res.sendRedirect(req.getContextPath() + "/supplier/dashboard?error=Award notice is not available yet for this tender"); return;
        }

        Award award = bidDAO.getAwardByTender(tenderId);
        if (award == null) {
            res.sendRedirect(req.getContextPath() + "/supplier/dashboard?error=Award details not found"); return;
        }

        boolean isSupplier = "SUPPLIER".equals(user.getRole());
        boolean isBidder   = false;
        boolean isWinner   = false;

        if (isSupplier) {
            isBidder = bidDAO.hasSupplierBid(tenderId, user.getUserId());
            if (!isBidder) {
                // Authenticated supplier who did NOT bid — redirect with clear message
                res.sendRedirect(req.getContextPath() +
                    "/supplier/dashboard?error=You did not submit a bid for this tender and cannot view its award notice"); return;
            }
            isWinner = (award.getWinningSupplierId() == user.getUserId());
        }

        req.setAttribute("tender",   tender);
        req.setAttribute("award",    award);
        req.setAttribute("isWinner", isWinner);
        req.setAttribute("isBidder", isBidder);
        req.setAttribute("userRole", user.getRole());
        req.getRequestDispatcher("/jsp/awardNotice.jsp").forward(req, res);
    }
}
