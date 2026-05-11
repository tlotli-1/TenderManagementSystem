
package dao;

import dao.BidDAO;
import model.Bid;
import model.Award;
import util.DBConnectionPool;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of BidDAO interface
 * Handles all database operations for Bid entity
 * Module 3: Supplier Bid Submission
 */
public class BidDAOImpl implements BidDAO {
    
    private static final Logger logger = Logger.getLogger(BidDAOImpl.class.getName());
    
    // ========== MODULE 3: BID SUBMISSION METHODS ==========
    
    @Override
    public boolean createBid(Bid bid) {
        String sql = "INSERT INTO bids (tender_id, supplier_id, bid_amount, technical_statement, " +
                     "delivery_timeline, supporting_doc_path, submission_date, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, bid.getTenderId());
            pstmt.setInt(2, bid.getSupplierId());
            pstmt.setBigDecimal(3, bid.getBidAmount());
            pstmt.setString(4, bid.getTechnicalStatement());
            pstmt.setInt(5, bid.getDeliveryTimeline());
            pstmt.setString(6, bid.getSupportingDocPath());
            pstmt.setBoolean(7, bid.isActive());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    bid.setBidId(rs.getInt(1));
                }
                logger.info("Bid created: Tender=" + bid.getTenderId() + ", Supplier=" + bid.getSupplierId());
                return true;
            }
            
        } catch (SQLException e) {
            logger.severe("Error creating bid: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public Bid findById(int bidId) {
        String sql = "SELECT b.*, u.company_name as supplier_company, u.username as supplier_username, " +
                     "t.title as tender_title, t.reference_number as tender_reference, t.status as tender_status " +
                     "FROM bids b " +
                     "JOIN users u ON b.supplier_id = u.user_id " +
                     "JOIN tenders t ON b.tender_id = t.tender_id " +
                     "WHERE b.bid_id = ?";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bidId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBid(rs);
            }
            
        } catch (SQLException e) {
            logger.severe("Error finding bid by ID: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public boolean hasSupplierBid(int tenderId, int supplierId) {
        String sql = "SELECT 1 FROM bids WHERE tender_id = ? AND supplier_id = ? AND is_active = true LIMIT 1";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            pstmt.setInt(2, supplierId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            logger.severe("Error checking supplier bid: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public List<Bid> getBidsBySupplier(int supplierId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT b.*, t.title as tender_title, t.reference_number as tender_reference, " +
                     "t.status as tender_status, " +
                     "fs.final_weighted_total, " +
                     "a.award_id as has_award " +
                     "FROM bids b " +
                     "JOIN tenders t ON b.tender_id = t.tender_id " +
                     "LEFT JOIN final_scores fs ON b.bid_id = fs.bid_id " +
                     "LEFT JOIN awards a ON b.tender_id = a.tender_id " +
                     "WHERE b.supplier_id = ? AND b.is_active = true " +
                     "ORDER BY b.submission_date DESC";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, supplierId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bid bid = mapResultSetToBid(rs);
                bid.setFinalWeightedScore(rs.getBigDecimal("final_weighted_total"));
                bids.add(bid);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting bids by supplier: " + e.getMessage());
        }
        return bids;
    }
    
    @Override
    public List<Bid> getBidsByTender(int tenderId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT b.*, u.company_name as supplier_company, u.username as supplier_username " +
                     "FROM bids b " +
                     "JOIN users u ON b.supplier_id = u.user_id " +
                     "WHERE b.tender_id = ? AND b.is_active = true " +
                     "ORDER BY b.bid_amount ASC";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bids.add(mapResultSetToBid(rs));
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting bids by tender: " + e.getMessage());
        }
        return bids;
    }
    
    @Override
    public Bid getSupplierBidForTender(int tenderId, int supplierId) {
        String sql = "SELECT b.*, u.company_name as supplier_company, u.username as supplier_username, " +
                     "t.title as tender_title, t.reference_number as tender_reference " +
                     "FROM bids b " +
                     "JOIN users u ON b.supplier_id = u.user_id " +
                     "JOIN tenders t ON b.tender_id = t.tender_id " +
                     "WHERE b.tender_id = ? AND b.supplier_id = ? AND b.is_active = true";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            pstmt.setInt(2, supplierId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToBid(rs);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting supplier bid for tender: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public boolean isTenderOpenForBidding(int tenderId) {
        String sql = "SELECT status, submission_deadline FROM tenders WHERE tender_id = ?";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("status");
                Timestamp deadline = rs.getTimestamp("submission_deadline");
                
                
                return "Open".equals(status) && 
                       (deadline == null || deadline.after(new Timestamp(System.currentTimeMillis())));
            }
            
        } catch (SQLException e) {
            logger.severe("Error checking tender open status: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public BigDecimal getLowestBidAmount(int tenderId) {
        String sql = "SELECT MIN(bid_amount) FROM bids WHERE tender_id = ? AND is_active = true";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting lowest bid amount: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public int getShortestDeliveryTimeline(int tenderId) {
        String sql = "SELECT MIN(delivery_timeline) FROM bids WHERE tender_id = ? AND is_active = true";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting shortest delivery timeline: " + e.getMessage());
        }
        return Integer.MAX_VALUE;
    }
    
    @Override
    public String getAwardOutcome(int tenderId, int supplierId) {
        String sql = "SELECT CASE WHEN winning_supplier_id = ? THEN 'WON' ELSE 'NOT_WON' END as outcome " +
                     "FROM awards WHERE tender_id = ?";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, supplierId);
            pstmt.setInt(2, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("outcome");
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting award outcome: " + e.getMessage());
        }
        return null;
    }
    
    // ========== MODULE 2: EXISTING METHODS ==========
    
    @Override
    public List<Bid> getRankedBidsByTender(int tenderId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT b.*, u.company_name as supplier_company, u.username as supplier_username, " +
                     "fs.final_weighted_total, fs.avg_technical_score as final_technical_score " +
                     "FROM bids b " +
                     "JOIN users u ON b.supplier_id = u.user_id " +
                     "LEFT JOIN final_scores fs ON b.bid_id = fs.bid_id " +
                     "WHERE b.tender_id = ? AND b.is_active = true " +
                     "ORDER BY fs.final_weighted_total DESC, b.bid_amount ASC";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bid bid = mapResultSetToBid(rs);
                bid.setFinalWeightedScore(rs.getBigDecimal("final_weighted_total"));
                bid.setFinalTechnicalScore(rs.getBigDecimal("final_technical_score"));
                bids.add(bid);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting ranked bids: " + e.getMessage());
        }
        return bids;
    }
    
    @Override
    public boolean createAward(int tenderId, int winningBidId, int winningSupplierId, 
                               BigDecimal awardedValue, String justification, int awardedBy) {
        String sql = "INSERT INTO awards (tender_id, winning_bid_id, winning_supplier_id, " +
                     "awarded_value, justification, awarded_by, award_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            pstmt.setInt(2, winningBidId);
            pstmt.setInt(3, winningSupplierId);
            pstmt.setBigDecimal(4, awardedValue);
            pstmt.setString(5, justification);
            pstmt.setInt(6, awardedBy);
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                logger.info("Award created for tender: " + tenderId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.severe("Error creating award: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public Award getAwardByTender(int tenderId) {
        String sql = "SELECT a.*, u.company_name as winning_company, u.registration_number as winning_reg_number " +
                     "FROM awards a " +
                     "JOIN users u ON a.winning_supplier_id = u.user_id " +
                     "WHERE a.tender_id = ?";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Award award = new Award();
                award.setAwardId(rs.getInt("award_id"));
                award.setTenderId(rs.getInt("tender_id"));
                award.setWinningBidId(rs.getInt("winning_bid_id"));
                award.setWinningSupplierId(rs.getInt("winning_supplier_id"));
                award.setWinningSupplierCompany(rs.getString("winning_company"));
                award.setWinningSupplierRegNumber(rs.getString("winning_reg_number"));
                award.setAwardedValue(rs.getBigDecimal("awarded_value"));
                award.setJustification(rs.getString("justification"));
                award.setAwardDate(rs.getTimestamp("award_date"));
                award.setAwardedBy(rs.getInt("awarded_by"));
                return award;
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting award: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public boolean hasSupplierBidOnTender(int tenderId, int supplierId) {
        return hasSupplierBid(tenderId, supplierId);
    }
    
    // ========== HELPER METHODS ==========
    
    private Bid mapResultSetToBid(ResultSet rs) throws SQLException {
        Bid bid = new Bid();
        bid.setBidId(rs.getInt("bid_id"));
        bid.setTenderId(rs.getInt("tender_id"));
        bid.setSupplierId(rs.getInt("supplier_id"));
        bid.setBidAmount(rs.getBigDecimal("bid_amount"));
        bid.setTechnicalStatement(rs.getString("technical_statement"));
        bid.setDeliveryTimeline(rs.getInt("delivery_timeline"));
        bid.setSupportingDocPath(rs.getString("supporting_doc_path"));
        bid.setSubmissionDate(rs.getTimestamp("submission_date"));
        bid.setActive(rs.getBoolean("is_active"));
        
        
        try {
            bid.setTenderTitle(rs.getString("tender_title"));
            bid.setTenderReference(rs.getString("tender_reference"));
            bid.setTenderStatus(rs.getString("tender_status"));
            bid.setSupplierCompany(rs.getString("supplier_company"));
            bid.setSupplierUsername(rs.getString("supplier_username"));
        } catch (SQLException e) {
            
        }
        
        return bid;
    }
}