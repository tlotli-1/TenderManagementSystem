
package dao;

import dao.UserDAO;
import model.User;
import util.DBConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of UserDAO interface Handles all database operations for User
 * entity
 */
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = Logger.getLogger(UserDAOImpl.class.getName());

    @Override
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, role, "
                + "registration_number, company_name, contact_number, physical_address) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getRegistrationNumber());
            pstmt.setString(7, user.getCompanyName());
            pstmt.setString(8, user.getContactNumber());
            pstmt.setString(9, user.getPhysicalAddress());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
                logger.info("User created successfully: " + user.getUsername());
                return true;
            }

        } catch (SQLException e) {
            logger.severe("Error creating user: " + e.getMessage());
        }
        return false;
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            logger.severe("Error finding user by username: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            logger.severe("Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            logger.severe("Error finding user by ID: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateFailedAttempts(int userId, int attempts) {
        String sql = "UPDATE users SET failed_attempts = ? WHERE user_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, attempts);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.severe("Error updating failed attempts: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean setAccountLocked(int userId, boolean locked) {
        String sql = "UPDATE users SET account_locked = ?, locked_until = NULL WHERE user_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, locked);
            pstmt.setInt(2, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.severe("Error setting account lock: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean setAccountLockWithUnlockTime(int userId, boolean locked, java.util.Date lockedUntil) {
        String sql = "UPDATE users SET account_locked = ?, locked_until = ? WHERE user_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, locked);
            if (lockedUntil == null) {
                pstmt.setNull(2, java.sql.Types.TIMESTAMP);
            } else {
                pstmt.setTimestamp(2, new java.sql.Timestamp(lockedUntil.getTime()));
            }
            pstmt.setInt(3, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Error setting account lock with unlock time: " + e.getMessage());
        }
        return false;
    }

    @Override
    public java.util.Date getLockedUntil(int userId) {
        String sql = "SELECT locked_until FROM users WHERE user_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Timestamp ts = rs.getTimestamp("locked_until");
                    if (ts == null) return null;
                    return new java.util.Date(ts.getTime());
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting locked_until: " + e.getMessage());
        }
        return null;
    }


    @Override
    public boolean updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            logger.severe("Error updating last login: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean resetFailedAttempts(int userId) {
        return updateFailedAttempts(userId, 0);
    }

    @Override
    public List<User> getAllSuppliers() {
        List<User> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'SUPPLIER'";

        try (Connection conn = DBConnectionPool.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                suppliers.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            logger.severe("Error getting all suppliers: " + e.getMessage());
        }
        return suppliers;
    }

    @Override
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            logger.severe("Error checking username existence: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = DBConnectionPool.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            logger.severe("Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    @Override
    public String generateRegistrationNumber() {
        String sql = "SELECT COUNT(*) FROM users WHERE registration_number LIKE 'REG-"
                + java.time.Year.now().getValue() + "-%'";

        try (Connection conn = DBConnectionPool.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }

            // Format: REG-2025-0001, REG-2025-0002, etc.
            return String.format("REG-%d-%04d",
                    java.time.Year.now().getValue(), count + 1);

        } catch (SQLException e) {
            logger.severe("Error generating registration number: " + e.getMessage());
            // Fallback with timestamp
            return String.format("REG-%d-%d",
                    java.time.Year.now().getValue(), System.currentTimeMillis());
        }
    }

    /**
     * Helper method to map ResultSet to User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setRegistrationNumber(rs.getString("registration_number"));
        user.setCompanyName(rs.getString("company_name"));
        user.setContactNumber(rs.getString("contact_number"));
        user.setPhysicalAddress(rs.getString("physical_address"));
        user.setAccountLocked(rs.getBoolean("account_locked"));
        user.setFailedAttempts(rs.getInt("failed_attempts"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setLastLogin(rs.getTimestamp("last_login"));
        return user;
    }
}
