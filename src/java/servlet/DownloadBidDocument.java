package servlet;

import dao.BidDAOImpl;
import dao.BidDAO;
import model.Bid;
import model.User;
import util.SessionValidator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Download Bid Document Servlet — serves supplier bid documents.
 *
 * Access rules:
 *  - SUPPLIER:             own documents only (supplierId match)
 *  - PROCUREMENT_OFFICER:  all bids
 *  - EVALUATION_COMMITTEE: all bids (needed for scoring)
 *
 * Hardened:
 *  - Login + role check
 *  - Safe bidId parse
 *  - File path resolved from DB — never from user input
 *  - Canonical path traversal guard
 *  - Content-Type forced to application/pdf
 */
public class DownloadBidDocument extends HttpServlet {

    private static final Logger logger = Logger.getLogger(DownloadBidDocument.class.getName());
    private BidDAO bidDAO;
    private String uploadDirectory;

    @Override public void init() throws ServletException {
        bidDAO = new BidDAOImpl();
        uploadDirectory = getServletContext().getInitParameter("upload.directory");
        if (uploadDirectory == null)
            uploadDirectory = System.getProperty("catalina.base") + File.separator + "procuregov-uploads";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!SessionValidator.isLoggedIn(req.getSession(false))) {
            res.sendRedirect(req.getContextPath() + "/login?error=Please login first"); return;
        }
        User user = SessionValidator.getLoggedInUser(req.getSession(false));

        int bidId;
        try { bidId = Integer.parseInt(req.getParameter("bidId")); }
        catch (NumberFormatException e) { res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid bidId"); return; }

        Bid bid = bidDAO.findById(bidId);
        if (bid == null) { res.sendError(HttpServletResponse.SC_NOT_FOUND, "Bid not found"); return; }

        // Role-based access
        boolean allowed;
        switch (user.getRole()) {
            case "SUPPLIER":             allowed = bid.getSupplierId() == user.getUserId(); break;
            case "PROCUREMENT_OFFICER":
            case "EVALUATION_COMMITTEE": allowed = true;                                    break;
            default:                     allowed = false;
        }
        if (!allowed) {
            logger.warning("Unauthorised bid document access: user=" + user.getUserId() + " bid=" + bidId);
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); return;
        }

        if (bid.getSupportingDocPath() == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "No document attached to this bid"); return;
        }

        File uploadBase    = new File(uploadDirectory).getCanonicalFile();
        File requestedFile = new File(uploadDirectory, bid.getSupportingDocPath()).getCanonicalFile();

        if (!requestedFile.getAbsolutePath().startsWith(uploadBase.getAbsolutePath() + File.separator)) {
            logger.warning("Path traversal attempt blocked for bid " + bidId);
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied"); return;
        }

        if (!requestedFile.exists()) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found"); return;
        }

        res.setContentType("application/pdf");
        res.setHeader("Content-Disposition", "inline; filename=\"" + requestedFile.getName() + "\"");
        res.setContentLengthLong(requestedFile.length());

        try (FileInputStream fis = new FileInputStream(requestedFile);
             OutputStream os = res.getOutputStream()) {
            byte[] buf = new byte[8192]; int n;
            while ((n = fis.read(buf)) != -1) os.write(buf, 0, n);
            logger.info("Bid doc downloaded: " + requestedFile.getName() + " by " + user.getUsername());
        }
    }
}
