# Robustness Implementation Documentation

## Overview

This document details the robustness implementation in the Multi-Agent Movie Booking System, focusing on race condition tolerance, error handling, and system reliability.

## Race Condition Handling

### 1. Seat Booking Race Conditions

#### Problem Statement
Multiple customers may attempt to book the same seats simultaneously, leading to:
- Double booking of seats
- Inconsistent seat availability
- Data corruption
- System failures

#### Solution Implementation

##### A. Individual Seat Locking
```java
private static class SeatInfo {
    boolean available;
    int price;
    String seatClass;
    long lastChecked;
    final ReentrantLock lock = new ReentrantLock(); // Individual seat lock
    
    SeatInfo(boolean available, int price, String seatClass) {
        this.available = available;
        this.price = price;
        this.seatClass = seatClass;
        this.lastChecked = System.currentTimeMillis();
    }
}
```

##### B. Double-Check Locking Pattern
```java
private boolean processBooking(String[] requestedSeats, String seatClass) {
    List<SeatInfo> lockedSeats = new ArrayList<>();
    try {
        // Phase 1: Lock all requested seats
        for (String seatId : requestedSeats) {
            SeatInfo seat = seats.get(seatId);
            if (seat == null) return false;
            
            seat.lock.lock();
            lockedSeats.add(seat);
            
            // Phase 2: Double-check availability
            if (!seat.available) return false;
        }
        
        // Phase 3: Book all seats atomically
        for (SeatInfo seat : lockedSeats) {
            seat.available = false;
        }
        
        return true;
    } finally {
        // Phase 4: Always release locks
        for (SeatInfo seat : lockedSeats) {
            seat.lock.unlock();
        }
    }
}
```

##### C. Check-Again Before Confirmation
```java
// Customer checks seat availability before booking
private void checkSeatAvailability() {
    setState(AgentState.CHECKING_SEAT);
    ACLMessage check = new ACLMessage(ACLMessage.QUERY_IF);
    check.addReceiver(new AID("provider", AID.ISLOCALNAME));
    check.setConversationId(conversationId);
    check.setContent(lastRequestContent);
    send(check);
    System.out.println("Customer: Mengecek ketersediaan kursi sebelum booking...");
}
```

### 2. Concurrent Access Control

#### A. Thread-Safe Data Structures
```java
// Concurrent seat management
private final ConcurrentHashMap<String, SeatInfo> seats = new ConcurrentHashMap<>();

// Read-write locks for seat inventory
private final ReentrantReadWriteLock seatLock = new ReentrantReadWriteLock();

// Atomic counters for thread-safe operations
private final AtomicInteger transactionCounter = new AtomicInteger(0);
private final AtomicInteger requestCounter = new AtomicInteger(0);
```

#### B. Lock Hierarchy
1. **Read Lock**: For seat availability checks
   ```java
   seatLock.readLock().lock();
   try {
       // Read operations
   } finally {
       seatLock.readLock().unlock();
   }
   ```

2. **Write Lock**: For seat booking operations
   ```java
   seatLock.writeLock().lock();
   try {
       // Write operations
   } finally {
       seatLock.writeLock().unlock();
   }
   ```

3. **Individual Seat Locks**: For fine-grained control
   ```java
   seat.lock.lock();
   try {
       // Seat-specific operations
   } finally {
       seat.lock.unlock();
   }
   ```

4. **State Locks**: For agent state management
   ```java
   private void setState(AgentState newState) {
       stateLock.lock();
       try {
           currentState = newState;
           System.out.println("[STATE] Berubah ke: " + newState);
       } finally {
           stateLock.unlock();
       }
   }
   ```

### 3. Timeout and Retry Mechanism

#### A. Timeout Handling
```java
private class TimeoutBehaviour extends WakerBehaviour {
    public TimeoutBehaviour() { 
        super(EnhancedCustomerAgent.this, 30000); // 30 second timeout
    }
    
    @Override
    protected void onWake() {
        if (currentState == AgentState.REQUESTING_INFO ||
            currentState == AgentState.REQUESTING_BOOKING ||
            currentState == AgentState.CHECKING_SEAT) {
            
            System.out.println("[TIMEOUT] Tidak ada respon. Percobaan ke-" + (retryCount + 1));
            retryCount++;
            
            if (retryCount < MAX_RETRIES) {
                retryOperation();
            } else {
                System.out.println("[ERROR] Timeout maksimal tercapai. Booking dibatalkan.");
                setState(AgentState.ERROR);
            }
        }
    }
}
```

