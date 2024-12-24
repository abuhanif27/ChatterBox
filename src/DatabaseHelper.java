import java.sql.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/ChatApp";
    private static final String USER = "root";
    private static final String PASSWORD = "root1234";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Class to hold message data
    public static class MessageData {
        public String name;
        public Timestamp messageDate;
        public String message;
        public String fileName;
        public byte[] fileData;

        public MessageData(String name, Timestamp messageDate, String message, String fileName, byte[] fileData) {
            this.name = name;
            this.messageDate = messageDate;
            this.message = message;
            this.fileName = fileName;
            this.fileData = fileData;
        }
    }

    public static void saveMessage(String name, String message, String fileName, byte[] fileData) {
        String query = "INSERT INTO messages (name, message_date, message, file_name, file) VALUES (?, NOW(), ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            statement.setString(2, message);
            statement.setString(3, fileName);
            statement.setBytes(4, fileData);

            statement.executeUpdate();
            System.out.println("Message saved to database.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<MessageData> retrieveMessages(int limit) {
        List<MessageData> messages = new ArrayList<>();
        String query = "SELECT * FROM messages ORDER BY message_date DESC LIMIT ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, limit);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                Timestamp messageDate = resultSet.getTimestamp("message_date");
                String message = resultSet.getString("message");
                String fileName = resultSet.getString("file_name");
                byte[] fileData = resultSet.getBytes("file");

                messages.add(new MessageData(name, messageDate, message, fileName, fileData));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}