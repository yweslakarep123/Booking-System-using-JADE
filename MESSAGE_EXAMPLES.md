# FIPA-ACL Message Examples

## Overview

This document provides comprehensive examples of FIPA-ACL messages used in the Multi-Agent Movie Booking System, demonstrating proper protocol implementation with conversation-id, reply-with, and in-reply-to fields.

## Message Flow Examples

### 1. Movie Information Request

#### Customer → Provider (REQUEST)

```java
ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
request.addReceiver(new AID("provider", AID.ISLOCALNAME));
request.setConversationId("movie_booking_1");
request.setReplyWith("info_request_1705743015000");
request.setContent("REQUEST_INFO:Film=Batman: The Dark Knight,Date=2025-01-20,Time=19:00,Class=VIP,Tickets=2");
```

**Message Details:**
- **Performative**: REQUEST
- **Conversation ID**: movie_booking_1
- **Reply-With**: info_request_1705743015000
- **Content**: Structured request with all required parameters

#### Provider → Customer (INFORM)

```java
ACLMessage reply = msg.createReply();
reply.setPerformative(ACLMessage.INFORM);
reply.setInReplyTo("info_request_1705743015000");
reply.setContent("Movie: Batman: The Dark Knight, Date: 2025-01-20, Showtimes: 10:00, 13:00, 16:00, 19:00, 22:00, Available VIP seats: A1(150000), A2(150000), A3(150000), Total available: 3");
```

**Message Details:**
- **Performative**: INFORM
- **In-Reply-To**: info_request_1705743015000
- **Content**: Available options with seat details and pricing

### 2. Seat Availability Check

#### Customer → Provider (QUERY_IF)

```java
ACLMessage check = new ACLMessage(ACLMessage.QUERY_IF);
check.addReceiver(new AID("provider", AID.ISLOCALNAME));
check.setConversationId("movie_booking_1");
check.setContent("REQUEST_INFO:Film=Batman: The Dark Knight,Date=2025-01-20,Time=19:00,Class=VIP,Tickets=2");
```

**Message Details:**
- **Performative**: QUERY_IF
- **Purpose**: Double-check seat availability before booking
- **Content**: Same as original request for consistency

#### Provider → Customer (AGREE)

```java
ACLMessage agree = msg.createReply();
agree.setPerformative(ACLMessage.AGREE);
agree.setInReplyTo(msg.getReplyWith());
agree.setContent("Seats A1 and A2 are available for booking");
```

**Message Details:**
- **Performative**: AGREE
- **Purpose**: Confirms seats are still available
- **Content**: Confirmation message

### 3. Booking Request

#### Customer → Provider (REQUEST)

```java
ACLMessage book = new ACLMessage(ACLMessage.REQUEST);
book.addReceiver(new AID("provider", AID.ISLOCALNAME));
book.setConversationId("movie_booking_1");
book.setReplyWith("booking_1705743020000");
book.setContent("BOOKING:Time=19:00,Seats=A1;A2,Class=VIP");
```

**Message Details:**
- **Performative**: REQUEST
- **Reply-With**: booking_1705743020000
- **Content**: Specific booking request with seat selection

#### Provider → Customer (CONFIRM)

```java
ACLMessage confirm = msg.createReply();
confirm.setPerformative(ACLMessage.CONFIRM);
confirm.setInReplyTo("booking_1705743020000");
confirm.setContent("Booking berhasil! Transaction ID: TXN_001, Kursi: A1,A2, Waktu: 19:00");
```

**Message Details:**
- **Performative**: CONFIRM
- **In-Reply-To**: booking_1705743020000
- **Content**: Success confirmation with transaction details

### 4. Booking Failure Scenario

#### Customer → Provider (REQUEST)

```java
ACLMessage book = new ACLMessage(ACLMessage.REQUEST);
book.addReceiver(new AID("provider", AID.ISLOCALNAME));
book.setConversationId("movie_booking_2");
book.setReplyWith("booking_1705743030000");
book.setContent("BOOKING:Time=19:00,Seats=A1;A2,Class=VIP");
```

#### Provider → Customer (DISCONFIRM)

