# Multi-Agent Movie Booking System

## Deskripsi
Sistem pemesanan film berbasis multi-agent yang mengimplementasikan protokol komunikasi FIPA-ACL dengan fitur negosiasi dan pemesanan layanan yang interaktif.

## Fitur Utama

### 1. Protokol Komunikasi FIPA-ACL
- **REQUEST**: Permintaan informasi film dan pemesanan
- **INFORM**: Penyediaan opsi film dan kursi
- **CONFIRM**: Konfirmasi pemesanan berhasil
- **DISCONFIRM**: Penolakan pemesanan dengan alasan
- **FAILURE**: Penanganan error sistem

### 2. Alur Negosiasi
```
Customer → Provider: REQUEST (informasi film)
Provider → Customer: INFORM (opsi tersedia)
Customer → Provider: REQUEST (pemesanan spesifik)
Provider → Customer: CONFIRM/DISCONFIRM (hasil)
```

### 3. State Management
- Tracking status agen secara real-time
- State machine untuk alur negosiasi
- Timeout handling dan retry mechanism

### 4. Race Condition Handling
- ReentrantReadWriteLock untuk manajemen kursi
- Double-check availability sebelum booking
- ConcurrentHashMap untuk thread-safe operations

### 5. Error Handling
- Timeout detection (30 detik)
- Retry mechanism (maksimal 3x)
- Alternative seat suggestions
- Comprehensive error logging

## Struktur File

### Core Classes
- `EnhancedMainContainer.java` - Container utama dengan GUI
- `EnhancedCustomerAgent.java` - Agen customer dengan state management
- `EnhancedProviderAgent.java` - Agen provider dengan seat management
- `MovieBookingOntology.java` - Definisi ontologi pesan
- `EnhancedLoggerUtil.java` - Logging system (CSV + JSON)

### GUI Components
- `BookingGUI.java` - Interface pemesanan untuk customer
- `EnhancedMainContainer.java` - Control panel untuk sistem

## Cara Menjalankan

### 1. Menggunakan IntelliJ IDEA
1. Buka project di IntelliJ IDEA
2. Pastikan JADE library sudah ditambahkan ke classpath
3. Klik tombol Run pada `EnhancedMainContainer.java`
4. Sistem akan membuka GUI utama

### 2. Menggunakan Command Line
```bash
javac -cp "libs/jade.jar:src" src/*.java
java -cp "libs/jade.jar:src" EnhancedMainContainer
```

## Fitur GUI

### Main Container
- **Start Agents**: Memulai sistem multi-agent
- **Stop Agents**: Menghentikan semua agen
- **Clear Log**: Membersihkan log area
- **Show Booking GUI**: Membuka interface pemesanan

### Booking Interface (Enhanced)
- **Daftar Film**: Dropdown dengan 5 film populer
  - Batman: The Dark Knight
  - Spider-Man: No Way Home
  - Avengers: Endgame
  - Dune
  - Top Gun: Maverick
- **Jam Tayang**: Otomatis update berdasarkan film yang dipilih
- **Tracking Kursi**: Real-time tracking kursi yang sudah terisi
- **Informasi Persisten**: Info kursi tetap ada saat ganti film/jam
- **Validasi Booking**: Cek ketersediaan kursi sebelum booking
- **Log Detail**: Timestamp dan status booking

## Ontologi Pesan

### MovieRequest
```
Film: string (wajib)
Date: string (wajib, format YYYY-MM-DD)
Time: string (wajib, format HH:MM)
Class: string (wajib, VIP/Regular/Economy)
Tickets: integer (wajib, 1-10)
```

### MovieOptions
```
Movie: string
Date: string
Showtimes: string (daftar jam tersedia)
Available seats: string (daftar kursi + harga)
Total available: integer
```

### BookingRequest
```
Time: string (jam yang dipilih)
Seats: string (daftar kursi, dipisah dengan semicolon)
Class: string (kelas kursi)
```

### BookingResponse
```
Success: boolean
Message: string (detail hasil)
Transaction ID: string (ID transaksi)
```

## Logging System

### Format CSV
```
timestamp,sender,receiver,performative,conversationId,content,level
2025-01-20 10:30:15,customer,provider,REQUEST,movie_booking_1,REQUEST_INFO:Film=Batman...,INFO
```

### Format JSON
```json
[
  {
    "timestamp": "2025-01-20T10:30:15",
    "sender": "customer",
    "receiver": "provider",
    "performative": "REQUEST",
    "conversationId": "movie_booking_1",
    "content": "REQUEST_INFO:Film=Batman...",
    "level": "INFO"
  }
]
```

## Test Cases

### Happy Path
1. Customer request info film "Batman"
2. Provider memberikan opsi tersedia
3. Customer memesan kursi A1, A2
4. Provider konfirmasi booking berhasil

### Error Path
1. Customer request info film
2. Provider memberikan opsi
3. Customer memesan kursi yang sudah terpesan
4. Provider memberikan alternatif kursi
5. Customer memesan alternatif
6. Provider konfirmasi booking

## Race Condition Handling

### Seat Availability Check
1. **Read Lock**: Check availability semua kursi
2. **Write Lock**: Double-check dan booking
3. **Timestamp**: Track last modification
4. **Atomic Operations**: Thread-safe counter

### Timeout Management
- Request timeout: 30 detik
- Retry mechanism: maksimal 3x
- Exponential backoff untuk retry

## Dependencies

### Required Libraries
- JADE (Java Agent DEvelopment Framework) 4.5+
- Java 8+ (untuk lambda dan concurrent features)

### External Dependencies
- Swing/AWT (built-in Java)
- Java concurrent utilities
- Java time utilities

## Troubleshooting

### Common Issues
1. **JADE not found**: Pastikan `jade.jar` ada di folder `libs/`
2. **Port conflicts**: JADE menggunakan port default, pastikan tidak ada konflik
3. **Memory issues**: Sistem memerlukan minimal 512MB RAM

### Debug Mode
- Enable debug logging dengan `EnhancedLoggerUtil.logDebug()`
- Check console output untuk detail error
- Monitor log files untuk tracking conversation

## Performance Considerations

### Optimization
- Connection pooling untuk agent communication
- Lazy loading untuk seat information
- Efficient locking strategy (read-write locks)
- Periodic cleanup untuk old logs

### Scalability
- Support untuk multiple customer agents
- Configurable seat inventory
- Extensible ontology system
- Plugin architecture untuk new features

## Future Enhancements

### Planned Features
1. **Database Integration**: SQLite/MySQL untuk persistent storage
2. **Web Interface**: REST API dan web dashboard
3. **Payment Integration**: Gateway pembayaran
4. **Notification System**: Email/SMS confirmations
5. **Analytics Dashboard**: Booking statistics dan reports

### Architecture Improvements
1. **Microservices**: Split into separate services
2. **Message Queue**: RabbitMQ/Apache Kafka
3. **Containerization**: Docker support
4. **Cloud Deployment**: AWS/Azure support

## Contributing

### Development Setup
1. Fork repository
2. Create feature branch
3. Implement changes
4. Add tests
5. Submit pull request

### Code Standards
- Java naming conventions
- Comprehensive error handling
- Thread-safe implementations
- Proper logging
- Unit test coverage

## License
MIT License - see LICENSE file for details

## Support
Untuk pertanyaan dan dukungan, silakan buat issue di repository ini.
