# Demo Guide - Multi-Agent Movie Booking System

## Cara Menjalankan Sistem

### 1. Menjalankan dari IntelliJ IDEA
1. Buka project di IntelliJ IDEA
2. Pastikan JADE library sudah ditambahkan ke classpath
3. Klik tombol **Run** pada `EnhancedMainContainer.java`
4. Sistem akan membuka GUI utama

### 2. Langkah-langkah Demo

#### Step 1: Start Agents
1. Klik tombol **"Start Agents"** di GUI utama
2. Tunggu hingga muncul pesan "All agents started successfully!"
3. Klik tombol **"Show Booking GUI"** untuk membuka interface booking

#### Step 2: Booking Film
1. **Pilih Film**: Pilih dari dropdown (contoh: "Batman: The Dark Knight")
2. **Pilih Tanggal**: Gunakan format YYYY-MM-DD (default: hari ini)
3. **Pilih Jam**: Jam tayang akan otomatis update berdasarkan film
4. **Pilih Kelas Kursi**: VIP, Regular, atau Economy
5. **Jumlah Tiket**: 1-10 tiket
6. Klik **"Check Seat Availability"** untuk melihat kursi tersedia
7. Klik **"Submit Booking Request"** untuk memesan

#### Step 3: Verifikasi Booking
1. Sistem akan menampilkan dialog konfirmasi
2. Kursi yang dipesan akan dihapus dari daftar tersedia
3. Log booking akan muncul di area log
4. Informasi kursi akan terupdate secara real-time

## Fitur yang Bisa Didemo

### 1. Tracking Kursi Real-time
- Pilih film dan jam yang sama
- Booking beberapa kursi
- Lihat kursi yang sudah terisi hilang dari daftar
- Coba booking kursi yang sudah terisi → akan muncul error

### 2. Informasi Persisten
- Ganti film → jam tayang berubah otomatis
- Ganti jam → info kursi terupdate
- Ganti kelas kursi → daftar kursi berubah
- Informasi tetap konsisten

### 3. Validasi Booking
- Coba booking lebih banyak tiket dari yang tersedia
- Sistem akan menampilkan error dengan detail
- Coba booking tanpa memilih film/jam
- Validasi input akan mencegah error

### 4. Logging System
- Semua aktivitas tercatat dengan timestamp
- Log tersimpan di file `conversation_log.csv`
- Status booking (SUCCESS/ERROR) tercatat jelas

## Test Cases

### Happy Path
1. Pilih "Batman: The Dark Knight"
2. Pilih jam "19:00"
3. Pilih kelas "VIP"
4. Booking 2 tiket
5. ✅ Berhasil booking kursi A1, A2

### Error Path
1. Pilih film yang sama dan jam yang sama
2. Coba booking 5 tiket VIP (hanya ada 3 kursi VIP)
3. ❌ Error: "Not enough seats available!"

### Race Condition Test
1. Buka 2 instance GUI (jika memungkinkan)
2. Booking kursi yang sama secara bersamaan
3. Sistem akan mencegah double booking

## Troubleshooting

### Jika GUI tidak muncul
- Pastikan JADE library sudah ditambahkan
- Check console untuk error message
- Restart IntelliJ IDEA

### Jika booking gagal
- Check apakah agents sudah running
- Lihat log di console untuk detail error
- Pastikan input valid (film, jam, kelas dipilih)

### Jika kursi tidak terupdate
- Klik "Check Seat Availability" untuk refresh
- Ganti film/jam untuk trigger update
- Restart sistem jika perlu

## File Output

### Log Files
- `conversation_log.csv`: Log semua komunikasi agent
- Console output: Real-time status dan error

### Data Persistence
- Kursi yang sudah terisi tetap terisi sampai program restart
- Informasi film dan jam tersimpan di memory
- Log booking tersimpan permanen

## Tips Demo

1. **Mulai dengan Happy Path** untuk menunjukkan fitur utama
2. **Demo Error Handling** untuk menunjukkan robustness
3. **Show Real-time Updates** dengan booking multiple kali
4. **Explain Logging** dengan membuka file CSV
5. **Demo Persistence** dengan ganti film/jam berulang kali
