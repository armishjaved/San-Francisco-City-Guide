package com.sfexplorer.ui;

import com.sfexplorer.db.FavoritesRepository;
import com.sfexplorer.db.CheckinRepository;
import com.sfexplorer.model.Place;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FavoritesPanel extends JPanel {
    private final FavoritesRepository repo;
    private final CheckinRepository checkins;
    private final String username;

    private final DefaultListModel<Place> listModel = new DefaultListModel<>();
    private final JList<Place> list = new JList<>(listModel);
    private final JTextArea details = new JTextArea();
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton removeButton = new JButton("Remove Selected");
    private final JButton checkInButton = new JButton("Check in");
    private final JLabel checkInLabel = new JLabel("Check-ins: 0");

    public FavoritesPanel(FavoritesRepository repo, CheckinRepository checkins, String username) {
        this.repo = repo;
        this.checkins = checkins;
        this.username = username;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);

        wireEvents();
        refresh();
    }

    private JComponent buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        JLabel label = new JLabel("Favorites saved in SQLite");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(refreshButton);
        right.add(checkInLabel);
        right.add(checkInButton);
        right.add(removeButton);
        removeButton.setEnabled(false);
        checkInButton.setEnabled(false);

        p.add(label, BorderLayout.WEST);
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

    private void wireEvents() {
        refreshButton.addActionListener(e -> refresh());

        list.addListSelectionListener(e -> {
            Place sel = list.getSelectedValue();
            removeButton.setEnabled(sel != null);
            checkInButton.setEnabled(sel != null);
            details.setText(sel == null ? "" : sel.toPrettyString());
            details.setCaretPosition(0);

            if (sel == null) {
                checkInLabel.setText("Check-ins: 0");
            } else {
                // load in a background thread to keep UI snappy
                new Thread(() -> {
                    try {
                        int count = checkins.getCount(sel.getPlaceId());
                        SwingUtilities.invokeLater(() -> checkInLabel.setText("Check-ins: " + count));
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> checkInLabel.setText("Check-ins: ?"));
                    }
                }, "checkin-loader").start();
            }
        });

        removeButton.addActionListener(e -> {
            Place sel = list.getSelectedValue();
            if (sel == null) return;
            try {
                repo.remove(username, sel);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to remove: " + ex.getMessage(),
                        "Favorites - Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        checkInButton.addActionListener(e -> {
            Place sel = list.getSelectedValue();
            if (sel == null) return;
            new Thread(() -> {
                try {
                    int newCount = checkins.incrementAndGet(sel.getPlaceId());
                    SwingUtilities.invokeLater(() -> checkInLabel.setText("Check-ins: " + newCount));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                            "Check-in failed: " + ex.getMessage(),
                            "Favorites - Error", JOptionPane.ERROR_MESSAGE));
                }
            }, "checkin-increment").start();
        });
    }

    private void refresh() {
        listModel.clear();
        try {
            List<Place> places = repo.listAll(username);
            for (Place p : places) listModel.addElement(p);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load favorites: " + ex.getMessage(),
                    "Favorites - Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
