import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookingGUI extends JFrame {
    private JTextField movieTitleField;
    private JTextField dateField;
    private JComboBox<String> timeCombo;
    private JComboBox<String> seatClassCombo;
    private JSpinner ticketCountSpinner;
    private JTextArea logArea;
    private JButton submitButton;
    private JButton clearButton;
    
    public BookingGUI() {
        setTitle("Movie Booking System - Customer Interface");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        // Initialize components
        initComponents();
        layoutComponents();
        setupEventHandlers();
        
        setVisible(true);
    }
    
    private void initComponents() {
        movieTitleField = new JTextField(20);
        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        String[] times = {"10:00", "13:00", "16:00", "19:00", "22:00"};
        timeCombo = new JComboBox<>(times);
        
        String[] seatClasses = {"VIP", "Regular", "Economy"};
        seatClassCombo = new JComboBox<>(seatClasses);
        
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 10, 1);
        ticketCountSpinner = new JSpinner(spinnerModel);
        
        logArea = new JTextArea(15, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        submitButton = new JButton("Submit Request");
        clearButton = new JButton("Clear");
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Movie Title:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(movieTitleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(dateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Preferred Time:"), gbc);
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
        
        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(clearButton);
        
        // Log Panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Conversation Log"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        // Add all panels
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitBookingRequest();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
    }
    
    private void submitBookingRequest() {
        // Validate input
        if (movieTitleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a movie title", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create booking request
        String request = String.format("Movie: %s, Date: %s, Time: %s, Class: %s, Tickets: %d",
                movieTitleField.getText().trim(),
                dateField.getText().trim(),
                timeCombo.getSelectedItem(),
                seatClassCombo.getSelectedItem(),
                (Integer) ticketCountSpinner.getValue());
        
        logArea.append("Customer Request: " + request + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        
        // Here you would typically send this to the CustomerAgent
        // For now, we'll just log it
        JOptionPane.showMessageDialog(this, "Request submitted successfully!\n" + request, 
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void clearFields() {
        movieTitleField.setText("");
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeCombo.setSelectedIndex(0);
        seatClassCombo.setSelectedIndex(0);
        ticketCountSpinner.setValue(1);
        logArea.setText("");
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
