/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author legacy
 */

import java.math.BigDecimal;
import java.util.Date;

/**
 * JavaBean representing a Contract Award
 * Module 2: Award Management
 */
public class Award {
    
    private int awardId;
    private int tenderId;
    private int winningBidId;
    private int winningSupplierId;
    private String winningSupplierCompany;
    private String winningSupplierRegNumber;
    private BigDecimal awardedValue;
    private String justification;
    private Date awardDate;
    private int awardedBy;
    private String awardedByName;
    private boolean noticeGenerated;
    
    // Constructors
    public Award() {}
    
    // Getters and Setters
    public int getAwardId() { return awardId; }
    public void setAwardId(int awardId) { this.awardId = awardId; }
    
    public int getTenderId() { return tenderId; }
    public void setTenderId(int tenderId) { this.tenderId = tenderId; }
    
    public int getWinningBidId() { return winningBidId; }
    public void setWinningBidId(int winningBidId) { this.winningBidId = winningBidId; }
    
    public int getWinningSupplierId() { return winningSupplierId; }
    public void setWinningSupplierId(int winningSupplierId) { this.winningSupplierId = winningSupplierId; }
    
    public String getWinningSupplierCompany() { return winningSupplierCompany; }
    public void setWinningSupplierCompany(String winningSupplierCompany) { this.winningSupplierCompany = winningSupplierCompany; }
    
    public String getWinningSupplierRegNumber() { return winningSupplierRegNumber; }
    public void setWinningSupplierRegNumber(String winningSupplierRegNumber) { this.winningSupplierRegNumber = winningSupplierRegNumber; }
    
    public BigDecimal getAwardedValue() { return awardedValue; }
    public void setAwardedValue(BigDecimal awardedValue) { this.awardedValue = awardedValue; }
    
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    
    public Date getAwardDate() { return awardDate; }
    public void setAwardDate(Date awardDate) { this.awardDate = awardDate; }
    
    public int getAwardedBy() { return awardedBy; }
    public void setAwardedBy(int awardedBy) { this.awardedBy = awardedBy; }
    
    public String getAwardedByName() { return awardedByName; }
    public void setAwardedByName(String awardedByName) { this.awardedByName = awardedByName; }
    
    public boolean isNoticeGenerated() { return noticeGenerated; }
    public void setNoticeGenerated(boolean noticeGenerated) { this.noticeGenerated = noticeGenerated; }
}