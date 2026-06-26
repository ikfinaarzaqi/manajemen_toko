import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class menu extends javax.swing.JFrame {

    private Connection con;
    private Statement st;
    private PreparedStatement pst;
    private ResultSet rs;
    private DefaultTableModel model;
    private String username;

    public menu() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setTitle("Welcome - Main Menu");

        con = MyConnection.getConnection();
        username = login.txt;

        loadData();
        loadImage(username);
        showUserInfo(username);
        loadComboBox();
        autonumber();
        txt_id.setEnabled(false);
        kosongkan_form();
    }

    private void kosongkan_form() {
        txt_nama.setText("");
        txt_harga.setText("");
        txt_stok.setText("");
        txt_deskripsi.setText("");
        txt_id.setText("");
        jc_kategori.setSelectedIndex(0);
        txt_id.requestFocus();
        autonumber();
    }

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

    public void loadData() {
        try {
            if (con == null) con = MyConnection.getConnection();
            String sql = "SELECT produk.id_produk, produk.nama_produk, produk.harga, produk.stok, produk.deskripsi, kategori.nama_kategori "
                    + "FROM produk "
                    + "INNER JOIN kategori ON produk.id_kategori = kategori.id_kategori "
                    + "ORDER BY produk.id_produk ASC";
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();

            model = new DefaultTableModel();
            model.addColumn("ID Produk");
            model.addColumn("Nama Produk");
            model.addColumn("Harga");
            model.addColumn("Stok");
            model.addColumn("Deskripsi");
            model.addColumn("Kategori");

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

    private void loadComboBox() {
        try {
            if (con == null) con = MyConnection.getConnection();
            jc_kategori.removeAllItems();
            rs = con.createStatement().executeQuery("SELECT nama_kategori FROM kategori ORDER BY nama_kategori ASC");
            while (rs.next()) jc_kategori.addItem(rs.getString("nama_kategori"));
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal memuat kategori!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

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

    private void loadImage(String username) {
        try {
            if (con == null) con = MyConnection.getConnection();
            pst = con.prepareStatement("SELECT foto FROM register WHERE username = ?");
            pst.setString(1, username);
            rs = pst.executeQuery();
            if (rs.next()) {
                String fotoPath = rs.getString("foto");
                if (fotoPath != null && !fotoPath.isEmpty()) {
                    ImageIcon icon = new ImageIcon(fotoPath);
                    Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    label_foto.setIcon(new ImageIcon(img));
                    label_foto.setText("");
                    label_foto.setBorder(null);
                }
            }
            rs.close();
            pst.close();
        } catch (SQLException e) {
            System.out.println("Error load image: " + e.getMessage());
        }
    }

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

    // ========== EVENT ==========
    private void btn_addMouseClicked(java.awt.event.MouseEvent evt) {
        String id = txt_id.getText().trim();
        String nama = txt_nama.getText().trim();
        String harga = txt_harga.getText().trim();
        String stok = txt_stok.getText().trim();
        String deskripsi = txt_deskripsi.getText().trim();
        String kategori = jc_kategori.getSelectedItem().toString();

        if (nama.isEmpty() || harga.isEmpty() || stok.isEmpty() || deskripsi.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try { Integer.parseInt(harga); Integer.parseInt(stok); } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Harga dan Stok harus angka!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (isIdExists(id)) {
            JOptionPane.showMessageDialog(null, "ID Produk sudah terdaftar!", "Error", JOptionPane.ERROR_MESSAGE);
            autonumber();
            return;
        }
        String idKategori = getIdKategori(kategori);
        if (idKategori.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Kategori tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
                loadData();
                kosongkan_form();
            }
            pst.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_updateMouseClicked(java.awt.event.MouseEvent evt) {
        String id = txt_id.getText().trim();
        String nama = txt_nama.getText().trim();
        String harga = txt_harga.getText().trim();
        String stok = txt_stok.getText().trim();
        String deskripsi = txt_deskripsi.getText().trim();
        String kategori = jc_kategori.getSelectedItem().toString();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih data yang akan diupdate!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (nama.isEmpty() || harga.isEmpty() || stok.isEmpty() || deskripsi.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try { Integer.parseInt(harga); Integer.parseInt(stok); } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Harga dan Stok harus angka!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String idKategori = getIdKategori(kategori);
        if (idKategori.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Kategori tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
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

    private void btn_deleteMouseClicked(java.awt.event.MouseEvent evt) {
        String id = txt_id.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih data yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
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

    private void btn_clearMouseClicked(java.awt.event.MouseEvent evt) {
        kosongkan_form();
    }

    private void btn_exitMouseClicked(java.awt.event.MouseEvent evt) {
        if (JOptionPane.showConfirmDialog(null, "Yakin keluar?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            System.exit(0);
    }

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
                    txt_id.setText(rs.getString("id_produk"));
                    txt_nama.setText(rs.getString("nama_produk"));
                    txt_harga.setText(String.valueOf(rs.getInt("harga")));
                    txt_stok.setText(String.valueOf(rs.getInt("stok")));
                    txt_deskripsi.setText(rs.getString("deskripsi"));
                    String idKategori = rs.getString("id_kategori");
                    ResultSet rs2 = con.createStatement().executeQuery("SELECT nama_kategori FROM kategori WHERE id_kategori = '" + idKategori + "'");
                    if (rs2.next()) jc_kategori.setSelectedItem(rs2.getString("nama_kategori"));
                    rs2.close();
                }
                rs.close();
                pst.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void btn_kategoriMouseClicked(java.awt.event.MouseEvent evt) {
        this.dispose();
        new kategori().setVisible(true);
    }

    private void btn_logoutMouseClicked(java.awt.event.MouseEvent evt) {
        if (JOptionPane.showConfirmDialog(null, "Yakin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            this.dispose();
            new login().setVisible(true);
        }
    }

    // ========== MAIN ==========
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new menu().setVisible(true));
    }

    // ========== VARIABLES ==========
    private javax.swing.JButton btn_add, btn_clear, btn_delete, btn_exit, btn_kategori, btn_logout, btn_update;
    private javax.swing.JComboBox<String> jc_kategori;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabel_produk;
    private javax.swing.JLabel label_foto, user;
    private javax.swing.JTextField txt_deskripsi, txt_harga, txt_id, txt_nama, txt_stok;

    // ========== INIT COMPONENTS (MODERN UI + TITLE BAR KUSTOM) ==========
    private void initComponents() {
        // ===== SETUP FRAME =====
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // ===== TITLE BAR KUSTOM =====
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(15, 15, 35));
        titleBar.setPreferredSize(new Dimension(1100, 40));

        JLabel titleLabel = new JLabel("Main Menu - Aplikasi Toko");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 0));
        titleBar.add(titleLabel, BorderLayout.WEST);

        // ===== TOMBOL CLOSE =====
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        rightPanel.setBackground(new Color(15, 15, 35));

        JButton closeButton = new JButton("✕");
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
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(15, 15, 35));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);

        // ===== SIDEBAR =====
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setBackground(new Color(25, 25, 55));
        sidebar.setPreferredSize(new Dimension(200, 750));
        sidebar.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 140), 1));

        GridBagConstraints sc = new GridBagConstraints();
        sc.insets = new Insets(15, 10, 15, 10);
        sc.gridx = 0;

        JLabel logo = new JLabel("");
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 50));
        sc.gridy = 0;
        sidebar.add(logo, sc);

        JLabel title = new JLabel("Main Menu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        sc.gridy = 1;
        sidebar.add(title, sc);

        label_foto = new JLabel();
        label_foto.setPreferredSize(new Dimension(100, 100));
        label_foto.setOpaque(true);
        label_foto.setBackground(new Color(40, 40, 80));
        label_foto.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 200), 2));
        label_foto.setHorizontalAlignment(SwingConstants.CENTER);
        label_foto.setText("");
        label_foto.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        label_foto.setForeground(Color.WHITE);
        sc.gridy = 2;
        sidebar.add(label_foto, sc);

        user = new JLabel("Admin");
        user.setFont(new Font("Segoe UI", Font.BOLD, 14));
        user.setForeground(Color.WHITE);
        sc.gridy = 3;
        sidebar.add(user, sc);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(80, 80, 140));
        sc.gridy = 4;
        sc.fill = GridBagConstraints.HORIZONTAL;
        sc.weightx = 1.0;
        sidebar.add(sep, sc);
        sc.fill = GridBagConstraints.NONE;
        sc.weightx = 0;

        String[] menuItems = {"Produk", "Kategori", "Logout", "Exit"};
        JButton[] sideButtons = new JButton[4];
        for (int i = 0; i < menuItems.length; i++) {
            sideButtons[i] = new JButton(menuItems[i]);
            sideButtons[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            sideButtons[i].setForeground(Color.WHITE);
            sideButtons[i].setBackground(new Color(35, 35, 75));
            sideButtons[i].setFocusPainted(false);
            sideButtons[i].setBorderPainted(false);
            sideButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            sideButtons[i].setPreferredSize(new Dimension(170, 40));
            sc.gridy = 5 + i;
            sidebar.add(sideButtons[i], sc);
        }

        // ===== CONTENT PANEL =====
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(10, 10, 30));
        GridBagConstraints cc = new GridBagConstraints();
        cc.insets = new Insets(8, 8, 8, 8);

        JLabel headerTitle = new JLabel("Daftar Data Produk");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerTitle.setForeground(Color.WHITE);
        cc.gridx = 0;
        cc.gridy = 0;
        cc.gridwidth = 3;
        contentPanel.add(headerTitle, cc);

        cc.gridwidth = 1;

        // ID
        cc.gridx = 0; cc.gridy = 1;
        contentPanel.add(label("ID Produk:"), cc);
        txt_id = new JTextField(12);
        txt_id.setEnabled(false);
        cc.gridx = 1; cc.gridy = 1;
        contentPanel.add(textField(txt_id, 12), cc);

        // Nama
        cc.gridx = 0; cc.gridy = 2;
        contentPanel.add(label("Nama Produk:"), cc);
        txt_nama = new JTextField(20);
        cc.gridx = 1; cc.gridy = 2;
        contentPanel.add(textField(txt_nama, 20), cc);

        // Harga
        cc.gridx = 0; cc.gridy = 3;
        contentPanel.add(label("Harga:"), cc);
        txt_harga = new JTextField(10);
        cc.gridx = 1; cc.gridy = 3;
        contentPanel.add(textField(txt_harga, 10), cc);

        // Stok
        cc.gridx = 0; cc.gridy = 4;
        contentPanel.add(label("Stok:"), cc);
        txt_stok = new JTextField(10);
        cc.gridx = 1; cc.gridy = 4;
        contentPanel.add(textField(txt_stok, 10), cc);

        // Deskripsi
        cc.gridx = 0; cc.gridy = 5;
        contentPanel.add(label("Deskripsi:"), cc);
        txt_deskripsi = new JTextField(20);
        cc.gridx = 1; cc.gridy = 5;
        contentPanel.add(textField(txt_deskripsi, 20), cc);

        // Kategori
        cc.gridx = 0; cc.gridy = 6;
        contentPanel.add(label("Kategori:"), cc);
        jc_kategori = new JComboBox<>();
        jc_kategori.setBackground(Color.WHITE);
        jc_kategori.setForeground(Color.BLACK);
        jc_kategori.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jc_kategori.setPreferredSize(new Dimension(200, 35));
        cc.gridx = 1; cc.gridy = 6;
        contentPanel.add(jc_kategori, cc);

        // BUTTONS
        btn_add = button("TAMBAH", new Color(46, 204, 113));
        btn_update = button("UPDATE", new Color(52, 152, 219));
        btn_delete = button("HAPUS", new Color(231, 76, 60));
        btn_clear = button("CLEAR", new Color(241, 196, 15));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.setBackground(new Color(10, 10, 30));
        btnPanel.add(btn_add);
        btnPanel.add(btn_update);
        btnPanel.add(btn_delete);
        btnPanel.add(btn_clear);

        cc.gridx = 0;
        cc.gridy = 7;
        cc.gridwidth = 3;
        contentPanel.add(btnPanel, cc);

        // TABLE
        tabel_produk = new JTable();
        tabel_produk.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabel_produk.setRowHeight(30);
        tabel_produk.setBackground(new Color(25, 25, 55));
        tabel_produk.setForeground(Color.WHITE);
        tabel_produk.setGridColor(new Color(60, 60, 120));
        tabel_produk.setSelectionBackground(new Color(70, 70, 150));
        tabel_produk.setSelectionForeground(Color.WHITE);
        tabel_produk.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabel_produk.getTableHeader().setBackground(new Color(40, 40, 80));
        tabel_produk.getTableHeader().setForeground(Color.WHITE);
        tabel_produk.getTableHeader().setReorderingAllowed(false);

        jScrollPane1 = new JScrollPane(tabel_produk);
        jScrollPane1.setBackground(new Color(10, 10, 30));
        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 120), 1));
        jScrollPane1.setPreferredSize(new Dimension(800, 250));

        cc.gridx = 0;
        cc.gridy = 8;
        cc.gridwidth = 3;
        cc.fill = GridBagConstraints.BOTH;
        cc.weightx = 1.0;
        cc.weighty = 1.0;
        contentPanel.add(jScrollPane1, cc);

        // ===== SIDEBAR + CONTENT =====
        c.gridx = 0; c.gridy = 0;
        c.weightx = 0.2;
        c.fill = GridBagConstraints.VERTICAL;
        mainPanel.add(sidebar, c);

        c.gridx = 1; c.gridy = 0;
        c.weightx = 0.8;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        mainPanel.add(contentPanel, c);

        // ===== ROOT PANEL (TITLE BAR + MAIN PANEL) =====
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(15, 15, 35));
        rootPanel.add(titleBar, BorderLayout.NORTH);
        rootPanel.add(mainPanel, BorderLayout.CENTER);

        // ===== EVENT LISTENER =====
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

        sideButtons[2].addActionListener(e -> btn_logoutMouseClicked(null));
        sideButtons[3].addActionListener(e -> btn_exitMouseClicked(null));
        sideButtons[1].addActionListener(e -> btn_kategoriMouseClicked(null));

        tabel_produk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { tabel_produkMouseClicked(evt); }
        });

        setContentPane(rootPanel);
        setVisible(true);
    }

    // ===== HELPER =====
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(Color.WHITE);
        return l;
    }

    private JTextField textField(JTextField tf, int cols) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setPreferredSize(new Dimension(200, 35));
        return tf;
    }

    private JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 40));
        return btn;
    }
}