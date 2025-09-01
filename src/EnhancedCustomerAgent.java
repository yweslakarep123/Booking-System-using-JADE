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
        WAITING_CONFIRMATION, BOOKING_COMPLETED, ERROR
    }
    
    private AgentState currentState = AgentState.IDLE;
    private String conversationId;
    private String lastRequestContent;
    private int retryCount = 0;
    private final int MAX_RETRIES = 3;
    private final ReentrantLock stateLock = new ReentrantLock();
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    
    @Override
    protected void setup() {
        System.out.println("EnhancedCustomerAgent started: " + getLocalName());
        System.out.println("Customer Agent is ready. Waiting for booking requests...");
        
        // Add cyclic behavior to handle responses
        addBehaviour(new HandleResponsesBehaviour());
        
        // Add timeout behavior
        addBehaviour(new TimeoutBehaviour());
    }
    

    
    private class HandleResponsesBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                handleResponse(msg);
            } else {
                block();
            }
        }
        
        private void handleResponse(ACLMessage msg) {
            EnhancedLoggerUtil.logMessage(msg.getSender().getLocalName(), getLocalName(),
                    ACLMessage.getPerformative(msg.getPerformative()), 
                    msg.getConversationId(), msg.getContent());
            
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
                default:
                    System.out.println("Customer: Pesan tidak dikenal: " + msg.getPerformative());
            }
        }
        
        private void handleInformMessage(ACLMessage msg) {
            setState(AgentState.RECEIVED_OPTIONS);
            System.out.println("Customer: Menerima opsi film dari provider");
            System.out.println("Opsi: " + msg.getContent());
            
            // Simulate customer decision making
            addBehaviour(new WakerBehaviour(EnhancedCustomerAgent.this, 2000) {
                @Override
                protected void onWake() {
                    requestBooking();
                }
            });
        }
        
        private void handleConfirmMessage(ACLMessage msg) {
            setState(AgentState.BOOKING_COMPLETED);
            System.out.println("Customer: Booking berhasil! → " + msg.getContent());
            retryCount = 0; // Reset retry count on success
        }
        
        private void handleDisconfirmMessage(ACLMessage msg) {
            setState(AgentState.ERROR);
            System.out.println("Customer: Booking gagal → " + msg.getContent());
            
            if (retryCount < MAX_RETRIES) {
                retryCount++;
                System.out.println("Customer: Mencoba alternatif (attempt " + retryCount + "/" + MAX_RETRIES + ")");
                
                // Request alternative options
                addBehaviour(new WakerBehaviour(EnhancedCustomerAgent.this, 1000) {
                    @Override
                    protected void onWake() {
                        requestAlternative();
                    }
                });
            } else {
                System.out.println("Customer: Maksimal percobaan tercapai. Booking dibatalkan.");
            }
        }
        
        private void handleFailureMessage(ACLMessage msg) {
            setState(AgentState.ERROR);
            System.out.println("Customer: Error dari provider → " + msg.getContent());
        }
    }
    
    private void requestBooking() {
        setState(AgentState.REQUESTING_BOOKING);
        
        String bookingRequest = createBookingRequest();
        lastRequestContent = bookingRequest;
        
        ACLMessage book = new ACLMessage(ACLMessage.REQUEST);
        book.addReceiver(new AID("provider", AID.ISLOCALNAME));
        book.setConversationId(conversationId);
        book.setContent(bookingRequest);
        book.setReplyWith("booking_" + System.currentTimeMillis());
        
        send(book);
        
                    EnhancedLoggerUtil.logMessage(getLocalName(), "provider", "REQUEST", conversationId, bookingRequest);
        System.out.println("Customer: Mengirim permintaan booking...");
        System.out.println("State: " + currentState);
    }
    
    private void requestAlternative() {
        setState(AgentState.REQUESTING_INFO);
        
        String alternativeRequest = createAlternativeRequest();
        lastRequestContent = alternativeRequest;
        
        ACLMessage alt = new ACLMessage(ACLMessage.REQUEST);
        alt.addReceiver(new AID("provider", AID.ISLOCALNAME));
        alt.setConversationId(conversationId);
        alt.setContent(alternativeRequest);
        alt.setReplyWith("alternative_" + System.currentTimeMillis());
        
        send(alt);

        EnhancedLoggerUtil.logMessage(getLocalName(), "provider", "REQUEST", conversationId, alternativeRequest);
        System.out.println("Customer: Meminta alternatif...");
    }
    
    private class TimeoutBehaviour extends WakerBehaviour {
        public TimeoutBehaviour() {
            super(EnhancedCustomerAgent.this, 30000); // 30 second timeout
        }
        
        @Override
        protected void onWake() {
            if (currentState == AgentState.REQUESTING_INFO || 
                currentState == AgentState.REQUESTING_BOOKING) {
                System.out.println("Customer: Timeout! Mencoba lagi...");
                retryCount++;
                
                if (retryCount < MAX_RETRIES) {
                    if (currentState == AgentState.REQUESTING_INFO) {
                        addBehaviour(new RequestMovieInfoBehaviour("Default", "Today", "19:00", "Regular", 1));
                    } else {
                        requestBooking();
                    }
                } else {
                    System.out.println("Customer: Timeout maksimal tercapai. Booking dibatalkan.");
                    setState(AgentState.ERROR);
                }
            }
        }
    }
    

    
    private String createBookingRequest() {
        return "BOOKING:Time=19:00,Seats=A1,A2,Class=VIP";
    }
    
    private String createAlternativeRequest() {
        return "ALTERNATIVE:Time=19:00,Class=Regular,Tickets=2";
    }
    
    private void setState(AgentState newState) {
        stateLock.lock();
        try {
            currentState = newState;
            System.out.println("Customer State changed to: " + newState);
        } finally {
            stateLock.unlock();
        }
    }
    
    public AgentState getCurrentState() {
        stateLock.lock();
        try {
            return currentState;
        } finally {
            stateLock.unlock();
        }
    }
    
    // Method untuk memulai request dari GUI
    public void startBookingRequest(String movieTitle, String date, String time, String seatClass, int ticketCount) {
        addBehaviour(new RequestMovieInfoBehaviour(movieTitle, date, time, seatClass, ticketCount));
    }
    
    // Updated RequestMovieInfoBehaviour dengan parameter
    private class RequestMovieInfoBehaviour extends OneShotBehaviour {
        private String movieTitle;
        private String date;
        private String time;
        private String seatClass;
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
            
            // Create structured request
            String movieRequest = createMovieRequest(movieTitle, date, time, seatClass, ticketCount);
            lastRequestContent = movieRequest;
            conversationId = "movie_booking_" + requestCounter.incrementAndGet();
            
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(new AID("provider", AID.ISLOCALNAME));
            request.setConversationId(conversationId);
            request.setContent(movieRequest);
            request.setReplyWith("request_" + System.currentTimeMillis());
            
            send(request);
            
            EnhancedLoggerUtil.logMessage(getLocalName(), "provider", "REQUEST", conversationId, movieRequest);
            System.out.println("Customer: Mengirim permintaan info film...");
            System.out.println("State: " + currentState);
        }
    }
    
    private String createDefaultMovieRequest() {
        return "REQUEST_INFO:Film=Default,Date=Today,Time=19:00,Class=Regular,Tickets=1";
    }
    
    private String createMovieRequest(String movieTitle, String date, String time, String seatClass, int ticketCount) {
        return String.format("REQUEST_INFO:Film=%s,Date=%s,Time=%s,Class=%s,Tickets=%d", 
                movieTitle, date, time, seatClass, ticketCount);
    }
}
