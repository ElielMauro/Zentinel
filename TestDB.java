import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres",
                    "maomaosar1");
            ResultSet rs = conn.createStatement().executeQuery("SELECT datname FROM pg_database");
            System.out.println("--- DATABASES ---");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
