package servlet;

import dao.TenderDAOImpl;
import dao.BidDAOImpl;
import dao.TenderDAO;
import dao.BidDAO;
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
import java.util.logging.Logger;

/**
 * Tender Details Servlet — shows full tender info and bid form for suppliers.
 * Hardened: role check, deadline enforcement, one-bid check, safe tenderId parse.
 */
public class TenderDetailsServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(TenderDetailsServlet.class.getName());
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

        if (!SessionValidator.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/login?error=Please login first");
            return;
        }

        // Safe parse of tenderId
        int tenderId;
        try {
            tenderId = Integer.parseInt(request.getParameter("tenderId"));
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/supplier/dashboard?error=Invalid tender");
            return;
        }

        Tender tender = tenderDAO.findById(tenderId);

        if (tender == null) {
            response.sendRedirect(request.getContextPath() + "/supplier/dashboard?error=Tender not found");
            return;
        }

        User user = SessionValidator.getLoggedInUser(session);
        boolean isOpen          = tender.isOpen() && !tender.isDeadlinePassed();
        boolean hasSubmittedBid = false;
        boolean canSubmitBid    = false;
        Bid     myBid           = null;

        if ("SUPPLIER".equals(user.getRole())) {
            hasSubmittedBid = bidDAO.hasSupplierBid(tenderId, user.getUserId());
            canSubmitBid    = isOpen && !hasSubmittedBid;
            // Show the supplier their own submitted bid if they have one
            if (hasSubmittedBid) {
                myBid = bidDAO.getSupplierBidForTender(tenderId, user.getUserId());
                if (myBid != null) {
                    String outcome = bidDAO.getAwardOutcome(tenderId, user.getUserId());
                    myBid.setAwardOutcome(outcome != null ? outcome : "PENDING");
                }
            }
        }

        request.setAttribute("tender",         tender);
        request.setAttribute("isOpen",         isOpen);
        request.setAttribute("hasSubmittedBid",hasSubmittedBid);
        request.setAttribute("canSubmitBid",   canSubmitBid);
        request.setAttribute("myBid",          myBid);
        request.setAttribute("userRole",       user.getRole());

        request.getRequestDispatcher("/jsp/tenderDetail.jsp").forward(request, response);
    }
}
