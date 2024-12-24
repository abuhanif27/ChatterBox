import java.sql.*;
import java.io.*;

public class DatabaseHelper {

    private static final String URL = "jdbc:mysql://localhost:3306/ChatApp"; // Replace with your DB URL
    private static final String USER = "root";  // Replace with your DB username
    private static final String PASSWORD = "root1234";  // Replace with your DB password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void saveMessage(String name, String message, String fileName, byte[] fileData) {
        String query = "INSERT INTO messages (name, message_date, message, file_name, file) VALUES (?, NOW(), ?, ?, ?)";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, message);
            statement.setString(3, fileName);
            statement.setBytes(4, fileData);  // If no file, pass null for fileData

            statement.executeUpdate();
            System.out.println("Message saved to database.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void retrieveMessages() {
        String query = "SELECT * FROM messages ORDER BY message_date ASC";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                Timestamp messageDate = resultSet.getTimestamp("message_date");
                String message = resultSet.getString("message");
                String fileName = resultSet.getString("file_name");
                byte[] fileData = resultSet.getBytes("file");

                System.out.println(name + " (" + messageDate + "): " + message);
                if (fileName != null) {
                    System.out.println("File: " + fileName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