```java
ACLMessage disconfirm = msg.createReply();
disconfirm.setPerformative(ACLMessage.DISCONFIRM);
disconfirm.setInReplyTo("booking_1705743030000");
disconfirm.setContent("Booking gagal! Kursi tidak tersedia atau sudah terpesan. Silakan pilih kursi lain.");
```

**Message Details:**
- **Performative**: DISCONFIRM
- **In-Reply-To**: booking_1705743030000
- **Content**: Failure message with explanation

### 5. Alternative Seat Request

#### Customer → Provider (REQUEST)

```java
ACLMessage alt = new ACLMessage(ACLMessage.REQUEST);
alt.addReceiver(new AID("provider", AID.ISLOCALNAME));
alt.setConversationId("movie_booking_2");
alt.setContent("ALTERNATIVE:VIP");
```

**Message Details:**
- **Performative**: REQUEST
- **Content**: Request for alternative seats in VIP class

#### Provider → Customer (INFORM)

```java
ACLMessage altReply = msg.createReply();
altReply.setPerformative(ACLMessage.INFORM);
altReply.setInReplyTo(msg.getReplyWith());
altReply.setContent("Alternatif kursi tersedia: A3(VIP), B1(Regular), B2(Regular), C1(Economy), C2(Economy)");
```

**Message Details:**
- **Performative**: INFORM
- **Content**: List of available alternative seats

### 6. System Error Handling

#### Provider → Customer (FAILURE)

```java
ACLMessage failure = msg.createReply();
failure.setPerformative(ACLMessage.FAILURE);
failure.setInReplyTo(msg.getReplyWith());
failure.setContent("Error: Format pesan tidak dikenali");
```

**Message Details:**
- **Performative**: FAILURE
- **Content**: Error message with details

## Message Validation Examples

### Valid Message Structure

```java
// Valid request message
ACLMessage validRequest = new ACLMessage(ACLMessage.REQUEST);
validRequest.addReceiver(new AID("provider", AID.ISLOCALNAME));
validRequest.setConversationId("movie_booking_1");
validRequest.setReplyWith("request_123");
validRequest.setContent("REQUEST_INFO:Film=Batman,Date=2025-01-20,Time=19:00,Class=VIP,Tickets=2");

// Valid response message
ACLMessage validResponse = msg.createReply();
validResponse.setPerformative(ACLMessage.INFORM);
validResponse.setInReplyTo("request_123");
validResponse.setContent("Movie information available");
```

### Invalid Message Examples

```java
// Missing conversation ID
ACLMessage invalid1 = new ACLMessage(ACLMessage.REQUEST);
invalid1.addReceiver(new AID("provider", AID.ISLOCALNAME));
// invalid1.setConversationId("movie_booking_1"); // MISSING
invalid1.setReplyWith("request_123");
invalid1.setContent("REQUEST_INFO:Film=Batman");

// Missing reply-with
ACLMessage invalid2 = new ACLMessage(ACLMessage.REQUEST);
invalid2.addReceiver(new AID("provider", AID.ISLOCALNAME));
invalid2.setConversationId("movie_booking_1");
// invalid2.setReplyWith("request_123"); // MISSING
invalid2.setContent("REQUEST_INFO:Film=Batman");

// Missing in-reply-to in response
ACLMessage invalid3 = msg.createReply();
invalid3.setPerformative(ACLMessage.INFORM);
// invalid3.setInReplyTo(msg.getReplyWith()); // MISSING
invalid3.setContent("Response content");
```

## Content Format Specifications

### Request Info Format

```
REQUEST_INFO:Film=<movie_title>,Date=<YYYY-MM-DD>,Time=<HH:MM>,Class=<VIP|Regular|Economy>,Tickets=<number>
```

**Example:**
```
REQUEST_INFO:Film=Batman: The Dark Knight,Date=2025-01-20,Time=19:00,Class=VIP,Tickets=2
```

### Booking Request Format

```
BOOKING:Time=<HH:MM>,Seats=<seat1>;<seat2>;<seat3>,Class=<VIP|Regular|Economy>
```

**Example:**
```
BOOKING:Time=19:00,Seats=A1;A2,Class=VIP
```

### Alternative Request Format

```
ALTERNATIVE:<seat_class>
```

**Example:**
```
ALTERNATIVE:VIP
```

## Conversation Tracking

### Conversation ID Format

```
movie_booking_<counter>
```

