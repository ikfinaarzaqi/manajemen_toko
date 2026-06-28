import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * CLASS MENU - Halaman utama setelah login
 * Menampilkan daftar produk dengan CRUD (Create, Read, Update, Delete)
 * Juga menampilkan profil user di sidebar
 */
public class menu extends javax.swing.JFrame {

    // ===== VARIABEL DATABASE =====
    private Connection con;          // Koneksi ke database
    private Statement st;            // Untuk menjalankan query SQL
    private PreparedStatement pst;   // Untuk query dengan parameter
    private ResultSet rs;            // Untuk menyimpan hasil query
    private DefaultTableModel model; // Model untuk tabel di GUI
    private String username;         // Username yang login

    // ===== VARIABEL GUI =====
    private javax.swing.JButton btn_add, btn_clear, btn_delete, btn_exit, btn_kategori, btn_logout, btn_update;
    private javax.swing.JComboBox<String> jc_kategori;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabel_produk;
    private javax.swing.JLabel label_foto, user, lbl_ubah_foto;
    private javax.swing.JTextField txt_deskripsi, txt_harga, txt_id, txt_nama, txt_stok;

    /**
     * KONSTRUKTOR - Dipanggil saat objek menu dibuat
     * Inisialisasi komponen GUI, koneksi database, dan load data
     */
    public menu() {
        initComponents();                    // Membuat dan mengatur semua komponen GUI
        this.setLocationRelativeTo(null);     // Posisi window di tengah layar
        this.setTitle("Welcome - Main Menu");

        con = MyConnection.getConnection();   // Membuka koneksi database
        username = login.txt;                 // Ambil username dari halaman login

        // Load semua data
        loadData();           // Load data produk ke tabel
        loadImage(username);  // Load foto profil user
        showUserInfo(username); // Tampilkan nama user
        loadComboBox();       // Load data kategori ke combobox
        autonumber();         // Generate ID produk otomatis
        txt_id.setEnabled(false); // ID produk tidak bisa diedit manual
        kosongkan_form();     // Reset form input

        setupFotoProfil();    // Setup listener untuk upload foto
    }

    // ================================================================
    // ===== BAGIAN 1: MANAJEMEN FOTO PROFIL =====
    // ================================================================

