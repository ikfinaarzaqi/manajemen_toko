import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnection {

    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/db_toko",
                    "root",
                    ""
            );
            System.out.println("✅ Koneksi database BERHASIL!");
        } catch (ClassNotFoundException ex) {
            System.out.println("❌ Driver tidak ditemukan: " + ex.getMessage());
        } catch (SQLException ex) {
            System.out.println("❌ Koneksi gagal: " + ex.getMessage());
        }
        return con;
    }

    // ===== TAMBAHKAN MAIN METHOD UNTUK TEST =====
    public static void main(String[] args) {
        System.out.println("🔄 Mencoba koneksi ke database...");
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("✅ Koneksi SUKSES! Database terhubung.");
        } else {
            System.out.println("❌ Koneksi GAGAL! Periksa Laragon dan database.");
        }
    }
}