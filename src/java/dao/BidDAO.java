
package dao;

import model.Bid;
import model.Award;
import java.math.BigDecimal;
import java.util.List;

public interface BidDAO {
    
    
    List<Bid> getRankedBidsByTender(int tenderId);
    boolean createAward(int tenderId, int winningBidId, int winningSupplierId, 
                        BigDecimal awardedValue, String justification, int awardedBy);
    Award getAwardByTender(int tenderId);
    boolean hasSupplierBidOnTender(int tenderId, int supplierId);
    
    
    /**
     * Create a new bid submission
     * @param bid Bid object to create
     * @return true if successful
     */
    boolean createBid(Bid bid);
    
    /**
     * Find bid by ID
     * @param bidId Bid ID
     * @return Bid object
     */
    Bid findById(int bidId);
    
    /**
     * Check if supplier has already submitted a bid for a tender
     * @param tenderId Tender ID
     * @param supplierId Supplier ID
     * @return true if bid exists
     */
    boolean hasSupplierBid(int tenderId, int supplierId);
    
    /**
     * Get all bids submitted by a supplier
     * @param supplierId Supplier ID
     * @return List of bids
     */
    List<Bid> getBidsBySupplier(int supplierId);
    
    /**
     * Get all bids for a specific tender
     * @param tenderId Tender ID
     * @return List of bids
     */
    List<Bid> getBidsByTender(int tenderId);
    
    /**
     * Get supplier's bid for a specific tender
     * @param tenderId Tender ID
     * @param supplierId Supplier ID
     * @return Bid object or null
     */
    Bid getSupplierBidForTender(int tenderId, int supplierId);
    
    /**
     * Check if tender is still open for bidding
     * @param tenderId Tender ID
     * @return true if open and deadline not passed
     */
    boolean isTenderOpenForBidding(int tenderId);
    
    /**
     * Get the lowest bid amount for a tender (for price score calculation)
     * @param tenderId Tender ID
     * @return Lowest bid amount
     */
    BigDecimal getLowestBidAmount(int tenderId);
    
    /**
     * Get the shortest delivery timeline for a tender (for delivery score calculation)
     * @param tenderId Tender ID
     * @return Shortest timeline in days
     */
    int getShortestDeliveryTimeline(int tenderId);
    
    /**
     * Get award outcome for a supplier on a tender
     * @param tenderId Tender ID
     * @param supplierId Supplier ID
     * @return "WON", "NOT_WON", or null if not awarded
     */
    String getAwardOutcome(int tenderId, int supplierId);
}
