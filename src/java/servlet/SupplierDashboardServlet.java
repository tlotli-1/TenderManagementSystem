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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Supplier Dashboard Servlet — hardened with internal role check,
 * auto-close of expired tenders, and award outcome enrichment.
 */
public class SupplierDashboardServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SupplierDashboardServlet.class.getName());
    private TenderDAO tenderDAO;
    private BidDAO bidDAO;

    @Override
    public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        bidDAO    = new BidDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Internal role guard (belt-and-suspenders beyond the filter)
        if (!SessionValidator.hasRole(session, "SUPPLIER")) {
            response.sendRedirect(request.getContextPath() + "/login?error=Access denied");
            return;
        }

        User supplier = SessionValidator.getLoggedInUser(session);

        // Auto-close any tenders whose deadline has passed
        tenderDAO.autoCloseExpiredTenders();

        // Open tenders for browsing
        List<Tender> openTenders = tenderDAO.findOpenTenders();

        // Supplier's own bids, enriched with tender info and award outcome
        List<Bid> myBids = bidDAO.getBidsBySupplier(supplier.getUserId());
        for (Bid bid : myBids) {
            String outcome = bidDAO.getAwardOutcome(bid.getTenderId(), supplier.getUserId());
            // Normalise: if tender is not yet Awarded, show PENDING
            if (outcome == null) {
                bid.setAwardOutcome("PENDING");
            } else {
                bid.setAwardOutcome(outcome);
            }
        }

        // Summary counters for the dashboard header
        long pendingCount = myBids.stream().filter(b -> "PENDING".equals(b.getAwardOutcome())).count();
        long wonCount     = myBids.stream().filter(b -> "WON".equals(b.getAwardOutcome())).count();

        request.setAttribute("openTenders",  openTenders);
        request.setAttribute("myBids",       myBids);
        request.setAttribute("supplier",     supplier);
        request.setAttribute("pendingCount", pendingCount);
        request.setAttribute("wonCount",     wonCount);

        request.getRequestDispatcher("/jsp/supplier/dashboard.jsp").forward(request, response);
    }
}
