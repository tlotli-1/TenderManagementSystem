/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author legacy
 */

import dao.EvaluationDAO;
import model.Evaluation;
import model.Bid;
import util.DBConnectionPool;
import service.ScoreCalculationService;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of EvaluationDAO interface
 * Module 4: Bid Evaluation
 */
public class EvaluationDAOImpl implements EvaluationDAO {
    
    private static final Logger logger = Logger.getLogger(EvaluationDAOImpl.class.getName());
    private ScoreCalculationService scoreService = new ScoreCalculationService();
    
@Override
    public boolean saveEvaluation(Evaluation evaluation) {
        // Use INSERT ... ON DUPLICATE KEY UPDATE for atomic upsert - more reliable than check-then-insert
        // Note: evaluations table has required tender_id column
        String sql = "INSERT INTO evaluations (tender_id, bid_id, evaluator_id, price_score, technical_score, " +
                     "delivery_score, weighted_total, submitted_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "price_score = VALUES(price_score), " +
                     "technical_score = VALUES(technical_score), " +
                     "delivery_score = VALUES(delivery_score), " +
                     "weighted_total = VALUES(weighted_total), " +
                     "submitted_date = NOW()";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Get tender_id from bid
            int tenderId = evaluation.getTenderId();
            pstmt.setInt(1, tenderId);
            pstmt.setInt(2, evaluation.getBidId());
            pstmt.setInt(3, evaluation.getEvaluatorId());
            pstmt.setBigDecimal(4, evaluation.getPriceScore());
            pstmt.setBigDecimal(5, evaluation.getTechnicalScore());
            pstmt.setBigDecimal(6, evaluation.getDeliveryScore());
            pstmt.setBigDecimal(7, evaluation.getWeightedTotal());
            
            int result = pstmt.executeUpdate();
            if (result > 0) {
                logger.info("Evaluation saved: Bid=" + evaluation.getBidId() + 
                           ", Evaluator=" + evaluation.getEvaluatorId() +
                           ", Weighted=" + evaluation.getWeightedTotal());
                return true;
            }
            
        } catch (SQLException e) {
            logger.severe("Error saving evaluation for bid " + evaluation.getBidId() + 
                       " evaluator " + evaluation.getEvaluatorId() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public Evaluation findByBidAndEvaluator(int bidId, int evaluatorId) {
        String sql = "SELECT * FROM evaluations WHERE bid_id = ? AND evaluator_id = ?";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bidId);
            pstmt.setInt(2, evaluatorId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEvaluation(rs);
            }
            
        } catch (SQLException e) {
            logger.severe("Error finding evaluation: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<Evaluation> getEvaluationsByBid(int bidId) {
        List<Evaluation> evaluations = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name as evaluator_name " +
                     "FROM evaluations e " +
                     "JOIN users u ON e.evaluator_id = u.user_id " +
                     "WHERE e.bid_id = ? ORDER BY e.submitted_date";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bidId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Evaluation eval = mapResultSetToEvaluation(rs);
                eval.setEvaluatorName(rs.getString("evaluator_name"));
                evaluations.add(eval);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting evaluations by bid: " + e.getMessage());
        }
        return evaluations;
    }
    
    @Override
    public List<Evaluation> getEvaluationsByTender(int tenderId) {
        List<Evaluation> evaluations = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name as evaluator_name, b.supplier_id " +
                     "FROM evaluations e " +
                     "JOIN bids b ON e.bid_id = b.bid_id " +
                     "JOIN users u ON e.evaluator_id = u.user_id " +
                     "WHERE b.tender_id = ? ORDER BY b.bid_id, e.submitted_date";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                evaluations.add(mapResultSetToEvaluation(rs));
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting evaluations by tender: " + e.getMessage());
        }
        return evaluations;
    }
    