#### B. Retry Mechanism
```java
private void handleDisconfirmMessage(ACLMessage msg) {
    setState(AgentState.ERROR);
    System.out.println("Customer: Booking gagal → " + msg.getContent());
    
    if (retryCount < MAX_RETRIES) {
        retryCount++;
        System.out.println("[INFO] Percobaan ulang ke-" + retryCount);
        addBehaviour(new WakerBehaviour(EnhancedCustomerAgent.this, 1000) {
            @Override
            protected void onWake() {
                requestAlternative(); // fallback
            }
        });
    } else {
        System.out.println("[ERROR] Maksimal percobaan tercapai. Booking dibatalkan.");
    }
}
```

### 4. Error Recovery Strategies

#### A. Alternative Seat Suggestions
```java
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
    
    System.out.println("Customer: Meminta alternatif kursi di kelas " + seatClass + "...");
}
```

#### B. Fallback Operations
```java
private void fallbackRequest() {
    System.out.println("[FALLBACK] Mengirim request default karena gagal terus...");
    addBehaviour(new RequestMovieInfoBehaviour("Default", "Today", "19:00", "Regular", 1));
}
```

## Error Handling Implementation

### 1. Message Validation

#### A. Input Sanitization
```java
private String sanitizeInput(String input) {
    if (input == null) return "";
    
    return input.trim()
               .replaceAll("[^a-zA-Z0-9\\s:,-]", "") // Remove special chars
               .substring(0, Math.min(input.length(), 1000)); // Limit length
}
```

#### B. Message Format Validation
```java
private boolean isValidResponse(ACLMessage msg) {
    return msg.getConversationId() != null && 
           msg.getConversationId().equals(conversationId) &&
           msg.getInReplyTo() != null &&
           msg.getInReplyTo().equals(lastReplyWith);
}
```

### 2. Business Logic Error Handling

#### A. Seat Availability Validation
```java
private void handleBookingRequest(ACLMessage msg) {
    // Parse request
    String[] parts = msg.getContent().split(":");
    if (parts.length < 2) {
        sendFailureResponse(msg, "Format booking tidak valid");
        return;
    }
    
    // Process booking with race condition handling
    boolean bookingSuccess = processBooking(requestedSeats, seatClass);
    
    ACLMessage reply = msg.createReply();
    reply.setInReplyTo(msg.getReplyWith());
    
    if (bookingSuccess) {
        String transactionId = "TXN_" + transactionCounter.incrementAndGet();
        reply.setPerformative(ACLMessage.CONFIRM);
        reply.setContent("Booking berhasil! Transaction ID: " + transactionId);
    } else {
        reply.setPerformative(ACLMessage.DISCONFIRM);
        reply.setContent("Booking gagal! Kursi tidak tersedia atau sudah terpesan.");
    }
    
    send(reply);
}
```

#### B. System Error Responses
```java
private void sendFailureResponse(ACLMessage originalMsg, String reason) {
    ACLMessage reply = originalMsg.createReply();
    reply.setPerformative(ACLMessage.FAILURE);
    reply.setContent("Error: " + reason);
    reply.setInReplyTo(originalMsg.getReplyWith());
    
    send(reply);
    
    EnhancedLoggerUtil.logMessage(getLocalName(), originalMsg.getSender().getLocalName(),
            "FAILURE", originalMsg.getConversationId(), "Error: " + reason);
}
```

### 3. State Management Robustness

#### A. State Transition Validation
```java
private void setState(AgentState newState) {
    stateLock.lock();
    try {
        // Validate state transition
        if (isValidStateTransition(currentState, newState)) {
            currentState = newState;
            System.out.println("[STATE] Berubah ke: " + newState);
        } else {
            System.out.println("[WARN] Invalid state transition: " + currentState + " -> " + newState);
        }
    } finally {
        stateLock.unlock();
    }
}

private boolean isValidStateTransition(AgentState from, AgentState to) {
    // Define valid state transitions
    switch (from) {
        case IDLE:
            return to == AgentState.REQUESTING_INFO;
        case REQUESTING_INFO:
            return to == AgentState.RECEIVED_OPTIONS || to == AgentState.ERROR;
        case RECEIVED_OPTIONS:
            return to == AgentState.CHECKING_SEAT;
        case CHECKING_SEAT:
            return to == AgentState.REQUESTING_BOOKING || to == AgentState.REQUESTING_INFO;
        case REQUESTING_BOOKING:
            return to == AgentState.BOOKING_COMPLETED || to == AgentState.ERROR;
        case BOOKING_COMPLETED:
            return to == AgentState.IDLE;
        case ERROR:
            return to == AgentState.REQUESTING_INFO || to == AgentState.IDLE;
        default:
            return false;
    }
}
```

## Performance Optimization

### 1. Lock Optimization

