import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;

public class EnhancedProviderAgent extends Agent {
    private final ConcurrentHashMap<String, SeatInfo> seats = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock seatLock = new ReentrantReadWriteLock();
    private final AtomicInteger transactionCounter = new AtomicInteger(0);
    private final Random random = new Random();
    
    private static class SeatInfo {
        boolean available;
        int price;
        String seatClass;
        long lastChecked;
        
        SeatInfo(boolean available, int price, String seatClass) {
            this.available = available;
            this.price = price;
            this.seatClass = seatClass;
            this.lastChecked = System.currentTimeMillis();
        }
    }
    
    @Override
    protected void setup() {
        System.out.println("EnhancedProviderAgent started: " + getLocalName());
        initializeSeats();
        
        addBehaviour(new HandleRequestsBehaviour());
        
        // Add periodic seat availability check
        addBehaviour(new PeriodicSeatCheckBehaviour());
    }
    
    private void initializeSeats() {
        // VIP seats
        seats.put("A1", new SeatInfo(true, 150000, "VIP"));
        seats.put("A2", new SeatInfo(true, 150000, "VIP"));
        seats.put("A3", new SeatInfo(true, 150000, "VIP"));
        
        // Regular seats
        seats.put("B1", new SeatInfo(true, 100000, "Regular"));
        seats.put("B2", new SeatInfo(true, 100000, "Regular"));
        seats.put("B3", new SeatInfo(true, 100000, "Regular"));
        seats.put("B4", new SeatInfo(true, 100000, "Regular"));
        
        // Economy seats
        seats.put("C1", new SeatInfo(true, 75000, "Economy"));
        seats.put("C2", new SeatInfo(true, 75000, "Economy"));
        seats.put("C3", new SeatInfo(true, 75000, "Economy"));
        seats.put("C4", new SeatInfo(true, 75000, "Economy"));
        seats.put("C5", new SeatInfo(true, 75000, "Economy"));
        
        System.out.println("Provider: Kursi diinisialisasi dengan " + seats.size() + " kursi");
    }
    
