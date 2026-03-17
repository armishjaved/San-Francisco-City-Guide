package com.sfexplorer.ui;

import com.sfexplorer.db.FavoritesRepository;
import com.sfexplorer.db.CheckinRepository;
import com.sfexplorer.model.Category;
import com.sfexplorer.model.Place;
import com.sfexplorer.service.PlacesService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryPanel extends JPanel {
    private final Category category;
    private final PlacesService service;
    private final FavoritesRepository favorites;
    private final CheckinRepository checkins;
    private final String username;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private final JTextField searchField = new JTextField();
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton addFavButton = new JButton("Add to Favorites");
    private final JButton checkInButton = new JButton("Check in");
    private final JLabel checkInLabel = new JLabel("Check-ins: 0");
    private final JLabel statusLabel = new JLabel(" ");
    private final JProgressBar progress = new JProgressBar();

    private final DefaultListModel<Place> listModel = new DefaultListModel<>();
    private final JList<Place> list = new JList<>(listModel);

    private final JTextArea details = new JTextArea();

    private List<Place> allPlaces = new ArrayList<>();

    public CategoryPanel(Category category, PlacesService service, FavoritesRepository favorites, CheckinRepository checkins, String username) {
        this.category = category;
        this.service = service;
        this.favorites = favorites;
        this.checkins = checkins;
        this.username = username;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        wireEvents();
        refresh();
    }

    private JComponent buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(new JLabel("Search:"), BorderLayout.WEST);
        left.add(searchField, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(refreshButton);

        p.add(left, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JComponent buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.45);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane left = new JScrollPane(list);

        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        JScrollPane right = new JScrollPane(details);

        split.setLeftComponent(left);
        split.setRightComponent(right);

        return split;
    }

    private JComponent buildBottomBar() {
        JPanel p = new JPanel(new BorderLayout(8, 8));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.add(addFavButton);
        left.add(checkInButton);
        left.add(checkInLabel);
        addFavButton.setEnabled(false);
        checkInButton.setEnabled(false);

        progress.setIndeterminate(true);
        progress.setVisible(false);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(progress);

        p.add(left, BorderLayout.WEST);
        p.add(statusLabel, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private void wireEvents() {
        refreshButton.addActionListener(e -> refresh());

        list.addListSelectionListener(e -> {
            Place sel = list.getSelectedValue();
            if (sel == null) {
                details.setText("");
                addFavButton.setEnabled(false);
                checkInButton.setEnabled(false);
                checkInLabel.setText("Check-ins: 0");
            } else {
                details.setText(sel.toPrettyString());
                details.setCaretPosition(0);
                addFavButton.setEnabled(true);
                checkInButton.setEnabled(true);
                loadCheckIns(sel);
            }
        });

        addFavButton.addActionListener(e -> {
            Place sel = list.getSelectedValue();
            if (sel == null) return;
            try {
                boolean added = favorites.add(username, sel);
                statusLabel.setText(added ? "Added to favorites." : "Already in favorites.");
            } catch (Exception ex) {
                statusLabel.setText("Failed to add favorite: " + ex.getMessage());
            }
        });

        checkInButton.addActionListener(e -> {
            Place sel = list.getSelectedValue();
            if (sel == null) return;
            // Run in background so UI stays responsive
            executor.submit(() -> {
                try {
                    int newCount = checkins.incrementAndGet(sel.getPlaceId());
                    SwingUtilities.invokeLater(() -> {
                        checkInLabel.setText("Check-ins: " + newCount);
                        statusLabel.setText("Checked in! (global count updated atomically)");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Check-in failed: " + ex.getMessage()));
                }
            });
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
    }

    private void refresh() {
        setLoading(true, "Loading " + category.label() + "…");

        executor.submit(() -> {
            try {
                List<Place> places = service.fetch(category);
                SwingUtilities.invokeLater(() -> {
                    allPlaces = places;
                    applyFilter();
                    setLoading(false, "Loaded " + places.size() + " items.");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setLoading(false, "Failed to load: " + ex.getMessage());
                    allPlaces = List.of();
                    applyFilter();
                });
            }
        });
    }

    private void loadCheckIns(Place place) {
        executor.submit(() -> {
            try {
                int count = checkins.getCount(place.getPlaceId());
                SwingUtilities.invokeLater(() -> checkInLabel.setText("Check-ins: " + count));
            } catch (Exception ignored) {
                SwingUtilities.invokeLater(() -> checkInLabel.setText("Check-ins: ?"));
            }
        });
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        listModel.clear();

        for (Place p : allPlaces) {
            if (q.isBlank()) {
                listModel.addElement(p);
                continue;
            }
            String hay = (p.getName() + " " + p.getAddress() + " " + p.getDescription()).toLowerCase();
            if (hay.contains(q)) listModel.addElement(p);
        }
    }

    private void setLoading(boolean loading, String msg) {
        progress.setVisible(loading);
        refreshButton.setEnabled(!loading);
        addFavButton.setEnabled(!loading && list.getSelectedValue() != null);
        checkInButton.setEnabled(!loading && list.getSelectedValue() != null);
        statusLabel.setText(msg);
    }
}
