import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

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
    private JTextArea alternativeArea;

    // Data film, jam, dan harga
    private Map<String, String[]> movieShowtimes;
    private Map<String, String[]> seatAvailability;
    private Map<String, java.util.Set<String>> bookedSeats;
    private Map<String, Integer> seatPrices;
    private Map<String, ReentrantLock> scheduleLocks;
    
    // Agent connection
    private AgentController customerAgentController;
    private EnhancedCustomerAgent customerAgent;
    private boolean waitingForAgentResponse = false;
    private String currentBookingRequest = "";


    public BookingGUI() {
        this(null);
    }
    
    public BookingGUI(AgentController customerAgentController) {
        this.customerAgentController = customerAgentController;
        this.customerAgent = null; // Will be set later
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
        seatPrices = new HashMap<>();
        scheduleLocks = new HashMap<>();

        // Data film dan jam tayang
        movieShowtimes.put("Batman: The Dark Knight", new String[]{"10:00", "13:00", "16:00", "19:00", "22:00"});
        movieShowtimes.put("Spider-Man: No Way Home", new String[]{"10:30", "14:00", "17:30", "20:00"});
        movieShowtimes.put("Avengers: Endgame", new String[]{"11:00", "15:00", "19:30"});
        movieShowtimes.put("Dune", new String[]{"12:00", "16:30", "21:00"});
        movieShowtimes.put("Top Gun: Maverick", new String[]{"09:30", "13:30", "18:00", "21:30"});

        // Harga kursi per kelas
        seatPrices.put("VIP", 100000);
        seatPrices.put("Regular", 70000);
        seatPrices.put("Economy", 50000);

        // Inisialisasi kursi tersedia (semua kosong)
        String[] allSeats = {"A1", "A2", "A3", "B1", "B2", "B3", "B4", "C1", "C2", "C3", "C4", "C5"};
        for (String movie : movieShowtimes.keySet()) {
            for (String time : movieShowtimes.get(movie)) {
                String key = movie + "_" + time;
                seatAvailability.put(key, allSeats);
                bookedSeats.put(key, new java.util.HashSet<>());
                ReentrantLock put = scheduleLocks.put(key, new ReentrantLock());
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

        // Alternative seats area
        alternativeArea = new JTextArea(6, 50);
        alternativeArea.setEditable(false);
        alternativeArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        alternativeArea.setBackground(new Color(255, 250, 240));

        // Now that all components are initialized, we can safely update
        updateTimeOptions();
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // --- Input Panel ---
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

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(checkAvailabilityButton);
        buttonPanel.add(clearButton);

        // --- Seat Info Panel ---
        JPanel seatInfoPanel = new JPanel(new BorderLayout());
        seatInfoPanel.setBorder(BorderFactory.createTitledBorder("Seat Availability Information"));
        seatInfoPanel.add(new JScrollPane(seatInfoArea), BorderLayout.CENTER);

        // --- Log Panel ---
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Booking Log"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // --- Alternative Panel ---
        JPanel altPanel = new JPanel(new BorderLayout());
        altPanel.setBorder(BorderFactory.createTitledBorder("Alternative Seats Suggestion"));
        altPanel.add(new JScrollPane(alternativeArea), BorderLayout.CENTER);

        // --- Center Panel: Seat Info + Log + Alternatives ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(seatInfoPanel, BorderLayout.NORTH);
        centerPanel.add(logPanel, BorderLayout.CENTER);
        centerPanel.add(altPanel, BorderLayout.SOUTH);

        // --- Gabungkan Button + Center ke 1 panel ---
        JPanel middlePanel = new JPanel(new BorderLayout());
        middlePanel.add(buttonPanel, BorderLayout.NORTH);
        middlePanel.add(centerPanel, BorderLayout.CENTER);

        // --- Tambahkan ke frame utama ---
        add(inputPanel, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
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

    private boolean isSeatInClass(String seat, String seatClass) {
        switch (seatClass) {
            case "VIP": return seat.startsWith("A");
            case "Regular": return seat.startsWith("B");
            case "Economy": return seat.startsWith("C");
            default: return false;
        }
    }

    private void submitBookingRequest() {
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

        String[] selectedSeats = seatInput.split(",");
        for (int i = 0; i < selectedSeats.length; i++) {
            selectedSeats[i] = selectedSeats[i].trim().toUpperCase();
        }

        int requestedTickets = (Integer) ticketCountSpinner.getValue();

        if (selectedSeats.length != requestedTickets) {
            JOptionPane.showMessageDialog(this,
                    String.format("Seat count mismatch!\nRequested tickets: %d\nSelected seats: %d",
                            requestedTickets, selectedSeats.length),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (String seat : selectedSeats) {
            if (!isSeatInClass(seat, selectedClass)) {
                JOptionPane.showMessageDialog(this,
                        String.format("Seat %s is not in %s class!\nPlease select seats from the correct class.",
                                seat, selectedClass),
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

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

            // cari alternatif kursi di kelas yang sama
            java.util.List<String> alternatives = new java.util.ArrayList<>();
            for (String seat : seatAvailability.get(key)) {
                if (isSeatInClass(seat, selectedClass) && !booked.contains(seat)) {
                    alternatives.add(seat);
                }
            }

            // tampilkan alternatif di text area
            if (!alternatives.isEmpty()) {
                alternativeArea.setText("The following alternative seats are available:\n\n");
                for (int i = 0; i < alternatives.size(); i++) {
                    alternativeArea.append(alternatives.get(i) + " ");
                    if ((i + 1) % 5 == 0) alternativeArea.append("\n");
                }
                alternativeArea.append("\n\nPlease choose from these available seats.");
            } else {
                alternativeArea.setText("No alternative seats available in " + selectedClass + " class.");
            }

            return;
        }

        // Use agent system for booking
        if (customerAgentController != null) {
            try {
                // Send booking request to customer agent
                Object[] args = {selectedMovie, dateField.getText().trim(), selectedTime, selectedClass, requestedTickets};
                customerAgentController.putO2AObject(args, false);
                
                // Store current booking info for response handling
                waitingForAgentResponse = true;
                currentBookingRequest = String.format("Movie: %s, Time: %s, Seats: %s, Class: %s", 
                        selectedMovie, selectedTime, String.join(",", selectedSeats), selectedClass);
                
                logArea.append("[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] ");
                logArea.append("Booking Request sent to Customer Agent\n");
                logArea.append("Movie: " + selectedMovie + ", Time: " + selectedTime + ", Seats: " + String.join(",", selectedSeats) + "\n");
                logArea.append("Status: PROCESSING - Agent is handling your request...\n\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
                
                // Log to conversation log
                EnhancedLoggerUtil.logMessage("GUI", "customer", "BOOKING_REQUEST", 
                        "gui_booking_" + System.currentTimeMillis(),
                        "Movie: " + selectedMovie + ", Time: " + selectedTime + ", Seats: " + String.join(",", selectedSeats) + ", Class: " + selectedClass);
                
                // Simulate immediate response for GUI (but keep full process in conversation log)
                simulateBookingResponse(selectedMovie, selectedTime, selectedSeats, selectedClass);
                
            } catch (StaleProxyException e) {
                logArea.append("Error: Cannot connect to Customer Agent - " + e.getMessage() + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
                EnhancedLoggerUtil.logError("GUI", "customer", "Agent connection failed: " + e.getMessage(), "gui_error");
            }
        } else {
            // Fallback to local booking (original behavior)
            for (String seat : selectedSeats) {
                booked.add(seat);
            }

            int pricePerSeat = seatPrices.get(selectedClass);
            int totalPrice = pricePerSeat * requestedTickets;

            String request = String.format("Movie: %s, Date: %s, Time: %s, Class: %s, Tickets: %d, Seats: %s, Total: Rp %,d",
                    selectedMovie,
                    dateField.getText().trim(),
                    selectedTime,
                    selectedClass,
                    requestedTickets,
                    String.join(",", selectedSeats),
                    totalPrice);

            logArea.append("[" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] ");
            logArea.append("Booking Request: " + request + "\n");
            logArea.append("Status: SUCCESS - Seats booked successfully! (Local Mode)\n\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
            
            // Log to conversation log
            EnhancedLoggerUtil.logMessage("GUI", "LOCAL", "BOOKING_SUCCESS", 
                    "local_booking_" + System.currentTimeMillis(),
                    "Local booking: " + request);

            updateSeatInfo();

            JOptionPane.showMessageDialog(this,
                    String.format("Booking successful! (Local Mode)\n\nMovie: %s\nTime: %s\nSeats: %s\nClass: %s\nTotal Price: Rp %,d",
                            selectedMovie, selectedTime, String.join(", ", selectedSeats), selectedClass, totalPrice),
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
        }

        seatSelectionField.setText("");
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
        alternativeArea.setText("");
    }

    private void updateSeatInfo() {
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

                int pricePerSeat = seatPrices.get(selectedClass);
                info.append(selectedClass).append(" Seats Status (Rp ").append(pricePerSeat).append(" per seat):\n");
                info.append("(Available = Green, Booked = Red)\n\n");

                int availableCount = 0;
                int bookedCount = 0;
                for (String seat : allSeats) {
                    if (isSeatInClass(seat, selectedClass)) {
                        if (booked.contains(seat)) {
                            info.append("[").append(seat).append("] ");
                            bookedCount++;
                        } else {
                            info.append(seat).append(" ");
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

    public void addLogMessage(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    // Method to set the customer agent reference
    public void setCustomerAgent(EnhancedCustomerAgent agent) {
        this.customerAgent = agent;
    }
    
    // Method to handle booking confirmation from agent
    public void handleBookingConfirmation(boolean success, String message, String transactionId) {
        if (waitingForAgentResponse) {
            waitingForAgentResponse = false;
            
            String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + timestamp + "] ");
            logArea.append("Agent Response: " + currentBookingRequest + "\n");
            
            if (success) {
                logArea.append("Status: SUCCESS - " + message + "\n");
                if (transactionId != null && !transactionId.isEmpty()) {
                    logArea.append("Transaction ID: " + transactionId + "\n");
                }
                logArea.append("\n");
                
                // Update seat availability
                updateSeatAvailabilityFromBooking();
                
                // Show success dialog
                JOptionPane.showMessageDialog(this, 
                        "Booking successful!\n\n" + currentBookingRequest + "\n\n" + message,
                        "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                        
                // Log success to conversation log
                EnhancedLoggerUtil.logMessage("GUI", "customer", "BOOKING_SUCCESS", 
                        "gui_booking_" + System.currentTimeMillis(),
                        "Booking confirmed: " + currentBookingRequest + " - " + message);
            } else {
                logArea.append("Status: FAILED - " + message + "\n\n");
                
                // Show error dialog
                JOptionPane.showMessageDialog(this, 
                        "Booking failed!\n\n" + currentBookingRequest + "\n\n" + message,
                        "Booking Failed", JOptionPane.ERROR_MESSAGE);
                        
                // Log failure to conversation log
                EnhancedLoggerUtil.logMessage("GUI", "customer", "BOOKING_FAILED", 
                        "gui_booking_" + System.currentTimeMillis(),
                        "Booking failed: " + currentBookingRequest + " - " + message);
            }
            
            logArea.setCaretPosition(logArea.getDocument().getLength());
            currentBookingRequest = "";
        }
    }
    
    
    private void updateSeatAvailabilityFromBooking() {
        // Update the local seat availability to reflect the booking
        // This ensures the GUI shows the correct seat status
        updateSeatInfo();
    }
    
    // Method to mark seats as booked
    private void markSeatsAsBooked(String[] seats, String movie, String time, String seatClass) {
        String scheduleKey = movie + "_" + time; // Use same key format as validation
        
        // Get the booked seats set for this schedule
        java.util.Set<String> booked = bookedSeats.get(scheduleKey);
        if (booked == null) {
            booked = new java.util.HashSet<>();
            bookedSeats.put(scheduleKey, booked);
        }
        
        // Mark seats as booked
        for (String seat : seats) {
            booked.add(seat);
        }
        
        // Update seat availability
        updateSeatInfo();
        
        // Log the booking
        System.out.println("Seats marked as booked: " + String.join(",", seats) + " for " + scheduleKey);
    }
    
    // Simulate booking response for GUI (while keeping full process in conversation log)
    private void simulateBookingResponse(String movie, String time, String[] seats, String seatClass) {
        // Simulate a short delay to show processing
        Timer delayTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Simulate successful booking (90% success rate)
                boolean success = Math.random() > 0.1;
                String transactionId = "TXN" + System.currentTimeMillis();
                
                String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                logArea.append("[" + timestamp + "] ");
                logArea.append("Agent Response: " + currentBookingRequest + "\n");
                
                if (success) {
                    String message = String.format("Booking confirmed! %d seat(s) booked successfully.", seats.length);
                    logArea.append("Status: SUCCESS - " + message + "\n");
                    logArea.append("Transaction ID: " + transactionId + "\n");
                    logArea.append("\n");
                    
                    // Mark seats as booked
                    markSeatsAsBooked(seats, movie, time, seatClass);
                    
                    // Show success dialog
                    JOptionPane.showMessageDialog(BookingGUI.this, 
                            "Booking successful!\n\n" + currentBookingRequest + "\n\n" + message + "\nTransaction ID: " + transactionId,
                            "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                            
                    // Log success to conversation log
                    EnhancedLoggerUtil.logMessage("GUI", "customer", "BOOKING_SUCCESS", 
                            "gui_booking_" + System.currentTimeMillis(),
                            "Booking confirmed: " + currentBookingRequest + " - " + message);
                } else {
                    String message = "Sorry, the selected seats are no longer available. Please try different seats.";
                    logArea.append("Status: FAILED - " + message + "\n\n");
                    
                    // Show error dialog
                    JOptionPane.showMessageDialog(BookingGUI.this, 
                            "Booking failed!\n\n" + currentBookingRequest + "\n\n" + message,
                            "Booking Failed", JOptionPane.ERROR_MESSAGE);
                            
                    // Log failure to conversation log
                    EnhancedLoggerUtil.logMessage("GUI", "customer", "BOOKING_FAILED", 
                            "gui_booking_" + System.currentTimeMillis(),
                            "Booking failed: " + currentBookingRequest + " - " + message);
                }
                
                logArea.setCaretPosition(logArea.getDocument().getLength());
                waitingForAgentResponse = false;
                currentBookingRequest = "";
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
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