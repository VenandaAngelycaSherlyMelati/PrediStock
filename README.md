<p align="center">
  <img src="src/main/resources/static/images/logo.png" alt="PrediStock Official Logo" width="460">
</p>

# PANDUAN CARA MENJALANKAN APLIKASI PREDISTOCK & TROUBLESHOOTING

> **Sistem Kasir (POS) dan Peramalan Kebutuhan Stok UMKM Berbasis Prinsip OOP Murni**  
> **Tugas Besar Pemrograman Berorientasi Objek (PBO) — Telkom University**  
> *Panduan ini ditujukan untuk seluruh rekan kelompok agar dapat menjalankan aplikasi di laptop masing-masing tanpa kendala.*

---

>  **PENTING UNTUK BAHAN PRESENTASI & SIDANG:**  
> Untuk penjelasan detail konsep OOP per modul (Modul 1 s.d. Modul 5, PIC masing-masing, skenario demo di depan Dosen, dan kisi-kisi tanya jawab sidang), silakan buka berkas:  
>  **[PANDUAN_MODUL_DAN_SIDANG.md](file:///c:/Users/zaini/Documents/Tugas_kuliah_Telkom_University/PBO/Tubes/PANDUAN_MODUL_DAN_SIDANG.md)**

---

##  Ringkasan Cepat: Mengapa Aplikasi Ini Bebas Repot? (Zero Configuration)

Rekan-rekan kelompok **TIDAK PERLU** pusing mengatur *environment* database yang rumit:
*  **TIDAK PERLU install XAMPP, MySQL, atau PostgreSQL** (Database SQLite berjalan otomatis di dalam Java).
*  **TIDAK PERLU install Maven mandiri di Windows** (VS Code dan IntelliJ sudah memiliki Maven bawaan).
*  **TIDAK PERLU install Node.js / NPM** (Antarmuka web dashboard sudah tertanam langsung di Spring Boot).
*  **TIDAK PERLU menginput data dari nol** (Saat aplikasi pertama kali berjalan di laptop Anda, sistem otomatis membuat database dan menyuntikkan data uji coba lengkap selama 14 hari: akun, produk, dan transaksi).

---

##  CHECKLIST: Apa Saja yang DIBUTUHKAN di Laptop Anda?

Hanya ada **2 HAL** yang wajib terpasang di laptop Anda:

### 1. Java Development Kit (JDK) Versi 17 atau Lebih Baru (WAJIB)
* **Cara Memeriksa:** Buka Command Prompt / PowerShell / Terminal, ketik:
  ```bash
  java -version
  ```
* **Kriteria:** Versi yang muncul harus minimal `17.x.x` (contoh: Java 17, Java 21, atau Java 25).
* **Jika belum punya JDK 17+:**
  Download installer resmi gratis dari [Adoptium Eclipse Temurin JDK 17 / 21](https://adoptium.net/temurin/releases/?version=17) (pilih versi *Windows x64 .msi*), lalu jalankan instalasi hingga selesai (*Next, Next, Finish*).

### 2. Code Editor: Visual Studio Code + Ekstensi Java (WAJIB)
* Buka **Visual Studio Code**.
* Tekan shortcut `Ctrl + Shift + X` (atau klik ikon kotak puzzle *Extensions* di bilah menu kiri VS Code).
* Di kotak pencarian, ketik: **`Extension Pack for Java`** (dibuat resmi oleh *Microsoft*).
* Klik tombol **Install**.
* *(Ekstensi ini otomatis menyertakan Java Language Server, Compiler, Debugger, dan Maven internal).*

---

##  Panduan Menjalankan Aplikasi (Langkah Demi Langkah)

### CARA 1: Menggunakan Visual Studio Code (Paling Mudah & Sangat Disarankan)

1. **Ekstrak File Proyek:**
   * Ekstrak file ZIP proyek ke salah satu folder di laptop Anda (misalnya di folder `Documents` atau `D:\Kuliah\Tubes`).
2. **Buka Folder di VS Code:**
   * Buka aplikasi VS Code.
   * Klik menu **File** ➔ **Open Folder...** ➔ Pilih folder **`Tubes`** tempat proyek ini berada.
   * *Tunggu sekitar 5–10 detik sampai VS Code selesai memuat project (ikon bulat kecil di status bar bawah berhenti berputar).*
3. **Buka Berkas Utama (Main Class):**
   * Di panel berkas (*Explorer*) sebelah kiri, buka folder:  
     `src` ➔ `main` ➔ `java` ➔ `com` ➔ `predistock` ➔ **`PrediStockApplication.java`**
4. **Jalankan Aplikasi (Tombol Run):**
   * Cari baris kode `public static void main(String[] args)`.
   * Tepat di atas baris tersebut akan muncul tombol teks kecil bertuliskan **`Run`** | `Debug`.
   * Klik tombol **`Run`**.
5. **Periksa Output Terminal:**
   * Terminal di bagian bawah VS Code akan otomatis terbuka dan menampilkan proses *startup*.
   * Begitu berhasil, Anda akan melihat banner terminal seperti berikut:
     ```text
     [DatabaseConnection] Using SQLite database at: .../.predistock/predistock.db
     [DatabaseConnection] Successfully seeded all requested test data.
     ==================================================
        PrediStock Backend is Running on port 8080!    
        Database: SQLite (Raw JDBC mode)              
        Store: Toko Zaini Sejahtera                    
        Owner:     owner  / admin123 (Tubagus Zaini)   
        Cashier 1: kasir1 / kasir123 (Ruqayyah)        
        Cashier 2: kasir2 / kasir123 (Ida)             
     ==================================================
     ```
6. **Buka Aplikasi di Browser:**
   * Buka Google Chrome, Microsoft Edge, atau Firefox.
   * Ketik alamat berikut di address bar:  
      **[http://localhost:8080/](http://localhost:8080/)**
   * Layar login interaktif PrediStock akan langsung terbuka!

---

### CARA 2: Menggunakan IntelliJ IDEA

1. Buka IntelliJ IDEA ➔ Pilih **Open** ➔ Arahkan ke folder **`Tubes`** (pilih buka sebagai *Maven Project*).
2. Tunggu proses *indexing* dependensi Maven selesai.
3. Buka file `src/main/java/com/predistock/PrediStockApplication.java`.
4. Klik tombol panah hijau **`▶`** (Run) di sebelah baris `public class PrediStockApplication` atau `main` method.
5. Buka browser di **`http://localhost:8080/`**.

---

### CARA 3: Menggunakan Command Prompt / Terminal Mandiri (Maven)

*(Hanya gunakan cara ini jika laptop Anda sudah mengkonfigurasi Maven di Windows PATH environment variables)*:
1. Buka Command Prompt / PowerShell di dalam folder `Tubes`.
2. Jalankan perintah:
   ```bash
   mvn spring-boot:run
   ```
3. Tunggu hingga tulisan `Started PrediStockApplication` muncul.
4. Buka browser di **`http://localhost:8080/`**.

---

##  Kredensial Login Akun Uji Coba (Tersedia Tombol 1-Klik)

Pada halaman login web, telah disediakan tombol **1-Klik Demo** agar Anda dan rekan tidak perlu mengetik username dan password berulang kali:

| Tombol Demo | Username | Password | Nama Pengguna | Peran (Role) & Hak Akses |
|---|---|---|---|---|
| **Owner** | `owner` | `admin123` | Tubagus Zaini | **OWNER (Pemilik Toko):** Hak penuh (POS, Tambah/Edit Harga/Hapus Produk, Peramalan AI, Laporan Finansial, Manajemen Akun Kasir, Profil Toko, Reset Data). |
| **Kasir 1** | `kasir1` | `kasir123` | Ruqayyah | **CASHIER (Shift 1 - Pagi):** Transaksi kasir POS, Cetak struk shift, Restok barang masuk, Edit info produk (Harga dikunci). Menu owner terkunci gembok. |
| **Kasir 2** | `kasir2` | `kasir123` | Ida | **CASHIER (Shift 2 - Siang):** Transaksi kasir POS, Cetak struk shift, Restok barang masuk, Edit info produk (Harga dikunci). Menu owner terkunci gembok. |

---

##  Panduan Troubleshooting & Solusi Masalah Umum (FAQ)

Berikut adalah daftar kemungkinan kendala yang mungkin dialami rekan kelompok dan solusi praktisnya:

---

### 1. Error: *"Port 8080 was already in use / Address already in use: bind"*
* **Gejala:** Muncul pesan error merah di terminal VS Code: `Web server failed to start. Port 8080 was already in use.`
* **Penyebab:** Masih ada aplikasi Java sebelumnya yang sedang berjalan di latar belakang (*background process*) dan belum ditutup.
* **Solusi Cepat (Pilih salah satu):**
  * **Opsi A (Paling Mudah):** Di VS Code, cari bilah debug bar kecil di bagian atas layar, lalu klik tombol **Kotak Merah (`■` Stop)**. Atau klik area terminal lalu tekan `Ctrl + C`.
  * **Opsi B (Via Command Prompt Windows):**
    Buka Command Prompt (CMD), ketik perintah berikut untuk mematikan proses yang menyandera port 8080:
    ```cmd
    netstat -ano | findstr :8080
    ```
    Perhatikan angka PID di kolom paling kanan (misal: `10104`), lalu matikan prosesnya dengan mengetik:
    ```cmd
    taskkill /PID 10104 /F
    ```
  * Setelah proses dimatikan, klik tombol **Run** kembali di `PrediStockApplication.java`.

---

### 2. Tombol teks `Run` kecil tidak muncul di atas `main()` di VS Code
* **Penyebab:** Ekstensi Java belum terpasang atau VS Code masih dalam proses *indexing/loading*.
* **Solusi:**
  1. Pastikan ekstensi **Extension Pack for Java** sudah terpasang (*Installed*) di tab Extensions VS Code (`Ctrl + Shift + X`).
  2. Perhatikan pojok kanan bawah status bar VS Code: jika ada ikon bulat berputar bertuliskan *"Opening Java Projects"*, tunggu sebentar hingga selesai.
  3. Tutup file `PrediStockApplication.java` lalu buka kembali.
  4. Jika masih belum muncul, klik kanan di dalam editor `PrediStockApplication.java` ➔ Pilih **Run Java**.

---

### 3. Error: *"package org.springframework.boot does not exist"* atau *"package does not exist"*
* **Penyebab:** Anda menjalankan file menggunakan tombol panah kanan atas milik ekstensi pihak ketiga (seperti *Code Runner*), bukan menggunakan Java Language Server resmi Microsoft.
* **Solusi:**
  * **JANGAN** klik ikon play segitiga di pojok kanan atas editor.
  * **KLIK TOMBOL TEKS `Run`** yang berada tepat di atas tulisan baris:  
    `public static void main(String[] args)`.

---

### 4. Error: *"java: class file has wrong version 61.0, should be 55.0"* atau *"Java version mismatch"*
* **Penyebab:** Komputer Anda masih menggunakan Java versi lama (misalnya Java 8 atau Java 11). Spring Boot 3.2 membutuhkan minimal **Java 17**.
* **Solusi:**
  1. Unduh dan pasang **JDK 17 atau JDK 21** dari [Adoptium Temurin](https://adoptium.net/).
  2. Di VS Code, tekan `Ctrl + Shift + P` ➔ ketik: `Java: Configure Java Runtime`.
  3. Pada bagian *Project JDKs*, pastikan versi Java yang terpilih mengarah ke JDK 17 atau JDK 21 yang baru diinstall.

---

### 5. Browser menampilkan: *"This site can't be reached / ERR_CONNECTION_REFUSED"*
* **Penyebab:** Aplikasi Spring Boot di VS Code belum dijalankan atau masih dalam proses kompilasi awal.
* **Solusi:**
  1. Pastikan di terminal VS Code sudah muncul pesan:  
     `PrediStock Backend is Running on port 8080!`
  2. Pastikan alamat URL yang diketik di browser sudah benar:  
     `http://localhost:8080/` (bukan https, melainkan http).
  3. Jangan menutup terminal atau menekan `Stop` di VS Code selama Anda menggunakan web dashboard.

---

### 6. Di Mana File Database SQLite Disimpan?
* Database disimpan secara otomatis di folder profil pengguna komputer Anda:  
  `C:\Users\<Nama_User_Komputer>\.predistock\predistock.db`
* *Mengapa diletakkan di sana?*  
  Lokasi ini dipilih secara khusus agar kebal dari fitur proteksi ransomware Windows (*Controlled Folder Access*) yang kerap memblokir aplikasi Java jika membuat file database langsung di dalam folder *Documents*.

---

### 7. Data Berantakan Setelah Diuji Coba & Ingin Mereset ke Kondisi Awal?
* **Solusi Sangat Mudah:**
  1. Login sebagai **Owner** (`owner` / `admin123`).
  2. Di bilah navigasi atas (kanan atas), klik tombol berwarna oranye bertuliskan **"Reset Data"**.
  3. Klik **OK** pada dialog konfirmasi.
  4. Database SQLite akan seketika di-reset bersih dan diisi ulang kembali dengan data uji coba 14 hari Toko Zaini Sejahtera lengkap!

---

##  Selanjutnya Apa?

Setelah aplikasi berhasil berjalan di browser Anda:
* Pelajari pembagian modul Anda (Modul 1 s.d. Modul 5), peran Anda, serta kisi-kisi jawaban pertanyaan Dosen dengan membaca:  
   **[PANDUAN_MODUL_DAN_SIDANG.md](file:///c:/Users/zaini/Documents/Tugas_kuliah_Telkom_University/PBO/Tubes/PANDUAN_MODUL_DAN_SIDANG.md)**

*Selamat mencoba dan semoga sukses untuk seluruh tim pengembang PrediStock!* 
