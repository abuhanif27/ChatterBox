import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(6001);
                System.out.println("Server started on port 6001");

                while (true) {
                    Socket socket = serverSocket.accept();
                    Server server = new Server(socket);
                    Thread thread = new Thread(server);
                    thread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        serverThread.start();

        SwingUtilities.invokeLater(() -> {
            CreateUser createUser = new CreateUser();
            createUser.setVisible(true);
        });
    }
}