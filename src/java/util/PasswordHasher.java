/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.math.BigInteger;

/**
 * Utility class for password hashing using SHA-256
 * Module 1: Authentication Requirement
 */
public class PasswordHasher {
    
    /**
     * Generates SHA-256 hash of the password
     * @param password Plain text password to hash
     * @return Hexadecimal string representation of the hash
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            
            // Pad with leading zeros to ensure 64 characters
            while (hexString.length() < 64) {
                hexString.insert(0, '0');
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
    
    /**
     * Verifies if a password matches the stored hash
     * @param plainPassword The password entered by user
     * @param storedHash The hash stored in database
     * @return true if password matches
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equals(storedHash);
    }
}