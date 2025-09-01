import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BookingGUI extends JFrame {
    private JComboBox<String> movieCombo;
    private JTextField dateField;
    private JComboBox<String> timeCombo;
    private JComboBox<String> seatClassCombo;
    private JSpinner ticketCountSpinner;
    private JTextField seatSelectionField;
    private JTextArea logArea;
    private JTextArea seatInfoArea;
    private JButton submitButton;
    private JButton clearButton;
    private JButton checkAvailabilityButton;
    
    // Data film dan jam yang tersedia
    private Map<String, String[]> movieShowtimes;
    private Map<String, String[]> seatAvailability;
    private Map<String, java.util.Set<String>> bookedSeats;
    
    public BookingGUI() {
        setTitle("Movie Booking System - Customer Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        
        // Initialize data
        initializeMovieData();
        
        // Initialize components
        initComponents();
        layoutComponents();
        setupEventHandlers();
        
        setVisible(true);
    }
    
    private void initializeMovieData() {
        movieShowtimes = new HashMap<>();
        seatAvailability = new HashMap<>();
        bookedSeats = new HashMap<>();
        
        // Data film dan jam tayang
        movieShowtimes.put("Batman: The Dark Knight", new String[]{"10:00", "13:00", "16:00", "19:00", "22:00"});
        movieShowtimes.put("Spider-Man: No Way Home", new String[]{"10:30", "14:00", "17:30", "20:00"});
        movieShowtimes.put("Avengers: Endgame", new String[]{"11:00", "15:00", "19:30"});
        movieShowtimes.put("Dune", new String[]{"12:00", "16:30", "21:00"});
        movieShowtimes.put("Top Gun: Maverick", new String[]{"09:30", "13:30", "18:00", "21:30"});
        
        // Inisialisasi kursi tersedia (semua kosong)
        String[] allSeats = {"A1", "A2", "A3", "B1", "B2", "B3", "B4", "C1", "C2", "C3", "C4", "C5"};
        for (String movie : movieShowtimes.keySet()) {
            for (String time : movieShowtimes.get(movie)) {
                String key = movie + "_" + time;
                seatAvailability.put(key, allSeats);
                bookedSeats.put(key, new java.util.HashSet<>());
            }
        }
    }
    
    private void initComponents() {
        // Movie selection
        String[] movies = movieShowtimes.keySet().toArray(new String[0]);
        movieCombo = new JComboBox<>(movies);
        movieCombo.addActionListener(e -> updateTimeOptions());
        
        // Date field
        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        // Seat class (initialize before time combo to avoid null reference)
        String[] seatClasses = {"VIP", "Regular", "Economy"};
        seatClassCombo = new JComboBox<>(seatClasses);
        seatClassCombo.addActionListener(e -> updateSeatInfo());
        
        // Time selection (akan diupdate berdasarkan film)
        timeCombo = new JComboBox<>();
        timeCombo.addActionListener(e -> updateSeatInfo());
        
        // Ticket count
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 10, 1);
        ticketCountSpinner = new JSpinner(spinnerModel);
        
        // Seat selection field
        seatSelectionField = new JTextField(20);
        seatSelectionField.setToolTipText("Enter seat numbers separated by commas (e.g., A1,A2,B1)");
        
        // Log area
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Seat info area
        seatInfoArea = new JTextArea(8, 50);
        seatInfoArea.setEditable(false);
        seatInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        seatInfoArea.setBackground(new Color(240, 248, 255));
        
        // Buttons
        submitButton = new JButton("Submit Booking Request");
        clearButton = new JButton("Clear Form");
        checkAvailabilityButton = new JButton("Check Seat Availability");
        
        // Now that all components are initialized, we can safely update
        updateTimeOptions();
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Movie:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(movieCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(dateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Show Time:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(timeCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Seat Class:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(seatClassCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Ticket Count:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(ticketCountSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("Select Seats:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(seatSelectionField, gbc);
        
        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(checkAvailabilityButton);
        buttonPanel.add(clearButton);
        
        // Seat Info Panel
        JPanel seatInfoPanel = new JPanel(new BorderLayout());
        seatInfoPanel.setBorder(BorderFactory.createTitledBorder("Seat Availability Information"));
        seatInfoPanel.add(new JScrollPane(seatInfoArea), BorderLayout.CENTER);
        
        // Log Panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Booking Log"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        // Center panel untuk seat info dan log
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(seatInfoPanel, BorderLayout.NORTH);
        centerPanel.add(logPanel, BorderLayout.CENTER);
        
        // Add all panels
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitBookingRequest();
            }
        });
        
        checkAvailabilityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSeatInfo();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
    }
    
    private void updateTimeOptions() {
        // Check if components are initialized
        if (movieCombo == null || timeCombo == null) {
            return;
        }
        
        String selectedMovie = (String) movieCombo.getSelectedItem();
        if (selectedMovie != null && movieShowtimes.containsKey(selectedMovie)) {
            timeCombo.removeAllItems();
            for (String time : movieShowtimes.get(selectedMovie)) {
                timeCombo.addItem(time);
            }
            updateSeatInfo();
        }
    }
    
    private void updateSeatInfo() {
        // Check if components are initialized
        if (movieCombo == null || timeCombo == null || seatClassCombo == null || seatInfoArea == null || dateField == null) {
            return;
        }
        
        String selectedMovie = (String) movieCombo.getSelectedItem();
        String selectedTime = (String) timeCombo.getSelectedItem();
        String selectedClass = (String) seatClassCombo.getSelectedItem();
        
        if (selectedMovie != null && selectedTime != null && selectedClass != null) {
            String key = selectedMovie + "_" + selectedTime;
            String[] allSeats = seatAvailability.get(key);
            java.util.Set<String> booked = bookedSeats.get(key);
            
            if (allSeats != null && booked != null) {
                StringBuilder info = new StringBuilder();
                info.append("Movie: ").append(selectedMovie).append("\n");
                info.append("Time: ").append(selectedTime).append("\n");
                info.append("Date: ").append(dateField.getText()).append("\n\n");
                
                info.append(selectedClass).append(" Seats Status:\n");
                info.append("(Available = Green, Booked = Red)\n\n");
                
                // Filter seats berdasarkan kelas dan tampilkan status
                int availableCount = 0;
                int bookedCount = 0;
                for (String seat : allSeats) {
                    if (isSeatInClass(seat, selectedClass)) {
                        if (booked.contains(seat)) {
                            info.append("[").append(seat).append("] "); // Booked
                            bookedCount++;
                        } else {
                            info.append(seat).append(" "); // Available
                            availableCount++;
                        }
                        if ((availableCount + bookedCount) % 4 == 0) info.append("\n");
                    }
                }
                
                info.append("\n\nTotal Available: ").append(availableCount).append(" seats");
                info.append("\nTotal Booked: ").append(bookedCount).append(" seats");
                info.append("\n\nNote: You can select any seat, but booking will fail if seat is already booked.");
                seatInfoArea.setText(info.toString());
            }
        }
    }
    
    private boolean isSeatInClass(String seat, String seatClass) {
        switch (seatClass) {
            case "VIP":
                return seat.startsWith("A");
            case "Regular":
                return seat.startsWith("B");
            case "Economy":
                return seat.startsWith("C");
            default:
                return false;
        }
    }
    
    private void submitBookingRequest() {
        // Validate input
        String selectedMovie = (String) movieCombo.getSelectedItem();
        String selectedTime = (String) timeCombo.getSelectedItem();
        String selectedClass = (String) seatClassCombo.getSelectedItem();
        String seatInput = seatSelectionField.getText().trim();
        
        if (selectedMovie == null || selectedTime == null || selectedClass == null) {
            JOptionPane.showMessageDialog(this, "Please select movie, time, and seat class", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (seatInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter seat numbers", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Parse seat selection
        String[] selectedSeats = seatInput.split(",");
        for (int i = 0; i < selectedSeats.length; i++) {
            selectedSeats[i] = selectedSeats[i].trim().toUpperCase();
        }
        
        int requestedTickets = (Integer) ticketCountSpinner.getValue();
        
        // Validate seat count matches ticket count
        if (selectedSeats.length != requestedTickets) {
            JOptionPane.showMessageDialog(this, 
                    String.format("Seat count mismatch!\nRequested tickets: %d\nSelected seats: %d", 
                            requestedTickets, selectedSeats.length), 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate seat class
        for (String seat : selectedSeats) {
            if (!isSeatInClass(seat, selectedClass)) {
                JOptionPane.showMessageDialog(this, 
                        String.format("Seat %s is not in %s class!\nPlease select seats from the correct class.", 
                                seat, selectedClass), 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Check if seats are already booked
        String key = selectedMovie + "_" + selectedTime;
        java.util.Set<String> booked = bookedSeats.get(key);
        java.util.List<String> alreadyBooked = new java.util.ArrayList<>();
        
        for (String seat : selectedSeats) {
            if (booked.contains(seat)) {
                alreadyBooked.add(seat);
            }
        }
        
        if (!alreadyBooked.isEmpty()) {
            String errorMessage = "Booking Failed!\n\n";
            errorMessage += "The following seats are already booked:\n";
            errorMessage += String.join(", ", alreadyBooked) + "\n\n";
            errorMessage += "Reason: These seats were previously booked by other customers.\n";
            errorMessage += "Please select different seats and try again.";
            
            logArea.append("[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] ");
            logArea.append("Booking Request: " + selectedMovie + " - " + selectedTime + " - Seats: " + String.join(",", selectedSeats) + "\n");
            logArea.append("Status: FAILED - Seats already booked: " + String.join(", ", alreadyBooked) + "\n\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            
            JOptionPane.showMessageDialog(this, errorMessage, 
                    "Booking Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Book the seats
        for (String seat : selectedSeats) {
            booked.add(seat);
        }
        
        // Create booking request
        String request = String.format("Movie: %s, Date: %s, Time: %s, Class: %s, Tickets: %d, Seats: %s",
                selectedMovie,
                dateField.getText().trim(),
                selectedTime,
                selectedClass,
                requestedTickets,
                String.join(",", selectedSeats));
        
        logArea.append("[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] ");
        logArea.append("Booking Request: " + request + "\n");
        logArea.append("Status: SUCCESS - Seats booked successfully!\n\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        
        // Update seat info
        updateSeatInfo();
        
        JOptionPane.showMessageDialog(this, 
                String.format("Booking successful!\n\nMovie: %s\nTime: %s\nSeats: %s\nClass: %s", 
                        selectedMovie, selectedTime, String.join(", ", selectedSeats), selectedClass), 
                "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
        
        // Clear seat selection field
        seatSelectionField.setText("");
    }
    
    private void removeSeatFromAvailability(String key, String seat) {
        String[] seats = seatAvailability.get(key);
        if (seats != null) {
            java.util.List<String> seatList = new java.util.ArrayList<>(java.util.Arrays.asList(seats));
            seatList.remove(seat);
            seatAvailability.put(key, seatList.toArray(new String[0]));
        }
    }
    
    private void clearFields() {
        movieCombo.setSelectedIndex(0);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeCombo.setSelectedIndex(0);
        seatClassCombo.setSelectedIndex(0);
        ticketCountSpinner.setValue(1);
        seatSelectionField.setText("");
        logArea.setText("");
        seatInfoArea.setText("");
    }
    
    public void addLogMessage(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BookingGUI();
            }
        });
    }
}