    @Override
    public List<Evaluation> getEvaluationsByEvaluator(int evaluatorId) {
        List<Evaluation> evaluations = new ArrayList<>();
        String sql = "SELECT e.*, b.tender_id, b.bid_amount, b.delivery_timeline, " +
                     "u.company_name as supplier_company " +
                     "FROM evaluations e " +
                     "JOIN bids b ON e.bid_id = b.bid_id " +
                     "JOIN users u ON b.supplier_id = u.user_id " +
                     "WHERE e.evaluator_id = ? ORDER BY e.submitted_date DESC";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, evaluatorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Evaluation eval = mapResultSetToEvaluation(rs);
                eval.setBidAmount(rs.getBigDecimal("bid_amount"));
                eval.setDeliveryTimeline(rs.getInt("delivery_timeline"));
                eval.setSupplierCompany(rs.getString("supplier_company"));
                evaluations.add(eval);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting evaluations by evaluator: " + e.getMessage());
        }
        return evaluations;
    }
    
    @Override
    public boolean hasEvaluatorSubmitted(int bidId, int evaluatorId) {
        return findByBidAndEvaluator(bidId, evaluatorId) != null;
    }
    
    @Override
    public List<Bid> getPendingBidsForEvaluator(int tenderId, int evaluatorId) {
        List<Bid> pendingBids = new ArrayList<>();
        String sql = "SELECT b.*, u.company_name as supplier_company " +
                     "FROM bids b " +
                     "JOIN users u ON b.supplier_id = u.user_id " +
                     "WHERE b.tender_id = ? AND b.is_active = true " +
                     "AND NOT EXISTS (SELECT 1 FROM evaluations e " +
                     "WHERE e.bid_id = b.bid_id AND e.evaluator_id = ?)";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            pstmt.setInt(2, evaluatorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bid bid = new Bid();
                bid.setBidId(rs.getInt("bid_id"));
                bid.setTenderId(rs.getInt("tender_id"));
                bid.setSupplierId(rs.getInt("supplier_id"));
                bid.setBidAmount(rs.getBigDecimal("bid_amount"));
                bid.setTechnicalStatement(rs.getString("technical_statement"));
                bid.setDeliveryTimeline(rs.getInt("delivery_timeline"));
                bid.setSupplierCompany(rs.getString("supplier_company"));
                pendingBids.add(bid);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting pending bids: " + e.getMessage());
        }
        return pendingBids;
    }
    
    @Override
    public List<Bid> getBidsWithFinalScores(int tenderId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT b.*, u.company_name as supplier_company, " +
                     "fs.final_weighted_total, fs.avg_price_score, fs.avg_technical_score, " +
                     "fs.avg_delivery_score, fs.rank_position " +
                     "FROM bids b " +
                     "JOIN users u ON b.supplier_id = u.user_id " +
                     "LEFT JOIN final_scores fs ON b.bid_id = fs.bid_id " +
                     "WHERE b.tender_id = ? AND b.is_active = true " +
                     "ORDER BY fs.rank_position ASC, fs.final_weighted_total DESC";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bid bid = new Bid();
                bid.setBidId(rs.getInt("bid_id"));
                bid.setTenderId(rs.getInt("tender_id"));
                bid.setSupplierId(rs.getInt("supplier_id"));
                bid.setBidAmount(rs.getBigDecimal("bid_amount"));
                bid.setTechnicalStatement(rs.getString("technical_statement"));
                bid.setDeliveryTimeline(rs.getInt("delivery_timeline"));
                bid.setSupplierCompany(rs.getString("supplier_company"));
                bid.setFinalWeightedScore(rs.getBigDecimal("final_weighted_total"));
                bid.setPriceScore(rs.getBigDecimal("avg_price_score"));
                bid.setTechnicalScore(rs.getBigDecimal("avg_technical_score"));
                bid.setDeliveryScore(rs.getBigDecimal("avg_delivery_score"));
                bids.add(bid);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting bids with final scores: " + e.getMessage());
        }
        return bids;
    }
    
    @Override
    public boolean calculateAndStoreFinalScores(int tenderId) {
        // Get all bids for this tender
        List<Bid> bids = getBidsWithFinalScores(tenderId);
        
        // For each bid, calculate average scores from all evaluators
        for (Bid bid : bids) {
            List<Evaluation> evaluations = getEvaluationsByBid(bid.getBidId());
            
            if (evaluations.isEmpty()) continue;
            
            // Calculate averages
            BigDecimal sumPrice = BigDecimal.ZERO;
            BigDecimal sumTechnical = BigDecimal.ZERO;
            BigDecimal sumDelivery = BigDecimal.ZERO;
            BigDecimal sumWeighted = BigDecimal.ZERO;
            
            for (Evaluation eval : evaluations) {
                if (eval.getPriceScore() != null) sumPrice = sumPrice.add(eval.getPriceScore());
                if (eval.getTechnicalScore() != null) sumTechnical = sumTechnical.add(eval.getTechnicalScore());
                if (eval.getDeliveryScore() != null) sumDelivery = sumDelivery.add(eval.getDeliveryScore());
                if (eval.getWeightedTotal() != null) sumWeighted = sumWeighted.add(eval.getWeightedTotal());
            }
            
            int count = evaluations.size();
            BigDecimal avgPrice = sumPrice.divide(new BigDecimal(count), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal avgTechnical = sumTechnical.divide(new BigDecimal(count), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal avgDelivery = sumDelivery.divide(new BigDecimal(count), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal avgWeighted = sumWeighted.divide(new BigDecimal(count), 2, java.math.RoundingMode.HALF_UP);
            
            // Store in final_scores table
            String sql = "INSERT INTO final_scores (bid_id, tender_id, avg_price_score, " +
                         "avg_technical_score, avg_delivery_score, final_weighted_total, calculated_date) " +
                         "VALUES (?, ?, ?, ?, ?, ?, NOW()) " +
                         "ON DUPLICATE KEY UPDATE " +
                         "avg_price_score = VALUES(avg_price_score), " +
                         "avg_technical_score = VALUES(avg_technical_score), " +
                         "avg_delivery_score = VALUES(avg_delivery_score), " +
                         "final_weighted_total = VALUES(final_weighted_total), " +
                         "calculated_date = NOW()";
            
            try (Connection conn = DBConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, bid.getBidId());
                pstmt.setInt(2, tenderId);
                pstmt.setBigDecimal(3, avgPrice);
                pstmt.setBigDecimal(4, avgTechnical);
                pstmt.setBigDecimal(5, avgDelivery);
                pstmt.setBigDecimal(6, avgWeighted);
                pstmt.executeUpdate();
                
            } catch (SQLException e) {
                logger.severe("Error storing final score: " + e.getMessage());
                return false;
            }
        }
        
        // Update rank positions
        updateRankPositions(tenderId);
        
        logger.info("Final scores calculated for tender: " + tenderId);
        return true;
    }
    
    private void updateRankPositions(int tenderId) {
        String sql = "UPDATE final_scores fs " +
                     "JOIN (SELECT bid_id, ROW_NUMBER() OVER (ORDER BY final_weighted_total DESC) as rank_pos " +
                     "FROM final_scores WHERE tender_id = ?) ranked ON fs.bid_id = ranked.bid_id " +
                     "SET fs.rank_position = ranked.rank_pos " +
                     "WHERE fs.tender_id = ?";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            pstmt.setInt(2, tenderId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.severe("Error updating rank positions: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isEvaluationComplete(int tenderId, int totalEvaluators) {
        int completedEvaluators = getCompletedEvaluatorCount(tenderId);
        return completedEvaluators >= totalEvaluators;
    }
    
    @Override
    public int getCompletedEvaluatorCount(int tenderId) {
        String sql = "SELECT COUNT(DISTINCT evaluator_id) as completed_count " +
                     "FROM evaluations e " +
                     "JOIN bids b ON e.bid_id = b.bid_id " +
                     "WHERE b.tender_id = ? " +
                     "GROUP BY b.tender_id";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("completed_count");
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting completed evaluator count: " + e.getMessage());
        }
        return 0;
    }
    
    @Override
    public int getBidCountForTender(int tenderId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE tender_id = ? AND is_active = true";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting bid count: " + e.getMessage());
        }
        return 0;
    }
    
    @Override
    public boolean deleteEvaluationsForTender(int tenderId) {
        String sql = "DELETE e FROM evaluations e " +
                     "JOIN bids b ON e.bid_id = b.bid_id " +
                     "WHERE b.tender_id = ?";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            int deleted = pstmt.executeUpdate();
            logger.info("Deleted " + deleted + " evaluations for tender: " + tenderId);
            return true;
            
        } catch (SQLException e) {
            logger.severe("Error deleting evaluations: " + e.getMessage());
        }
        return false;
    }
    
    private Evaluation mapResultSetToEvaluation(ResultSet rs) throws SQLException {
        Evaluation eval = new Evaluation();
        eval.setEvaluationId(rs.getInt("evaluation_id"));
        eval.setBidId(rs.getInt("bid_id"));
        eval.setEvaluatorId(rs.getInt("evaluator_id"));
        
        BigDecimal priceScore = rs.getBigDecimal("price_score");
        if (priceScore != null) eval.setPriceScore(priceScore);
        
        BigDecimal technicalScore = rs.getBigDecimal("technical_score");
        if (technicalScore != null) eval.setTechnicalScore(technicalScore);
        
        BigDecimal deliveryScore = rs.getBigDecimal("delivery_score");
        if (deliveryScore != null) eval.setDeliveryScore(deliveryScore);
        
        BigDecimal weightedTotal = rs.getBigDecimal("weighted_total");
        if (weightedTotal != null) eval.setWeightedTotal(weightedTotal);
        
        eval.setSubmittedDate(rs.getTimestamp("submitted_date"));
        return eval;
    }
    
    @Override
    public boolean isAllBidsFullyEvaluated(int tenderId, int requiredEvaluators) {
        // Get total number of bids for tender
        int totalBids = getBidCountForTender(tenderId);
        
        if (totalBids == 0) {
            return false; // No bids to evaluate
        }
        
        // Check if all bids have been evaluated by all required evaluators
        String sql = "SELECT COUNT(DISTINCT b.bid_id) as fully_evaluated_bids " +
                     "FROM bids b " +
                     "WHERE b.tender_id = ? " +
                     "AND (SELECT COUNT(DISTINCT evaluator_id) FROM evaluations WHERE bid_id = b.bid_id) >= ? " +
                     "GROUP BY b.tender_id";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tenderId);
            pstmt.setInt(2, requiredEvaluators);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int fullyEvaluatedBids = rs.getInt("fully_evaluated_bids");
                boolean complete = (fullyEvaluatedBids == totalBids);
                logger.info("Tender " + tenderId + ": " + fullyEvaluatedBids + "/" + totalBids + 
                           " bids fully evaluated by " + requiredEvaluators + " evaluators. Complete: " + complete);
                return complete;
            }
            
        } catch (SQLException e) {
            logger.severe("Error checking if all bids fully evaluated: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public int getTotalEvaluatorCount() {
        String sql = "SELECT COUNT(user_id) as evaluator_count FROM users WHERE role = 'EVALUATION_COMMITTEE'";
        
        try (Connection conn = DBConnectionPool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("evaluator_count");
                // Ensure minimum of 2 evaluators
                return Math.max(count, 2);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting total evaluator count: " + e.getMessage());
        }
        return 2; // Default to 2 if error
    }
}