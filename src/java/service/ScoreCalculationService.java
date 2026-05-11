
package service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Dedicated service class for score calculations
 * Module 4: Bid Evaluation
 * 
 * All score calculations must be in this service class - not in Servlet or JSP
 * 
 * Weighted Score Formula:
 * Final Score = (Price Score × 0.40) + (Technical Score × 0.35) + (Delivery Score × 0.25)
 * 
 * Where:
 * - Price Score = (Lowest Bid Amount / This Bid Amount) × 100
 * - Delivery Score = (Shortest Timeline / This Bid's Timeline) × 100
 * - Technical Score = Entered by evaluator (0-100)
 */
public class ScoreCalculationService {
    
    private static final BigDecimal PRICE_WEIGHT = new BigDecimal("0.40");
    private static final BigDecimal TECHNICAL_WEIGHT = new BigDecimal("0.35");
    private static final BigDecimal DELIVERY_WEIGHT = new BigDecimal("0.25");
    
    /**
     * Calculate Price Score
     * Formula: (Lowest Bid Amount / This Bid Amount) × 100
     * 
     * @param lowestBidAmount The lowest bid amount for this tender
     * @param thisBidAmount The bid amount being evaluated
     * @return Price score (0-100), rounded to 2 decimal places
     */
    public BigDecimal calculatePriceScore(BigDecimal lowestBidAmount, BigDecimal thisBidAmount) {
        if (lowestBidAmount == null || thisBidAmount == null || lowestBidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        // (Lowest Bid / This Bid) * 100
        BigDecimal ratio = lowestBidAmount.divide(thisBidAmount, 4, RoundingMode.HALF_UP);
        BigDecimal score = ratio.multiply(new BigDecimal("100"));
        
        // Ensure score doesn't exceed 100
        if (score.compareTo(new BigDecimal("100")) > 0) {
            score = new BigDecimal("100");
        }
        
        return score.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate Delivery Score
     * Formula: (Shortest Timeline / This Bid's Timeline) × 100
     * 
     * @param shortestTimeline The shortest delivery timeline for this tender (in days)
     * @param thisBidTimeline The bid's proposed timeline (in days)
     * @return Delivery score (0-100), rounded to 2 decimal places
     */
    public BigDecimal calculateDeliveryScore(int shortestTimeline, int thisBidTimeline) {
        if (shortestTimeline == 0 || thisBidTimeline == 0) {
            return BigDecimal.ZERO;
        }
        
        // (Shortest Timeline / This Timeline) * 100
        double ratio = (double) shortestTimeline / thisBidTimeline;
        double score = ratio * 100;
        
        // Ensure score doesn't exceed 100
        if (score > 100) {
            score = 100;
        }
        
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate Weighted Total Score for a single evaluator
     * Formula: (Price Score × 0.40) + (Technical Score × 0.35) + (Delivery Score × 0.25)
     * 
     * @param priceScore Calculated price score (0-100)
     * @param technicalScore Entered technical score (0-100)
     * @param deliveryScore Calculated delivery score (0-100)
     * @return Weighted total score, rounded to 2 decimal places
     */
    public BigDecimal calculateWeightedTotal(BigDecimal priceScore, BigDecimal technicalScore, BigDecimal deliveryScore) {
        if (priceScore == null) priceScore = BigDecimal.ZERO;
        if (technicalScore == null) technicalScore = BigDecimal.ZERO;
        if (deliveryScore == null) deliveryScore = BigDecimal.ZERO;
        
        BigDecimal priceContribution = priceScore.multiply(PRICE_WEIGHT);
        BigDecimal technicalContribution = technicalScore.multiply(TECHNICAL_WEIGHT);
        BigDecimal deliveryContribution = deliveryScore.multiply(DELIVERY_WEIGHT);
        
        BigDecimal total = priceContribution.add(technicalContribution).add(deliveryContribution);
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate Final Score by averaging all evaluator scores for a bid
     * 
     * @param evaluatorScores List of weighted totals from all evaluators
     * @return Average score, rounded to 2 decimal places
     */
    public BigDecimal calculateFinalScore(java.util.List<BigDecimal> evaluatorScores) {
        if (evaluatorScores == null || evaluatorScores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal score : evaluatorScores) {
            if (score != null) {
                sum = sum.add(score);
            }
        }
        
        BigDecimal average = sum.divide(new BigDecimal(evaluatorScores.size()), 4, RoundingMode.HALF_UP);
        return average.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Validate technical score is within valid range (0-100)
     * 
     * @param technicalScore The technical score to validate
     * @return true if valid
     */
    public boolean isValidTechnicalScore(BigDecimal technicalScore) {
        if (technicalScore == null) return false;
        return technicalScore.compareTo(BigDecimal.ZERO) >= 0 && 
               technicalScore.compareTo(new BigDecimal("100")) <= 0;
    }
    
    /**
     * Parse technical score from string input
     * 
     * @param scoreStr String representation of score
     * @return BigDecimal score or null if invalid
     */
    public BigDecimal parseTechnicalScore(String scoreStr) {
        try {
            BigDecimal score = new BigDecimal(scoreStr);
            if (isValidTechnicalScore(score)) {
                return score.setScale(2, RoundingMode.HALF_UP);
            }
        } catch (NumberFormatException e) {
            // Invalid format
        }
        return null;
    }
    
    /**
     * Worked Example for Documentation (matches exam requirement)
     * 
     * Example:
     * - Lowest Bid Amount: M 100,000
     * - This Bid Amount: M 125,000
     * - Price Score = (100,000 / 125,000) × 100 = 80.00
     * - Price Contribution = 80.00 × 0.40 = 32.00
     * 
     * - Technical Score entered: 85.00
     * - Technical Contribution = 85.00 × 0.35 = 29.75
     * 
     * - Shortest Timeline: 30 days
     * - This Timeline: 40 days
     * - Delivery Score = (30 / 40) × 100 = 75.00
     * - Delivery Contribution = 75.00 × 0.25 = 18.75
     * 
     * - FINAL SCORE = 32.00 + 29.75 + 18.75 = 80.50
     */
    public String getWorkedExample() {
        return "Worked Example:\n" +
               "Lowest Bid: 100,000 | This Bid: 125,000\n" +
               "Price Score = (100,000/125,000) × 100 = 80.00 × 0.40 = 32.00\n\n" +
               "Technical Score = 85.00 × 0.35 = 29.75\n\n" +
               "Shortest Timeline: 30 days | This Timeline: 40 days\n" +
               "Delivery Score = (30/40) × 100 = 75.00 × 0.25 = 18.75\n\n" +
               "FINAL SCORE = 32.00 + 29.75 + 18.75 = 80.50";
    }
}