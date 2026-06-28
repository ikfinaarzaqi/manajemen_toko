import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class kategori extends javax.swing.JFrame {

    private Connection con;
    private PreparedStatement pst;
    private ResultSet rs;
    private DefaultTableModel tableModel;

    public kategori() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setTitle("Manajemen Kategori - Toko");
        con = MyConnection.getConnection();
        tampilkanData();
        kosongkan_form();
    }

    private void kosongkan_form() {
        txt_idKategori.setText("");
        txt_namaKategori.setText("");
        txt_deskripsi.setText("");
        txt_idKategori.setEnabled(false);
        txt_idKategori.requestFocus();
    }

    private String generateIdKategori() {
        String newId = "K001";
        try {
            if (con == null) con = MyConnection.getConnection();
            Statement st = con.createStatement();
            rs = st.executeQuery("SELECT id_kategori FROM kategori ORDER BY id_kategori DESC LIMIT 1");
            if (rs.next()) {
                String lastId = rs.getString("id_kategori");
                if (lastId != null && lastId.startsWith("K")) {
                    int number = Integer.parseInt(lastId.substring(1)) + 1;
                    newId = "K" + String.format("%03d", number);
                }
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            System.out.println("Error generate ID: " + e.getMessage());
        }
        return newId;
    }

    private void tampilkanData() {
        try {
            if (con == null) con = MyConnection.getConnection();
            tableModel = new DefaultTableModel();
            tableModel.addColumn("ID Kategori");
            tableModel.addColumn("Nama Kategori");
            tableModel.addColumn("Deskripsi");

            rs = con.createStatement().executeQuery("SELECT * FROM kategori ORDER BY id_kategori ASC");
            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getString("id_kategori");
                row[1] = rs.getString("nama_kategori");
                row[2] = rs.getString("deskripsi");
                tableModel.addRow(row);
            }
            tabel_kategori.setModel(tableModel);
            tabel_kategori.getColumnModel().getColumn(0).setPreferredWidth(80);
            tabel_kategori.getColumnModel().getColumn(1).setPreferredWidth(150);
            tabel_kategori.getColumnModel().getColumn(2).setPreferredWidth(250);
            rs.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Gagal menampilkan data!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_submitMouseClicked(java.awt.event.MouseEvent evt) {
        String idKategori = txt_idKategori.getText().trim();
        String namaKategori = txt_namaKategori.getText().trim();
        String deskripsi = txt_deskripsi.getText().trim();

        if (namaKategori.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nama Kategori harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txt_namaKategori.requestFocus();
            return;
        }

        try {
            if (con == null) con = MyConnection.getConnection();
            if (idKategori.isEmpty()) idKategori = generateIdKategori();

            PreparedStatement cekPs = con.prepareStatement("SELECT id_kategori FROM kategori WHERE id_kategori = ?");
            cekPs.setString(1, idKategori);
            rs = cekPs.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "ID Kategori sudah terdaftar!", "Error", JOptionPane.ERROR_MESSAGE);
                rs.close();
                cekPs.close();
                return;
            }
            rs.close();
            cekPs.close();

            pst = con.prepareStatement("INSERT INTO kategori VALUES (?, ?, ?)");
            pst.setString(1, idKategori);
            pst.setString(2, namaKategori);
            pst.setString(3, deskripsi);
            if (pst.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "Kategori berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                tampilkanData();
                kosongkan_form();
                txt_idKategori.setText(generateIdKategori());
            }
            pst.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_updateMouseClicked(java.awt.event.MouseEvent evt) {
        String idKategori = txt_idKategori.getText().trim();
        String namaKategori = txt_namaKategori.getText().trim();
        String deskripsi = txt_deskripsi.getText().trim();

        if (idKategori.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih data yang akan diupdate!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (namaKategori.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nama Kategori harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txt_namaKategori.requestFocus();
            return;
        }

        try {
            if (con == null) con = MyConnection.getConnection();
            pst = con.prepareStatement("UPDATE kategori SET nama_kategori = ?, deskripsi = ? WHERE id_kategori = ?");
            pst.setString(1, namaKategori);
            pst.setString(2, deskripsi);
            pst.setString(3, idKategori);
            if (pst.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "Kategori berhasil diupdate!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                tampilkanData();
                kosongkan_form();
                txt_idKategori.setText(generateIdKategori());
            } else {
                JOptionPane.showMessageDialog(null, "Data tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            pst.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_deleteMouseClicked(java.awt.event.MouseEvent evt) {
        String idKategori = txt_idKategori.getText().trim();
        if (idKategori.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih data yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(null, "Yakin hapus kategori ini?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (con == null) con = MyConnection.getConnection();
                pst = con.prepareStatement("DELETE FROM kategori WHERE id_kategori = ?");
                pst.setString(1, idKategori);
                if (pst.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(null, "Kategori berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    tampilkanData();
                    kosongkan_form();
                    txt_idKategori.setText(generateIdKategori());
                }
                pst.close();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void btn_cancelMouseClicked(java.awt.event.MouseEvent evt) {
        kosongkan_form();
        txt_idKategori.setText(generateIdKategori());
    }

    private void btn_refreshMouseClicked(java.awt.event.MouseEvent evt) {
        tampilkanData();
        kosongkan_form();
        txt_idKategori.setText(generateIdKategori());
        JOptionPane.showMessageDialog(null, "Data berhasil di-refresh!", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void btn_backMouseClicked(java.awt.event.MouseEvent evt) {
        this.dispose();
        new menu().setVisible(true);
    }

    private void tabel_kategoriMouseClicked(java.awt.event.MouseEvent evt) {
        int selectedRow = tabel_kategori.getSelectedRow();
        if (selectedRow >= 0) {
            txt_idKategori.setText(tabel_kategori.getValueAt(selectedRow, 0).toString());
            txt_namaKategori.setText(tabel_kategori.getValueAt(selectedRow, 1).toString());
            txt_deskripsi.setText(tabel_kategori.getValueAt(selectedRow, 2).toString());
            txt_idKategori.setEnabled(true);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new kategori().setVisible(true));
    }

    // VARIABLES
    private javax.swing.JButton btn_back, btn_cancel, btn_delete, btn_refresh, btn_submit, btn_update;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane scrollDeskripsi; // Menggunakan JScrollPane untuk area deskripsi
    private javax.swing.JTable tabel_kategori;
    private javax.swing.JTextField txt_idKategori, txt_namaKategori;
    private javax.swing.JTextArea txt_deskripsi; // Diubah menjadi JTextArea agar multiline

    // INIT COMPONENTS
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(850, 640); // Ditambah sedikit tingginya agar lebih lega
        setLocationRelativeTo(null);

        // TITLE BAR
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(15, 15, 35));
        titleBar.setPreferredSize(new Dimension(850, 45));

        JLabel titleLabel = new JLabel("Manajemen Kategori - Toko");
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

        // MAIN PANEL
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(10, 10, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);

        // HEADER
        JLabel header = new JLabel("Daftar Data Kategori");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Color.WHITE);
        c.gridx = 0; c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        mainPanel.add(header, c);

        // ID KATEGORI
        c.gridx = 0; c.gridy = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST; // Label ditarik ke kanan mendekati teks field
        JLabel lblId = new JLabel("ID Kategori :", SwingConstants.RIGHT);
        lblId.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblId.setForeground(new Color(200, 200, 230));
        lblId.setPreferredSize(new Dimension(120, 32));
        mainPanel.add(lblId, c);

        txt_idKategori = new JTextField();
        txt_idKategori.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt_idKategori.setBackground(Color.WHITE);
        txt_idKategori.setForeground(Color.BLACK);
        txt_idKategori.setPreferredSize(new Dimension(150, 32)); // Ukuran ID diperpendek agar proporsional
        txt_idKategori.setBorder(BorderFactory.createCompoundBorder(txt_idKategori.getBorder(), BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        txt_idKategori.setEnabled(false);
        c.gridx = 1; c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        mainPanel.add(txt_idKategori, c);

        // NAMA KATEGORI
        c.gridx = 0; c.gridy = 2;
        c.anchor = GridBagConstraints.EAST;
        JLabel lblNama = new JLabel("Nama Kategori :", SwingConstants.RIGHT);
        lblNama.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblNama.setForeground(new Color(200, 200, 230));
        lblNama.setPreferredSize(new Dimension(120, 32));
        mainPanel.add(lblNama, c);

        txt_namaKategori = new JTextField();
        txt_namaKategori.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt_namaKategori.setBackground(Color.WHITE);
        txt_namaKategori.setForeground(Color.BLACK);
        txt_namaKategori.setPreferredSize(new Dimension(350, 32));
        txt_namaKategori.setBorder(BorderFactory.createCompoundBorder(txt_namaKategori.getBorder(), BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        c.gridx = 1; c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        mainPanel.add(txt_namaKategori, c);

        // DESKRIPSI (Menggunakan JTextArea + JScrollPane)
        c.gridx = 0; c.gridy = 3;
        c.anchor = GridBagConstraints.EAST;
        JLabel lblDesk = new JLabel("Deskripsi :", SwingConstants.RIGHT);
        lblDesk.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesk.setForeground(new Color(200, 200, 230));
        lblDesk.setPreferredSize(new Dimension(120, 32));
        mainPanel.add(lblDesk, c);

        txt_deskripsi = new JTextArea();
        txt_deskripsi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt_deskripsi.setBackground(Color.WHITE);
        txt_deskripsi.setForeground(Color.BLACK);
        txt_deskripsi.setLineWrap(true);
        txt_deskripsi.setWrapStyleWord(true);
        txt_deskripsi.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        scrollDeskripsi = new JScrollPane(txt_deskripsi);
        scrollDeskripsi.setPreferredSize(new Dimension(350, 70)); // Tinggi disesuaikan untuk beberapa baris teks
        c.gridx = 1; c.gridy = 3;
        c.anchor = GridBagConstraints.WEST;
        mainPanel.add(scrollDeskripsi, c);

        // BUTTON ROW 1
        btn_submit = new JButton("SUBMIT");
        btn_submit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn_submit.setBackground(new Color(46, 204, 113));
        btn_submit.setForeground(Color.WHITE);
        btn_submit.setFocusPainted(false);
        btn_submit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn_submit.setPreferredSize(new Dimension(110, 38));

        btn_update = new JButton("UPDATE");
        btn_update.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn_update.setBackground(new Color(52, 152, 219));
        btn_update.setForeground(Color.WHITE);
        btn_update.setFocusPainted(false);
        btn_update.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn_update.setPreferredSize(new Dimension(110, 38));

        btn_delete = new JButton("DELETE");
        btn_delete.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn_delete.setBackground(new Color(231, 76, 60));
        btn_delete.setForeground(Color.WHITE);
        btn_delete.setFocusPainted(false);
        btn_delete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn_delete.setPreferredSize(new Dimension(110, 38));

        JPanel btnPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel1.setBackground(new Color(10, 10, 30));
        btnPanel1.add(btn_submit);
        btnPanel1.add(btn_update);
        btnPanel1.add(btn_delete);

        c.gridx = 0; c.gridy = 4;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(15, 10, 5, 10); // Menambahkan margin top pada baris tombol pertama
        mainPanel.add(btnPanel1, c);

        // BUTTON ROW 2
        btn_cancel = new JButton("CANCEL");
        btn_cancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn_cancel.setBackground(new Color(241, 196, 15));
        btn_cancel.setForeground(Color.BLACK);
        btn_cancel.setFocusPainted(false);
        btn_cancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn_cancel.setPreferredSize(new Dimension(110, 38));

        btn_refresh = new JButton("REFRESH");
        btn_refresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn_refresh.setBackground(new Color(52, 152, 219));
        btn_refresh.setForeground(Color.WHITE);
        btn_refresh.setFocusPainted(false);
        btn_refresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn_refresh.setPreferredSize(new Dimension(110, 38));

        btn_back = new JButton("KEMBALI"); // Diubah karakter panahnya agar tidak memicu glyph error []
        btn_back.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn_back.setBackground(new Color(155, 89, 182));
        btn_back.setForeground(Color.WHITE);
        btn_back.setFocusPainted(false);
        btn_back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn_back.setPreferredSize(new Dimension(130, 38));

        JPanel btnPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnPanel2.setBackground(new Color(10, 10, 30));
        btnPanel2.add(btn_cancel);
        btnPanel2.add(btn_refresh);
        btnPanel2.add(btn_back);

        c.gridx = 0; c.gridy = 5;
        c.gridwidth = 2;
        c.insets = new Insets(5, 10, 15, 10);
        mainPanel.add(btnPanel2, c);

        // TABLE
        tabel_kategori = new JTable();
        tabel_kategori.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabel_kategori.setRowHeight(30);
        tabel_kategori.setBackground(new Color(25, 25, 55));
        tabel_kategori.setForeground(Color.WHITE);
        tabel_kategori.setGridColor(new Color(60, 60, 120));
        tabel_kategori.setSelectionBackground(new Color(70, 70, 150));
        tabel_kategori.setSelectionForeground(Color.WHITE);
        tabel_kategori.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabel_kategori.getTableHeader().setBackground(new Color(40, 40, 80));
        tabel_kategori.getTableHeader().setForeground(Color.WHITE);
        tabel_kategori.getTableHeader().setReorderingAllowed(false);

        jScrollPane1 = new JScrollPane(tabel_kategori);
        jScrollPane1.setBackground(new Color(10, 10, 30));
        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 120), 1));

        c.gridx = 0; c.gridy = 6;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(5, 10, 10, 10);
        jScrollPane1.setPreferredSize(new Dimension(720, 180));
        mainPanel.add(jScrollPane1, c);

        // EVENT LISTENERS
        btn_submit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { btn_submitMouseClicked(evt); }
        });
        btn_update.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { btn_updateMouseClicked(evt); }
        });
        btn_delete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { btn_deleteMouseClicked(evt); }
        });
        btn_cancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { btn_cancelMouseClicked(evt); }
        });
        btn_refresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { btn_refreshMouseClicked(evt); }
        });
        btn_back.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { btn_backMouseClicked(evt); }
        });
        tabel_kategori.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) { tabel_kategoriMouseClicked(evt); }
        });

        // ROOT PANEL
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(15, 15, 35));
        rootPanel.add(titleBar, BorderLayout.NORTH);
        rootPanel.add(mainPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);
        setVisible(true);
    }
}