import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server implements Runnable {
    Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    public static Vector<DataOutputStream> clients = new Vector<>();
    private static final int BUFFER_SIZE = 4096;

    public Server(Socket s) {
        try {
            socket = s;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeClient() {
        if (dataOutputStream != null) {
            clients.remove(dataOutputStream);
            try {
                if (dataInputStream != null) dataInputStream.close();
                if (dataOutputStream != null) dataOutputStream.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            clients.add(dataOutputStream);

            while (true) {
                // Read message type
                String messageType = dataInputStream.readUTF();

                if (messageType.equals("FILE")) {
                    // Handle file transfer
                    String fileName = dataInputStream.readUTF();
                    long fileSize = dataInputStream.readLong();
                    byte[] fileData = new byte[(int) fileSize];
                    dataInputStream.readFully(fileData);

                    // Broadcast file to all clients
                    broadcastToClients("FILE", fileName, fileSize, fileData);
                } else if (messageType.equals("MESSAGE")) {
                    // Handle text message
                    String msgInput = dataInputStream.readUTF();
                    System.out.println("received " + msgInput);

                    broadcastToClients("MESSAGE", msgInput, 0, null);
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected");
        } finally {
            removeClient();
        }
    }

    private void broadcastToClients(String messageType, String content, long fileSize, byte[] fileData) {
        Vector<DataOutputStream> deadClients = new Vector<>();

        for (DataOutputStream dos : clients) {
            try {
                dos.writeUTF(messageType);
                if (messageType.equals("FILE")) {
                    dos.writeUTF(content); // fileName
                    dos.writeLong(fileSize);
                    dos.write(fileData);
                } else {
                    dos.writeUTF(content); // message
                }
                dos.flush();
            } catch (Exception e) {
                deadClients.add(dos);
            }
        }

        // Remove any dead clients
        clients.removeAll(deadClients);
    }

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(6001);
        System.out.println("Server started on port 6001");

        while (true) {
            Socket socket = serverSocket.accept();
            Server server = new Server(socket);
            Thread thread = new Thread(server);
            thread.start();
        }
    }
}
