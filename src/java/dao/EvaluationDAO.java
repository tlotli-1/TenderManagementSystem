
package dao;

import java.util.List;
import model.Bid;
import model.Evaluation;

/**
 * DAO Interface for Evaluation operations
 * Module 4: Bid Evaluation
 */
public interface EvaluationDAO {
    
    /**
     * Create or update an evaluation (UPSERT)
     * @param evaluation Evaluation object
     * @return true if successful
     */
    boolean saveEvaluation(Evaluation evaluation);
    
    /**
     * Find evaluation by bid ID and evaluator ID
     * @param bidId Bid ID
     * @param evaluatorId Evaluator ID
     * @return Evaluation object or null
     */
    Evaluation findByBidAndEvaluator(int bidId, int evaluatorId);
    
    /**
     * Get all evaluations for a specific bid
     * @param bidId Bid ID
     * @return List of evaluations
     */
    List<Evaluation> getEvaluationsByBid(int bidId);
    
    /**
     * Get all evaluations for a tender
     * @param tenderId Tender ID
     * @return List of evaluations
     */
    List<Evaluation> getEvaluationsByTender(int tenderId);
    
    /**
     * Get all evaluations submitted by a specific evaluator
     * @param evaluatorId Evaluator ID
     * @return List of evaluations
     */
    List<Evaluation> getEvaluationsByEvaluator(int evaluatorId);
    
    /**
     * Check if an evaluator has already submitted scores for a bid
     * @param bidId Bid ID
     * @param evaluatorId Evaluator ID
     * @return true if already submitted
     */
    boolean hasEvaluatorSubmitted(int bidId, int evaluatorId);
    
    /**
     * Get all bids that an evaluator hasn't scored yet for a tender
     * @param tenderId Tender ID
     * @param evaluatorId Evaluator ID
     * @return List of bids pending evaluation
     */
    List<Bid> getPendingBidsForEvaluator(int tenderId, int evaluatorId);
    
    /**
     * Get all bids for a tender with their averaged final scores
     * @param tenderId Tender ID
     * @return List of bids with final scores
     */
    List<Bid> getBidsWithFinalScores(int tenderId);
    
    /**
     * Calculate and store final scores for all bids in a tender
     * Averages all evaluator scores and updates final_scores table
     * @param tenderId Tender ID
     * @return true if successful
     */
    boolean calculateAndStoreFinalScores(int tenderId);
    
    /**
     * Check if all evaluators have submitted scores for all bids in a tender
     * @param tenderId Tender ID
     * @param totalEvaluators Number of evaluators assigned
     * @return true if all evaluations are complete
     */
    boolean isEvaluationComplete(int tenderId, int totalEvaluators);
    
    /**
     * Get the number of evaluators who have completed scoring for a tender
     * @param tenderId Tender ID
     * @return Count of evaluators who have submitted all scores
     */
    int getCompletedEvaluatorCount(int tenderId);
    
    /**
     * Get total number of bids for a tender
     * @param tenderId Tender ID
     * @return Number of bids
     */
    int getBidCountForTender(int tenderId);
    
    /**
     * Delete evaluations for a tender (for resetting evaluation)
     * @param tenderId Tender ID
     * @return true if successful
     */
    boolean deleteEvaluationsForTender(int tenderId);
    
    /**
     * Check if all bids for a tender have been evaluated by all evaluators
     * @param tenderId Tender ID
     * @param requiredEvaluators Number of evaluators required
     * @return true if all bids are fully evaluated
     */
    boolean isAllBidsFullyEvaluated(int tenderId, int requiredEvaluators);
    
    /**
     * Get total count of evaluation committee members
     * @return Count of users with EVALUATION_COMMITTEE role
     */
    int getTotalEvaluatorCount();
}