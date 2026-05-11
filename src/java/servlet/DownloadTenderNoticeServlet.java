package servlet;

import dao.TenderDAOImpl;
import dao.TenderDAO;
import model.Tender;
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
 * Download Tender Notice Servlet — serves tender notice PDFs securely.
 *
 * Hardened:
 *  - Login required (any role)
 *  - File path resolved from DB via tenderId — never from raw user input
 *  - Path traversal blocked: canonicalPath must be inside uploadDirectory
 *  - Content-Type forced to application/pdf
 */
public class DownloadTenderNoticeServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(DownloadTenderNoticeServlet.class.getName());
    private TenderDAO tenderDAO;
    private String    uploadDirectory;

    @Override public void init() throws ServletException {
        tenderDAO = new TenderDAOImpl();
        uploadDirectory = getServletContext().getInitParameter("upload.directory");
        if (uploadDirectory == null)
            uploadDirectory = System.getProperty("catalina.base") + File.separator + "procuregov-uploads";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (!SessionValidator.isLoggedIn(req.getSession(false))) {
            res.sendRedirect(req.getContextPath() + "/login?error=Please login first"); return;
        }

        // Always resolve file path from DB — never trust raw file param
        String tenderIdParam = req.getParameter("tenderId");
        if (tenderIdParam == null || tenderIdParam.isEmpty()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "tenderId required"); return;
        }

        int tenderId;
        try { tenderId = Integer.parseInt(tenderIdParam); }
        catch (NumberFormatException e) { res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenderId"); return; }

        Tender tender = tenderDAO.findById(tenderId);
        if (tender == null || tender.getNoticeFilePath() == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Tender notice not found"); return;
        }

        File uploadBase  = new File(uploadDirectory).getCanonicalFile();
        File requestedFile = new File(uploadDirectory, tender.getNoticeFilePath()).getCanonicalFile();

        // Path traversal guard: canonical path must start with upload base
        if (!requestedFile.getAbsolutePath().startsWith(uploadBase.getAbsolutePath() + File.separator)) {
            logger.warning("Path traversal attempt blocked: " + requestedFile.getAbsolutePath());
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
            logger.info("Notice downloaded: " + requestedFile.getName());
        }
    }
}
