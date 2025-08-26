import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class CinemaSeatGUI extends JFrame {
    private JPanel seatPanel;
    private JTextArea logArea;
    private JLabel statusLabel;
    private JButton confirmButton;
    private JButton clearButton;
    
    private Map<String, JButton> seatButtons;
    private Set<String> selectedSeats;
    private Set<String> bookedSeats;
    private String currentMovie;
    private String currentTime;
    private String currentDate;
    
    // Seat configuration
    private static final String[] ROWS = {"A", "B", "C", "D", "E", "F", "G", "H"};
    private static final int SEATS_PER_ROW = 12;
    private static final int SEAT_SIZE = 40;
    private static final int SEAT_MARGIN = 5;
    
    public CinemaSeatGUI() {
        setTitle("Cinema XXI - Seat Selection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        
        setVisible(true);
    }
    
    private void initializeComponents() {
        seatPanel = new JPanel();
        seatPanel.setLayout(new GridBagLayout());
        seatPanel.setBorder(BorderFactory.createTitledBorder("Select Your Seats"));
        
        logArea = new JTextArea(15, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        statusLabel = new JLabel("Please select your seats");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.BLUE);
        
        confirmButton = new JButton("Confirm Booking");
        confirmButton.setEnabled(false);
        clearButton = new JButton("Clear Selection");
        
        seatButtons = new HashMap<>();
        selectedSeats = new HashSet<>();
        bookedSeats = new HashSet<>();
        
        // Initialize with some booked seats for demo
        initializeBookedSeats();
    }
    
    private void initializeBookedSeats() {
        // Simulate some already booked seats
        bookedSeats.add("A1");
        bookedSeats.add("A2");
        bookedSeats.add("B5");
        bookedSeats.add("C8");
        bookedSeats.add("D3");
        bookedSeats.add("E10");
        bookedSeats.add("F7");
        bookedSeats.add("G12");
        bookedSeats.add("H4");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Top panel for movie info
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
        
        // Create seat layout
        createSeatLayout();
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Movie Information"));
        
        JLabel movieLabel = new JLabel("Movie: Batman vs Superman");
        JLabel timeLabel = new JLabel("Time: 19:00");
        JLabel dateLabel = new JLabel("Date: 2025-01-20");
        
        movieLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        panel.add(movieLabel);
        panel.add(new JLabel(" | "));
        panel.add(timeLabel);
        panel.add(new JLabel(" | "));
        panel.add(dateLabel);
        
        currentMovie = "Batman vs Superman";
        currentTime = "19:00";
        currentDate = "2025-01-20";
        
        return panel;
    }
    
    private JPanel createScreenPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(800, 60));
        panel.setBackground(new Color(70, 130, 180));
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        
        JLabel screenLabel = new JLabel("SCREEN");
        screenLabel.setFont(new Font("Arial", Font.BOLD, 24));
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
        controlPanel.add(statusLabel);
        controlPanel.add(confirmButton);
        controlPanel.add(clearButton);
        
        // Log panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Booking Log"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(logPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void createSeatLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(SEAT_MARGIN, SEAT_MARGIN, SEAT_MARGIN, SEAT_MARGIN);
        
        // Add row labels
        for (int i = 0; i < ROWS.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            JLabel rowLabel = new JLabel(ROWS[i]);
            rowLabel.setFont(new Font("Arial", Font.BOLD, 16));
            rowLabel.setPreferredSize(new Dimension(30, SEAT_SIZE));
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
                
                // Set seat status
                updateSeatStatus(seatId);
            }
        }
        
        // Add seat number labels at the bottom
        gbc.gridy = ROWS.length;
        for (int seat = 1; seat <= SEATS_PER_ROW; seat++) {
            gbc.gridx = seat;
            JLabel seatLabel = new JLabel(String.valueOf(seat));
            seatLabel.setFont(new Font("Arial", Font.BOLD, 12));
            seatLabel.setHorizontalAlignment(SwingConstants.CENTER);
            seatLabel.setPreferredSize(new Dimension(SEAT_SIZE, 20));
            seatPanel.add(seatLabel, gbc);
        }
    }
    
    private JButton createSeatButton(String seatId) {
        JButton button = new JButton(seatId);
        button.setPreferredSize(new Dimension(SEAT_SIZE, SEAT_SIZE));
        button.setFont(new Font("Arial", Font.BOLD, 10));
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
            statusLabel.setText("Please select your seats");
            statusLabel.setForeground(Color.BLUE);
        } else {
            statusLabel.setText("Selected seats: " + String.join(", ", selectedSeats) + 
                              " | Total: " + selectedSeats.size());
            statusLabel.setForeground(Color.GREEN);
        }
    }
    
    private void setupEventHandlers() {
        confirmButton.addActionListener(e -> confirmBooking());
        clearButton.addActionListener(e -> clearSelection());
    }
    
    private void confirmBooking() {
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please select at least one seat!", 
                "No Seats Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create booking summary
        StringBuilder summary = new StringBuilder();
        summary.append("=== BOOKING CONFIRMED ===\n");
        summary.append("Movie: ").append(currentMovie).append("\n");
        summary.append("Time: ").append(currentTime).append("\n");
        summary.append("Date: ").append(currentDate).append("\n");
        summary.append("Selected Seats: ").append(String.join(", ", selectedSeats)).append("\n");
        summary.append("Total Seats: ").append(selectedSeats.size()).append("\n");
        summary.append("Timestamp: ").append(new Date()).append("\n");
        summary.append("========================\n\n");
        
        logArea.append(summary.toString());
        
        // Mark seats as booked
        for (String seatId : selectedSeats) {
            bookedSeats.add(seatId);
            updateSeatStatus(seatId);
        }
        
        // Log to conversation log
        logBookingToFile();
        
        // Clear selection
        selectedSeats.clear();
        updateStatusLabel();
        confirmButton.setEnabled(false);
        
        JOptionPane.showMessageDialog(this, 
            "Booking confirmed! Seats: " + String.join(", ", selectedSeats), 
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
    
    private void logBookingToFile() {
        try {
            // Use the existing EnhancedLoggerUtil
            EnhancedLoggerUtil.logMessage("CUSTOMER", "SYSTEM", "BOOKING_CONFIRMED", 
                "cinema_booking_" + System.currentTimeMillis(), 
                "Movie: " + currentMovie + ", Seats: " + String.join(",", selectedSeats) + 
                ", Time: " + currentTime + ", Date: " + currentDate);
        } catch (Exception e) {
            System.err.println("Error logging booking: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CinemaSeatGUI());
    }
}
