
package dao;

import model.User;
import java.util.List;

public interface UserDAO {
    
    boolean createUser(User user);
    
    User findByUsername(String username);
    
    User findByEmail(String email);
    
    User findById(int userId);
    
    boolean updateFailedAttempts(int userId, int attempts);
    

    boolean setAccountLocked(int userId, boolean locked);

    /**
     * Temporarily locks an account until the provided timestamp.
     * If lockedUntil is null, the account is treated as locked indefinitely.
     */
    boolean setAccountLockWithUnlockTime(int userId, boolean locked, java.util.Date lockedUntil);

    /**
     * @return the timestamp until which the account remains locked, or null if not locked / no unlock time.
     */
    java.util.Date getLockedUntil(int userId);

    boolean updateLastLogin(int userId);

    
    boolean resetFailedAttempts(int userId);
    
    List<User> getAllSuppliers();
    
    boolean usernameExists(String username);
    
    boolean emailExists(String email);
    
    String generateRegistrationNumber();  
}