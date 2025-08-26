# Demo GUI Bioskop Cinema XXI Style

## Cara Menggunakan Enhanced Cinema GUI

### 1. Memulai Aplikasi
1. Jalankan `EnhancedCinemaGUI.java` dari IntelliJ IDEA
2. Atau gunakan command line: `java -cp "libs/jade.jar:src" EnhancedCinemaGUI`
3. Atau gunakan script: `run_cinema_gui.bat` (Windows) / `./run_cinema_gui.sh` (Linux/Mac)

### 2. Interface Overview
```
┌─────────────────────────────────────────────────────────────────────────────┐
│ Cinema XXI - Enhanced Seat Selection                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│ [Movie Selection]                                                          │
│ Movie: [Batman vs Superman ▼] Time: [19:00 ▼] Date: [2025-01-20] [Load]  │
│ Current: Batman vs Superman | 19:00 | 2025-01-20 | Please select seats   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                    SCREEN                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                    [Select Your Seats]                                     │
│ A  [A1] [A2] [A3] [A4] [A5] [A6] [A7] [A8] [A9] [A10] [A11] [A12]     │
│ B  [B1] [B2] [B3] [B4] [B5] [B6] [B7] [B8] [B9] [B10] [B11] [B12]     │
│ C  [C1] [C2] [C3] [C4] [C5] [C6] [C7] [C8] [C9] [C10] [C11] [C12]     │
│ D  [D1] [D2] [D3] [D4] [D5] [D6] [D7] [D8] [D9] [D10] [D11] [D12]     │
│ E  [E1] [E2] [E3] [E4] [E5] [E6] [E7] [E8] [E9] [E10] [E11] [E12]     │
│ F  [F1] [F2] [F3] [F4] [F5] [F6] [F7] [F8] [F9] [F10] [F11] [F12]     │
│ G  [G1] [G2] [G3] [G4] [G5] [G6] [G7] [G8] [G9] [G10] [G11] [G12]     │
│ H  [H1] [H2] [H3] [H4] [H5] [H6] [H7] [H8] [H9] [H10] [H11] [H12]     │
│     1    2    3    4    5    6    7    8    9    10    11    12           │
│                                                                             │
│ [Legend] Available: [🟢] Selected: [🔵] Booked: [🔴]                      │
├─────────────────────────────────────────────────────────────────────────────┤
│ [Confirm Booking] [Clear Selection] [Refresh Seats]                       │
│ [Booking Log & System Messages]                                            │
│ === Seats loaded for Batman vs Superman at 19:00 on 2025-01-20 ===        │
│ Booked seats: A1, A2, A3, B5, B6, C8, C9, D3, D4, E10, F7, G12, H4      │
│ Available seats: 83                                                        │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3. Langkah-langkah Booking

#### Step 1: Pilih Film dan Waktu
1. **Movie**: Pilih film dari dropdown (default: Batman vs Superman)
2. **Time**: Pilih jam tayang (default: 19:00)
3. **Date**: Masukkan tanggal (default: hari ini)
4. **Load Seats**: Klik tombol "Load Seats" untuk memuat kursi

#### Step 2: Pilih Kursi
1. **Available Seats** (🟢): Klik kursi hijau untuk memilih
2. **Selected Seats** (🔵): Kursi yang dipilih akan berubah menjadi biru
3. **Booked Seats** (🔴): Kursi merah tidak bisa dipilih
4. **Multiple Selection**: Bisa pilih beberapa kursi sekaligus

#### Step 3: Konfirmasi Booking
1. **Review Selection**: Lihat kursi yang dipilih di status bar
2. **Confirm**: Klik "Confirm Booking" untuk konfirmasi
3. **Success**: Kursi akan berubah menjadi merah (booked)
4. **Log Update**: Detail booking akan muncul di log area

### 4. Fitur Interaktif

#### Real-time Updates
- Status kursi berubah secara real-time
- Counter jumlah kursi yang dipilih
- Visual feedback untuk setiap aksi

#### Smart Seat Loading
- Setiap kombinasi film+waktu memiliki pattern booking yang berbeda
- Simulasi realistic untuk popularitas film
- Kursi VIP (baris A) lebih sering terpesan

#### Error Handling
- Pesan error yang jelas untuk kursi yang sudah terpesan
- Validasi input tanggal
- Confirmation dialog untuk setiap aksi penting

### 5. Tips Penggunaan

#### Optimal Seat Selection
- **Baris A**: VIP seats, view terbaik, harga tertinggi
- **Baris B-D**: Regular seats, balance view dan harga
- **Baris E-H**: Economy seats, harga terjangkau

#### Booking Strategy
- Pilih kursi di tengah untuk view optimal
- Hindari kursi di pinggir untuk kenyamanan
- Book lebih awal untuk film populer

#### Interface Navigation
- Gunakan scroll untuk melihat semua kursi
- Hover mouse untuk tooltip informasi
- Gunakan legend untuk memahami warna

### 6. Troubleshooting

#### Common Issues
1. **Kursi tidak bisa dipilih**: Pastikan kursi berwarna hijau (available)
2. **Error saat booking**: Refresh seats dan coba lagi
3. **Interface tidak responsive**: Pastikan semua dependencies terinstall

#### Performance Tips
- Tutup aplikasi lain untuk performa optimal
- Gunakan refresh button jika ada lag
- Monitor log area untuk informasi sistem

## Demo Scenarios

### Scenario 1: Happy Path
1. Pilih "Batman vs Superman" + "19:00"
2. Load seats
3. Pilih kursi A4, A5
4. Confirm booking
5. Lihat kursi berubah menjadi merah

### Scenario 2: Alternative Selection
1. Pilih "Avengers: Endgame" + "22:00"
2. Load seats (akan ada lebih sedikit kursi terpesan)
3. Pilih kursi B1, B2
4. Confirm booking

### Scenario 3: Multiple Movies
1. Test berbagai kombinasi film dan waktu
2. Bandingkan pattern booking yang berbeda
3. Lihat bagaimana sistem menangani perubahan

## Integration dengan Multi-Agent System

### Logging Integration
- Setiap booking otomatis tersimpan di `conversation_log.csv`
- Format log: `CUSTOMER,SYSTEM,BOOKING_CONFIRMED,cinema_booking_xxx,details`
- Data tersimpan dengan timestamp dan conversation ID

### Future Integration
- Bisa diintegrasikan dengan `EnhancedProviderAgent` untuk real-time seat management
- Support untuk multiple customer agents
- Real-time synchronization antar GUI dan agent system

## Technical Details

### Seat Layout Algorithm
- Grid-based layout menggunakan `GridBagLayout`
- Dynamic seat creation berdasarkan konfigurasi
- Responsive design untuk berbagai ukuran window

### Color Management
- Consistent color scheme untuk semua status
- High contrast untuk accessibility
- Tooltip untuk informasi detail

### Event Handling
- Mouse click events untuk seat selection
- Real-time UI updates
- Comprehensive error handling