**Examples:**
- `movie_booking_1`
- `movie_booking_2`
- `movie_booking_3`

### Reply-With Format

```
<operation>_<timestamp>
```

**Examples:**
- `info_request_1705743015000`
- `booking_1705743020000`
- `check_1705743018000`

## Error Message Examples

### Format Validation Errors

```java
// Invalid date format
"Error: Format tanggal tidak valid. Gunakan format YYYY-MM-DD"

// Invalid time format
"Error: Format waktu tidak valid. Gunakan format HH:MM"

// Invalid seat class
"Error: Kelas kursi tidak valid. Pilih VIP, Regular, atau Economy"

// Invalid ticket count
"Error: Jumlah tiket harus antara 1-10"
```

### Business Logic Errors

```java
// Seat not available
"Booking gagal! Kursi A1 sudah tidak tersedia"

// Insufficient seats
"Booking gagal! Hanya 1 kursi tersedia, tetapi diminta 2 kursi"

// Invalid seat selection
"Booking gagal! Kursi A1 tidak termasuk dalam kelas VIP"

// System error
"Error: Sistem sedang mengalami gangguan. Silakan coba lagi nanti"
```

## Logging Examples

### CSV Log Entry

```csv
2025-01-20 10:30:15,customer,provider,REQUEST,movie_booking_1,REQUEST_INFO:Film=Batman: The Dark Knight,Date=2025-01-20,Time=19:00,Class=VIP,Tickets=2,INFO
```

### JSON Log Entry

```json
{
  "timestamp": "2025-01-20T10:30:15",
  "sender": "customer",
  "receiver": "provider",
  "performative": "REQUEST",
  "conversationId": "movie_booking_1",
  "content": "REQUEST_INFO:Film=Batman: The Dark Knight,Date=2025-01-20,Time=19:00,Class=VIP,Tickets=2",
  "level": "INFO"
}
```

## Performance Considerations

### Message Size Optimization

- **Content Length**: Keep messages under 1KB when possible
- **Parameter Count**: Limit to essential parameters only
- **Encoding**: Use efficient string encoding
- **Compression**: Consider compression for large messages

### Network Optimization

- **Connection Reuse**: Reuse agent connections
- **Batch Operations**: Group related requests
- **Async Processing**: Use non-blocking message handling
- **Timeout Management**: Implement appropriate timeouts

## Security Considerations

### Message Validation

```java
private boolean validateMessage(ACLMessage msg) {
    // Check required fields
    if (msg.getConversationId() == null) return false;
    if (msg.getReplyWith() == null) return false;
    
    // Validate content format
    String content = msg.getContent();
    if (content == null || content.trim().isEmpty()) return false;
    
    // Check for injection attempts
    if (content.contains(";") || content.contains("'") || content.contains("\"")) {
        return false;
    }
    
    return true;
}
```

### Input Sanitization

```java
private String sanitizeInput(String input) {
    if (input == null) return "";
    
    return input.trim()
               .replaceAll("[^a-zA-Z0-9\\s:,-]", "") // Remove special chars
               .substring(0, Math.min(input.length(), 1000)); // Limit length
}
```

## Testing Message Examples

### Unit Test Cases

```java
@Test
public void testValidRequestMessage() {
    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    msg.setConversationId("movie_booking_1");
    msg.setReplyWith("request_123");
    msg.setContent("REQUEST_INFO:Film=Batman,Date=2025-01-20,Time=19:00,Class=VIP,Tickets=2");
    
    assertTrue(validateMessage(msg));
}

@Test
public void testInvalidRequestMessage() {
    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    // Missing conversation ID
    msg.setReplyWith("request_123");
    msg.setContent("REQUEST_INFO:Film=Batman");
    
    assertFalse(validateMessage(msg));
}
```

### Integration Test Scenarios

1. **Complete Booking Flow**: Test end-to-end message exchange
2. **Error Handling**: Test failure scenarios
3. **Concurrent Requests**: Test multiple simultaneous bookings
4. **Timeout Scenarios**: Test message timeout handling
5. **Recovery Testing**: Test system recovery after failures

## Conclusion

These message examples demonstrate proper FIPA-ACL protocol implementation with comprehensive error handling, validation, and logging. The system ensures reliable communication between agents while maintaining data integrity and system security.
