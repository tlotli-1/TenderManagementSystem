
package dao;

import java.util.Date;
import model.Tender;
import java.util.List;

/**
 * DAO Interface for Tender operations
 * Module 5: Data Persistence Layer requirement
 */
public interface TenderDAO {
    
    // Create
    boolean createTender(Tender tender);
    
    // Read
    Tender findById(int tenderId);
    Tender findByReferenceNumber(String referenceNumber);
    List<Tender> findAllTenders();
    List<Tender> findTendersByStatus(String status);
    List<Tender> findTendersByCategory(String category);
    List<Tender> findOpenTenders();  // For suppliers
    List<Tender> findTendersByProcurementOfficer(int officerId);
    
    // Update
    boolean updateTender(Tender tender);
    boolean updateTenderStatus(int tenderId, String status);
    boolean updateTenderStatusWithDate(int tenderId, String status, Date date);
    
    // Delete (soft delete or archive - not required by exam, but useful)
    boolean deleteTender(int tenderId);
    
    // Validation
    boolean canEditTender(int tenderId);  // Only Draft status
    boolean canPublishTender(int tenderId);  // Draft -> Open
    
    // Counts
    int countTendersByStatus(String status);
    
    // Generate reference number (MPW-YYYY-NNNN)
    String generateReferenceNumber();
    
    // Auto-close expired tenders (called by scheduled job or during login)
    int autoCloseExpiredTenders();
}