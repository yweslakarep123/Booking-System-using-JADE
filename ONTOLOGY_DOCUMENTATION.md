# Movie Booking Ontology Documentation

## Overview

The Movie Booking Ontology defines the structure and semantics of messages exchanged between agents in the Multi-Agent Movie Booking System. This ontology ensures consistent communication and data validation across all system components.

## Ontology Schema Definitions

### 1. MovieRequest Schema

**Purpose**: Request for movie information and available options

**Fields**:
- `movieTitle` (String, **REQUIRED**): Name of the movie
- `date` (String, **REQUIRED**): Date in YYYY-MM-DD format
- `preferredTime` (String, **REQUIRED**): Preferred showtime in HH:MM format
- `seatClass` (String, **REQUIRED**): Seat class (VIP, Regular, Economy)
- `ticketCount` (Integer, **REQUIRED**): Number of tickets (1-10)

**Data Types**:
- `movieTitle`: String (max 100 characters)
- `date`: String (format: YYYY-MM-DD)
- `preferredTime`: String (format: HH:MM, 24-hour)
- `seatClass`: Enum (VIP, Regular, Economy)
- `ticketCount`: Integer (range: 1-10)

**Example**:
```
REQUEST_INFO:Film=Batman: The Dark Knight,Date=2025-01-20,Time=19:00,Class=VIP,Tickets=2
```

**Validation Rules**:
- All fields are mandatory
- Date must be valid and not in the past
- Time must be in valid 24-hour format
- Seat class must be one of: VIP, Regular, Economy
- Ticket count must be between 1 and 10

### 2. SeatInfo Schema

**Purpose**: Information about individual seat availability and pricing

**Fields**:
- `seatId` (String, **REQUIRED**): Unique seat identifier
- `price` (Integer, **REQUIRED**): Seat price in local currency
- `available` (Boolean, **REQUIRED**): Seat availability status
- `lastChecked` (Long, **OPTIONAL**): Timestamp of last availability check

**Data Types**:
- `seatId`: String (format: [A-C][1-9], e.g., A1, B2, C3)
- `price`: Integer (positive value)
- `available`: Boolean (true/false)
- `lastChecked`: Long (Unix timestamp)

**Example**:
```
A1(150000):true:1705743000000
```

**Validation Rules**:
- Seat ID must follow pattern [A-C][1-9]
- Price must be positive integer
- Availability must be boolean
- Last checked timestamp must be valid

### 3. MovieOptions Schema

**Purpose**: Response containing available movie options and seat information

**Fields**:
- `showtimes` (String, **REQUIRED**): Available showtimes
- `availableSeats` (String, **REQUIRED**): Available seats with pricing
- `prices` (String, **REQUIRED**): Price information by class

**Data Types**:
- `showtimes`: String (comma-separated times)
- `availableSeats`: String (seat list with prices)
- `prices`: String (price breakdown by class)

**Example**:
```
Movie: Batman: The Dark Knight, Date: 2025-01-20, Showtimes: 10:00, 13:00, 16:00, 19:00, 22:00, Available VIP seats: A1(150000), A2(150000), A3(150000), Total available: 3
```

**Validation Rules**:
- Showtimes must be valid time format
- Available seats must be valid seat IDs
- Prices must be positive integers

### 4. BookingRequest Schema

**Purpose**: Request to book specific seats for a movie

**Fields**:
- `selectedTime` (String, **REQUIRED**): Chosen showtime
- `selectedSeats` (String, **REQUIRED**): Comma-separated seat IDs
- `seatClass` (String, **REQUIRED**): Seat class for validation

**Data Types**:
- `selectedTime`: String (format: HH:MM)
- `selectedSeats`: String (semicolon-separated seat IDs)
- `seatClass`: String (VIP, Regular, Economy)

**Example**:
```
BOOKING:Time=19:00,Seats=A1;A2,Class=VIP
```

**Validation Rules**:
- Time must match available showtimes
- Seats must be available and in correct class
- Seat count must match ticket count

### 5. BookingResponse Schema

**Purpose**: Response to booking request with transaction details

