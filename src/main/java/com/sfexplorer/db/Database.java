package com.sfexplorer.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class Database {
    private final Path dbDir;
    private final Path dbPath;

    public Database() {
        String home = System.getProperty("user.home");
        this.dbDir = Paths.get(home, ".sfexplorer");
        this.dbPath = dbDir.resolve("sfexplorer.db");
    }

    public void init() throws Exception {
        Files.createDirectories(dbDir);

        // Load SQLite driver (xerial)
        Class.forName("org.sqlite.JDBC");

        try (Connection c = connect(); Statement st = c.createStatement()) {
            // Concurrency-friendly SQLite settings
            st.execute("PRAGMA foreign_keys=ON");
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("PRAGMA busy_timeout=5000");

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            " username TEXT PRIMARY KEY," +
                            " salt_b64 TEXT NOT NULL," +
                            " hash_b64 TEXT NOT NULL," +
                            " iterations INTEGER NOT NULL" +
                            ");"
            );

            // If an earlier version of the project created a favorites table without usernames,
            // migrate it forward so the app keeps working.
            if (tableExists(c, "favorites") && !tableHasColumn(c, "favorites", "username")) {
                st.executeUpdate("ALTER TABLE favorites RENAME TO favorites_v1_backup");
            }

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS favorites (" +
                            " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            " username TEXT NOT NULL," +
                            " place_id TEXT NOT NULL," +
                            " category TEXT NOT NULL," +
                            " name TEXT NOT NULL," +
                            " address TEXT," +
                            " description TEXT," +
                            " extra TEXT," +
                            " source_url TEXT," +
                            " created_at TEXT NOT NULL DEFAULT (datetime('now'))," +
                            " UNIQUE(username, place_id)," +
                            " FOREIGN KEY(username) REFERENCES users(username)" +
                            ");"
            );

            // Global, concurrency-safe counter for places (demonstrates atomic transactions)
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS checkins (" +
                            " place_id TEXT PRIMARY KEY," +
                            " count INTEGER NOT NULL DEFAULT 0," +
                            " updated_at TEXT NOT NULL DEFAULT (datetime('now'))" +
                            ");"
            );
        }
    }

    public Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + dbPath.toAbsolutePath());
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys=ON");
            st.execute("PRAGMA busy_timeout=5000");
        } catch (SQLException ignored) {
            // best-effort pragmas
        }
        return c;
    }

    public Path getDbPath() {
        return dbPath;
    }

    private boolean tableExists(Connection c, String table) {
        try (ResultSet rs = c.getMetaData().getTables(null, null, table, new String[]{"TABLE"})) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean tableHasColumn(Connection c, String table, String column) {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                String colName = rs.getString("name");
                if (column.equalsIgnoreCase(colName)) return true;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }
}
