import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EnhancedCinemaGUI extends JFrame {
    private JPanel mainPanel;
    private JPanel seatPanel;
    private JTextArea logArea;
    private JLabel statusLabel;
    private JButton confirmButton;
    private JButton clearButton;
    private JButton refreshButton;
    
    // Movie selection components
    private JComboBox<String> movieCombo;
    private JComboBox<String> timeCombo;
    private JTextField dateField;
    private JButton loadSeatsButton;
    
    private Map<String, JButton> seatButtons;
    private Set<String> selectedSeats;
    private Set<String> bookedSeats;
    private String currentMovie;
    private String currentTime;
    private String currentDate;
    
    // Menyimpan kursi ter-booking per kombinasi MOVIE|DATE|TIME
    private final Map<String, Set<String>> bookingStore = new HashMap<>();
    
    // Seat configuration
    private static final String[] ROWS = {"A", "B", "C", "D", "E", "F", "G", "H"};
    private static final int SEATS_PER_ROW = 12;
    private static final int SEAT_SIZE = 35;
    private static final int SEAT_MARGIN = 3;
    
    // Available movies
    private static final String[] MOVIES = {
        "Batman vs Superman", "Avengers: Endgame", "Spider-Man: No Way Home",
        "The Dark Knight", "Iron Man", "Black Panther", "Wonder Woman"
    };
    
    // Available times
    private static final String[] TIMES = {"10:00", "13:00", "16:00", "19:00", "22:00"};
    
    public EnhancedCinemaGUI() {
        setTitle("Cinema XXI - Enhanced Seat Selection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);
        setLocationRelativeTo(null);
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        
        setVisible(true);
    }
    
    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());
        seatPanel = new JPanel();
        seatPanel.setLayout(new GridBagLayout());
        seatPanel.setBorder(BorderFactory.createTitledBorder("Select Your Seats"));
        
        logArea = new JTextArea(12, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
        statusLabel = new JLabel("Please select movie, time, and date, then load seats");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.BLUE);
        
        confirmButton = new JButton("Confirm Booking");
        confirmButton.setEnabled(false);
        clearButton = new JButton("Clear Selection");
        refreshButton = new JButton("Refresh Seats");
        
        // Movie selection components
        movieCombo = new JComboBox<>(MOVIES);
        timeCombo = new JComboBox<>(TIMES);
        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        loadSeatsButton = new JButton("Load Seats");
        
        seatButtons = new HashMap<>();
        selectedSeats = new HashSet<>();
        bookedSeats = new HashSet<>();
        
        // Set default values
        currentMovie = MOVIES[0];
        currentTime = TIMES[3]; // 19:00
        currentDate = dateField.getText();
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Top panel for movie selection
        JPanel topPanel = createTopPanel();
        
        // Center panel for seats
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(createScreenPanel(), BorderLayout.NORTH);
        centerPanel.add(seatPanel, BorderLayout.CENTER);
        
        // Bottom panel for controls and log
        JPanel bottomPanel = createBottomPanel();
        
        // Add all panels
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Movie Selection"));
        
        // Movie selection controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Movie:"));
        controlPanel.add(movieCombo);
        controlPanel.add(new JLabel("Time:"));
        controlPanel.add(timeCombo);
        controlPanel.add(new JLabel("Date:"));
        controlPanel.add(dateField);
        controlPanel.add(loadSeatsButton);
        
        // Current selection display
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Current Selection"));
        infoPanel.add(statusLabel);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createScreenPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(900, 50));
        panel.setBackground(new Color(70, 130, 180));
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        
        JLabel screenLabel = new JLabel("SCREEN");
        screenLabel.setFont(new Font("Arial", Font.BOLD, 20));
        screenLabel.setForeground(Color.WHITE);
        screenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.setLayout(new GridBagLayout());
        panel.add(screenLabel);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(confirmButton);
        controlPanel.add(clearButton);
        controlPanel.add(refreshButton);
        
        // Log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Booking Log & System Messages"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(logPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void createSeatLayout() {
        seatPanel.removeAll();
        seatButtons.clear();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(SEAT_MARGIN, SEAT_MARGIN, SEAT_MARGIN, SEAT_MARGIN);
        
        // Add row labels
        for (int i = 0; i < ROWS.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            JLabel rowLabel = new JLabel(ROWS[i]);
            rowLabel.setFont(new Font("Arial", Font.BOLD, 14));
            rowLabel.setPreferredSize(new Dimension(25, SEAT_SIZE));
            seatPanel.add(rowLabel, gbc);
        }
        
        // Add seats
        for (int row = 0; row < ROWS.length; row++) {
            for (int seat = 1; seat <= SEATS_PER_ROW; seat++) {
                gbc.gridx = seat;
                gbc.gridy = row;
                
                String seatId = ROWS[row] + seat;
                JButton seatButton = createSeatButton(seatId);
                seatButtons.put(seatId, seatButton);
                seatPanel.add(seatButton, gbc);
            }
        }
        
        // Add seat number labels at the bottom
        gbc.gridy = ROWS.length;
        for (int seat = 1; seat <= SEATS_PER_ROW; seat++) {
            gbc.gridx = seat;
            JLabel seatLabel = new JLabel(String.valueOf(seat));
            seatLabel.setFont(new Font("Arial", Font.BOLD, 10));
            seatLabel.setHorizontalAlignment(SwingConstants.CENTER);
            seatLabel.setPreferredSize(new Dimension(SEAT_SIZE, 15));
            seatPanel.add(seatLabel, gbc);
        }
        
        // Add legend
        JPanel legendPanel = createLegendPanel();
        gbc.gridx = 0;
        gbc.gridy = ROWS.length + 1;
        gbc.gridwidth = SEATS_PER_ROW + 1;
        seatPanel.add(legendPanel, gbc);
        
        seatPanel.revalidate();
        seatPanel.repaint();
    }
    
    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createTitledBorder("Legend"));
        
        // Available seat
        JButton availableSample = new JButton("A1");
        availableSample.setPreferredSize(new Dimension(30, 25));
        availableSample.setBackground(Color.GREEN);
        availableSample.setForeground(Color.BLACK);
        availableSample.setEnabled(false);
        
        // Selected seat
        JButton selectedSample = new JButton("B1");
        selectedSample.setPreferredSize(new Dimension(30, 25));
        selectedSample.setBackground(Color.BLUE);
        selectedSample.setForeground(Color.WHITE);
        selectedSample.setEnabled(false);
        
        // Booked seat
        JButton bookedSample = new JButton("C1");
        bookedSample.setPreferredSize(new Dimension(30, 25));
        bookedSample.setBackground(Color.RED);
        bookedSample.setForeground(Color.WHITE);
        bookedSample.setEnabled(false);
        
        panel.add(new JLabel("Available:"));
        panel.add(availableSample);
        panel.add(new JLabel("Selected:"));
        panel.add(selectedSample);
        panel.add(new JLabel("Booked:"));
        panel.add(bookedSample);
        
        return panel;
    }
    
    private JButton createSeatButton(String seatId) {
        JButton button = new JButton(seatId);
        button.setPreferredSize(new Dimension(SEAT_SIZE, SEAT_SIZE));
        button.setFont(new Font("Arial", Font.BOLD, 9));
        button.setFocusPainted(false);
        
        button.addActionListener(e -> handleSeatClick(seatId));
        
        return button;
    }
    
    private void handleSeatClick(String seatId) {
        if (bookedSeats.contains(seatId)) {
            JOptionPane.showMessageDialog(this, 
                "Seat " + seatId + " is already booked!", 
                "Seat Unavailable", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedSeats.contains(seatId)) {
            selectedSeats.remove(seatId);
            logArea.append("Seat " + seatId + " deselected\n");
        } else {
            selectedSeats.add(seatId);
            logArea.append("Seat " + seatId + " selected\n");
        }
        
        updateSeatStatus(seatId);
        updateStatusLabel();
        confirmButton.setEnabled(!selectedSeats.isEmpty());
        
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void updateSeatStatus(String seatId) {
        JButton button = seatButtons.get(seatId);
        if (button == null) return;
        
        if (bookedSeats.contains(seatId)) {
            // Booked seat - Red
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
            button.setEnabled(false);
            button.setToolTipText("Seat " + seatId + " - Booked");
        } else if (selectedSeats.contains(seatId)) {
            // Selected seat - Blue
            button.setBackground(Color.BLUE);
            button.setForeground(Color.WHITE);
            button.setToolTipText("Seat " + seatId + " - Selected");
        } else {
            // Available seat - Green
            button.setBackground(Color.GREEN);
            button.setForeground(Color.BLACK);
            button.setToolTipText("Seat " + seatId + " - Available");
        }
    }
    
    private void updateStatusLabel() {
        if (selectedSeats.isEmpty()) {
            statusLabel.setText("Current: " + currentMovie + " | " + currentTime + " | " + currentDate + 
                              " | Please select seats");
            statusLabel.setForeground(Color.BLUE);
        } else {
            statusLabel.setText("Current: " + currentMovie + " | " + currentTime + " | " + currentDate + 
                              " | Selected: " + String.join(", ", selectedSeats) + 
                              " | Total: " + selectedSeats.size());
            statusLabel.setForeground(Color.GREEN);
        }
    }
    
    private void setupEventHandlers() {
        confirmButton.addActionListener(e -> confirmBooking());
        clearButton.addActionListener(e -> clearSelection());
        refreshButton.addActionListener(e -> refreshSeats());
        loadSeatsButton.addActionListener(e -> loadSeats());
        
        // Update current values when selection changes
        movieCombo.addActionListener(e -> {
            currentMovie = (String) movieCombo.getSelectedItem();
            updateStatusLabel();
        });
        
        timeCombo.addActionListener(e -> {
            currentTime = (String) timeCombo.getSelectedItem();
            updateStatusLabel();
        });
        
        dateField.addActionListener(e -> {
            currentDate = dateField.getText();
            updateStatusLabel();
        });
    }
    
    private String currentKey() {
        return currentMovie + "|" + currentDate + "|" + currentTime;
    }
    
    private void loadSeats() {
        currentMovie = (String) movieCombo.getSelectedItem();
        currentTime = (String) timeCombo.getSelectedItem();
        currentDate = dateField.getText();
        
        // Clear previous selection
        selectedSeats.clear();
        
        // Muat kursi yang sudah ter-booking dari store, tanpa prefill
        Set<String> persisted = bookingStore.getOrDefault(currentKey(), Collections.emptySet());
        bookedSeats = new HashSet<>(persisted);
        
        createSeatLayout();
        // update semua status kursi setelah layout dibuat
        for (String seatId : seatButtons.keySet()) {
            updateSeatStatus(seatId);
        }
        updateStatusLabel();
        confirmButton.setEnabled(false);
        
        logArea.append("=== Seats loaded for " + currentMovie + " at " + currentTime + " on " + currentDate + " ===\n");
        logArea.append("Booked seats: " + (bookedSeats.isEmpty() ? "-" : String.join(", ", bookedSeats)) + "\n");
        logArea.append("Available seats: " + (ROWS.length * SEATS_PER_ROW - bookedSeats.size()) + "\n\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void refreshSeats() {
        loadSeats();
        logArea.append("Seats refreshed\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void confirmBooking() {
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please select at least one seat!", 
                "No Seats Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Salin kursi yang akan dibooking sebelum di-clear
        List<String> seatsToBook = new ArrayList<>(selectedSeats);
        
        // Update store persisten untuk kombinasi saat ini
        String key = currentKey();
        Set<String> persisted = bookingStore.getOrDefault(key, new HashSet<>());
        persisted.addAll(seatsToBook);
        bookingStore.put(key, persisted);
        
        // Update tampilan lokal
        for (String seatId : seatsToBook) {
            bookedSeats.add(seatId);
            updateSeatStatus(seatId);
        }
        
        // Buat ringkasan booking
        StringBuilder summary = new StringBuilder();
        summary.append("=== BOOKING CONFIRMED ===\n");
        summary.append("Movie: ").append(currentMovie).append("\n");
        summary.append("Time: ").append(currentTime).append("\n");
        summary.append("Date: ").append(currentDate).append("\n");
        summary.append("Selected Seats: ").append(String.join(", ", seatsToBook)).append("\n");
        summary.append("Total Seats: ").append(seatsToBook.size()).append("\n");
        summary.append("Timestamp: ").append(new Date()).append("\n");
        summary.append("========================\n\n");
        logArea.append(summary.toString());
        
        // Log ke conversation log
        logBookingToFile(seatsToBook);
        
        // Clear selection
        selectedSeats.clear();
        updateStatusLabel();
        confirmButton.setEnabled(false);
        
        JOptionPane.showMessageDialog(this, 
            "Booking confirmed! Seats: " + String.join(", ", seatsToBook), 
            "Booking Successful", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void clearSelection() {
        selectedSeats.clear();
        for (String seatId : seatButtons.keySet()) {
            updateSeatStatus(seatId);
        }
        updateStatusLabel();
        confirmButton.setEnabled(false);
        logArea.append("Selection cleared\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void logBookingToFile(List<String> seats) {
        try {
            // Use the existing EnhancedLoggerUtil
            EnhancedLoggerUtil.logMessage("CUSTOMER", "SYSTEM", "BOOKING_CONFIRMED", 
                "cinema_booking_" + System.currentTimeMillis(), 
                "Movie: " + currentMovie + ", Seats: " + String.join(",", seats) + 
                ", Time: " + currentTime + ", Date: " + currentDate);
        } catch (Exception e) {
            System.err.println("Error logging booking: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EnhancedCinemaGUI());
    }
}
