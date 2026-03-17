package com.sfexplorer.db;

import java.sql.*;

public class UserRepository {
    private final Database db;

    public UserRepository(Database db) {
        this.db = db;
    }

    public boolean anyUsersExist() throws SQLException {
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) AS c FROM users")) {
            return rs.next() && rs.getInt("c") > 0;
        }
    }

    public boolean userExists(String username) throws SQLException {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void createUser(String username, String saltB64, String hashB64, int iterations) throws SQLException {
        String sql = "INSERT INTO users(username,salt_b64,hash_b64,iterations) VALUES(?,?,?,?)";
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, saltB64);
            ps.setString(3, hashB64);
            ps.setInt(4, iterations);
            ps.executeUpdate();
        }
    }

    public UserRecord getUser(String username) throws SQLException {
        String sql = "SELECT username,salt_b64,hash_b64,iterations FROM users WHERE username=?";
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new UserRecord(
                        rs.getString("username"),
                        rs.getString("salt_b64"),
                        rs.getString("hash_b64"),
                        rs.getInt("iterations")
                );
            }
        }
    }

    public record UserRecord(String username, String saltB64, String hashB64, int iterations) {}
}
