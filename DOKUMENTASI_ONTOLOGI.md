# Dokumentasi Ontologi Sistem Booking Film Multi-Agen

## 1. Gambaran Umum

Ontologi `MovieBookingOntology` mendefinisikan struktur data dan performatif yang digunakan dalam komunikasi antar agen dalam sistem booking film. Ontologi ini mengatur bagaimana informasi film, kursi, dan booking ditukar antara Customer Agent dan Provider Agent.

## 2. Performatif (Performatives)

### 2.1 Performatif Utama
- **REQUEST**: Permintaan informasi atau layanan
- **INFORM**: Memberikan informasi
- **CONFIRM**: Konfirmasi keberhasilan operasi
- **DISCONFIRM**: Konfirmasi kegagalan operasi
- **FAILURE**: Pesan kegagalan sistem
- **AGREE**: Persetujuan terhadap permintaan
- **REFUSE**: Penolakan terhadap permintaan
- **QUERY_IF**: Query untuk mengecek ketersediaan

### 2.2 Deskripsi Performatif

| Performatif | Deskripsi | Penggunaan |
|-------------|-----------|------------|
| REQUEST | Permintaan informasi film atau booking | Customer → Provider |
| INFORM | Memberikan daftar film dan jadwal | Provider → Customer |
| CONFIRM | Konfirmasi booking berhasil | Provider → Customer |
| DISCONFIRM | Konfirmasi booking gagal | Provider → Customer |
| FAILURE | Error sistem atau timeout | Provider → Customer |
| AGREE | Setuju dengan permintaan booking | Provider → Customer |
| REFUSE | Menolak permintaan booking | Provider → Customer |
| QUERY_IF | Mengecek ketersediaan kursi | Customer → Provider |

## 3. Konsep (Concepts)

### 3.1 MOVIE_INFO
**Deskripsi**: Informasi tentang film yang tersedia

**Field Wajib**:
- `title` (String): Judul film
- `showtimes` (String): Jam tayang (format: "10:00,13:00,16:00,19:00,22:00")

**Field Opsional**:
- `date` (String): Tanggal tayang
- `description` (String): Deskripsi film

**Contoh**:
```
title: "Batman: The Dark Knight"
showtimes: "10:00,13:00,16:00,19:00,22:00"
date: "2025-09-04"
description: "Action movie about Batman"
```

### 3.2 BOOKING_REQUEST
**Deskripsi**: Permintaan booking kursi film

**Field Wajib**:
- `movieTitle` (String): Judul film
- `date` (String): Tanggal booking
- `time` (String): Jam tayang
- `seatIds` (String): ID kursi yang diminta (format: "A1;A2;A3")
- `seatClass` (String): Kelas kursi (VIP, Regular, Economy)

**Field Opsional**:
- `customerName` (String): Nama customer
- `customerPhone` (String): Nomor telepon customer

**Contoh**:
```
movieTitle: "Spider-Man: No Way Home"
date: "2025-09-04"
time: "19:00"
seatIds: "A1;A2"
seatClass: "VIP"
customerName: "John Doe"
```

### 3.3 BOOKING_CONFIRMATION
**Deskripsi**: Konfirmasi hasil booking

**Field Wajib**:
- `success` (Boolean): Status keberhasilan
- `message` (String): Pesan konfirmasi
- `transactionId` (String): ID transaksi

**Field Opsional**:
- `bookedSeats` (String): Kursi yang berhasil dibooking
- `totalPrice` (Integer): Total harga

**Contoh**:
```
success: true
message: "Booking berhasil! 2 kursi VIP dibooking untuk Spider-Man: No Way Home"
transactionId: "TXN1756973420551"
bookedSeats: "A1;A2"
totalPrice: 100000
```

### 3.4 ALTERNATIVE_REQUEST
**Deskripsi**: Permintaan kursi alternatif

**Field Wajib**:
- `seatClass` (String): Kelas kursi yang diinginkan
- `preferredSeats` (String): Kursi yang diinginkan (opsional)

**Contoh**:
```
seatClass: "Regular"
preferredSeats: "B1;B2;B3"
```

### 3.5 SEAT_AVAILABILITY
**Deskripsi**: Informasi ketersediaan kursi

**Field Wajib**:
- `seatId` (String): ID kursi
- `available` (Boolean): Status ketersediaan
- `lastChecked` (Integer): Timestamp terakhir dicek

**Contoh**:
```
seatId: "A1"
available: false
lastChecked: 1756973420551
```

## 4. Skema Data

### 4.1 Struktur Pesan REQUEST
```
Performative: REQUEST
Content: MOVIE_INFO atau BOOKING_REQUEST
ConversationId: movie_booking_[timestamp]
ReplyWith: [unique_id]
```

### 4.2 Struktur Pesan INFORM
```
Performative: INFORM
Content: MOVIE_INFO
ConversationId: movie_booking_[timestamp]
InReplyTo: [reply_with_dari_request]
```

