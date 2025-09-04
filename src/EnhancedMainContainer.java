import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jade.core.Runtime;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EnhancedMainContainer extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton clearButton;
    private JButton showGUIButton;

    private Runtime rt;
    private AgentContainer container;
    private AgentController providerAgent;
    private AgentController customerAgent;
    private BookingGUI bookingGUI;

    private boolean isRunning = false;

    public EnhancedMainContainer() {
        setTitle("Multi-Agent Movie Booking System - Main Container");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();
        setupEventHandlers();

        setVisible(true);
    }

    private void initComponents() {
        logArea = new JTextArea(20, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        startButton = new JButton("Start Agents");
        stopButton = new JButton("Stop Agents");
        clearButton = new JButton("Clear Log");
        showGUIButton = new JButton("Show Booking GUI");

        stopButton.setEnabled(false);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // Control Panel
        JPanel controlPanel = new JPanel();
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(clearButton);
        controlPanel.add(showGUIButton);

        // Log Panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("System Log"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Add panels
        add(controlPanel, BorderLayout.NORTH);
        add(logPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startAgents();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopAgents();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logArea.setText("");
            }
        });

        showGUIButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBookingGUI();
            }
        });
    }

    private void startAgents() {
        try {
            logMessage("Starting Multi-Agent System...");

            // Log system startup
            EnhancedLoggerUtil.logSystemStartup();
            logMessage("Log file created: " + EnhancedLoggerUtil.getCurrentLogFileName());

            // Initialize JADE Runtime
            rt = Runtime.instance();
            Profile p = new ProfileImpl();
            container = rt.createMainContainer(p);

            logMessage("Main container created successfully");

            // Create and start Provider Agent
            providerAgent = container.createNewAgent("provider",
                    EnhancedProviderAgent.class.getName(), null);
            providerAgent.start();
            logMessage("Provider Agent started successfully");

            // Create and start Customer Agent
            customerAgent = container.createNewAgent("customer",
                    EnhancedCustomerAgent.class.getName(), null);
            customerAgent.start();
            logMessage("Customer Agent started successfully");

            // Update UI state
            isRunning = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);

            logMessage("All agents started successfully!");
            logMessage("System is ready for movie booking operations");

        } catch (StaleProxyException e) {
            logMessage("Error starting agents: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logMessage("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopAgents() {
        try {
            logMessage("Stopping Multi-Agent System...");

            if (customerAgent != null) {
                customerAgent.kill();
                logMessage("Customer Agent stopped");
            }

            if (providerAgent != null) {
                providerAgent.kill();
                logMessage("Provider Agent stopped");
            }

            if (container != null) {
                container.kill();
                logMessage("Main container stopped");
            }

            // Update UI state
            isRunning = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);

            logMessage("All agents stopped successfully");

        } catch (StaleProxyException e) {
            logMessage("Error stopping agents: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logMessage("Unexpected error while stopping: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showBookingGUI() {
        if (bookingGUI == null || !bookingGUI.isVisible()) {
            // Pass customer agent to BookingGUI
            bookingGUI = new BookingGUI(customerAgent);
            
            // Get the actual agent instance and set it in GUI
            try {
                EnhancedCustomerAgent agentInstance = customerAgent.getO2AInterface(EnhancedCustomerAgent.class);
                bookingGUI.setCustomerAgent(agentInstance);
            } catch (Exception e) {
                System.err.println("Warning: Could not get agent instance: " + e.getMessage());
            }
            
            logMessage("Booking GUI opened with agent connection");
        } else {
            bookingGUI.toFront();
            logMessage("Booking GUI brought to front");
        }
    }

    // Method untuk mendapatkan reference ke customer agent
    public AgentController getCustomerAgent() {
        return customerAgent;
    }

    private void logMessage(String message) {
        String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + timestamp + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        System.out.println("[" + timestamp + "] " + message);
    }

    public static void main(String[] args) {
        // Set JADE properties for better console output
        System.setProperty("jade.core.messaging.TopicManagementService", "jade.core.messaging.TopicManagementService");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new EnhancedMainContainer();
            }
        });
    }
}