    private class HandleRequestsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                processMessage(msg);
            } else {
                block();
            }
        }
        
        private void processMessage(ACLMessage msg) {
            EnhancedLoggerUtil.logMessage(msg.getSender().getLocalName(), getLocalName(),
                    ACLMessage.getPerformative(msg.getPerformative()), 
                    msg.getConversationId(), msg.getContent());
            
            String content = msg.getContent();
            
            if (content.startsWith("REQUEST_INFO:")) {
                handleInfoRequest(msg);
            } else if (content.startsWith("BOOKING:")) {
                handleBookingRequest(msg);
            } else if (content.startsWith("ALTERNATIVE:")) {
                handleAlternativeRequest(msg);
            } else {
                sendFailureResponse(msg, "Format pesan tidak dikenali");
            }
        }
        
        private void handleInfoRequest(ACLMessage msg) {
            System.out.println("Provider: Menerima permintaan info film");
            
            // Parse request
            String[] parts = msg.getContent().split(":");
            if (parts.length < 2) {
                sendFailureResponse(msg, "Format request tidak valid");
                return;
            }
            
            String[] params = parts[1].split(",");
            String movieTitle = "";
            String date = "";
            String time = "";
            String seatClass = "";
            int ticketCount = 1;
            
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    switch (keyValue[0].trim()) {
                        case "Film":
                            movieTitle = keyValue[1].trim();
                            break;
                        case "Date":
                            date = keyValue[1].trim();
                            break;
                        case "Time":
                            time = keyValue[1].trim();
                            break;
                        case "Class":
                            seatClass = keyValue[1].trim();
                            break;
                        case "Tickets":
                            try {
                                ticketCount = Integer.parseInt(keyValue[1].trim());
                            } catch (NumberFormatException e) {
                                ticketCount = 1;
                            }
                            break;
                    }
                }
            }
            
            // Generate response
            String response = generateInfoResponse(movieTitle, date, time, seatClass, ticketCount);
            
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(response);
            
            send(reply);
            
            EnhancedLoggerUtil.logMessage(getLocalName(), msg.getSender().getLocalName(),
                    "INFORM", msg.getConversationId(), response);
            
            System.out.println("Provider: Mengirim info opsi film");
        }
        
        private void handleBookingRequest(ACLMessage msg) {
            System.out.println("Provider: Menerima permintaan booking");
            
            String[] parts = msg.getContent().split(":");
            if (parts.length < 2) {
                sendFailureResponse(msg, "Format booking tidak valid");
                return;
            }
            
            String[] params = parts[1].split(",");
            String time = "";
            String[] requestedSeats = {};
            String seatClass = "";
            
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    switch (keyValue[0].trim()) {
                        case "Time":
                            time = keyValue[1].trim();
                            break;
                        case "Seats":
                            requestedSeats = keyValue[1].trim().split(";");
                            break;
                        case "Class":
                            seatClass = keyValue[1].trim();
                            break;
                    }
                }
            }
            
            // Process booking with race condition handling
            boolean bookingSuccess = processBooking(requestedSeats, seatClass);
            
            ACLMessage reply = msg.createReply();
            if (bookingSuccess) {
                String transactionId = "TXN_" + transactionCounter.incrementAndGet();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("Booking berhasil! Transaction ID: " + transactionId + 
                               ", Kursi: " + String.join(",", requestedSeats) + 
                               ", Waktu: " + time);
            } else {
                reply.setPerformative(ACLMessage.DISCONFIRM);
                reply.setContent("Booking gagal! Kursi tidak tersedia atau sudah terpesan. " +
                               "Silakan pilih kursi lain.");
            }
            
            send(reply);
            
            EnhancedLoggerUtil.logMessage(getLocalName(), msg.getSender().getLocalName(),
                    ACLMessage.getPerformative(reply.getPerformative()), 
                    msg.getConversationId(), reply.getContent());
        }
        
        private void handleAlternativeRequest(ACLMessage msg) {
            System.out.println("Provider: Menerima permintaan alternatif");
            
            // Offer alternative seats
            String alternatives = findAlternativeSeats();
            
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent("Alternatif kursi tersedia: " + alternatives);
            
            send(reply);
            
            EnhancedLoggerUtil.logMessage(getLocalName(), msg.getSender().getLocalName(),
                    "INFORM", msg.getConversationId(), reply.getContent());
        }
        
        private void sendFailureResponse(ACLMessage originalMsg, String reason) {
            ACLMessage reply = originalMsg.createReply();
            reply.setPerformative(ACLMessage.FAILURE);
            reply.setContent("Error: " + reason);
            
            send(reply);
            
            EnhancedLoggerUtil.logMessage(getLocalName(), originalMsg.getSender().getLocalName(),
                    "FAILURE", originalMsg.getConversationId(), "Error: " + reason);
        }
    }
    
    private String generateInfoResponse(String movieTitle, String date, String time, String seatClass, int ticketCount) {
        StringBuilder response = new StringBuilder();
        response.append("Movie: ").append(movieTitle).append(", ");
        response.append("Date: ").append(date).append(", ");
        response.append("Showtimes: 10:00, 13:00, 16:00, 19:00, 22:00, ");
        
        // Get available seats for requested class
        response.append("Available ").append(seatClass).append(" seats: ");
        
        seatLock.readLock().lock();
        try {
            int availableCount = 0;
            for (String seatId : seats.keySet()) {
                SeatInfo seat = seats.get(seatId);
                if (seat.available && seat.seatClass.equals(seatClass)) {
                    if (availableCount > 0) response.append(", ");
                    response.append(seatId).append("(").append(seat.price).append(")");
                    availableCount++;
                }
            }
            response.append(", Total available: ").append(availableCount);
        } finally {
            seatLock.readLock().unlock();
        }
        
        return response.toString();
    }
    
    private boolean processBooking(String[] requestedSeats, String seatClass) {
        seatLock.writeLock().lock();
        try {
            // Double-check availability before booking
            for (String seatId : requestedSeats) {
                SeatInfo seat = seats.get(seatId);
                if (seat == null || !seat.available || !seat.seatClass.equals(seatClass)) {
                    return false;
                }
            }
            
            // Book all requested seats
            for (String seatId : requestedSeats) {
                SeatInfo seat = seats.get(seatId);
                seat.available = false;
                seat.lastChecked = System.currentTimeMillis();
            }
            
            return true;
        } finally {
            seatLock.writeLock().unlock();
        }
    }
    
    private String findAlternativeSeats() {
        seatLock.readLock().lock();
        try {
            StringBuilder alternatives = new StringBuilder();
            int count = 0;
            
            for (String seatId : seats.keySet()) {
                SeatInfo seat = seats.get(seatId);
                if (seat.available) {
                    if (count > 0) alternatives.append(", ");
                    alternatives.append(seatId).append("(").append(seat.seatClass).append(")");
                    count++;
                    if (count >= 5) break; // Limit to 5 alternatives
                }
            }
            
            return alternatives.toString();
        } finally {
            seatLock.readLock().unlock();
        }
    }
    
    private class PeriodicSeatCheckBehaviour extends CyclicBehaviour {
        private long lastCheck = System.currentTimeMillis();
        private static final long CHECK_INTERVAL = 10000; // 10 seconds
        
        @Override
        public void action() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCheck >= CHECK_INTERVAL) {
                performSeatCheck();
                lastCheck = currentTime;
            }
            block(CHECK_INTERVAL);
        }
        
        private void performSeatCheck() {
            seatLock.readLock().lock();
            try {
                int availableCount = 0;
                for (SeatInfo seat : seats.values()) {
                    if (seat.available) availableCount++;
                }
                System.out.println("Provider: Seat check - " + availableCount + " kursi tersedia dari " + seats.size() + " total");
            } finally {
                seatLock.readLock().unlock();
            }
        }
    }
}