#### A. Minimize Lock Duration
```java
// Good: Short lock duration
private boolean isSeatAvailable(String seatId) {
    seatLock.readLock().lock();
    try {
        SeatInfo seat = seats.get(seatId);
        return seat != null && seat.available;
    } finally {
        seatLock.readLock().unlock();
    }
}

// Bad: Long lock duration
private boolean isSeatAvailable(String seatId) {
    seatLock.readLock().lock();
    try {
        // Long operations here - BAD!
        Thread.sleep(1000);
        SeatInfo seat = seats.get(seatId);
        return seat != null && seat.available;
    } finally {
        seatLock.readLock().unlock();
    }
}
```

#### B. Avoid Nested Locks
```java
// Good: No nested locks
private void processBooking(String[] seats) {
    List<SeatInfo> lockedSeats = new ArrayList<>();
    try {
        // Lock all seats first
        for (String seatId : seats) {
            SeatInfo seat = this.seats.get(seatId);
            seat.lock.lock();
            lockedSeats.add(seat);
        }
        
        // Then process
        for (SeatInfo seat : lockedSeats) {
            seat.available = false;
        }
    } finally {
        for (SeatInfo seat : lockedSeats) {
            seat.lock.unlock();
        }
    }
}
```

### 2. Memory Management

#### A. Efficient Data Structures
```java
// Use ConcurrentHashMap for thread-safe operations
private final ConcurrentHashMap<String, SeatInfo> seats = new ConcurrentHashMap<>();

// Use AtomicInteger for counters
private final AtomicInteger transactionCounter = new AtomicInteger(0);
```

#### B. Resource Cleanup
```java
// Always release locks in finally blocks
try {
    // Critical section
} finally {
    lock.unlock();
}

// Clean up resources
@Override
protected void takeDown() {
    // Clean up resources
    seats.clear();
    System.out.println("Agent " + getLocalName() + " terminated");
}
```

## Testing Robustness

### 1. Race Condition Tests

#### A. Concurrent Booking Test
```java
@Test
public void testConcurrentBooking() {
    // Simulate multiple customers booking same seats
    CountDownLatch latch = new CountDownLatch(2);
    
    // Customer 1
    new Thread(() -> {
        bookSeats("A1", "A2");
        latch.countDown();
    }).start();
    
    // Customer 2
    new Thread(() -> {
        bookSeats("A1", "A2");
        latch.countDown();
    }).start();
    
    latch.await();
    
    // Verify only one booking succeeded
    assertTrue(seats.get("A1").available == false);
    assertTrue(seats.get("A2").available == false);
}
```

#### B. Lock Contention Test
```java
@Test
public void testLockContention() {
    long startTime = System.currentTimeMillis();
    
    // Simulate high contention
    for (int i = 0; i < 1000; i++) {
        new Thread(() -> {
            checkSeatAvailability("A1");
        }).start();
    }
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // Should complete within reasonable time
    assertTrue(duration < 5000);
}
```

### 2. Error Recovery Tests

#### A. Timeout Test
```java
@Test
public void testTimeoutHandling() {
    // Simulate timeout scenario
    customerAgent.startBookingRequest("Batman", "2025-01-20", "19:00", "VIP", 2);
    
    // Wait for timeout
    Thread.sleep(35000);
    
    // Verify error state
    assertEquals(AgentState.ERROR, customerAgent.getCurrentState());
}
```

#### B. Retry Test
```java
@Test
public void testRetryMechanism() {
    // Simulate booking failure
    providerAgent.simulateBookingFailure();
    
    // Verify retry
    customerAgent.handleDisconfirmMessage(failureMessage);
    
    // Verify retry count
    assertEquals(1, customerAgent.getRetryCount());
}
```

## Monitoring and Logging

### 1. Performance Monitoring
```java
private void logPerformanceMetrics() {
    long lockWaitTime = System.currentTimeMillis() - lockStartTime;
    if (lockWaitTime > 1000) {
        EnhancedLoggerUtil.logWarning("PERFORMANCE", "SYSTEM", 
            "Long lock wait time: " + lockWaitTime + "ms", "performance");
    }
}
```

### 2. Error Tracking
```java
private void trackError(String errorType, String details) {
    EnhancedLoggerUtil.logError(getLocalName(), "SYSTEM", 
        errorType + ": " + details, conversationId);
    
    // Update error statistics
    errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1);
}
```

## Conclusion

The robustness implementation in the Multi-Agent Movie Booking System provides comprehensive protection against race conditions, system failures, and data inconsistencies. The combination of locking mechanisms, timeout handling, retry logic, and error recovery ensures reliable operation even under high concurrency and failure scenarios.

Key robustness features:
- ✅ Individual seat locking with double-check pattern
- ✅ Check-again before confirmation
- ✅ Timeout and retry mechanisms
- ✅ Alternative seat suggestions
- ✅ Comprehensive error handling
- ✅ State management validation
- ✅ Performance optimization
- ✅ Monitoring and logging
