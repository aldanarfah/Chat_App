# Java WebSocket App

Fondasi awal aplikasi chat Java berbasis Maven. Slice ini sengaja masih sederhana: frontend statis, server HTTP, server WebSocket, dan broadcast pesan real-time antarklien browser.

## Fitur saat ini
- Server HTTP di port `8080`
- Server WebSocket di port `8887`
- Default bind ke `0.0.0.0` agar bisa diakses lewat VPN/LAN
- Frontend statis dari `src/main/resources/public`
- Broadcast pesan ke semua client yang sedang terhubung
- Build, test, dan run lewat Maven

## Menjalankan

Perintah utama:

```bash
mvn exec:java
```

Shortcut lama tetap ada dan sekarang hanya membungkus Maven:

```bash
./run.sh
```

Lalu buka:
- `http://localhost:8080`

Jika ingin diakses orang lain lewat VPN yang sama:
- Jalankan server di komputer yang menjadi host.
- Cari IP host pada interface VPN yang dipakai.
- Minta pengguna lain membuka `http://IP_VPN_HOST:8080`
- Frontend akan otomatis membuat koneksi WebSocket ke host yang sama pada port `8887`.
- Pastikan port `8080` dan `8887` tidak diblokir firewall lokal atau aturan VPN.

Opsional:
- Ubah bind host dengan env `APP_BIND_HOST` bila perlu.
- Contoh: `APP_BIND_HOST=0.0.0.0 mvn exec:java`

## Build dan test

```bash
mvn test
mvn package
```

Catatan:
- Repo ini menyimpan cache Maven lokal di `.m2/repository` lewat `.mvn/maven.config`.
- Run pertama tetap butuh internet untuk mengunduh dependency Maven.

## Cara mencoba
1. Buka aplikasi di dua tab browser.
2. Masukkan nama berbeda di tiap tab.
3. Klik **Connect**.
4. Kirim pesan dari salah satu tab.
5. Pesan akan muncul di semua client yang sedang terhubung.

## Struktur singkat
- `pom.xml`: konfigurasi build Maven
- `src/main/java/app/Application.java`: bootstrap server HTTP dan WebSocket
- `src/main/java/app/http`: static file serving
- `src/main/java/app/websocket`: server WebSocket dan util terkait koneksi
- `src/main/resources/public/index.html`: UI client
- `src/main/resources/public/app.js`: logika WebSocket di browser
- `src/test/java`: test fondasi HTTP dan WebSocket

## Catatan akses jaringan
- Client browser harus bisa menjangkau port HTTP `8080` dan WebSocket `8887` milik host.
- Untuk akses lintas perangkat, gunakan alamat IP VPN/LAN milik host, bukan `localhost`.
