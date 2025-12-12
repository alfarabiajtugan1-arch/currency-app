package org.example.db;

import org.example.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UserDAO {

    private static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean register(String name, String email, String plainPassword) {
        String sql = "INSERT INTO users(name, email, password) VALUES(?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hashPassword(plainPassword));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static User login(String name, String plainPassword) {
        String sql = "SELECT id, name, email, password FROM users WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            String storedHash = rs.getString("password");
            String givenHash = hashPassword(plainPassword);

            if (!storedHash.equals(givenHash)) return null;

            return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                storedHash
            );

        } catch (SQLException e) {
            return null;
        }
    }
}