**Fields**:
- `success` (Boolean, **REQUIRED**): Booking success status
- `message` (String, **REQUIRED**): Response message
- `transactionId` (String, **REQUIRED**): Unique transaction identifier

**Data Types**:
- `success`: Boolean (true/false)
- `message`: String (descriptive message)
- `transactionId`: String (format: TXN_[number])

**Example**:
```
Booking berhasil! Transaction ID: TXN_001, Kursi: A1,A2, Waktu: 19:00
```

**Validation Rules**:
- Success status must be boolean
- Message must be descriptive
- Transaction ID must be unique

### 6. AlternativeRequest Schema

**Purpose**: Request for alternative seat options

**Fields**:
- `seatClass` (String, **REQUIRED**): Preferred seat class
- `preferredSeats` (String, **OPTIONAL**): Previously requested seats

**Data Types**:
- `seatClass`: String (VIP, Regular, Economy)
- `preferredSeats`: String (comma-separated seat IDs)

**Example**:
```
ALTERNATIVE:VIP
```

**Validation Rules**:
- Seat class must be valid
- Preferred seats must be valid seat IDs

### 7. SeatAvailability Schema

**Purpose**: Real-time seat availability information

**Fields**:
- `seatId` (String, **REQUIRED**): Seat identifier
- `available` (Boolean, **REQUIRED**): Current availability
- `lastChecked` (Long, **REQUIRED**): Last check timestamp

**Data Types**:
- `seatId`: String (format: [A-C][1-9])
- `available`: Boolean (true/false)
- `lastChecked`: Long (Unix timestamp)

**Example**:
```
A1:true:1705743000000
```

**Validation Rules**:
- Seat ID must be valid format
- Availability must be boolean
- Timestamp must be current

## Message Flow Schema

### Request-Response Patterns

#### 1. Movie Information Request
```
Customer → Provider: REQUEST (MovieRequest)
Provider → Customer: INFORM (MovieOptions)
```

#### 2. Seat Availability Check
```
Customer → Provider: QUERY_IF (SeatAvailability)
Provider → Customer: AGREE/REFUSE (SeatAvailability)
```

#### 3. Booking Request
```
Customer → Provider: REQUEST (BookingRequest)
Provider → Customer: CONFIRM/DISCONFIRM (BookingResponse)
```

#### 4. Alternative Request
```
Customer → Provider: REQUEST (AlternativeRequest)
Provider → Customer: INFORM (MovieOptions)
```

## Data Validation Rules

### Field Validation

#### Movie Title
- **Type**: String
- **Length**: 1-100 characters
- **Pattern**: Alphanumeric with spaces, colons, and hyphens
- **Required**: Yes

#### Date
- **Type**: String
- **Format**: YYYY-MM-DD
- **Validation**: Must be valid date, not in past
- **Required**: Yes

#### Time
- **Type**: String
- **Format**: HH:MM (24-hour)
- **Range**: 00:00-23:59
- **Required**: Yes

#### Seat Class
- **Type**: Enum
- **Values**: VIP, Regular, Economy
- **Case**: Case-sensitive
- **Required**: Yes

#### Ticket Count
- **Type**: Integer
- **Range**: 1-10
- **Required**: Yes

#### Seat ID
- **Type**: String
- **Pattern**: [A-C][1-9]
- **Examples**: A1, A2, B1, B2, C1, C2
- **Required**: Yes

#### Price
- **Type**: Integer
- **Range**: > 0
- **Currency**: Local currency units
- **Required**: Yes

## Error Handling Schema

### Error Response Format
```
Error: <error_type>: <error_message>
```

### Error Types

#### 1. Format Errors
- **Code**: FORMAT_ERROR
- **Message**: "Format pesan tidak dikenali"
- **Action**: Request resend with correct format

#### 2. Validation Errors
- **Code**: VALIDATION_ERROR
- **Message**: "Parameter tidak valid"
- **Action**: Check parameter values

#### 3. Business Logic Errors
- **Code**: BUSINESS_ERROR
- **Message**: "Kursi tidak tersedia"
- **Action**: Request alternatives

