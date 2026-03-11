import java.sql.Connection;
import java.sql.DriverManager;

public class CreateDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres",
                    "maomaosar1");
            conn.createStatement().executeUpdate("CREATE DATABASE zentinel_db");
            System.out.println("Database 'zentinel_db' created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
