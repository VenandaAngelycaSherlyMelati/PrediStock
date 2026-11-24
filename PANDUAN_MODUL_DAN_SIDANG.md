<p align="center">
  <img src="src/main/resources/static/images/logo.png" alt="PrediStock Official Logo" width="460">
</p>

# 🎓 PANDUAN LENGKAP MODUL, ARSITEKTUR OOP & MATERI SIDANG PBO

> **Project: PrediStock — Point of Sale & Inventory Forecasting System for MSMEs**  
> **Mata Kuliah: Pemrograman Berorientasi Objek (PBO) — Kode: CDK3BAB4**  
> **Telkom University**  
> *Dokumen ini disusun secara terperinci agar **seluruh anggota kelompok** dapat memahami alur program, letak source code modul masing-masing, konsep OOP yang diterapkan, serta siap mendemokan dan menjawab pertanyaan Dosen Penguji saat sidang/presentasi.*

---

> 💻 **BUTUH PANDUAN CARA MENJALANKAN APLIKASI DI LAPTOP?**  
> Silakan buka dokumen: 👉 **[README.md](file:///c:/Users/zaini/Documents/Tugas_kuliah_Telkom_University/PBO/Tubes/README.md)**

---

## 👥 Tim Pengembang & Pembagian Modul Resmi

| No | Nama Anggota | Peran / Modul | Tanggung Jawab Kode & Konsep OOP Utama |
|:---:|---|---|---|
| 1 | **Ruqayyah** | **Modul 1: Akun & Toko (PIC)** | Role-Based Access Control (`User`, `Owner`, `Cashier`), Generic Repository (`Repository<T>`), Hashing SHA-256 (`PasswordUtil`), SQLite Raw JDBC (`DatabaseConnection`). |
| 2 | **Venanda** | **Modul 2: Produk & Persediaan (PIC)** | Hierarki Produk (`Product`, `FoodProduct`, `GoodsProduct`), Validasi Stok (`reduceStock()`), Custom Exception (`InsufficientStockException`), Polimorfisme Info Simpan. |
| 3 | **Ida** | **Modul 3: Transaksi Penjualan (PIC)** | Multi-item Cart (`Transaction`, `TransactionItem`), Polimorfisme Diskon (`Discount`, `PercentDiscount`, `NominalDiscount`), ACID Transactions (`setAutoCommit(false)`), Dynamic Receipt Header. |
| 4 | **Windi** | **Modul 4: Peramalan Stok (PIC)** | Logika Matematika Manual (*No ML Lib*), Rolling Moving Average, Regresi Linear Tren OLS, Evaluasi MAPE, Polimorfisme Forecaster Loop, Safety Stock (+20%). |
| 5 | **Tubagus Zaini** | **Modul 5: Laporan & Dashboard (PIC)** | Agregasi Penjualan Harian/Bulanan (`SalesSummary`), Peringkat Produk Terlaris (Sorted Map), Polimorfisme Ekspor (`Exportable`, `CsvExporter`, `TextReportExporter`), Integrasi Visual Chart.js. |

---

## 🎨 Identitas Visual & Desain Resmi (Brand Guidelines)

Aplikasi **PrediStock** menggunakan identitas visual terpadu:
1. **Logo Resmi:**
   * **Simbol/Ikon:** Troli belanja berwarna *Midnight Navy* dengan diagram batang persediaan dan panah tren pertumbuhan melengkung ke atas berwarna *Vibrant Orange*, mencerminkan sistem kasir retail pintar dengan kapabilitas prediksi stok masa depan.
   * **Tipografi Nama:** Kata `"Predi"` menggunakan warna *Deep Midnight Navy* (`#072450`), dan `"Stock"` menggunakan warna *Vibrant Retail Orange* (`#ff7713`) dengan huruf `"o"` berbentuk kotak paket 3D (*isometric box*). Slogan kalimat panjang telah dihilangkan agar logo tampil bersih (*clean & modern*).
   * **Aset Terpasang:** 
     * Full Logo: `src/main/resources/static/images/logo.png`
     * Dark/Navbar Mode: `src/main/resources/static/images/logo-light.png`
     * Icon & Favicon: `src/main/resources/static/images/logo-icon.png` dan `favicon.png`
2. **Palet Warna Utama (Color Palette):**
   * **Primary Midnight Navy (`#072450`):** Digunakan untuk bilah navigasi atas (*top navbar*), latar belakang halaman login, header tabel, dan kartu mode Owner.
   * **Vibrant Retail Orange (`#ff7713`):** Digunakan untuk tombol utama (*Call to Action* seperti Proses Bayar, Masuk Dashboard, Hitung Prediksi), tab navigasi aktif, total harga belanja, dan kurva regresi tren penjualan.
   * **Emerald Green (`#10b981`):** Digunakan untuk status kasir bertugas, tombol restok barang masuk, dan uang kembalian.
3. **Tipografi (Typography):**
   * Menggunakan Google Font **Plus Jakarta Sans**, jenis huruf *geometric sans-serif* modern dengan kurva membulat yang selaras secara geometris dengan bentuk logotype PrediStock.

---

## 🔐 MATRIKS HAK AKSES SISTEM (RBAC) OWNER VS KASIR

> **Catatan Penting untuk Presentasi:**  
> Tabel ini merangkum wewenang antara **OWNER** dan **KASIR** di seluruh modul aplikasi:

| No | Modul / Fitur Aplikasi | Kode Fitur | Owner (Pemilik Toko) | Kasir (Operator POS) | Alasan Bisnis & Teknis OOP |
|:---:|---|:---:|:---:|:---:|---|
| 1 | **Transaksi Kasir & Hitung Kembalian** | C-01 s.d C-03 | ✅ Boleh | ✅ **FOKUS UTAMA** | Inti operasional pelayanan transaksi pelanggan di toko. |
| 2 | **Cetak Struk Transaksi Resmi** | C-04 | ✅ Boleh | ✅ **Eksklusif Shift** | Mencantumkan nama kasir & nomor shift aktif (`Kasir: Ruqayyah (Shift 1)`). |
| 3 | **Panel Sesi Kasir & Shift Aktif** | A-01 | ✅ Mode Admin | ✅ **Eksklusif Kasir** | Menjamin akuntabilitas uang fisik di laci kasir per giliran kerja. |
| 4 | **Lihat Katalog Produk & Stok Rak (Read)** | B-01 | ✅ Boleh | ✅ Boleh | Kasir perlu mengecek ketersediaan barang dan harga saat melayani pembeli. |
| 5 | **Restok Barang Masuk Gudang** | B-03 | ✅ Boleh | ✅ **Boleh (Keduanya)** | Kasir di UMKM bertugas menerima & mencatat barang datang dari pemasok saat shift. |
| 6 | **Tambah Produk Baru ke Katalog (Create)** | B-02 | ✅ **Eksklusif Owner** | ❌ **DIBLOKIR** | Hanya Owner yang berhak mendaftarkan item baru ke database toko. |
| 7 | **Edit Data Produk (Update)** | B-02 | ✅ **Penuh (Semua Field)** | ⚠️ **Terbatas (Harga Dikunci 🔒)** | Kasir boleh koreksi nama/merek/info simpan, tetapi **dilarang mengubah harga jual toko**. |
| 8 | **Hapus Produk dari Katalog (Delete)** | B-02 | ✅ **Eksklusif Owner** | ❌ **DISEMBUNYIKAN** | Menghapus aset katalog produk adalah wewenang mutlak pemilik toko. |
| 9 | **Manajemen Akun Kasir (Tambah, Edit, Hapus)** | **A-03** | ✅ **Eksklusif Owner** | ❌ **TERKUNCI 🔒** | Hanya pemilik yang berhak merekrut, mengatur giliran shift, atau menonaktifkan kasir. |
| 10 | **Pengaturan Profil Toko (Nama & Alamat Struk)** | **A-04** | ✅ **Eksklusif Owner** | ❌ **TERKUNCI 🔒** | Identitas legal dan alamat toko yang tercetak di kepala struk hanya ditentukan oleh Owner. |
| 11 | **Peramalan Stok AI (Forecasting)** | D-01 s.d D-05 | ✅ **Eksklusif Owner** | ❌ **TERKUNCI 🔒** | Keputusan pengadaan 7–30 hari ke depan adalah wewenang strategis pemilik. |
| 12 | **Laporan Keuangan & Omzet Penjualan** | E-01 s.d E-04 | ✅ **Eksklusif Owner** | ❌ **TERKUNCI 🔒** | Melindungi kerahasiaan data omzet dan mencegah kecurangan kasir. |
| 13 | **Ekspor Laporan (CSV & TXT)** | E-05 | ✅ **Eksklusif Owner** | ❌ **TERKUNCI 🔒** | Data rekapitulasi penjualan hanya untuk pembukuan dan perpajakan Owner. |
| 14 | **Reset Database & Data Uji Coba** | - | ✅ **Eksklusif Owner** | ❌ **DISEMBUNYIKAN** | Menghapus atau me-reset database berisiko fatal jika diakses kasir. |

---

## 📌 BEDAH DETAIL MODUL 1: AKUN & TOKO (PIC: RUQAYYAH)

### 1. Deskripsi Modul & Daftar Fitur
Modul 1 bertanggung jawab atas sistem autentikasi, pembagian wewenang hak akses peran (*Role-Based Access Control*), enkripsi kata sandi, manajemen data akun kasir, serta identitas profil toko yang tercetak di kepala struk kasir.
* **Fitur A-01 (Login Akun):** Memvalidasi kredensial pengguna dan mengembalikan identitas objek peran polimorfik.
* **Fitur A-02 (Logout Sesi):** Menghapus sesi aktif dan mengembalikan tampilan ke halaman login murni.
* **Fitur A-03 (Manajemen Akun Kasir):** Owner dapat menambah akun kasir baru, merotasi jadwal shift kerja, dan menghapus akun kasir.
* **Fitur A-04 (Profil Toko):** Menyimpan nama toko dan alamat legal usaha yang otomatis tercetak di kepala struk belanja (*receipt header*).

### 2. Berkas Source Code yang Menjadi Milik Ruqayyah:
1. `src/main/java/com/predistock/model/User.java` (Abstract Class)
2. `src/main/java/com/predistock/model/Owner.java` (Subclass User)
3. `src/main/java/com/predistock/model/Cashier.java` (Subclass User)
4. `src/main/java/com/predistock/model/Store.java` (Model Profil Toko)
5. `src/main/java/com/predistock/repository/Repository.java` (Generic Interface)
6. `src/main/java/com/predistock/repository/UserRepository.java` (Implementasi Generic Repository via Raw JDBC)
7. `src/main/java/com/predistock/repository/DatabaseConnection.java` (Koneksi SQLite & Inisialisasi Tabel)
8. `src/main/java/com/predistock/util/PasswordUtil.java` (Enkripsi SHA-256)
9. `src/main/java/com/predistock/service/AuthService.java` (Logika Bisnis Akun)
10. `src/main/java/com/predistock/controller/AuthController.java` (Endpoint REST API)

### 3. Konsep PBO yang Harus Dijelaskan Ruqayyah ke Dosen:
* **Inheritance & Polymorphism:**
  `User` adalah *abstract class* induk. Diturunkan menjadi kelas konkret `Owner` dan `Cashier`. Method-method otorisasi didefinisikan secara abstrak di kelas induk:
  ```java
  public abstract boolean canAddProduct();
  public abstract boolean canDeleteProduct();
  public abstract boolean canRestock();
  public abstract boolean canEditPrice();
  public abstract boolean canViewReports();
  ```
  Di kelas `Owner`, semua method mengembalikan `true`. Di kelas `Cashier`, `canRestock()` mengembalikan `true`, sedangkan `canAddProduct()`, `canDeleteProduct()`, `canEditPrice()`, dan `canViewReports()` mengembalikan `false`.
* **Encapsulation:**
  Atribut `passwordHash` bertipe `private` dan tidak pernah diekspos melalui *getter*. Pengecekan password dilakukan via method `checkPassword(rawPassword)` yang memanggil `PasswordUtil.verify()`. Nomor shift kerja (`shiftNumber`) hanya dienkapsulasi pada kelas `Cashier`.
* **Generics:**
  Interface generik `Repository<T>` menyediakan kontrak CRUD standar (`findById`, `findAll`, `save`, `update`, `delete`) yang kemudian diimplementasikan oleh `UserRepository implements Repository<User>`.
* **Raw JDBC murni:**
  Tidak menggunakan JPA/Hibernate. Semua operasi SQL menggunakan `PreparedStatement` untuk mencegah *SQL Injection*.

### 4. Skenario Demo untuk Ruqayyah:
1. **Demo Fitur A-04 (Profil Toko):**
   * Login sebagai `owner`. Klik tab **"Toko & Kasir"**.
   * Ubah Nama Toko menjadi `TOKO BERKAH JAYA` dan Alamat Toko menjadi `Jl. Telekomunikasi No. 5, Bandung`.
   * Tunjukkan **Live Preview Kepala Struk** di sampingnya langsung berubah seketika. Klik **"Simpan Profil Toko"**.
   * Pindah ke tab Kasir (POS), buat transaksi kilat. Buktikan di struk tercetak `TOKO BERKAH JAYA` secara rata tengah.
2. **Demo Fitur A-03 (Kelola Kasir):**
   * Di tab **"Toko & Kasir"**, klik **"+ Tambah Kasir Baru"**.
   * Masukkan username `kasir3`, nama `Siti Rahma`, password `kasir123`, Shift `Shift 3 (Malam)`. Klik simpan.
   * Tunjukkan kasir bertambah di tabel. Uji tombol **Edit** untuk memindahkan ke Shift 1, lalu uji tombol **Hapus**.
3. **Demo Proteksi Kasir:**
   * Logout, lalu login sebagai `kasir1` (Ruqayyah).
   * Coba klik tab **"Toko & Kasir"**. Tunjukkan muncul peringatan edukatif penolakan akses khusus Owner.

---

## 📌 BEDAH DETAIL MODUL 2: PRODUK & PERSEDIAAN (PIC: VENANDA)

### 1. Deskripsi Modul & Daftar Fitur
Modul 2 bertanggung jawab atas hierarki katalog produk toko, penanganan perbedaan sifat barang (makanan kedaluwarsa vs barang tahan lama), manajemen persediaan fisik (stok), serta peringatan dini otomatis.
* **Fitur B-01 (Katalog Produk):** Menampilkan daftar stok barang di toko dengan pemisahan status visual.
* **Fitur B-02 (Tambah & Edit Produk):** Owner dapat menambah dan mengubah data produk lengkap; Kasir hanya boleh mengubah nama/deskripsi produk (harga jual toko dikunci).
* **Fitur B-03 (Restok Barang Masuk):** Kasir dan Owner dapat mencatat penambahan stok barang fisik yang baru datang dari pemasok.
* **Fitur B-04 (Sistem Peringatan Stok & Kedaluwarsa):** Mendeteksi barang dengan stok $\le \text{minStock}$ dan makanan dengan tanggal kedaluwarsa $\le 3\text{ hari}$.
* **Fitur B-05 (Info Simpan Polimorfik):** Menampilkan informasi penyimpanan khusus suhu dingin/kering tanpa merusak kerapian tabel (*collapsible dropdown*).

### 2. Berkas Source Code yang Menjadi Milik Venanda:
1. `src/main/java/com/predistock/model/Product.java` (Abstract Class)
2. `src/main/java/com/predistock/model/FoodProduct.java` (Subclass Product)
3. `src/main/java/com/predistock/model/GoodsProduct.java` (Subclass Product)
4. `src/main/java/com/predistock/exception/InsufficientStockException.java` (Custom Exception)
5. `src/main/java/com/predistock/exception/ProductNotFoundException.java` (Custom Exception)
6. `src/main/java/com/predistock/repository/ProductRepository.java` (Implementasi Repository via Raw JDBC)
7. `src/main/java/com/predistock/service/ProductService.java` (Logika Stok & Validasi)
8. `src/main/java/com/predistock/controller/ProductController.java` (Endpoint REST API)

### 3. Konsep PBO yang Harus Dijelaskan Venanda ke Dosen:
* **Inheritance:**
  `Product` adalah kelas induk abstrak dengan atribut dasar `id`, `name`, `price`, `stock`, `minStock`. Diturunkan menjadi dua kelas anak:
  * `FoodProduct`: Memiliki atribut unik `LocalDate expiredDate` dan method pengecekan `isNearExpired(int daysThreshold)`.
  * `GoodsProduct`: Memiliki atribut unik `String brand`.
* **Polymorphism pada Method `getStorageInfo()`:**
  Pada kelas `Product`, didefinisikan *abstract method*:
  ```java
  public abstract String getStorageInfo();
  ```
  * Pada `FoodProduct`, menghasilkan teks:  
    `"Produk makanan/minuman berkedaluwarsa. Simpan pada suhu sejuk/kering (15-25°C) atau lemari pendingin. Jauhkan dari sinar matahari langsung."`
  * Pada `GoodsProduct`, menghasilkan teks:  
    `"Barang tahan lama merek [Brand]. Simpan pada rak standar gudang, ventilasi udara normal, dan hindari kelembapan berlebih."`
* **Custom Exception & Encapsulation pada `reduceStock()`:**
  Pengurangan stok fisik dienkapsulasi di dalam method `reduceStock(int quantity)`:
  ```java
  public void reduceStock(int quantity) {
      if (quantity <= 0) throw new IllegalArgumentException("Jumlah harus > 0");
      if (this.stock < quantity) {
          throw new InsufficientStockException("Stok tidak mencukupi untuk produk: " + this.name);
      }
      this.stock -= quantity;
  }
  ```

### 4. Skenario Demo untuk Venanda:
1. **Demo Hierarki Produk & Polimorfisme:**
   * Buka tab **"Produk & Stok"**.
   * Perhatikan produk **Roti Tawar Kismis** (kategori `FOOD`). Terdapat badge oranye berkedip `⏳ Segera Expired`. Klik dropdown `ℹ️ Info Petunjuk Simpan` untuk melihat instruksi penyimpanan suhu dingin.
   * Bandingkan dengan **Sabun Mandi Cair** (kategori `GOODS`) yang menampilkan nama merek (*Brand*) dan instruksi rak gudang.
2. **Demo Polimorfisme Form Tambah Produk:**
   * Klik **"+ Tambah Produk Baru"**.
   * Saat memilih kategori **Makanan**, form otomatis memunculkan input *Tanggal Kedaluwarsa*.
   * Saat diganti ke **Barang**, form secara dinamis berganti meminta input *Merek (Brand)*.
3. **Demo Restok & Granular Edit:**
   * Klik tombol **"+ Restok"** pada salah satu produk untuk menambah 20 unit stok.
   * Uji tombol **"Edit"**: Tunjukkan bahwa saat login sebagai kasir, input harga jual terkunci abu-abu (`🔒 Terkunci`), sedangkan saat login sebagai Owner, harga dapat diedit bebas.

---

## 📌 BEDAH DETAIL MODUL 3: TRANSAKSI PENJUALAN (PIC: IDA)

### 1. Deskripsi Modul & Daftar Fitur
Modul 3 bertanggung jawab atas operasional mesin kasir (*Point of Sale*), keranjang belanja multi-item, kalkulasi strategi diskon polimorfik, jaminan atomisitas transaksi keuangan (ACID), serta pencetakan struk transaksi belanja resmi.
* **Fitur C-01 (Keranjang Multi-Item):** Menampung pembelian lebih dari satu jenis barang dengan kuantitas berbeda.
* **Fitur C-02 (Pilihan Diskon Polimorfik):** Menerapkan strategi potongan harga diskon persen atau diskon nominal.
* **Fitur C-03 (Kalkulasi Total & Kembalian):** Menghitung subtotal, diskon, total tagihan, dan uang kembalian pelanggan secara presisi.
* **Fitur C-04 (Cetak Struk Resmi):** Mencetak struk belanja kasir dengan format kepala struk dari toko, nama kasir shift aktif, rincian produk, dan nominal kembalian.
* **Fitur C-05 (Pengurangan Stok Otomatis):** Memotong stok persediaan secara atomik di database SQLite.

### 2. Berkas Source Code yang Menjadi Milik Ida:
1. `src/main/java/com/predistock/model/Transaction.java` (Model Transaksi Kasir)
2. `src/main/java/com/predistock/model/TransactionItem.java` (Model Rincian Item Keranjang)
3. `src/main/java/com/predistock/model/Discount.java` (Interface Strategi Diskon)
4. `src/main/java/com/predistock/model/PercentDiscount.java` (Implementasi Diskon Persen)
5. `src/main/java/com/predistock/model/NominalDiscount.java` (Implementasi Diskon Rupiah)
6. `src/main/java/com/predistock/repository/TransactionRepository.java` (JDBC Transaksi Keuangan)
7. `src/main/java/com/predistock/service/TransactionService.java` (Logika Pembayaran & ACID)
8. `src/main/java/com/predistock/controller/TransactionController.java` (Endpoint REST API)

### 3. Konsep PBO yang Harus Dijelaskan Ida ke Dosen:
* **Interface & Strategy Pattern (Polymorphism Diskon):**
  Interface `Discount` mendefinisikan kontrak:
  ```java
  public interface Discount {
      double apply(double subtotal);
      String getDescription();
  }
  ```
  * `PercentDiscount`: Mengurangi subtotal sebesar $P\%$ (`subtotal * (1 - percentage / 100)`).
  * `NominalDiscount`: Mengurangi subtotal sebesar nominal tetap rupiah (`Math.max(0, subtotal - amount)`).
  Objek `Transaction` tidak perlu tahu rumus internal diskon, cukup memanggil `discount.apply(subtotal)`.
* **Composition & Aggregation:**
  * `Transaction` memiliki relasi **Komposisi** terhadap `TransactionItem` (daftar item belanja hidup dan mati bersama transaksi).
  * `Transaction` memiliki relasi **Agregasi** terhadap `User` (kasir yang bertugas) dan `Product`.
* **ACID Transactions (Atomisitas Transaksi):**
  Untuk memastikan stok barang tidak terpotong jika penyimpanan struk gagal (atau sebaliknya), transaksi dibungkus raw JDBC transaction manual:
  ```java
  conn.setAutoCommit(false); // Memulai blok transaksi ACID
  try {
      // 1. Simpan header transaksi ke tabel 'transactions'
      // 2. Loop simpan tiap item ke 'transaction_items'
      // 3. Update potong stok fisik di tabel 'products'
      conn.commit(); // Jika semua sukses, simpan permanen
  } catch (Exception e) {
      conn.rollback(); // Jika ada error di tengah jalan, batalkan semua perubahan!
      throw e;
  } finally {
      conn.setAutoCommit(true);
  }
  ```

### 4. Skenario Demo untuk Ida:
1. **Demo POS & Diskon:**
   * Buka tab **"Kasir (POS)"**.
   * Tambahkan 2 produk ke keranjang (misal: *Mie Instan Goreng* 3 unit dan *Teh Botol 350ml* 2 unit).
   * Pada dropdown diskon, pilih **Diskon Persen (%)** masukkan `10`. Tunjukkan total tagihan berkurang 10%.
   * Ganti ke **Diskon Nominal (Rp)** masukkan `5000`. Tunjukkan polimorfisme diskon memotong total sebesar Rp 5.000.
2. **Demo Checkout & Struk:**
   * Masukkan uang tunai yang dibayarkan pelanggan (misal: Rp 50.000). Kembalian otomatis terhitung.
   * Klik tombol **"Proses Pembayaran & Cetak Struk"**.
   * Di struk yang muncul, tunjukkan ke Dosen:
     * Nama dan alamat toko tercetak rapi di bagian atas.
     * Baris `Kasir: Ruqayyah (Shift 1)` tercetak sesuai akun kasir yang sedang aktif.
     * Stok barang di toko otomatis berkurang seketika.

---

## 📌 BEDAH DETAIL MODUL 4: PERAMALAN STOK (PIC: WINDI)

### 1. Deskripsi Modul & Daftar Fitur
Modul 4 bertanggung jawab atas kecerdasan peramalan kebutuhan stok barang (*Inventory Forecasting*) untuk 7 hingga 30 hari ke depan.
* **Fitur D-01 (Ambil Historis 14 Hari):** Membaca data deret waktu (*time-series*) penjualan harian dari database SQLite.
* **Fitur D-02 (Rolling Moving Average):** Menghitung rata-rata bergerak 7 hari terakhir.
* **Fitur D-03 (Regresi Linear Tren OLS):** Menghitung kemiringan tren penjualan menggunakan metode kuadrat terkecil (*Ordinary Least Squares*).
* **Fitur D-04 (Evaluasi Akurasi MAPE):** Menghitung tingkat kesalahan *Mean Absolute Percentage Error* secara manual untuk memilih algoritma dengan akurasi terbaik.
* **Fitur D-05 (Rekomendasi Restok & Safety Stock):** Menghitung jumlah barang yang harus dipesan ke supplier lengkap dengan penyangga keselamatan stok 20%.

### 2. Berkas Source Code yang Menjadi Milik Windi:
1. `src/main/java/com/predistock/forecasting/Forecaster.java` (Abstract Class)
2. `src/main/java/com/predistock/forecasting/MovingAverageForecaster.java` (Algoritma 1)
3. `src/main/java/com/predistock/forecasting/LinearTrendForecaster.java` (Algoritma 2)
4. `src/main/java/com/predistock/forecasting/ForecastResult.java` (DTO Hasil Prediksi)
5. `src/main/java/com/predistock/forecasting/DailyForecast.java` (DTO Prediksi Harian)
6. `src/main/java/com/predistock/util/MathUtil.java` (Kalkulasi Statistik Manual)
7. `src/main/java/com/predistock/service/ForecastService.java` (Komparasi & Safety Stock)
8. `src/main/java/com/predistock/controller/ForecastController.java` (Endpoint REST API)

### 3. Konsep PBO & Matematika yang Harus Dijelaskan Windi ke Dosen:
* **Strict NO Machine Learning Libraries:**
  Tidak ada library Weka atau Apache Commons Math. Semua rumus dihitung manual dalam Java murni:
  * **Regresi Linear Tren ($y = \alpha + \beta x$):**
    $$\beta = \frac{\sum_{i=1}^{n} (x_i - \bar{x})(y_i - \bar{y})}{\sum_{i=1}^{n} (x_i - \bar{x})^2}, \quad \alpha = \bar{y} - \beta \bar{x}$$
  * **MAPE (Mean Absolute Percentage Error):**
    $$\text{MAPE} = \frac{1}{n} \sum_{i=1}^{n} \left| \frac{y_i - \hat{y}_i}{y_i} \right| \times 100\%$$
* **Abstract Class & Polymorphism Forecaster Loop:**
  `Forecaster` adalah kelas induk abstrak. Pemilihan metode terbaik dilakukan melalui perulangan polimorfik:
  ```java
  List<Forecaster> forecasters = List.of(
      new MovingAverageForecaster(history, 7),
      new LinearTrendForecaster(history)
  );
  Forecaster bestMethod = null;
  double lowestMape = Double.MAX_VALUE;

  for (Forecaster f : forecasters) { // Polymorphic Dispatch
      ForecastResult result = f.forecast(horizonDays);
      if (result.mape() < lowestMape) {
          lowestMape = result.mape();
          bestMethod = f;
      }
  }
  ```
* **Kalkulasi Safety Stock (+20%) & Rekomendasi Restok:**
  $$\text{Safety Stock} = \text{Total Prediksi Kebutuhan} \times 0.20$$
  $$\text{Unit Restok yang Harus Dipesan} = \max(0, (\text{Total Prediksi} + \text{Safety Stock}) - \text{Stok Saat Ini})$$

### 4. Skenario Demo untuk Windi:
1. **Demo Peramalan:**
   * Login sebagai **Owner**. Buka tab **"Peramalan (Forecasting)"**.
   * Pada pilihan produk, pilih **Mie Instan Goreng** (memiliki data tren penjualan meningkat selama 14 hari).
   * Tentukan horizon: `7 Hari`. Klik **"Jalankan Peramalan"**.
2. **Penjelasan Hasil ke Dosen:**
   * Tunjukkan grafik Chart.js: Garis biru tua adalah data historis aktual 14 hari, garis oranye adalah proyeksi tren masa depan.
   * Tunjukkan kartu **Rekomendasi Restok**:
     * Metode terbaik terpilih otomatis berdasarkan nilai MAPE terendah.
     * Tunjukkan angka **Safety Stock (20%)** dan total unit yang harus dipesan ke supplier agar toko tidak mengalami kehabisan barang (*stockout*).

---

## 📌 BEDAH DETAIL MODUL 5: LAPORAN & DASHBOARD (PIC: TUBAGUS ZAINI)

### 1. Deskripsi Modul & Daftar Fitur
Modul 5 bertanggung jawab atas agregasi intelijen bisnis toko, visualisasi tren keuangan harian, analisis produk terlaris (*best-seller ranking*), audit riwayat transaksi per kasir, serta ekspor dokumen laporan keuangan.
* **Fitur E-01 (Ringkasan Keuangan Real-Time):** Menampilkan total omzet uang masuk dan jumlah transaksi lunas.
* **Fitur E-02 (Grafik Pendapatan Visual):** Diagram batang tren omzet harian menggunakan Chart.js.
* **Fitur E-03 (Peringkat Produk Terlaris):** Mengelompokkan barang berdasarkan kuantitas terbanyak yang terjual.
* **Fitur E-04 (Tabel Riwayat Transaksi Interaktif):** Menampilkan seluruh rekam jejak penjualan dengan dropdown rincian item (*collapsible items badge*).
* **Fitur E-05 (Ekspor Dokumen Polimorfik):** Mengunduh berkas laporan dalam format `.csv` atau `.txt` (format tabel akuntansi teks).

### 2. Berkas Source Code yang Menjadi Milik Zaini:
1. `src/main/java/com/predistock/reporting/SalesSummary.java` (Model Agregasi Data Finansial)
2. `src/main/java/com/predistock/reporting/Exportable.java` (Interface Strategi Ekspor)
3. `src/main/java/com/predistock/reporting/CsvExporter.java` (Implementasi Ekspor CSV)
4. `src/main/java/com/predistock/reporting/TextReportExporter.java` (Implementasi Ekspor TXT Struk Akuntansi)
5. `src/main/java/com/predistock/service/ReportService.java` (Logika Agregasi & Sorting Map)
6. `src/main/java/com/predistock/controller/ReportController.java` (Endpoint REST API)
7. `src/main/resources/static/index.html` (Integrasi Frontend Web Tailwind & Chart.js)

### 3. Konsep PBO yang Harus Dijelaskan Zaini ke Dosen:
* **Polymorphism pada Ekspor Dokumen (`Exportable` Interface):**
  Didefinisikan interface kontrak:
  ```java
  public interface Exportable {
      void export(SalesSummary summary, OutputStream out) throws IOException;
      String getContentType();
      String getFileExtension();
  }
  ```
  * `CsvExporter`: Menghasilkan format baris koma (*Comma-Separated Values*) yang siap dibuka di Microsoft Excel.
  * `TextReportExporter`: Menghasilkan format laporan teks terstruktur dengan bingkai karakter ASCII (`===`, `---`, perataan teks) untuk dicetak di printer kasir atau diarsipkan secara fisik.
  Controller cukup memanggil `exporter.export(summary, response.getOutputStream())` secara polimorfik sesuai parameter `format=csv` atau `format=txt`.
* **Collection Framework & Sorting Algorithm (Best-Seller Ranking):**
  Menggunakan `Map<String, Integer>` untuk mengagregasi kuantitas tiap produk dari seluruh transaksi, lalu diurutkan secara menurun (*descending*) menggunakan `Map.Entry.comparingByValue().reversed()`.

### 4. Skenario Demo untuk Zaini:
1. **Demo Dashboard & Visualisasi:**
   * Login sebagai **Owner**. Buka tab **"Laporan & Dashboard"**.
   * Tunjukkan kartu **Total Omzet Penjualan** dan **Total Transaksi Selesai** yang terhitung otomatis.
   * Tunjukkan grafik batang tren pendapatan harian berwarna oranye PrediStock di kanvas Chart.js.
2. **Demo Dropdown Rincian Riwayat Transaksi:**
   * Pada tabel riwayat transaksi, klik tombol badge `📦 2 Produk (12 item) ▼`.
   * Tunjukkan kepada Dosen bagaimana rincian nama produk, kuantitas per item, dan subtotalnya terbuka rapi tanpa merusak tata letak tabel (*table layout stable*).
3. **Demo Ekspor Laporan Polimorfik:**
   * Klik tombol **"Download CSV"**: Tunjukkan file CSV otomatis terunduh dengan data tabel rapi.
   * Klik tombol **"Download Teks"**: Tunjukkan file teks struk akuntansi terunduh dengan garis pembatas rapi.

---

## 🎯 KISI-KISI TANYA JAWAB DOSEN PENGUJI (CHEAT SHEET SIDANG)

Berikut adalah pertanyaan yang paling sering ditanyakan Dosen Penguji PBO beserta kunci jawabannya:

> **Q1: "Mengapa sistem ini tidak menggunakan JPA/Hibernate?"**  
> **A:** *"Sesuai batasan tugas besar PBO Telkom University, kami diwajibkan memahami mekanisme dasar manipulasi objek Java ke basis data secara murni (*Raw JDBC*). Dengan menulis `PreparedStatement` manual dan pola Repository generik, kami membuktikan penguasaan siklus hidup koneksi, penanganan `SQLException`, pencegahan SQL Injection, serta pengelolaan transaksi atomik ACID secara nyata tanpa bantuan *magic* dari framework ORM."*

> **Q2: "Di mana letak penerapan prinsip Polymorphism yang paling menonjol?"**  
> **A:** *"Kami menerapkan polimorfisme pada 4 tempat kunci:*  
> *1. **Otorisasi Pengguna:** `User.canManageProducts()` dan `User.canEditPrice()` yang memiliki respons berbeda antara objek `Owner` dan `Cashier`.*  
> *2. **Hierarki Produk:** Method `Product.getStorageInfo()` yang menghasilkan instruksi berbeda pada `FoodProduct` dan `GoodsProduct`.*  
> *3. **Strategi Diskon Kasir:** Interface `Discount.apply()` yang diimplementasikan oleh `PercentDiscount` dan `NominalDiscount`.*  
> *4. **Ekspor Laporan:** Interface `Exportable.export()` yang dieksekusi secara polimorfik oleh `CsvExporter` dan `TextReportExporter`."*

> **Q3: "Bagaimana cara kalian menjamin keamanan data transaksi jika terjadi error di tengah penyimpanan?"**  
> **A:** *"Pada `TransactionRepository`, kami menerapkan transaksi atomik ACID dengan menonaktifkan autocommit (`conn.setAutoCommit(false)`). Penyimpanan nomor transaksi, penyimpanan rincian item, dan pemotongan stok barang fisik berada dalam satu blok `try-catch`. Jika salah satu gagal, sistem mengeksekusi `conn.rollback()` sehingga integritas data stok toko tidak akan pernah korup."*

> **Q4: "Mengapa peramalan stok tidak menggunakan library machine learning seperti Weka atau Python?"**  
> **A:** *"Tujuan utama tugas besar PBO adalah mengimplementasikan logika algoritma berorientasi objek mandiri. Kami merancang kelas abstrak `Forecaster` dan menghitung rumus statistik Ordinary Least Squares (OLS) serta evaluasi MAPE menggunakan Java murni (`MathUtil`), membuktikan bahwa pemodelan matematika dapat dibangun bersih menggunakan paradigma OOP tanpa dependensi library eksternal."*

---
*Semoga sukses untuk seluruh anggota tim: Ruqayyah, Venanda, Ida, Windi, dan Tubagus Zaini dalam presentasi & sidang Tugas Besar PBO PrediStock!* 🎓🏆✨
