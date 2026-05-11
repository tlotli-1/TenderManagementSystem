/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author legacy
 */
import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import model.Tender;
import util.DBConnectionPool;

/**
 * Implementation of TenderDAO interface Handles all database operations for
 * Tender entity
 */
public class TenderDAOImpl implements TenderDAO {

    private static final Logger logger = Logger.getLogger(TenderDAOImpl.class.getName());

    @Override
    public boolean createTender(Tender tender) {
        String sql = "INSERT INTO tenders (reference_number, title, category, description, "
                + "estimated_value, submission_deadline, status, notice_file_path, created_by) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, tender.getReferenceNumber());
            pstmt.setString(2, tender.getTitle());
            pstmt.setString(3, tender.getCategory());
            pstmt.setString(4, tender.getDescription());
            pstmt.setBigDecimal(5, tender.getEstimatedValue());
            pstmt.setTimestamp(6, Timestamp.valueOf(tender.getSubmissionDeadline()));
            pstmt.setString(7, tender.getStatus());
            pstmt.setString(8, tender.getNoticeFilePath());
            pstmt.setInt(9, tender.getCreatedBy());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    tender.setTenderId(rs.getInt(1));
                }
                logger.info("Tender created successfully: " + tender.getReferenceNumber());
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Error creating tender: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Tender findById(int tenderId) {
        String sql = "SELECT * FROM tenders WHERE tender_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTender(rs);
            }

        } catch (SQLException e) {
            logger.severe("Error finding tender by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Tender findByReferenceNumber(String referenceNumber) {
        String sql = "SELECT * FROM tenders WHERE reference_number = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, referenceNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTender(rs);
            }

        } catch (SQLException e) {
            logger.severe("Error finding tender by reference number: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Tender> findAllTenders() {
        List<Tender> tenders = new ArrayList<>();
        String sql = "SELECT * FROM tenders ORDER BY created_date DESC";

        try (Connection conn = DBConnectionPool.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tenders.add(mapResultSetToTender(rs));
            }

        } catch (SQLException e) {
            logger.severe("Error finding all tenders: " + e.getMessage());
        }
        return tenders;
    }

    @Override
    public List<Tender> findTendersByStatus(String status) {
        List<Tender> tenders = new ArrayList<>();
        String sql = "SELECT * FROM tenders WHERE status = ? ORDER BY created_date DESC";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tenders.add(mapResultSetToTender(rs));
            }

        } catch (SQLException e) {
            logger.severe("Error finding tenders by status: " + e.getMessage());
        }
        return tenders;
    }

    @Override
    public List<Tender> findTendersByCategory(String category) {
        List<Tender> tenders = new ArrayList<>();
        String sql = "SELECT * FROM tenders WHERE category = ? ORDER BY created_date DESC";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tenders.add(mapResultSetToTender(rs));
            }

        } catch (SQLException e) {
            logger.severe("Error finding tenders by category: " + e.getMessage());
        }
        return tenders;
    }

    @Override
    public List<Tender> findOpenTenders() {
        List<Tender> tenders = new ArrayList<>();
        String sql = "SELECT * FROM tenders WHERE status = 'Open' AND submission_deadline > NOW() "
                + "ORDER BY submission_deadline ASC";

        try (Connection conn = DBConnectionPool.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tenders.add(mapResultSetToTender(rs));
            }

        } catch (SQLException e) {
            logger.severe("Error finding open tenders: " + e.getMessage());
        }
        return tenders;
    }

    @Override
    public List<Tender> findTendersByProcurementOfficer(int officerId) {
        List<Tender> tenders = new ArrayList<>();
        String sql = "SELECT * FROM tenders WHERE created_by = ? ORDER BY created_date DESC";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, officerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tenders.add(mapResultSetToTender(rs));
            }

        } catch (SQLException e) {
            logger.severe("Error finding tenders by officer: " + e.getMessage());
        }
        return tenders;
    }

    @Override
    public boolean updateTender(Tender tender) {
        // Only allow update if status is Draft
        if (!canEditTender(tender.getTenderId())) {
            logger.warning("Attempted to edit non-draft tender: " + tender.getReferenceNumber());
            return false;
        }

        String sql = "UPDATE tenders SET title = ?, category = ?, description = ?, "
                + "estimated_value = ?, submission_deadline = ?, notice_file_path = ? "
                + "WHERE tender_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tender.getTitle());
            pstmt.setString(2, tender.getCategory());
            pstmt.setString(3, tender.getDescription());
            pstmt.setBigDecimal(4, tender.getEstimatedValue());
            pstmt.setTimestamp(5, Timestamp.valueOf(tender.getSubmissionDeadline()));
            pstmt.setString(6, tender.getNoticeFilePath());
            pstmt.setInt(7, tender.getTenderId());

            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                logger.info("Tender updated: " + tender.getReferenceNumber());
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Error updating tender: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateTenderStatus(int tenderId, String status) {
        String sql = "UPDATE tenders SET status = ? WHERE tender_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, tenderId);

            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                logger.info("Tender status updated: ID=" + tenderId + " -> " + status);
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Error updating tender status: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateTenderStatusWithDate(int tenderId, String status, java.util.Date date) {
        String sql = "";
        switch (status) {
            case "Closed":
                sql = "UPDATE tenders SET status = ?, closed_date = ? WHERE tender_id = ?";
                break;
            case "Evaluated":
                sql = "UPDATE tenders SET status = ?, evaluated_date = ? WHERE tender_id = ?";
                break;
            case "Awarded":
                sql = "UPDATE tenders SET status = ?, awarded_date = ? WHERE tender_id = ?";
                break;
            default:
                sql = "UPDATE tenders SET status = ? WHERE tender_id = ?";
                return updateTenderStatus(tenderId, status);
        }

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setTimestamp(2, new Timestamp(date.getTime()));
            pstmt.setInt(3, tenderId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.severe("Error updating tender status with date: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteTender(int tenderId) {
        String sql = "DELETE FROM tenders WHERE tender_id = ? AND status = 'Draft'";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tenderId);
            int deleted = pstmt.executeUpdate();
            if (deleted > 0) {
                logger.info("Tender deleted: ID=" + tenderId);
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Error deleting tender: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean canEditTender(int tenderId) {
        String sql = "SELECT status FROM tenders WHERE tender_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return "Draft".equals(rs.getString("status"));
            }

        } catch (SQLException e) {
            logger.severe("Error checking edit permission: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean canPublishTender(int tenderId) {
        String sql = "SELECT status FROM tenders WHERE tender_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return "Draft".equals(rs.getString("status"));
            }

        } catch (SQLException e) {
            logger.severe("Error checking publish permission: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int countTendersByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM tenders WHERE status = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.severe("Error counting tenders by status: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public String generateReferenceNumber() {
        int currentYear = Year.now().getValue();
        String yearPrefix = String.format("MPW-%d-", currentYear);

        String sql = "SELECT COUNT(*) FROM tenders WHERE reference_number LIKE ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, yearPrefix + "%");
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }

            // Format: MPW-2025-0001, MPW-2025-0002, etc.
            return String.format("MPW-%d-%04d", currentYear, count + 1);

        } catch (SQLException e) {
            logger.severe("Error generating reference number: " + e.getMessage());
            // Fallback with timestamp
            return String.format("MPW-%d-%d", currentYear, System.currentTimeMillis());
        }
    }

    @Override
    public int autoCloseExpiredTenders() {
        String sql = "UPDATE tenders SET status = 'Closed', closed_date = NOW() "
                + "WHERE status = 'Open' AND submission_deadline < NOW()";

        try (Connection conn = DBConnectionPool.getConnection(); Statement stmt = conn.createStatement()) {

            int updated = stmt.executeUpdate(sql);
            if (updated > 0) {
                logger.info("Auto-closed " + updated + " expired tenders");
            }
            return updated;

        } catch (SQLException e) {
            logger.severe("Error auto-closing tenders: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Helper method to map ResultSet to Tender object
     */
    private Tender mapResultSetToTender(ResultSet rs) throws SQLException {
        Tender tender = new Tender();
        tender.setTenderId(rs.getInt("tender_id"));
        tender.setReferenceNumber(rs.getString("reference_number"));
        tender.setTitle(rs.getString("title"));
        tender.setCategory(rs.getString("category"));
        tender.setDescription(rs.getString("description"));
        tender.setEstimatedValue(rs.getBigDecimal("estimated_value"));

        Timestamp deadline = rs.getTimestamp("submission_deadline");
        if (deadline != null) {
            tender.setSubmissionDeadline(deadline.toLocalDateTime());
        }

        tender.setStatus(rs.getString("status"));
        tender.setNoticeFilePath(rs.getString("notice_file_path"));
        tender.setCreatedBy(rs.getInt("created_by"));
        tender.setCreatedDate(rs.getTimestamp("created_date"));
        tender.setClosedDate(rs.getTimestamp("closed_date"));
        tender.setEvaluatedDate(rs.getTimestamp("evaluated_date"));
        tender.setAwardedDate(rs.getTimestamp("awarded_date"));

        return tender;
    }
}
