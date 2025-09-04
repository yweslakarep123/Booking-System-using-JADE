import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class EnhancedCustomerAgent extends Agent {
    private enum AgentState {
        IDLE, REQUESTING_INFO, RECEIVED_OPTIONS, REQUESTING_BOOKING,
        WAITING_CONFIRMATION, BOOKING_COMPLETED, ERROR, CHECKING_SEAT
    }

    private AgentState currentState = AgentState.IDLE;
    private String conversationId;
    private String lastRequestContent;
    private String lastReplyWith;
    private int retryCount = 0;
    private final int MAX_RETRIES = 3;
    private final ReentrantLock stateLock = new ReentrantLock();
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    @Override
    protected void setup() {
        System.out.println("EnhancedCustomerAgent started: " + getLocalName());
        addBehaviour(new HandleResponsesBehaviour());
        addBehaviour(new TimeoutBehaviour());
        addBehaviour(new HandleO2AMessageBehaviour());
        
        // Enable O2A communication
        setEnabledO2ACommunication(true, 0);
    }
    
    @Override
    protected void takeDown() {
        System.out.println("EnhancedCustomerAgent " + getLocalName() + " terminated");
    }
    
    // Handle O2A messages from GUI
    private class HandleO2AMessageBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            Object obj = getO2AObject();
            if (obj != null) {
                if (obj instanceof Object[]) {
                    Object[] args = (Object[]) obj;
                    if (args.length >= 5) {
                        String movieTitle = (String) args[0];
                        String date = (String) args[1];
                        String time = (String) args[2];
                        String seatClass = (String) args[3];
                        int ticketCount = (Integer) args[4];
                        
                        // Log GUI request
                        EnhancedLoggerUtil.logMessage("GUI", getLocalName(), "O2A_REQUEST", 
                                "gui_request_" + System.currentTimeMillis(),
                                "GUI booking request: " + movieTitle + " at " + time + " for " + ticketCount + " tickets");
                        
                        // Start booking process directly (skip info request, go straight to booking)
                        startDirectBookingRequest(movieTitle, date, time, seatClass, ticketCount);
                    }
                }
            } else {
                block();
            }
        }
    }

    // ====================== Handle Responses ======================
    private class HandleResponsesBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                // Robustness: validasi pesan
                if (!isValidResponse(msg)) {
                    System.out.println("[WARN] Pesan diabaikan (conversationId tidak cocok): " + msg.getContent());
                    return;
                }
                handleResponse(msg);
            } else {
                block();
            }
        }

        private void handleResponse(ACLMessage msg) {
            // Log incoming message
            EnhancedLoggerUtil.logMessage(msg.getSender().getLocalName(), getLocalName(),
                    ACLMessage.getPerformative(msg.getPerformative()),
                    msg.getConversationId(), msg.getContent());
            
            // Validate in-reply-to for proper FIPA-ACL compliance
            if (lastReplyWith != null && !lastReplyWith.equals(msg.getInReplyTo())) {
                System.out.println("[WARN] Message in-reply-to mismatch. Expected: " + lastReplyWith + ", Got: " + msg.getInReplyTo());
                EnhancedLoggerUtil.logWarning(getLocalName(), msg.getSender().getLocalName(),
                        "Message in-reply-to mismatch", msg.getConversationId());
            }
            
            switch (msg.getPerformative()) {
                case ACLMessage.INFORM:
                    handleInformMessage(msg);
                    break;
                case ACLMessage.CONFIRM:
                    handleConfirmMessage(msg);
                    break;
                case ACLMessage.DISCONFIRM:
                    handleDisconfirmMessage(msg);
                    break;
                case ACLMessage.FAILURE:
                    handleFailureMessage(msg);
                    break;
                case ACLMessage.AGREE:
                    if (currentState == AgentState.CHECKING_SEAT) {
                        System.out.println("Customer: Kursi tersedia, melanjutkan booking...");
                        sendActualBookingRequest();
                    }
                    break;
                case ACLMessage.REFUSE:
                    if (currentState == AgentState.CHECKING_SEAT) {
                        System.out.println("Customer: Kursi sudah tidak tersedia, meminta alternatif...");
                        requestAlternative();
                    }
                    break;
                default:
                    System.out.println("Customer: Pesan tidak dikenal: " + msg.getPerformative());
            }
        }

        private void handleInformMessage(ACLMessage msg) {
            setState(AgentState.RECEIVED_OPTIONS);

            if (msg.getContent().contains("Alternatif kursi")) {
                System.out.println("Customer: Menerima kursi alternatif → " + msg.getContent());
            } else {
                System.out.println("Customer: Menerima opsi film → " + msg.getContent());
                // Don't automatically check seat availability - wait for user to make booking request
                // The seat check will happen when user actually requests booking
            }
        }

        private void handleConfirmMessage(ACLMessage msg) {
            setState(AgentState.BOOKING_COMPLETED);
            System.out.println("Customer: Booking berhasil → " + msg.getContent());
            retryCount = 0;
            
            // Extract transaction ID from message
            String transactionId = "";
            if (msg.getContent().contains("Transaction ID:")) {
                String[] parts = msg.getContent().split("Transaction ID:");
                if (parts.length > 1) {
                    transactionId = parts[1].trim().split(",")[0];
                }
            }
            
            // Send confirmation to GUI via O2A
            sendConfirmationToGUI(true, msg.getContent(), transactionId);
        }

        private void handleDisconfirmMessage(ACLMessage msg) {
            setState(AgentState.ERROR);
            System.out.println("Customer: Booking gagal → " + msg.getContent());
            
            // Log booking failure
            EnhancedLoggerUtil.logError(getLocalName(), "provider", 
                    "Booking failed: " + msg.getContent(), msg.getConversationId());

            if (msg.getContent().contains("Alternatif")) {
                System.out.println("Customer: Kursi alternatif yang ditawarkan → " + msg.getContent());
            }

            if (retryCount < MAX_RETRIES) {
                retryCount++;
                System.out.println("[INFO] Percobaan ulang ke-" + retryCount);
                EnhancedLoggerUtil.logMessage(getLocalName(), "SYSTEM", "RETRY", 
                        msg.getConversationId(), "Retry attempt " + retryCount);
                addBehaviour(new WakerBehaviour(EnhancedCustomerAgent.this, 1000) {
                    @Override
                    protected void onWake() {
                        requestAlternative(); // fallback
                    }
                });
            } else {
                System.out.println("[ERROR] Maksimal percobaan tercapai. Booking dibatalkan.");
                EnhancedLoggerUtil.logError(getLocalName(), "SYSTEM", 
                        "Maximum retry attempts reached. Booking cancelled.", msg.getConversationId());
                
                // Send failure confirmation to GUI
                sendConfirmationToGUI(false, msg.getContent(), null);
            }
        }

        private void handleFailureMessage(ACLMessage msg) {
            setState(AgentState.ERROR);
            System.out.println("[ERROR] Dari provider → " + msg.getContent());
            
            // Log system failure
            EnhancedLoggerUtil.logError(getLocalName(), "provider", 
                    "System failure: " + msg.getContent(), msg.getConversationId());
            
            if (retryCount < MAX_RETRIES) {
                retryCount++;
                System.out.println("[INFO] Retry booking (Failure). Percobaan ke-" + retryCount);
                EnhancedLoggerUtil.logMessage(getLocalName(), "SYSTEM", "RETRY", 
                        msg.getConversationId(), "Retry after failure - attempt " + retryCount);
                sendActualBookingRequest();
            } else {
                EnhancedLoggerUtil.logError(getLocalName(), "SYSTEM", 
                        "Maximum retry attempts reached after failure. Using fallback.", msg.getConversationId());
                fallbackRequest();
            }
        }
    }

    // ====================== Booking / Check ======================
    private void checkSeatAvailability() {
        setState(AgentState.CHECKING_SEAT);
        ACLMessage check = new ACLMessage(ACLMessage.QUERY_IF);
        check.addReceiver(new AID("provider", AID.ISLOCALNAME));
        check.setConversationId(conversationId);
        check.setContent(lastRequestContent);
        send(check);
        
        // Log outgoing message
        EnhancedLoggerUtil.logMessage(getLocalName(), "provider", "QUERY_IF", 
                conversationId, lastRequestContent);
        
        System.out.println("Customer: Mengecek ketersediaan kursi sebelum booking...");
    }

    private void sendBookingRequest() {
        // First check seat availability
        checkSeatAvailability();
    }
    
    private void sendActualBookingRequest() {
        setState(AgentState.REQUESTING_BOOKING);
        ACLMessage book = new ACLMessage(ACLMessage.REQUEST);
        book.addReceiver(new AID("provider", AID.ISLOCALNAME));
        book.setConversationId(conversationId);
        book.setContent(lastRequestContent);
        lastReplyWith = "booking_" + System.currentTimeMillis();
        book.setReplyWith(lastReplyWith);
        send(book);
        
        // Log outgoing message
        EnhancedLoggerUtil.logMessage(getLocalName(), "provider", "REQUEST", 
                conversationId, lastRequestContent);
        
        System.out.println("Customer: Mengirim permintaan booking...");
    }

    private void requestAlternative() {
        setState(AgentState.REQUESTING_INFO);

        String seatClass = "Regular"; // default
        if (lastRequestContent.contains("Class=")) {
            seatClass = lastRequestContent.split("Class=")[1].split(",")[0];
        }

        String alternativeRequest = String.format("ALTERNATIVE:%s", seatClass);
        lastRequestContent = alternativeRequest;

        ACLMessage alt = new ACLMessage(ACLMessage.REQUEST);
        alt.addReceiver(new AID("provider", AID.ISLOCALNAME));
        alt.setConversationId(conversationId);
        alt.setContent(alternativeRequest);
        send(alt);
        
        // Log outgoing message
        EnhancedLoggerUtil.logMessage(getLocalName(), "provider", "REQUEST", 
                conversationId, alternativeRequest);

        System.out.println("Customer: Meminta alternatif kursi di kelas " + seatClass + "...");
    }

    private void fallbackRequest() {
        System.out.println("[FALLBACK] Mengirim request default karena gagal terus...");
        addBehaviour(new RequestMovieInfoBehaviour("Default", "Today", "19:00", "Regular", 1));
    }

    private boolean isValidResponse(ACLMessage msg) {
        return msg.getConversationId() != null && msg.getConversationId().equals(conversationId);
    }

    // ====================== Timeout ======================
    private class TimeoutBehaviour extends WakerBehaviour {
        public TimeoutBehaviour() { super(EnhancedCustomerAgent.this, 30000); }
        @Override
        protected void onWake() {
            if (currentState == AgentState.REQUESTING_INFO ||
                    currentState == AgentState.REQUESTING_BOOKING ||
                    currentState == AgentState.CHECKING_SEAT) {
                System.out.println("[TIMEOUT] Tidak ada respon. Percobaan ke-" + (retryCount + 1));
                
                // Log timeout
                EnhancedLoggerUtil.logWarning(getLocalName(), "SYSTEM", 
                        "Timeout occurred in state: " + currentState, 
                        conversationId != null ? conversationId : "system");
                
                retryCount++;
                if (retryCount < MAX_RETRIES) {
                    EnhancedLoggerUtil.logMessage(getLocalName(), "SYSTEM", "TIMEOUT_RETRY", 
                            conversationId != null ? conversationId : "system", 
                            "Timeout retry attempt " + retryCount);
                    
                    if (currentState == AgentState.REQUESTING_INFO) {
                        addBehaviour(new RequestMovieInfoBehaviour("Default", "Today", "19:00", "Regular", 1));
                    } else if (currentState == AgentState.CHECKING_SEAT) {
                        checkSeatAvailability();
                    } else if (currentState == AgentState.REQUESTING_BOOKING) {
                        sendActualBookingRequest();
                    }
                } else {
                    System.out.println("[ERROR] Timeout maksimal tercapai. Booking dibatalkan.");
                    EnhancedLoggerUtil.logError(getLocalName(), "SYSTEM", 
                            "Maximum timeout retry attempts reached. Booking cancelled.", 
                            conversationId != null ? conversationId : "system");
                    setState(AgentState.ERROR);
                }
            }
        }
    }

    // ====================== Request Movie Info ======================
    public void startBookingRequest(String movieTitle, String date, String time, String seatClass, int ticketCount) {
        addBehaviour(new RequestMovieInfoBehaviour(movieTitle, date, time, seatClass, ticketCount));
    }
    
    // Direct booking request from GUI (skip info request)
    public void startDirectBookingRequest(String movieTitle, String date, String time, String seatClass, int ticketCount) {
        setState(AgentState.REQUESTING_BOOKING);
        
        // Create booking request content
        String seatIds = generateSeatIds(seatClass, ticketCount);
        lastRequestContent = String.format("BOOKING:Time=%s,Seats=%s,Class=%s", time, seatIds, seatClass);
        conversationId = "movie_booking_" + requestCounter.incrementAndGet();
        
        // Log the booking request
        EnhancedLoggerUtil.logMessage(getLocalName(), "provider", "BOOKING_REQUEST", 
                conversationId, "Direct booking: " + movieTitle + " at " + time + " for seats " + seatIds);
        
        // Send booking request directly
        sendActualBookingRequest();
    }
    
    private String generateSeatIds(String seatClass, int ticketCount) {
        StringBuilder seats = new StringBuilder();
        String prefix = "";
        switch (seatClass) {
            case "VIP": prefix = "A"; break;
            case "Regular": prefix = "B"; break;
            case "Economy": prefix = "C"; break;
        }
        
        for (int i = 1; i <= ticketCount; i++) {
            if (seats.length() > 0) seats.append(";");
            seats.append(prefix).append(i);
        }
        
        return seats.toString();
    }
    
    // Send confirmation to GUI
    private void sendConfirmationToGUI(boolean success, String message, String transactionId) {
        try {
            // Create confirmation object
            Object[] confirmation = {success, message, transactionId};
            putO2AObject(confirmation, false);
            
            // Log the confirmation
            EnhancedLoggerUtil.logMessage(getLocalName(), "GUI", "CONFIRMATION", 
                    conversationId != null ? conversationId : "system",
                    "Sending confirmation to GUI: " + (success ? "SUCCESS" : "FAILED") + " - " + message);
        } catch (Exception e) {
            System.err.println("Error sending confirmation to GUI: " + e.getMessage());
            EnhancedLoggerUtil.logError(getLocalName(), "GUI", 
                    "Failed to send confirmation: " + e.getMessage(), 
                    conversationId != null ? conversationId : "system");
        }
    }

    private class RequestMovieInfoBehaviour extends OneShotBehaviour {
        private String movieTitle, date, time, seatClass;
        private int ticketCount;

        public RequestMovieInfoBehaviour(String movieTitle, String date, String time, String seatClass, int ticketCount) {
            this.movieTitle = movieTitle;
            this.date = date;
            this.time = time;
            this.seatClass = seatClass;
            this.ticketCount = ticketCount;
        }

        @Override
        public void action() {
            setState(AgentState.REQUESTING_INFO);
            lastRequestContent = createMovieRequest(movieTitle, date, time, seatClass, ticketCount);
            conversationId = "movie_booking_" + requestCounter.incrementAndGet();

            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(new AID("provider", AID.ISLOCALNAME));
            request.setConversationId(conversationId);
            request.setContent(lastRequestContent);
            lastReplyWith = "info_request_" + System.currentTimeMillis();
            request.setReplyWith(lastReplyWith);
            send(request);
            
            // Log outgoing message
            EnhancedLoggerUtil.logMessage(getLocalName(), "provider", "REQUEST", 
                    conversationId, lastRequestContent);

            System.out.println("Customer: Mengirim permintaan info film...");
        }
    }

    private String createMovieRequest(String movieTitle, String date, String time, String seatClass, int ticketCount) {
        return String.format("REQUEST_INFO:Film=%s,Date=%s,Time=%s,Class=%s,Tickets=%d",
                movieTitle, date, time, seatClass, ticketCount);
    }

    private void setState(AgentState newState) {
        stateLock.lock();
        try {
            AgentState oldState = currentState;
            currentState = newState;
            System.out.println("[STATE] Berubah ke: " + newState);
            
            // Log state transition
            EnhancedLoggerUtil.logMessage(getLocalName(), "SYSTEM", "STATE_CHANGE", 
                    conversationId != null ? conversationId : "system", 
                    "State changed from " + oldState + " to " + newState);
        } finally {
            stateLock.unlock();
        }
    }
}