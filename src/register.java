import java.sql.*;//untuk mengkoneksikan dengan database
import java.io.File;//untuk membaca dan menulis file
import java.io.IOException;//untuk menangani error ketika operasi file
import java.nio.file.Files;//untuk operasi copy file
import java.nio.file.StandardCopyOption;//untuk opsi copy file
import javax.swing.*;//komponen gui
import java.awt.*;//untuk layout dan warna

public class register extends javax.swing.JFrame {

    private Connection con;//koneksi ke database
    private String filename;//menyimpan path file foto yang dipilih user
    private PreparedStatement pst;//untuk menjalankan query sql
    private ResultSet rs;//menyimpan hasil query sql

    public register() {
        initComponents();//untuk memanggil method yang membuat semua komponen GUI
        this.setLocationRelativeTo(null);//untuk memastikan
        autonumber();//untuk membuat id number otomatis
        txt_id.setEnabled(false);//membuat id yang sudah tidak bisa diedit
        txt_id.setEditable(false);//id tidak bisa diedit lagi
        label_foto.setHorizontalAlignment(SwingConstants.CENTER);
        label_foto.setText("");
        label_foto.setFont(new Font("Segoe UI", Font.PLAIN, 30));

        btn_submit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_submitMouseClicked(evt);
            }
        });//registrasi user untuk menyimpan ke database
        btn_cancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_cancelMouseClicked(evt);
            }
        });//mengkosongkan semua field form
        btn_pilih.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_pilihMouseClicked(evt);
            }
        });//untuk membuka dialog pilih foto
        jLabel13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel13MouseClicked(evt);
            }
        });//untuk berpindah ke halaman login
    }

    private void kosongkan_form() {
        txt_username.setText("");
        txt_nama.setText("");
        txt_password.setText("");
        txt_conpassword.setText("");
        txt_filename.setText("");
        label_foto.setIcon(null);
        label_foto.setText("");
        label_foto.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        txt_username.requestFocus();//pindah kursor ke field username supaya bisa langsung mengetik
    }

    private void autonumber() {
        try {//memulai blok coba kalau error ditangkap di catch
            Connection con = MyConnection.getConnection();//membuka file yang mengkoneksikan ke database
            if (con == null) {
                JOptionPane.showMessageDialog(null, "Koneksi Database Gagal!");
                return;//jika gagal koneksi muncul pesan database tidak konek
            }
            Statement st = con.createStatement();//untuk menjalankan query sql
            ResultSet r = st.executeQuery("SELECT id FROM register ORDER BY id DESC");//mengambil id terakhir dari register diurutkan dari yang terbesar
            if (r.next()) {
                String idLama = r.getString("id");
                if (idLama != null && idLama.startsWith("AU")) {//mengambil id lama
                    int angka = Integer.parseInt(idLama.substring(2)) + 1;//parseint mengubah teks menjadi angka,,menambah 1
                    txt_id.setText("AU" + String.format("%03d", angka));//format nya AU+id
                } else {
                    txt_id.setText("AU001");
                }
            } else {
                txt_id.setText("AU001");
            }
            r.close();//Menutup hasil query
            st.close();//Menutup statement SQL
            con.close();//menutup koneksi database
        } catch (Exception e) {//menangkap semua jenis error
            System.out.println("Autonumber error: " + e.getMessage());//dapatkan pesan error nya
            txt_id.setText("AU001");//kalau error di set default AU001
        }
    }

    private boolean validasiPassword(String password, String conPassword) {//method yang berfungsi untuk memvalidasi password yang dimasukan user
        if (!password.equals(conPassword)) {//jika password tidak sama dengan konfirmasi password
            JOptionPane.showMessageDialog(null, "Password dan Konfirmasi Password tidak sama!", "Error", JOptionPane.ERROR_MESSAGE);
            txt_conpassword.setText("");//kosongkan field konfirmasi
            txt_conpassword.requestFocus();//pindah ke kursor fiels konfirmasi password
            return false;//gagal memvalidasi
        }
        if (password.length() < 4) {//panjang passowrd kurang dari 4
            JOptionPane.showMessageDialog(null, "Password minimal 4 karakter!", "Error", JOptionPane.ERROR_MESSAGE);
            txt_password.setText("");
            txt_conpassword.setText("");//kosongkan field konfirmasi
            txt_password.requestFocus();//pindah ke kursor fiels konfirmasi password
            return false;
        }
        return true;//kalau lolos validasi lanjut ke proses berikutnya
    }

    private void btn_submitMouseClicked(java.awt.event.MouseEvent evt) {
        String id = txt_id.getText().trim();//ambil teks dari field id dan menghapus spasi di awal dan akhir supaya kode tetap rapi
        String username = txt_username.getText().trim();
        String name = txt_nama.getText().trim();
        String password = txt_password.getText().trim();
        String conPassword = txt_conpassword.getText().trim();

        if (username.isEmpty() || name.isEmpty() || password.isEmpty() || conPassword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }//pesan jika field ada yang kosong
        if (!validasiPassword(password, conPassword)) return;//memanggil method validasi jika !(gagal) maka hentikan method

        try {
            Connection con = MyConnection.getConnection();
            PreparedStatement cekPs = con.prepareStatement("SELECT * FROM register WHERE username = ?");//mencari username di database
            cekPs.setString(1, username);//memasukan username ke query
            ResultSet cekRs = cekPs.executeQuery();//jika ada hasil berati username sudah terdaftar
            if (cekRs.next()) {
                JOptionPane.showMessageDialog(null, "Username sudah terdaftar!", "Error", JOptionPane.ERROR_MESSAGE);
                txt_username.setText("");
                txt_username.requestFocus();
                cekRs.close();
                cekPs.close();
                con.close();
                return;
            }
            cekRs.close();
            cekPs.close();
            con.close();
        } catch (Exception e) {
            System.out.println("Error cek username: " + e.getMessage());
        }

        String fotoPath = "";//untuk menyimpan lokasi foto setelah di upload memang nilai awal nya dibuat kosong
        if (filename != null && !filename.isEmpty()) {//proses upload
            try {
                String ext = filename.substring(filename.lastIndexOf(".") + 1);
                String newFileName = username + "." + ext;
                String uploadPath = "src/upload/";//folder tujuan upload foto
                File dir = new File(uploadPath);
                if (!dir.exists()) dir.mkdirs();//jika folder belom ada maka buat folder baru
                File fileAwal = new File(filename);
                File fileAkhir = new File(uploadPath + newFileName);
                Files.copy(fileAwal.toPath(), fileAkhir.toPath(), StandardCopyOption.REPLACE_EXISTING);//untuk menyalin file dari fila asal ke file tujuan jika sudah ada maka ditimpa/diganti
                fotoPath = uploadPath + newFileName;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Gagal upload foto!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            Connection con = MyConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("INSERT INTO register VALUES (?, ?, ?, ?, ?)");
            pst.setString(1, id);
            pst.setString(2, username);
            pst.setString(3, name);
            pst.setString(4, password);
            pst.setString(5, fotoPath);
            if (pst.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "Registrasi Berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                kosongkan_form();
                autonumber();
                this.dispose();
                new login().setVisible(true);
            }
            pst.close();
            con.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_cancelMouseClicked(java.awt.event.MouseEvent evt) {//jika file di cancel
        kosongkan_form();
    }

    private void btn_pilihMouseClicked(java.awt.event.MouseEvent evt) {//jika tombol pilih di klik
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle("Pilih Foto");
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            filename = f.getAbsolutePath();
            txt_filename.setText(filename);
            try {
                ImageIcon icon = new ImageIcon(filename);
                Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                label_foto.setIcon(new ImageIcon(img));
                label_foto.setText("");
                label_foto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            } catch (Exception e) {
                label_foto.setIcon(null);
                label_foto.setText("📷");
            }
        }
    }

    private void jLabel13MouseClicked(java.awt.event.MouseEvent evt) {//menampilkan dialog PINDAH KE HALAMAN LOGIN SETELAH DIKONFIRMASI
        if (JOptionPane.showConfirmDialog(null, "Kembali ke Login?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            this.dispose();
            new login().setVisible(true);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new register().setVisible(true));
    }

    // ========== VARIABLES ==========
    private javax.swing.JButton btn_cancel, btn_pilih, btn_submit;
    private javax.swing.JLabel jLabel13, label_foto, txt_filename;
    private javax.swing.JPasswordField txt_conpassword, txt_password;
    private javax.swing.JTextField txt_id, txt_nama, txt_username;

    // ========== KOMPONEN GUI ==========
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);//menghilangkan title bar bawaan
        setSize(600, 620);
        setLocationRelativeTo(null);//posisi window di tengah layar

        // ===== TITLE BAR =====
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(15, 15, 35));//warna biru gelap
        titleBar.setPreferredSize(new Dimension(600, 45));

        JLabel titleLabel = new JLabel("REGISTER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 0));
        titleBar.add(titleLabel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        rightPanel.setBackground(new Color(15, 15, 35));

        JButton closeBtn = new JButton("X");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(new Color(200, 50, 50));
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        rightPanel.add(closeBtn);

        titleBar.add(rightPanel, BorderLayout.EAST);

        // ===== MAIN PANEL =====
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(10, 10, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);

        // Header
        JLabel header = new JLabel("Register akun ADMIN toko");
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(new Color(180, 180, 220));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        mainPanel.add(header, c);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 120));
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        mainPanel.add(sep, c);
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;

        c.gridwidth = 1;

        // ===== ID =====
        c.gridx = 0; c.gridy = 2;
        mainPanel.add(label("ID:"), c);
        txt_id = new JTextField(15);
        txt_id.setEnabled(false);
        txt_id.setBackground(new Color(30, 30, 60));
        txt_id.setForeground(Color.WHITE);
        c.gridx = 1; c.gridy = 2;
        c.gridwidth = 2;
        mainPanel.add(textField(txt_id), c);
        c.gridwidth = 1;

        // ===== USERNAME =====
        c.gridx = 0; c.gridy = 3;
        mainPanel.add(label("Username:"), c);
        txt_username = new JTextField(15);
        txt_username.setBackground(Color.WHITE);
        txt_username.setForeground(Color.BLACK);
        c.gridx = 1; c.gridy = 3;
        c.gridwidth = 2;
        mainPanel.add(textField(txt_username), c);
        c.gridwidth = 1;

        // ===== NAME =====
        c.gridx = 0; c.gridy = 4;
        mainPanel.add(label("Name:"), c);
        txt_nama = new JTextField(15);
        txt_nama.setBackground(Color.WHITE);
        txt_nama.setForeground(Color.BLACK);
        c.gridx = 1; c.gridy = 4;
        c.gridwidth = 2;
        mainPanel.add(textField(txt_nama), c);
        c.gridwidth = 1;

        // ===== PASSWORD =====
        c.gridx = 0; c.gridy = 5;
        mainPanel.add(label("Password:"), c);
        txt_password = new JPasswordField(15);
        txt_password.setBackground(Color.WHITE);
        txt_password.setForeground(Color.BLACK);
        c.gridx = 1; c.gridy = 5;
        c.gridwidth = 2;
        mainPanel.add(textField(txt_password), c);
        c.gridwidth = 1;

        // ===== CONFIRM PASSWORD =====
        c.gridx = 0; c.gridy = 6;
        mainPanel.add(label("Con.Password:"), c);
        txt_conpassword = new JPasswordField(15);
        txt_conpassword.setBackground(Color.WHITE);
        txt_conpassword.setForeground(Color.BLACK);
        c.gridx = 1; c.gridy = 6;
        c.gridwidth = 2;
        mainPanel.add(textField(txt_conpassword), c);
        c.gridwidth = 1;

        // ===== UPLOAD FOTO =====
        c.gridx = 0; c.gridy = 7;
        mainPanel.add(label("Upload:"), c);
        btn_pilih = new JButton("Pilih Foto");
        btn_pilih.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn_pilih.setBackground(new Color(52, 152, 219));
        btn_pilih.setForeground(Color.WHITE);
        btn_pilih.setFocusPainted(false);
        btn_pilih.setCursor(new Cursor(Cursor.HAND_CURSOR));
        c.gridx = 1; c.gridy = 7;
        c.gridwidth = 1;
        mainPanel.add(btn_pilih, c);

        // ===== FILE NAME & PHOTO =====
        txt_filename = new JLabel("Belum ada foto");
        txt_filename.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txt_filename.setForeground(new Color(180, 180, 220));
        c.gridx = 2; c.gridy = 7;
        c.gridwidth = 1;
        mainPanel.add(txt_filename, c);

        label_foto = new JLabel("");
        label_foto.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        label_foto.setPreferredSize(new Dimension(80, 80));
        label_foto.setBackground(new Color(30, 30, 60));
        label_foto.setOpaque(true);//supaya background terlihat
        label_foto.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 120), 2));
        label_foto.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 1; c.gridy = 8;
        c.gridwidth = 2;
        mainPanel.add(label_foto, c);
        c.gridwidth = 1;

        // ===== SUBMIT & CANCEL =====
        btn_submit = button("Submit", new Color(46, 204, 113));
        btn_cancel = button("Cancel", new Color(231, 76, 60));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        btnPanel.setBackground(new Color(10, 10, 30));
        btnPanel.add(btn_submit);
        btnPanel.add(btn_cancel);

        c.gridx = 0; c.gridy = 9;
        c.gridwidth = 3;
        mainPanel.add(btnPanel, c);

        // ===== LOGIN LINK =====
        jLabel13 = new JLabel("Sudah punya akun? Login di sini");
        jLabel13.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jLabel13.setForeground(new Color(100, 150, 255));
        jLabel13.setCursor(new Cursor(Cursor.HAND_CURSOR));//kursor berubah jadi tangan ketika di hover
        jLabel13.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0; c.gridy = 10;
        c.gridwidth = 3;
        mainPanel.add(jLabel13, c);

        // ===== ROOT PANEL =====
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(15, 15, 35));
        rootPanel.add(titleBar, BorderLayout.NORTH);
        rootPanel.add(mainPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);//menarus semua ke window
        setVisible(true);
    }

    // ===== HELPERS =====
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(new Color(200, 200, 230));
        return l;
    }

    private JTextField textField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(250, 32));
        return tf;
    }

    private JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 38));
        return btn;
    }
}