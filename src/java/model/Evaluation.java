
package model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * JavaBean representing an Evaluation (scores per evaluator per bid)
 * Module 4: Bid Evaluation
 */
public class Evaluation {
    
    private int evaluationId;
private int tenderId;
    private int bidId;
    private int evaluatorId;
    private BigDecimal priceScore;      // Calculated automatically (0-100)
    private BigDecimal technicalScore;   // Entered by evaluator (0-100)
    private BigDecimal deliveryScore;    // Calculated automatically (0-100)
    private BigDecimal weightedTotal;    // Calculated (Price*0.40 + Technical*0.35 + Delivery*0.25)
    private Date submittedDate;
    
    // Additional fields for display
    private String evaluatorName;
    private String supplierCompany;
    private BigDecimal bidAmount;
    private int deliveryTimeline;
    private String tenderTitle;
    private String tenderReference;
    
    // Constructors
    public Evaluation() {}
    
    public Evaluation(int bidId, int evaluatorId, BigDecimal technicalScore) {
        this.bidId = bidId;
        this.evaluatorId = evaluatorId;
        this.technicalScore = technicalScore;
    }
    
    // Getters and Setters
public int getEvaluationId() { return evaluationId; }
    public void setEvaluationId(int evaluationId) { this.evaluationId = evaluationId; }
    
    public int getTenderId() { return tenderId; }
    public void setTenderId(int tenderId) { this.tenderId = tenderId; }
    
    public int getBidId() { return bidId; }
    public void setBidId(int bidId) { this.bidId = bidId; }
    
    public int getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(int evaluatorId) { this.evaluatorId = evaluatorId; }
    
    public BigDecimal getPriceScore() { return priceScore; }
    public void setPriceScore(BigDecimal priceScore) { this.priceScore = priceScore; }
    
    public BigDecimal getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(BigDecimal technicalScore) { this.technicalScore = technicalScore; }
    
    public BigDecimal getDeliveryScore() { return deliveryScore; }
    public void setDeliveryScore(BigDecimal deliveryScore) { this.deliveryScore = deliveryScore; }
    
    public BigDecimal getWeightedTotal() { return weightedTotal; }
    public void setWeightedTotal(BigDecimal weightedTotal) { this.weightedTotal = weightedTotal; }
    
    public Date getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(Date submittedDate) { this.submittedDate = submittedDate; }
    
    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }
    
    public String getSupplierCompany() { return supplierCompany; }
    public void setSupplierCompany(String supplierCompany) { this.supplierCompany = supplierCompany; }
    
    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal bidAmount) { this.bidAmount = bidAmount; }
    
    public int getDeliveryTimeline() { return deliveryTimeline; }
    public void setDeliveryTimeline(int deliveryTimeline) { this.deliveryTimeline = deliveryTimeline; }
    
    public String getTenderTitle() { return tenderTitle; }
    public void setTenderTitle(String tenderTitle) { this.tenderTitle = tenderTitle; }
    
    public String getTenderReference() { return tenderReference; }
    public void setTenderReference(String tenderReference) { this.tenderReference = tenderReference; }
}