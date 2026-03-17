package com.sfexplorer.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Global check-in counter for each place.
 *
 * Why this exists: it demonstrates transactional atomicity and
 * safe updates under concurrency. The increment uses an UPSERT that is
 * atomic inside SQLite.
 */
public class CheckinRepository {
    private final Database db;

    public CheckinRepository(Database db) {
        this.db = db;
    }

    /** Atomically increments the counter for the place and returns the new count. */
    public int incrementAndGet(String placeId) throws SQLException {
        return withRetry(() -> {
            try (Connection c = db.connect()) {
                c.setAutoCommit(false);
                try {
                    // Atomic UPSERT
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO checkins(place_id, count, updated_at) VALUES(?, 1, datetime('now')) " +
                            "ON CONFLICT(place_id) DO UPDATE SET count = count + 1, updated_at=datetime('now')")) {
                        ps.setString(1, placeId);
                        ps.executeUpdate();
                    }

                    int count;
                    try (PreparedStatement ps = c.prepareStatement("SELECT count FROM checkins WHERE place_id=?")) {
                        ps.setString(1, placeId);
                        try (ResultSet rs = ps.executeQuery()) {
                            count = rs.next() ? rs.getInt(1) : 0;
                        }
                    }

                    c.commit();
                    return count;
                } catch (SQLException e) {
                    c.rollback();
                    throw e;
                } finally {
                    c.setAutoCommit(true);
                }
            }
        });
    }

    public int getCount(String placeId) throws SQLException {
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(
                "SELECT count FROM checkins WHERE place_id=?")) {
            ps.setString(1, placeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // --- small retry helper for "database is locked" when multiple app instances write ---

    private interface SqlSupplier<T> { T get() throws SQLException; }

    private <T> T withRetry(SqlSupplier<T> op) throws SQLException {
        SQLException last = null;
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                return op.get();
            } catch (SQLException e) {
                last = e;
                String msg = (e.getMessage() == null) ? "" : e.getMessage().toLowerCase();
                boolean locked = msg.contains("locked") || msg.contains("busy");
                if (!locked || attempt == 3) throw e;
                try {
                    Thread.sleep(80L * (attempt + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw last;
    }
}
