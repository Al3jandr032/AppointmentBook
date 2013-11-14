package Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private Connection connection;
    private String connectionString;
    private String username;
    private String password;

    public Database(String connectionString, String username, String password) {
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
    }

    public Connection connect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("com.microsoft.sqlserver.jdbc.SQLServerDriver not found [ is it in the classpath? ]");
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(connectionString, username, password);
        } catch (SQLException e) {
            System.err.println("unable to connect to mssql database.");
            e.printStackTrace();
        }
        return connection;
    }
}