#### 4. System Errors
- **Code**: SYSTEM_ERROR
- **Message**: "Sistem sedang mengalami gangguan"
- **Action**: Retry after delay

## Robustness Implementation

### Race Condition Handling

#### 1. Seat Locking Mechanism
```java
// Individual seat locks
private final ReentrantLock lock = new ReentrantLock();

// Read-write locks for seat inventory
private final ReentrantReadWriteLock seatLock = new ReentrantReadWriteLock();
```

#### 2. Double-Check Pattern
```java
// Phase 1: Lock all requested seats
for (String seatId : requestedSeats) {
    SeatInfo seat = seats.get(seatId);
    seat.lock.lock();
    lockedSeats.add(seat);
    
    // Phase 2: Double-check availability
    if (!seat.available) return false;
}

// Phase 3: Book all seats atomically
for (SeatInfo seat : lockedSeats) {
    seat.available = false;
}
```

#### 3. Check-Again Before Confirmation
```java
// Customer checks seat availability before booking
private void checkSeatAvailability() {
    setState(AgentState.CHECKING_SEAT);
    ACLMessage check = new ACLMessage(ACLMessage.QUERY_IF);
    check.setContent(lastRequestContent);
    send(check);
}
```

### Concurrent Access Control

#### 1. Thread-Safe Data Structures
```java
// Concurrent seat management
private final ConcurrentHashMap<String, SeatInfo> seats = new ConcurrentHashMap<>();

// Atomic counters
private final AtomicInteger transactionCounter = new AtomicInteger(0);
private final AtomicInteger requestCounter = new AtomicInteger(0);
```

#### 2. Lock Hierarchy
1. **Read Lock**: For seat availability checks
2. **Write Lock**: For seat booking operations
3. **Individual Seat Locks**: For fine-grained control
4. **State Locks**: For agent state management

#### 3. Timeout and Retry
```java
// Timeout handling
private class TimeoutBehaviour extends WakerBehaviour {
    public TimeoutBehaviour() { super(EnhancedCustomerAgent.this, 30000); }
}

// Retry mechanism
if (retryCount < MAX_RETRIES) {
    retryCount++;
    retryOperation();
}
```

## Extensibility Features

### 1. New Message Types
- Add new performatives to ontology
- Extend existing schemas with optional fields
- Maintain backward compatibility

### 2. New Seat Classes
- Add new seat types to enum
- Update pricing logic
- Maintain existing seat mappings

### 3. New Movie Attributes
- Add fields to MovieRequest schema
- Update validation rules
- Maintain existing message format

## Performance Considerations

### 1. Message Size Optimization
- Keep messages under 1KB
- Use efficient encoding
- Compress large responses

### 2. Validation Efficiency
- Cache validation results
- Use compiled regex patterns
- Minimize string operations

### 3. Lock Optimization
- Minimize lock duration
- Use read locks for queries
- Avoid nested locks

## Security Considerations

### 1. Input Sanitization
```java
private String sanitizeInput(String input) {
    return input.trim()
               .replaceAll("[^a-zA-Z0-9\\s:,-]", "")
               .substring(0, Math.min(input.length(), 1000));
}
```

### 2. Message Validation
```java
private boolean validateMessage(ACLMessage msg) {
    return msg.getConversationId() != null && 
           msg.getReplyWith() != null &&
           msg.getContent() != null &&
           !msg.getContent().trim().isEmpty();
}
```

### 3. Access Control
- Validate agent identities
- Check conversation permissions
- Log all message exchanges

## Testing Schema

### 1. Unit Test Cases
- Schema validation tests
- Message format tests
- Error handling tests

### 2. Integration Tests
- End-to-end message flows
- Concurrent access tests
- Race condition tests

### 3. Performance Tests
- Message throughput tests
- Lock contention tests
- Memory usage tests

## Conclusion

The Movie Booking Ontology provides a robust, extensible foundation for agent communication in the Multi-Agent Movie Booking System. It ensures data consistency, handles race conditions effectively, and provides comprehensive error handling while maintaining system performance and security.
