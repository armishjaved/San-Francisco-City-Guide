package com.sfexplorer.ui;

import com.sfexplorer.db.Database;
import com.sfexplorer.db.FavoritesRepository;
import com.sfexplorer.db.CheckinRepository;
import com.sfexplorer.model.Category;
import com.sfexplorer.service.PlacesService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame(Database db, String username) {
        super("SF Explorer — San Francisco City Guide");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);

        PlacesService service = new PlacesService();
        FavoritesRepository favorites = new FavoritesRepository(db);
        CheckinRepository checkins = new CheckinRepository(db);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab(Category.EAT_AND_DRINK.label(), new CategoryPanel(Category.EAT_AND_DRINK, service, favorites, checkins, username));
        tabs.addTab(Category.ATTRACTIONS.label(), new CategoryPanel(Category.ATTRACTIONS, service, favorites, checkins, username));
        tabs.addTab(Category.EVENTS.label(), new CategoryPanel(Category.EVENTS, service, favorites, checkins, username));
        tabs.addTab(Category.OUTDOOR.label(), new CategoryPanel(Category.OUTDOOR, service, favorites, checkins, username));
        tabs.addTab("Favorites", new FavoritesPanel(favorites, checkins, username));

        setLayout(new BorderLayout());
        add(buildHeader(username, db), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private JComponent buildHeader(String username, Database db) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel title = new JLabel("SF Explorer");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JLabel meta = new JLabel("Logged in as: " + username + "   •   DB: " + db.getDbPath());
        meta.setFont(meta.getFont().deriveFont(12f));

        p.add(title, BorderLayout.WEST);
        p.add(meta, BorderLayout.EAST);
        return p;
    }
}