    /**
     * SETUP FOTO PROFIL - Membuat label foto bisa diklik untuk upload
     */
    private void setupFotoProfil() {
        // Event klik pada foto
        label_foto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label_foto.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                uploadFoto();
            }
        });

        // Event klik pada label "Tambah/Ganti Foto"
        lbl_ubah_foto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbl_ubah_foto.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                uploadFoto();
            }
        });
    }

    /**
     * UPLOAD FOTO - Membuka dialog untuk memilih dan mengupload foto profil
     */
    private void uploadFoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih Foto Profil");
        // Filter hanya file gambar
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fotoPath = selectedFile.getAbsolutePath();

            // Validasi ukuran file maksimal 2MB
            if (selectedFile.length() > 2 * 1024 * 1024) {
                JOptionPane.showMessageDialog(this,
                        "Ukuran file terlalu besar! Maksimal 2MB.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update path foto di database
            try {
                if (con == null) con = MyConnection.getConnection();
                pst = con.prepareStatement("UPDATE register SET foto = ? WHERE username = ?");
                pst.setString(1, fotoPath);
                pst.setString(2, username);
                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this,
                            "✅ Foto profil berhasil diupdate!",
                            "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadImage(username); // Refresh tampilan foto
                }
                pst.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error database: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * LOAD IMAGE - Memuat foto profil dari database
     * Jika ada foto, tampilkan; jika tidak, tampilkan default
     */
    private void loadImage(String username) {
        try {
            if (con == null) con = MyConnection.getConnection();
            pst = con.prepareStatement("SELECT foto FROM register WHERE username = ?");
            pst.setString(1, username);
            rs = pst.executeQuery();
            if (rs.next()) {
                String fotoPath = rs.getString("foto");
                if (fotoPath != null && !fotoPath.isEmpty()) {
                    File file = new File(fotoPath);
                    if (file.exists()) {
                        // Resize gambar agar sesuai dengan label (80x80)
                        ImageIcon icon = new ImageIcon(fotoPath);
                        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                        label_foto.setIcon(new ImageIcon(img));
                        label_foto.setText("");
                        label_foto.setHorizontalAlignment(SwingConstants.CENTER);
                        lbl_ubah_foto.setText("Ganti Foto");
                        lbl_ubah_foto.setForeground(new Color(100, 200, 100));
                        return;
                    }
                }
            }
            rs.close();
            pst.close();
        } catch (SQLException e) {
            System.out.println("Error load image: " + e.getMessage());
        }
        setDefaultPhoto(); // Jika tidak ada foto, tampilkan default
    }

    /**
     * SET DEFAULT PHOTO - Tampilan default jika belum ada foto
     */
    private void setDefaultPhoto() {
        label_foto.setIcon(null);
        label_foto.setText("");
        label_foto.setFont(new Font("Segoe UI", Font.PLAIN, 35));
        label_foto.setForeground(Color.WHITE);
        label_foto.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_ubah_foto.setText("Tambah Foto");
        lbl_ubah_foto.setForeground(new Color(100, 150, 255));
    }

    // ================================================================
    // ===== BAGIAN 2: MANAJEMEN FORM PRODUK =====
    // ================================================================

    /**
     * KOSONGKAN FORM - Mereset semua field input ke keadaan kosong
     */
    private void kosongkan_form() {
        txt_nama.setText("");
        txt_harga.setText("");
        txt_stok.setText("");
        txt_deskripsi.setText("");
        txt_id.setText("");
        jc_kategori.setSelectedIndex(0);
        txt_id.requestFocus();
        autonumber(); // Generate ID baru
    }

    /**
     * AUTONUMBER - Generate ID produk otomatis dengan format P001, P002, dst.
     */
    private void autonumber() {
        try {
            if (con == null) con = MyConnection.getConnection();
            st = con.createStatement();
            rs = st.executeQuery("SELECT id_produk FROM produk ORDER BY id_produk DESC");
            if (rs.next()) {
                String lastId = rs.getString("id_produk");
                if (lastId != null && lastId.startsWith("P")) {
                    int number = Integer.parseInt(lastId.substring(1)) + 1;
                    txt_id.setText("P" + String.format("%03d", number));
                } else {
                    txt_id.setText("P001");
                }
            } else {
                txt_id.setText("P001");
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            System.out.println("Autonumber error: " + e.getMessage());
            txt_id.setText("P001");
        }
    }

    // ================================================================
    // ===== BAGIAN 3: LOAD DATA DARI DATABASE =====
    // ================================================================

    /**
     * LOAD DATA - Mengambil semua data produk dari database dan menampilkan di tabel
     * Menggunakan INNER JOIN untuk mengambil nama kategori dari tabel kategori
     */
    public void loadData() {
        try {
            if (con == null) con = MyConnection.getConnection();
            // Query SQL dengan JOIN untuk mendapatkan nama kategori
            String sql = "SELECT produk.id_produk, produk.nama_produk, produk.harga, produk.stok, produk.deskripsi, kategori.nama_kategori "
                    + "FROM produk "
                    + "INNER JOIN kategori ON produk.id_kategori = kategori.id_kategori "
                    + "ORDER BY produk.id_produk ASC";
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();

            // Membuat model tabel dengan 6 kolom
            model = new DefaultTableModel();
            model.addColumn("ID Produk");
            model.addColumn("Nama Produk");
            model.addColumn("Harga");
            model.addColumn("Stok");
            model.addColumn("Deskripsi");
            model.addColumn("Kategori");

            // Mengisi data ke model tabel
            while (rs.next()) {
                Object[] row = new Object[6];
                row[0] = rs.getString("id_produk");
                row[1] = rs.getString("nama_produk");
                row[2] = rs.getInt("harga");
                row[3] = rs.getInt("stok");
                row[4] = rs.getString("deskripsi");
                row[5] = rs.getString("nama_kategori");
                model.addRow(row);
            }

            // Set model ke tabel dan atur lebar kolom
            tabel_produk.setModel(model);
            tabel_produk.getColumnModel().getColumn(0).setPreferredWidth(80);
            tabel_produk.getColumnModel().getColumn(1).setPreferredWidth(200);
            tabel_produk.getColumnModel().getColumn(2).setPreferredWidth(100);
            tabel_produk.getColumnModel().getColumn(3).setPreferredWidth(80);
            tabel_produk.getColumnModel().getColumn(4).setPreferredWidth(200);
            tabel_produk.getColumnModel().getColumn(5).setPreferredWidth(100);

            rs.close();
            pst.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat data!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * LOAD COMBOBOX - Mengisi combobox kategori dengan data dari database
     */
    private void loadComboBox() {
        try {
            if (con == null) con = MyConnection.getConnection();
            jc_kategori.removeAllItems();
            rs = con.createStatement().executeQuery("SELECT nama_kategori FROM kategori ORDER BY nama_kategori ASC");
            while (rs.next()) {
                jc_kategori.addItem(rs.getString("nama_kategori"));
            }
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat kategori!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * SHOW USER INFO - Menampilkan nama user di sidebar
     */
    private void showUserInfo(String username) {
        try {
            if (con == null) con = MyConnection.getConnection();
            pst = con.prepareStatement("SELECT name FROM register WHERE username = ?");
            pst.setString(1, username);
            rs = pst.executeQuery();
            if (rs.next()) {
                user.setText(rs.getString("name"));
            }
            rs.close();
            pst.close();
        } catch (SQLException e) {
            user.setText("Admin");
        }
    }

    // ================================================================
    // ===== BAGIAN 4: HELPER DATABASE =====
    // ================================================================

    /**
     * GET ID KATEGORI - Mencari id_kategori berdasarkan nama kategori
     * @param namaKategori Nama kategori yang dicari
     * @return id_kategori dalam bentuk String
     */
    private String getIdKategori(String namaKategori) {
        try {
            if (con == null) con = MyConnection.getConnection();
            pst = con.prepareStatement("SELECT id_kategori FROM kategori WHERE nama_kategori = ?");
            pst.setString(1, namaKategori);
            rs = pst.executeQuery();
            if (rs.next()) return rs.getString("id_kategori");
            rs.close();
            pst.close();
        } catch (SQLException e) {}
        return "";
    }

    /**
     * IS ID EXISTS - Mengecek apakah ID produk sudah terdaftar di database
     * @param id ID produk yang dicek
     * @return true jika sudah ada, false jika belum
     */
    private boolean isIdExists(String id) {
        try {
            if (con == null) con = MyConnection.getConnection();
            pst = con.prepareStatement("SELECT id_produk FROM produk WHERE id_produk = ?");
            pst.setString(1, id);
            rs = pst.executeQuery();
            if (rs.next()) return true;
            rs.close();
            pst.close();
        } catch (SQLException e) {}
        return false;
    }

    // ================================================================
    // ===== BAGIAN 5: EVENT CRUD (CREATE, READ, UPDATE, DELETE) =====
    // ================================================================

    /**
     * BTN ADD - Menambahkan produk baru ke database
     * Validasi: semua field harus diisi, harga dan stok harus angka
     */
    private void btn_addMouseClicked(java.awt.event.MouseEvent evt) {
        String id = txt_id.getText().trim();
        String nama = txt_nama.getText().trim();
        String harga = txt_harga.getText().trim();
        String stok = txt_stok.getText().trim();
        String deskripsi = txt_deskripsi.getText().trim();
        String kategori = jc_kategori.getSelectedItem().toString();

        // Validasi: semua field harus diisi
        if (nama.isEmpty() || harga.isEmpty() || stok.isEmpty() || deskripsi.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validasi: harga dan stok harus angka
        try {
            Integer.parseInt(harga);
            Integer.parseInt(stok);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Harga dan Stok harus angka!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validasi: ID tidak boleh duplikat
        if (isIdExists(id)) {
            JOptionPane.showMessageDialog(null, "ID Produk sudah terdaftar!", "Error", JOptionPane.ERROR_MESSAGE);
            autonumber();
            return;
        }

        // Dapatkan id_kategori dari nama kategori yang dipilih
        String idKategori = getIdKategori(kategori);
        if (idKategori.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Kategori tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Insert ke database
        try {
            if (con == null) con = MyConnection.getConnection();
            pst = con.prepareStatement("INSERT INTO produk VALUES (?, ?, ?, ?, ?, ?)");
            pst.setString(1, id);
            pst.setString(2, nama);
            pst.setInt(3, Integer.parseInt(harga));
            pst.setInt(4, Integer.parseInt(stok));
            pst.setString(5, deskripsi);
            pst.setString(6, idKategori);
            if (pst.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "Data produk berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadData();      // Refresh tabel
                kosongkan_form(); // Reset form
            }
            pst.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * BTN UPDATE - Mengupdate data produk yang sudah dipilih
     * Data diambil dari form yang sudah terisi (klik tabel)
     */
    private void btn_updateMouseClicked(java.awt.event.MouseEvent evt) {
        String id = txt_id.getText().trim();
        String nama = txt_nama.getText().trim();
        String harga = txt_harga.getText().trim();
        String stok = txt_stok.getText().trim();
        String deskripsi = txt_deskripsi.getText().trim();
        String kategori = jc_kategori.getSelectedItem().toString();

        // Validasi: harus pilih data dulu
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih data yang akan diupdate!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validasi: semua field harus diisi
        if (nama.isEmpty() || harga.isEmpty() || stok.isEmpty() || deskripsi.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validasi: harga dan stok harus angka
        try {
            Integer.parseInt(harga);
            Integer.parseInt(stok);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Harga dan Stok harus angka!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idKategori = getIdKategori(kategori);
        if (idKategori.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Kategori tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update database
        try {
            if (con == null) con = MyConnection.getConnection();
            pst = con.prepareStatement("UPDATE produk SET nama_produk = ?, harga = ?, stok = ?, deskripsi = ?, id_kategori = ? WHERE id_produk = ?");
            pst.setString(1, nama);
            pst.setInt(2, Integer.parseInt(harga));
            pst.setInt(3, Integer.parseInt(stok));
            pst.setString(4, deskripsi);
            pst.setString(5, idKategori);
            pst.setString(6, id);
            if (pst.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "Data produk berhasil diupdate!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                loadData();
                kosongkan_form();
            } else {
                JOptionPane.showMessageDialog(null, "Data tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            pst.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * BTN DELETE - Menghapus produk dari database
     * Ada konfirmasi sebelum menghapus
     */
    private void btn_deleteMouseClicked(java.awt.event.MouseEvent evt) {
        String id = txt_id.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih data yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Konfirmasi hapus
        if (JOptionPane.showConfirmDialog(null, "Yakin hapus produk ID: " + id + "?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (con == null) con = MyConnection.getConnection();
                pst = con.prepareStatement("DELETE FROM produk WHERE id_produk = ?");
                pst.setString(1, id);
                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(null, "Data berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                    kosongkan_form();
                }
                pst.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * BTN CLEAR - Membersihkan form input
     */
    private void btn_clearMouseClicked(java.awt.event.MouseEvent evt) {
        kosongkan_form();
    }

    // ================================================================
    // ===== BAGIAN 6: NAVIGASI =====
    // ================================================================

    /**
     * BTN KATEGORI - Pindah ke halaman manajemen kategori
     */
    private void btn_kategoriMouseClicked(java.awt.event.MouseEvent evt) {
        this.dispose();        // Tutup halaman ini
        new kategori().setVisible(true); // Buka halaman kategori
    }

    /**
     * BTN LOGOUT - Keluar dari aplikasi ke halaman login
     */
    private void btn_logoutMouseClicked(java.awt.event.MouseEvent evt) {
        if (JOptionPane.showConfirmDialog(null, "Yakin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            this.dispose();
            new login().setVisible(true);
        }
    }

    /**
     * BTN EXIT - Keluar dari aplikasi
     */
    private void btn_exitMouseClicked(java.awt.event.MouseEvent evt) {
        if (JOptionPane.showConfirmDialog(null, "Yakin keluar?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            System.exit(0);
    }

    // ================================================================
    // ===== BAGIAN 7: INTERAKSI TABEL =====
    // ================================================================

    /**
     * TABEL PRODUK MOUSE CLICKED - Saat klik baris di tabel
     * Data akan otomatis masuk ke form untuk diedit/dihapus
     */
    private void tabel_produkMouseClicked(java.awt.event.MouseEvent evt) {
        int row = tabel_produk.getSelectedRow();
        if (row >= 0) {
            String id = tabel_produk.getValueAt(row, 0).toString();
            try {
                if (con == null) con = MyConnection.getConnection();
                pst = con.prepareStatement("SELECT * FROM produk WHERE id_produk = ?");
                pst.setString(1, id);
                rs = pst.executeQuery();
                if (rs.next()) {
                    // Isi form dengan data dari tabel
                    txt_id.setText(rs.getString("id_produk"));
                    txt_nama.setText(rs.getString("nama_produk"));
                    txt_harga.setText(String.valueOf(rs.getInt("harga")));
                    txt_stok.setText(String.valueOf(rs.getInt("stok")));
                    txt_deskripsi.setText(rs.getString("deskripsi"));

                    // Set combobox sesuai dengan kategori produk
                    String idKategori = rs.getString("id_kategori");
                    ResultSet rs2 = con.createStatement().executeQuery("SELECT nama_kategori FROM kategori WHERE id_kategori = '" + idKategori + "'");
                    if (rs2.next()) {
                        jc_kategori.setSelectedItem(rs2.getString("nama_kategori"));
                    }
                    rs2.close();
                }
                rs.close();
                pst.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ================================================================
    // ===== BAGIAN 8: MAIN METHOD =====
    // ================================================================

    /**
     * MAIN METHOD - Entry point program
     * Menjalankan GUI menu di Event Dispatch Thread
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new menu().setVisible(true));
    }

    // ================================================================
    // ===== BAGIAN 9: INISIALISASI KOMPONEN GUI =====
    // ================================================================

    /**
     * INIT COMPONENTS - Membuat dan mengatur semua komponen GUI
     * Ini adalah bagian terbesar yang membuat tampilan aplikasi
     */
    private void initComponents() {
        // ===== SETUP FRAME =====
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);           // Hilangkan title bar default
        setSize(1100, 750);              // Ukuran window
        setMinimumSize(new Dimension(800, 600)); // Ukuran minimal
        setLocationRelativeTo(null);

        // ===== TITLE BAR KUSTOM =====
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(20, 20, 50));
        titleBar.setPreferredSize(new Dimension(getWidth(), 40));

        // Label judul di kiri
        JLabel titleLabel = new JLabel("Manajemen Toko");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 0));
        titleBar.add(titleLabel, BorderLayout.WEST);

        // Tombol kontrol di kanan (Minimize, Maximize, Close)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        rightPanel.setBackground(new Color(20, 20, 50));

        // Tombol Minimize
        JButton minButton = new JButton("─");
        minButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        minButton.setForeground(Color.WHITE);
        minButton.setBackground(new Color(60, 60, 120));
        minButton.setFocusPainted(false);
        minButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        minButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        minButton.addActionListener(e -> setState(JFrame.ICONIFIED));
        rightPanel.add(minButton);

        // Tombol Maximize/Restore
        JButton maxButton = new JButton("□");
        maxButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        maxButton.setForeground(Color.WHITE);
        maxButton.setBackground(new Color(60, 60, 120));
        maxButton.setFocusPainted(false);
        maxButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        maxButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        maxButton.addActionListener(e -> {
            if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                setExtendedState(JFrame.NORMAL);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });
        rightPanel.add(maxButton);

        // Tombol Close
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(200, 50, 50));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> System.exit(0));
        rightPanel.add(closeButton);

        titleBar.add(rightPanel, BorderLayout.EAST);

        // ===== MAIN PANEL =====
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(15, 15, 35));

        // ============================================================
        // ===== SIDEBAR (KIRI) =====
        // ============================================================
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(25, 25, 55));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setMinimumSize(new Dimension(150, 0));

        // ---- HEADER SIDEBAR: "Navigasi" ----
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 55));
        headerPanel.setMaximumSize(new Dimension(200, 45));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JLabel navLabel = new JLabel("Navigasi");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        navLabel.setForeground(Color.WHITE);
        navLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(navLabel, BorderLayout.CENTER);

        sidebar.add(headerPanel);

        // ---- SEPARATOR (Garis Pemisah) ----
        sidebar.add(createSeparator());

        // ---- FOTO PROFIL ----
        JPanel fotoPanel = new JPanel(new GridBagLayout());
        fotoPanel.setBackground(new Color(25, 25, 55));
        fotoPanel.setMaximumSize(new Dimension(200, 160));
        fotoPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        GridBagConstraints fc = new GridBagConstraints();
        fc.insets = new Insets(2, 0, 2, 0);
        fc.gridx = 0;

        // Label foto (tempat menampilkan gambar)
        label_foto = new JLabel();
        label_foto.setPreferredSize(new Dimension(70, 70));
        label_foto.setMinimumSize(new Dimension(70, 70));
        label_foto.setMaximumSize(new Dimension(70, 70));
        label_foto.setOpaque(true);
        label_foto.setBackground(new Color(40, 40, 80));
        label_foto.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 200), 2));
        label_foto.setHorizontalAlignment(SwingConstants.CENTER);
        label_foto.setVerticalAlignment(SwingConstants.CENTER);
        label_foto.setText("");
        label_foto.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        label_foto.setForeground(Color.WHITE);
        label_foto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fc.gridy = 0;
        fotoPanel.add(label_foto, fc);

        // Label "Tambah/Ganti Foto"
        lbl_ubah_foto = new JLabel("Tambah Foto");
        lbl_ubah_foto.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lbl_ubah_foto.setForeground(new Color(100, 150, 255));
        lbl_ubah_foto.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_ubah_foto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        fc.gridy = 1;
        fotoPanel.add(lbl_ubah_foto, fc);

        // Label nama user
        user = new JLabel("Admin");
        user.setFont(new Font("Segoe UI", Font.BOLD, 12));
        user.setForeground(Color.WHITE);
        user.setHorizontalAlignment(SwingConstants.CENTER);
        fc.gridy = 2;
        fotoPanel.add(user, fc);

        sidebar.add(fotoPanel);

        // ---- SEPARATOR ----
        sidebar.add(createSeparator());

        // ---- MENU ITEMS ----
        String[][] menuItems = {
                {"Produk", "produk"},
                {"Kategori", "kategori"},
                {"", "", "spacer"},
                {"Logout", "logout"},
                {"Exit", "exit"}
        };

        for (int i = 0; i < menuItems.length; i++) {
            if (menuItems[i][0].isEmpty()) {
                sidebar.add(Box.createRigidArea(new Dimension(0, 3)));
                continue;
            }

            // Panel untuk setiap menu
            JPanel menuPanel = new JPanel(new BorderLayout());
            menuPanel.setBackground(new Color(25, 25, 55));
            menuPanel.setMaximumSize(new Dimension(200, 38));
            menuPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            menuPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

            // Panel kiri (icon + teks)
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            leftPanel.setBackground(new Color(25, 25, 55));

            JLabel textLabel = new JLabel(menuItems[i][0]);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textLabel.setForeground(Color.WHITE);

            leftPanel.add(textLabel);
            menuPanel.add(leftPanel, BorderLayout.WEST);

            // Arrow di kanan
            JLabel arrowLabel = new JLabel("›");
            arrowLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            arrowLabel.setForeground(new Color(60, 60, 120));
            menuPanel.add(arrowLabel, BorderLayout.EAST);

            // Hover effect
            menuPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    menuPanel.setBackground(new Color(45, 45, 85));
                    leftPanel.setBackground(new Color(45, 45, 85));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    menuPanel.setBackground(new Color(25, 25, 55));
                    leftPanel.setBackground(new Color(25, 25, 55));
                }
            });

            // Action untuk setiap menu
            String menuName = menuItems[i][1];
            if (menuName.equals("kategori")) {
                menuPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        btn_kategoriMouseClicked(null);
                    }
                });
            } else if (menuName.equals("logout")) {
                menuPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        btn_logoutMouseClicked(null);
                    }
                });
            } else if (menuName.equals("exit")) {
                menuPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        btn_exitMouseClicked(null);
                    }
                });
            }

            sidebar.add(menuPanel);
        }

        // Spacer di bagian bawah sidebar (mendorong menu ke atas)
        sidebar.add(Box.createVerticalGlue());

        // ============================================================
        // ===== CONTENT PANEL (KANAN) =====
        // ============================================================
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(10, 10, 30));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Wrapper untuk content (agar bisa melebar)
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(new Color(10, 10, 30));
        GridBagConstraints cc = new GridBagConstraints();
        cc.fill = GridBagConstraints.BOTH;
        cc.weightx = 1.0;
        cc.weighty = 1.0;

        // ===== FORM PANEL =====
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(10, 10, 30));
        GridBagConstraints fc2 = new GridBagConstraints();
        fc2.insets = new Insets(3, 5, 3, 5);
        fc2.fill = GridBagConstraints.HORIZONTAL;
        fc2.weightx = 1.0;

        // ---- HEADER ----
        JLabel headerTitle = new JLabel("Daftar Data Produk");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitle.setForeground(Color.WHITE);
        fc2.gridx = 0;
        fc2.gridy = 0;
        fc2.gridwidth = 4;
        fc2.anchor = GridBagConstraints.WEST;
        formPanel.add(headerTitle, fc2);
        fc2.anchor = GridBagConstraints.CENTER;
        fc2.gridwidth = 1;

        // ---- FORM FIELDS ----
        String[][] fields = {
                {"ID Produk:", "txt_id"},
                {"Nama Produk:", "txt_nama"},
                {"Harga:", "txt_harga"},
                {"Stok:", "txt_stok"},
                {"Deskripsi:", "txt_deskripsi"},
                {"Kategori:", "jc_kategori"}
        };

        int y = 1;
        for (int i = 0; i < fields.length; i++) {
            // Label
            fc2.gridx = 0;
            fc2.gridy = y + i;
            JLabel lbl = new JLabel(fields[i][0]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(new Color(200, 200, 230));
            formPanel.add(lbl, fc2);

            // Field input
            fc2.gridx = 1;
            fc2.gridy = y + i;
            fc2.gridwidth = 3;
            if (i == 0) {
                txt_id = new JTextField(12);
                txt_id.setEnabled(false);
                formPanel.add(textField(txt_id, 12), fc2);
            } else if (i == 1) {
                txt_nama = new JTextField(18);
                formPanel.add(textField(txt_nama, 18), fc2);
            } else if (i == 2) {
                txt_harga = new JTextField(12);
                formPanel.add(textField(txt_harga, 12), fc2);
            } else if (i == 3) {
                txt_stok = new JTextField(12);
                formPanel.add(textField(txt_stok, 12), fc2);
            } else if (i == 4) {
                txt_deskripsi = new JTextField(18);
                formPanel.add(textField(txt_deskripsi, 18), fc2);
            } else if (i == 5) {
                jc_kategori = new JComboBox<>();
                jc_kategori.setBackground(Color.WHITE);
                jc_kategori.setForeground(Color.BLACK);
                jc_kategori.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                jc_kategori.setPreferredSize(new Dimension(200, 30));
                formPanel.add(jc_kategori, fc2);
            }
            fc2.gridwidth = 1;
        }

        // ---- BUTTONS ----
        y = 1 + fields.length;
        fc2.gridx = 0;
        fc2.gridy = y;
        fc2.gridwidth = 4;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        btnPanel.setBackground(new Color(10, 10, 30));

        btn_add = button("TAMBAH", new Color(46, 204, 113));
        btn_update = button("UPDATE", new Color(52, 152, 219));
        btn_delete = button("HAPUS", new Color(231, 76, 60));
        btn_clear = button("CLEAR", new Color(241, 196, 15));

        btnPanel.add(btn_add);
        btnPanel.add(btn_update);
        btnPanel.add(btn_delete);
        btnPanel.add(btn_clear);

        formPanel.add(btnPanel, fc2);

        // ---- TABLE ----
        y++;
        fc2.gridx = 0;
        fc2.gridy = y;
        fc2.gridwidth = 4;
        fc2.fill = GridBagConstraints.BOTH;
        fc2.weightx = 1.0;
        fc2.weighty = 1.0;
        fc2.insets = new Insets(5, 0, 0, 0);

        tabel_produk = new JTable();
        tabel_produk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabel_produk.setRowHeight(26);
        tabel_produk.setBackground(new Color(25, 25, 55));
        tabel_produk.setForeground(Color.WHITE);
        tabel_produk.setGridColor(new Color(60, 60, 120));
        tabel_produk.setSelectionBackground(new Color(70, 70, 150));
        tabel_produk.setSelectionForeground(Color.WHITE);
        tabel_produk.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabel_produk.getTableHeader().setBackground(new Color(40, 40, 80));
        tabel_produk.getTableHeader().setForeground(Color.WHITE);
        tabel_produk.getTableHeader().setReorderingAllowed(false);

        jScrollPane1 = new JScrollPane(tabel_produk);
        jScrollPane1.setBackground(new Color(10, 10, 30));
        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 120), 1));

        formPanel.add(jScrollPane1, fc2);

        // Gabungkan semua ke wrapper
        wrapperPanel.add(formPanel, cc);
        contentPanel.add(wrapperPanel, BorderLayout.CENTER);

        // ============================================================
        // ===== GABUNGKAN SIDEBAR + CONTENT =====
        // ============================================================
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // ============================================================
        // ===== ROOT PANEL =====
        // ============================================================
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(15, 15, 35));
        rootPanel.add(titleBar, BorderLayout.NORTH);
        rootPanel.add(mainPanel, BorderLayout.CENTER);

        // ============================================================
        // ===== EVENT LISTENER =====
        // ============================================================
        btn_add.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { btn_addMouseClicked(evt); }
        });
        btn_update.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { btn_updateMouseClicked(evt); }
        });
        btn_delete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { btn_deleteMouseClicked(evt); }
        });
        btn_clear.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { btn_clearMouseClicked(evt); }
        });

        tabel_produk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { tabel_produkMouseClicked(evt); }
        });

        // ============================================================
        // ===== TAMPILKAN FRAME =====
        // ============================================================
        setContentPane(rootPanel);
        setVisible(true);
    }

    // ================================================================
    // ===== BAGIAN 10: HELPER METHODS =====
    // ================================================================

    /**
     * CREATE SEPARATOR - Membuat garis pemisah di sidebar
     */
    private JPanel createSeparator() {
        JPanel sepPanel = new JPanel(new BorderLayout());
        sepPanel.setBackground(new Color(25, 25, 55));
        sepPanel.setMaximumSize(new Dimension(200, 8));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 120));
        sep.setBackground(new Color(60, 60, 120));
        sepPanel.add(sep, BorderLayout.CENTER);

        return sepPanel;
    }

    /**
     * TEXT FIELD - Membuat text field dengan style yang konsisten
     */
    private JTextField textField(JTextField tf, int cols) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setPreferredSize(new Dimension(200, 30));
        return tf;
    }

    /**
     * BUTTON - Membuat button dengan style yang konsisten
     */
    private JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 32));
        return btn;
    }
}