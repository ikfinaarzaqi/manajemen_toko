import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class login extends javax.swing.JFrame {

    public static String txt;//menyimpan username yang login

    public login() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setTitle("Login - Aplikasi Toko");
        txt_username.requestFocus();//untuk mengarahkan kursor supaya langsung ke field username
    }

    private void kosongkan_form() {
        txt_username.setText("");
        txt_password.setText("");
        txt_username.requestFocus();
    }

    private void btn_loginMouseClicked(java.awt.event.MouseEvent evt) {
        if (txt_username.getText().trim().isEmpty() || txt_password.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Username dan Password tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txt_username.requestFocus();
            return;
        }

        try {
            Connection con = MyConnection.getConnection();
            if (con == null) {
                JOptionPane.showMessageDialog(null, "Koneksi Database Gagal!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "SELECT * FROM register WHERE username = ? AND password = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, txt_username.getText().trim());
            ps.setString(2, txt_password.getText().trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                txt = rs.getString("username");
                JOptionPane.showMessageDialog(null, "✅ Login Berhasil! Selamat Datang " + rs.getString("name"), "Sukses", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                new menu().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "❌ Username atau Password salah!", "Error", JOptionPane.ERROR_MESSAGE);
                txt_password.setText("");
                txt_password.requestFocus();
            }

            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_cancelMouseClicked(java.awt.event.MouseEvent evt) {
        kosongkan_form();
    }

    private void jLabel_registerMouseClicked(java.awt.event.MouseEvent evt) {
        if (JOptionPane.showConfirmDialog(null, "Pindah ke halaman Registrasi?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            this.dispose();
            new register().setVisible(true);
        }
    }

    private void jLabel_exitMouseClicked(java.awt.event.MouseEvent evt) {
        if (JOptionPane.showConfirmDialog(null, "Yakin keluar?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new login().setVisible(true));
    }

    // ========== VARIABLES ==========
    private javax.swing.JButton btn_cancel, btn_login;
    private javax.swing.JLabel jLabel_exit, jLabel_register;
    private javax.swing.JPasswordField txt_password;
    private javax.swing.JTextField txt_username;

    // ========== KOMPONEN GUI ==========
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(500, 580);
        setLocationRelativeTo(null);

        // ===== TITLE BAR =====
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(15, 15, 35));
        titleBar.setPreferredSize(new Dimension(450, 45));

        JLabel titleLabel = new JLabel("LOGIN");
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
        c.insets = new Insets(10, 15, 10, 15);

        // ===== LOGO =====
        JLabel logo = new JLabel("");
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        logo.setForeground(new Color(100, 150, 255));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        mainPanel.add(logo, c);

        JLabel title = new JLabel("LOGIN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        c.gridy = 1;
        mainPanel.add(title, c);

        // ===== USERNAME =====
        c.gridx = 0; c.gridy = 2;
        c.gridwidth = 2;
        JLabel lblUser = new JLabel("Username :");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(new Color(200, 200, 230));
        mainPanel.add(lblUser, c);

        txt_username = new JTextField(20);
        txt_username.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt_username.setBackground(new Color(255, 255, 255));
        txt_username.setForeground(Color.BLACK);
        txt_username.setPreferredSize(new Dimension(300, 35));
        c.gridy = 3;
        mainPanel.add(txt_username, c);

        // ===== PASSWORD =====
        c.gridy = 4;
        JLabel lblPass = new JLabel("Password :");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPass.setForeground(new Color(200, 200, 230));
        mainPanel.add(lblPass, c);

        txt_password = new JPasswordField(20);
        txt_password.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt_password.setBackground(new Color(255, 255, 255));
        txt_password.setForeground(Color.BLACK);
        txt_password.setPreferredSize(new Dimension(300, 35));
        c.gridy = 5;
        mainPanel.add(txt_password, c);

        // ===== BUTTONS =====
        btn_login = new JButton("LOGIN");
        btn_login.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn_login.setBackground(new Color(46, 204, 113));
        btn_login.setForeground(Color.WHITE);
        btn_login.setFocusPainted(false);
        btn_login.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn_login.setPreferredSize(new Dimension(140, 38));
        btn_login.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_loginMouseClicked(evt);
            }
        });

        btn_cancel = new JButton("CANCEL");
        btn_cancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn_cancel.setBackground(new Color(231, 76, 60));
        btn_cancel.setForeground(Color.WHITE);
        btn_cancel.setFocusPainted(false);
        btn_cancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn_cancel.setPreferredSize(new Dimension(140, 38));
        btn_cancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_cancelMouseClicked(evt);
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        btnPanel.setBackground(new Color(10, 10, 30));
        btnPanel.add(btn_login);
        btnPanel.add(btn_cancel);

        c.gridx = 0; c.gridy = 6;
        c.gridwidth = 2;
        mainPanel.add(btnPanel, c);

        // ===== REGISTER LINK =====
        jLabel_register = new JLabel("Belum punya akun? Register di sini");
        jLabel_register.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jLabel_register.setForeground(new Color(100, 150, 255));
        jLabel_register.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jLabel_register.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel_register.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_registerMouseClicked(evt);
            }
        });
        c.gridy = 7;
        mainPanel.add(jLabel_register, c);

        // ===== EXIT LINK =====
        jLabel_exit = new JLabel("Exit");
        jLabel_exit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jLabel_exit.setForeground(new Color(200, 80, 80));
        jLabel_exit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jLabel_exit.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel_exit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_exitMouseClicked(evt);
            }
        });
        c.gridy = 8;
        mainPanel.add(jLabel_exit, c);

        // ===== ROOT PANEL =====
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(15, 15, 35));
        rootPanel.add(titleBar, BorderLayout.NORTH);
        rootPanel.add(mainPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);
        setVisible(true);
    }
}