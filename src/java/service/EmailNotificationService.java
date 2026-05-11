package service;

import dao.BidDAOImpl;
import dao.TenderDAOImpl;
import dao.BidDAO;
import dao.TenderDAO;
import dao.UserDAO;
import dao.UserDAOImpl;
import java.io.UnsupportedEncodingException;
import model.Bid;
import model.Tender;
import model.Award;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;

/**
 * Email Notification Service Module 6: Supplier Email Notification (Bonus)
 *
 * When a tender is awarded, sends email to all suppliers who bid: - Winner
 * receives "WON" notification - Losers receive "Not Won" notification with link
 * to award notice
 */
public class EmailNotificationService {

    private static final Logger logger = Logger.getLogger(EmailNotificationService.class.getName());
    private BidDAO bidDAO;
    private TenderDAO tenderDAO;
    private UserDAO userDAO;

    // Email configuration (can also be from JNDI)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

private static final String SMTP_USERNAME = "noreply@procuregov.local";  
private static final String SMTP_PASSWORD = "";  // TODO: set valid SMTP password or use JNDI mail session

private static final String FROM_EMAIL = "noreply@procuregov.local";

    private static final String FROM_NAME = "ProcureGov - Ministry of Public Works, Lesotho";

public EmailNotificationService() {
        this.bidDAO = new BidDAOImpl();
        this.tenderDAO = new TenderDAOImpl();
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Send award notifications to all suppliers who bid on a tender
     *
     * @param tenderId The tender ID
     * @param winningBidId The winning bid ID
     * @return true if all emails sent successfully, false otherwise
     */
    public boolean sendAwardNotifications(int tenderId, int winningBidId) {
        try {
            // Get tender details
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null) {
                logger.severe("Tender not found: " + tenderId);
                return false;
            }

            // Get award details
            Award award = bidDAO.getAwardByTender(tenderId);
            if (award == null) {
                logger.severe("Award not found for tender: " + tenderId);
                return false;
            }

            // Get all bids for this tender
            List<Bid> bids = bidDAO.getBidsByTender(tenderId);

            if (bids.isEmpty()) {
                logger.warning("No bids found for tender: " + tenderId);
                return false;
            }

            // Send email to each supplier
            int successCount = 0;
            for (Bid bid : bids) {
                boolean isWinner = (bid.getBidId() == winningBidId);
                boolean sent = sendSingleNotification(tender, award, bid, isWinner);
                if (sent) {
                    successCount++;
                }
            }

            logger.info("Email notifications sent: " + successCount + "/" + bids.size()
                    + " for tender: " + tender.getReferenceNumber());
            return successCount == bids.size();

        } catch (Exception e) {
            logger.severe("Failed to send award notifications: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send a single email notification to a supplier
     *
     * @param tender Tender object
     * @param award Award object
     * @param bid Bid object
     * @param isWinner true if this supplier won
     * @return true if email sent successfully
     */
    private boolean sendSingleNotification(Tender tender, Award award, Bid bid, boolean isWinner) {
        try {
            // Get supplier email (need to fetch from database)
            String supplierEmail = getSupplierEmail(bid.getSupplierId());
            if (supplierEmail == null) {
                logger.warning("No email found for supplier: " + bid.getSupplierId());
                return false;
            }

            // Prepare email content
            String subject = prepareSubject(tender, isWinner);
            String body = prepareEmailBody(tender, award, bid, isWinner);

            // Send email
            sendEmail(supplierEmail, subject, body);

            logger.info("Email sent to: " + supplierEmail + " | Outcome: " + (isWinner ? "WON" : "NOT_WON"));
            return true;

        } catch (Exception e) {
            logger.severe("Failed to send email to supplier " + bid.getSupplierId() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Get supplier email by user ID
     */
    private String getSupplierEmail(int supplierId) {
        User user = userDAO.findById(supplierId);
        if (user != null && user.getEmail() != null) {
            return user.getEmail();
        }
        logger.warning("No email found for user ID: " + supplierId);
        return null;
    }

    /**
     * Prepare email subject line
     */
    private String prepareSubject(Tender tender, boolean isWinner) {
        if (isWinner) {
            return "CONGRATULATIONS! You have WON the tender: " + tender.getReferenceNumber();
        } else {
            return "Tender Award Notification - " + tender.getReferenceNumber();
        }
    }

    /**
     * Prepare email body content
     */
    private String prepareEmailBody(Tender tender, Award award, Bid bid, boolean isWinner) {
        StringBuilder body = new StringBuilder();

        body.append("Dear ").append(bid.getSupplierCompany()).append(",\n\n");
        body.append("RE: ").append(tender.getReferenceNumber()).append(" - ").append(tender.getTitle()).append("\n\n");

        if (isWinner) {
            body.append("🎉 CONGRATULATIONS! 🎉\n\n");
            body.append("We are pleased to inform you that your bid has been SELECTED as the winning bid for the above tender.\n\n");
            body.append("Award Details:\n");
            body.append("- Awarded Value: M ").append(String.format("%,.2f", award.getAwardedValue())).append("\n");
            body.append("- Award Date: ").append(award.getAwardDate()).append("\n\n");
            body.append("Your bid amount: M ").append(String.format("%,.2f", bid.getBidAmount())).append("\n\n");
        } else {
            body.append("Thank you for submitting a bid for the above tender.\n\n");
            body.append("After careful evaluation of all bids, the contract has been awarded to another supplier.\n\n");
            body.append("Your bid details:\n");
            body.append("- Bid Amount: M ").append(String.format("%,.2f", bid.getBidAmount())).append("\n");
            body.append("- Delivery Timeline: ").append(bid.getDeliveryTimeline()).append(" days\n\n");
        }

        body.append("Award Justification: ").append(award.getJustification()).append("\n\n");
        body.append("For full award details, please view the official Award Notice:\n");
        body.append(getAwardNoticeLink(tender.getTenderId())).append("\n\n");

        body.append("We appreciate your participation and encourage you to bid on future opportunities.\n\n");
        body.append("Sincerely,\n");
        body.append("Ministry of Public Works\n");
        body.append("Directorate of ICT\n");
        body.append("Kingdom of Lesotho\n\n");
        body.append("This is an automated message from ProcureGov. Please do not reply to this email.");

        return body.toString();
    }

    /**
     * Generate award notice link
     */
    private String getAwardNoticeLink(int tenderId) {
        // In production, use actual server URL
        return "http://localhost:8080/ProcureGov/tender/awardNotice?tenderId=" + tenderId;
    }

    /**
     * Send email using JavaMail API
     */
    private void sendEmail(String toEmail, String subject, String body) throws MessagingException {
        // Prefer JNDI mail session as required for deployable projects.
        // If JNDI is not configured, fall back to direct SMTP.
        try {
            sendViaJNDIMailSession(toEmail, subject, body);
        } catch (MessagingException jndiFailure) {
            // JNDI mail is not configured in this environment (Tomcat config shows only JDBC).
            logger.warning("JNDI mail session failed (expected if mail/ProcureGovMail not configured): "
                + jndiFailure.getMessage());
            sendViaDirectSMTP(toEmail, subject, body);
        }
    }



    /**
     * Send email using direct SMTP configuration
     */
    private void sendViaDirectSMTP(String toEmail, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        session.setDebug(true);

        Message message = new MimeMessage(session);

        // Set sender with proper exception handling
        try {
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
        } catch (java.io.UnsupportedEncodingException e) {
            // Fallback to email only if name causes encoding issues
            message.setFrom(new InternetAddress(FROM_EMAIL));
            logger.warning("Sender name not supported, using email only");
        }

        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
        logger.info("Email sent to: " + toEmail);
    }

    /**
     * Send email using JNDI Mail Session (configured in context.xml)
     */
    private void sendViaJNDIMailSession(String toEmail, String subject, String body) throws MessagingException {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            Session mailSession = (Session) envContext.lookup("mail/ProcureGovMail");

            Message message = new MimeMessage(mailSession);

            // Fix here too
            try {
                message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            } catch (java.io.UnsupportedEncodingException e) {
                message.setFrom(new InternetAddress(FROM_EMAIL));
            }

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            logger.info("Email sent via JNDI to: " + toEmail);

        } catch (Exception e) {
            logger.severe("JNDI mail session failed: " + e.getMessage());
            throw new MessagingException("Failed to send email via JNDI", e);
        }
    }

    /**
     * Send tender result notice to a supplier showing their bid evaluation scores
     *
     * @param tenderId The tender ID
     * @param supplierId The supplier user ID
     * @return true if email sent successfully, false otherwise
     */
    public boolean sendTenderResultNotice(int tenderId, int supplierId) {
        try {
            // Get tender details
            Tender tender = tenderDAO.findById(tenderId);
            if (tender == null) {
                logger.severe("Tender not found: " + tenderId);
                return false;
            }

            // Get the specific bid for this supplier
            List<Bid> allBids = bidDAO.getBidsByTender(tenderId);
            Bid supplierBid = null;
            for (Bid bid : allBids) {
                if (bid.getSupplierId() == supplierId) {
                    supplierBid = bid;
                    break;
                }
            }

            if (supplierBid == null) {
                logger.warning("No bid found for supplier " + supplierId + " on tender " + tenderId);
                return false;
            }

            // Get supplier email
            String supplierEmail = getSupplierEmail(supplierId);
            if (supplierEmail == null) {
                logger.warning("No email found for supplier: " + supplierId);
                return false;
            }

            // Prepare result notice email
            String subject = "Tender Evaluation Results - " + tender.getReferenceNumber();
            String body = prepareResultNoticeBody(tender, supplierBid);

            // Send email
            sendEmail(supplierEmail, subject, body);

            logger.info("Result notice email sent to supplier " + supplierId + " (" + supplierEmail + ") for tender " + tenderId);
            return true;

        } catch (Exception e) {
            logger.severe("Failed to send tender result notice to supplier " + supplierId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Prepare result notice email body with evaluation scores
     */
    private String prepareResultNoticeBody(Tender tender, Bid bid) {
        StringBuilder body = new StringBuilder();

        body.append("Dear ").append(bid.getSupplierCompany()).append(",\n\n");
        body.append("RE: Tender Evaluation Results - ").append(tender.getReferenceNumber()).append("\n");
        body.append("Title: ").append(tender.getTitle()).append("\n\n");

        body.append("The evaluation process for the above tender has been completed.\n\n");

        body.append("YOUR BID EVALUATION SCORES:\n");
        body.append("================================\n");
        body.append("Bid Amount: M ").append(String.format("%,.2f", bid.getBidAmount())).append("\n");
        body.append("Delivery Timeline: ").append(bid.getDeliveryTimeline()).append(" days\n\n");

        body.append("EVALUATION SCORES (Weighted):\n");
        body.append("- Price Score (30%): ").append(formatScore(bid.getPriceScore())).append("\n");
        body.append("- Technical Score (50%): ").append(formatScore(bid.getTechnicalScore())).append("\n");
        body.append("- Delivery Score (20%): ").append(formatScore(bid.getDeliveryScore())).append("\n");
        body.append("- FINAL WEIGHTED SCORE: ").append(formatScore(bid.getFinalWeightedScore())).append("\n\n");

        body.append("Scoring Formula: Final Score = (Price × 0.30) + (Technical × 0.50) + (Delivery × 0.20)\n\n");

        body.append("For detailed information on the tender award, please visit:\n");
        body.append("http://localhost:8080/TlotlisangKhutlang2333874/officer/resultNotice?tenderId=").append(tender.getTenderId()).append("\n\n");

        body.append("We appreciate your participation in the public procurement process.\n\n");
        body.append("Sincerely,\n");
        body.append("Procurement Management System\n");
        body.append("Ministry of Public Works\n");
        body.append("Kingdom of Lesotho\n\n");
        body.append("This is an automated message from ProcureGov. Please do not reply to this email.");

        return body.toString();
    }

    /**
     * Format score to string, handling null values
     */
    private String formatScore(java.math.BigDecimal score) {
        if (score == null) {
            return "Not Available";
        }
        return String.format("%.2f/100", score);
    }
}
