
package model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * JavaBean representing a Bid submitted by a supplier
 * Module 3: Supplier Bid Submission
 */
public class Bid {
    
    private int bidId;
    private int tenderId;
    private int supplierId;
    private BigDecimal bidAmount;
    private String technicalStatement;
    private int deliveryTimeline;  // Days
    private String supportingDocPath;
    private Date submissionDate;
    private boolean isActive;
    
    // Additional fields for display (not stored in bids table)
    private String tenderTitle;
    private String tenderReference;
    private String tenderStatus;
    private String supplierCompany;
    private String supplierUsername;
    
    // Evaluation-related fields (from final_scores table)
    private BigDecimal priceScore;
    private BigDecimal technicalScore;
    private BigDecimal deliveryScore;
    private BigDecimal finalWeightedScore;
    private BigDecimal finalTechnicalScore;
    private String awardOutcome;  // WON, NOT_WON, PENDING
    
    // Constructors
    public Bid() {}
    
    public Bid(int tenderId, int supplierId, BigDecimal bidAmount, 
               String technicalStatement, int deliveryTimeline, String supportingDocPath) {
        this.tenderId = tenderId;
        this.supplierId = supplierId;
        this.bidAmount = bidAmount;
        this.technicalStatement = technicalStatement;
        this.deliveryTimeline = deliveryTimeline;
        this.supportingDocPath = supportingDocPath;
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }
    
    public int getTenderId() { return tenderId; }
    public void setTenderId(int tenderId) { this.tenderId = tenderId; }
    
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    
    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
    
    public String getTechnicalStatement() { return technicalStatement; }
    public void setTechnicalStatement(String technicalStatement) { this.technicalStatement = technicalStatement; }
    
    public int getDeliveryTimeline() { return deliveryTimeline; }
    public void setDeliveryTimeline(int deliveryTimeline) { this.deliveryTimeline = deliveryTimeline; }
    
    public String getSupportingDocPath() { return supportingDocPath; }
    public void setSupportingDocPath(String supportingDocPath) { this.supportingDocPath = supportingDocPath; }
    
    public Date getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(Date submissionDate) { this.submissionDate = submissionDate; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getTenderTitle() { return tenderTitle; }
    public void setTenderTitle(String tenderTitle) { this.tenderTitle = tenderTitle; }
    
    public String getTenderReference() { return tenderReference; }
    public void setTenderReference(String tenderReference) { this.tenderReference = tenderReference; }
    
    public String getTenderStatus() { return tenderStatus; }
    public void setTenderStatus(String tenderStatus) { this.tenderStatus = tenderStatus; }
    
    public String getSupplierCompany() { return supplierCompany; }
    public void setSupplierCompany(String supplierCompany) { this.supplierCompany = supplierCompany; }
    
    public String getSupplierUsername() { return supplierUsername; }
    public void setSupplierUsername(String supplierUsername) { this.supplierUsername = supplierUsername; }
    
    public BigDecimal getPriceScore() { return priceScore; }
    public void setPriceScore(BigDecimal priceScore) { this.priceScore = priceScore; }
    
    public BigDecimal getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(BigDecimal technicalScore) { this.technicalScore = technicalScore; }
    
    public BigDecimal getDeliveryScore() { return deliveryScore; }
    public void setDeliveryScore(BigDecimal deliveryScore) { this.deliveryScore = deliveryScore; }
    
    public BigDecimal getFinalWeightedScore() { return finalWeightedScore; }
    public void setFinalWeightedScore(BigDecimal finalWeightedScore) { this.finalWeightedScore = finalWeightedScore; }
    
    public BigDecimal getFinalTechnicalScore() { return finalTechnicalScore; }
    public void setFinalTechnicalScore(BigDecimal finalTechnicalScore) { this.finalTechnicalScore = finalTechnicalScore; }
    
    public String getAwardOutcome() { return awardOutcome; }
    public void setAwardOutcome(String awardOutcome) { this.awardOutcome = awardOutcome; }
    
    // Helper methods
    public boolean isBidAmountValid(BigDecimal estimatedValue) {
        return bidAmount != null && bidAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isTechnicalStatementValid() {
        return technicalStatement != null && technicalStatement.length() <= 600;
    }
}