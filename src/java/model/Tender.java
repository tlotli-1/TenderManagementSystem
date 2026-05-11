/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * JavaBean representing a Tender in the ProcureGov system
 * Module 2: Tender Management
 */
public class Tender {
    
    private int tenderId;
    private String referenceNumber;  // Format: MPW-YYYY-NNNN
    private String title;
    private String category;  // Construction, Roads, Electrical, Plumbing, General Services
    private String description;
    private BigDecimal estimatedValue;  // Maloti
    private LocalDateTime submissionDeadline;
    private String status;  // Draft, Open, Closed, Under_Evaluation, Evaluated, Awarded
    private String noticeFilePath;
    private int createdBy;
    private Date createdDate;
    private Date closedDate;
    private Date evaluatedDate;
    private Date awardedDate;
    
    // Constructors
    public Tender() {}
    
    public Tender(String title, String category, String description, 
                  BigDecimal estimatedValue, LocalDateTime submissionDeadline) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.estimatedValue = estimatedValue;
        this.submissionDeadline = submissionDeadline;
        this.status = "Draft";
    }
    
    // Getters and Setters
    public int getTenderId() { return tenderId; }
    public void setTenderId(int tenderId) { this.tenderId = tenderId; }
    
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(BigDecimal estimatedValue) { this.estimatedValue = estimatedValue; }
    
    public LocalDateTime getSubmissionDeadline() { return submissionDeadline; }
    public void setSubmissionDeadline(LocalDateTime submissionDeadline) { this.submissionDeadline = submissionDeadline; }

    public Date getSubmissionDeadlineAsDate() {
        if (submissionDeadline == null) {
            return null;
        }
        return Timestamp.valueOf(submissionDeadline);
    }

    public String getSubmissionDeadlineIsoValue() {
        if (submissionDeadline == null) {
            return "";
        }
        return submissionDeadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getNoticeFilePath() { return noticeFilePath; }
    public void setNoticeFilePath(String noticeFilePath) { this.noticeFilePath = noticeFilePath; }
    
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    
    public Date getClosedDate() { return closedDate; }
    public void setClosedDate(Date closedDate) { this.closedDate = closedDate; }
    
    public Date getEvaluatedDate() { return evaluatedDate; }
    public void setEvaluatedDate(Date evaluatedDate) { this.evaluatedDate = evaluatedDate; }
    
    public Date getAwardedDate() { return awardedDate; }
    public void setAwardedDate(Date awardedDate) { this.awardedDate = awardedDate; }
    
    // Helper methods
    public boolean isEditable() {
        return "Draft".equals(this.status);
    }
    
    public boolean isOpen() {
        return "Open".equals(this.status);
    }
    
    public boolean isClosed() {
        return "Closed".equals(this.status);
    }
    
    public boolean isUnderEvaluation() {
        return "Under_Evaluation".equals(this.status);
    }
    
    public boolean isEvaluated() {
        return "Evaluated".equals(this.status);
    }
    
    public boolean isAwarded() {
        return "Awarded".equals(this.status);
    }
    
    public boolean isDeadlinePassed() {
        return LocalDateTime.now().isAfter(submissionDeadline);
    }
}