### 4.3 Struktur Pesan CONFIRM
```
Performative: CONFIRM
Content: BOOKING_CONFIRMATION
ConversationId: movie_booking_[timestamp]
InReplyTo: [reply_with_dari_booking_request]
```

### 4.4 Struktur Pesan QUERY_IF
```
Performative: QUERY_IF
Content: SEAT_AVAILABILITY
ConversationId: movie_booking_[timestamp]
ReplyWith: [unique_id]
```

## 5. Alur Komunikasi

### 5.1 Alur Happy Path
1. **Customer → Provider**: REQUEST (MOVIE_INFO)
2. **Provider → Customer**: INFORM (MOVIE_INFO)
3. **Customer → Provider**: REQUEST (BOOKING_REQUEST)
4. **Provider → Customer**: AGREE
5. **Customer → Provider**: REQUEST (BOOKING_REQUEST dengan kursi spesifik)
6. **Provider → Customer**: CONFIRM (BOOKING_CONFIRMATION)

### 5.2 Alur Error Handling
1. **Customer → Provider**: REQUEST (BOOKING_REQUEST)
2. **Provider → Customer**: DISCONFIRM (kursi tidak tersedia)
3. **Customer → Provider**: REQUEST (ALTERNATIVE_REQUEST)
4. **Provider → Customer**: INFORM (kursi alternatif) atau REFUSE

## 6. Validasi Data

### 6.1 Validasi MOVIE_INFO
- `title`: Tidak boleh kosong, maksimal 100 karakter
- `showtimes`: Format waktu harus valid (HH:MM)
- `date`: Format tanggal harus valid (YYYY-MM-DD)

### 6.2 Validasi BOOKING_REQUEST
- `movieTitle`: Harus sesuai dengan film yang tersedia
- `date`: Harus sesuai dengan jadwal film
- `time`: Harus sesuai dengan showtimes film
- `seatIds`: Format harus valid (A1;A2;A3)
- `seatClass`: Harus salah satu dari (VIP, Regular, Economy)

### 6.3 Validasi BOOKING_CONFIRMATION
- `success`: Harus boolean
- `message`: Tidak boleh kosong
- `transactionId`: Harus unik, format TXN[timestamp]

## 7. Contoh Penggunaan

### 7.1 Permintaan Informasi Film
```java
ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
msg.setContent("REQUEST_INFO");
msg.setConversationId("movie_booking_1756973420551");
msg.setReplyWith("req_1756973420551");
```

### 7.2 Permintaan Booking
```java
ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
msg.setContent("Movie: Spider-Man: No Way Home, Date: 2025-09-04, Time: 19:00, Seats: A1;A2, Class: VIP");
msg.setConversationId("movie_booking_1756973420551");
msg.setReplyWith("booking_1756973420551");
```

### 7.3 Konfirmasi Booking
```java
ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
msg.setContent("Booking berhasil! 2 kursi VIP dibooking untuk Spider-Man: No Way Home. Transaction ID: TXN1756973420551");
msg.setConversationId("movie_booking_1756973420551");
msg.setInReplyTo("booking_1756973420551");
```

## 8. Ekstensibilitas

### 8.1 Penambahan Performatif Baru
Untuk menambahkan performatif baru:
1. Tambahkan konstanta di `MovieBookingOntology`
2. Update method `addPerformatives()`
3. Update dokumentasi

### 8.2 Penambahan Konsep Baru
Untuk menambahkan konsep baru:
1. Buat schema baru di `MovieBookingOntology`
2. Tambahkan field dengan tipe data yang sesuai
3. Update dokumentasi dan contoh

### 8.3 Penambahan Field Baru
Untuk menambahkan field ke konsep yang ada:
1. Update schema di `MovieBookingOntology`
2. Update validasi data
3. Update dokumentasi

## 9. Best Practices

### 9.1 Penamaan
- Gunakan nama yang deskriptif dan konsisten
- Gunakan format UPPER_CASE untuk konstanta
- Gunakan format camelCase untuk field

### 9.2 Validasi
- Selalu validasi input sebelum mengirim pesan
- Gunakan tipe data yang tepat untuk setiap field
- Implementasikan error handling yang robust

### 9.3 Logging
- Log semua pesan yang dikirim dan diterima
- Gunakan conversation ID untuk tracking
- Implementasikan timestamp untuk audit trail

## 10. Troubleshooting

### 10.1 Masalah Umum
- **Pesan tidak diterima**: Periksa conversation ID dan reply-with
- **Validasi gagal**: Periksa format data dan tipe data
- **Timeout**: Implementasikan retry mechanism

### 10.2 Debug Tips
- Gunakan EnhancedLoggerUtil untuk tracking pesan
- Periksa console output untuk error messages
- Validasi schema ontology secara berkala
