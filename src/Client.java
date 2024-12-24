import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class Client implements ActionListener, Runnable {
    private final String serverAddress;
    private final int serverPort;
    public final String username;
    public final Color themeColor;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private JFrame frame;
    private JPanel topPanel;
    private JTextField msgField;
    private JButton sendButton;
    private JButton fileButton;
    private JPanel textArea;
    private Box vertical;
    private JFileChooser fileChooser;
    static String mymsg;

    private final Color blackForBg = new Color(26, 32, 47);
    private final Color blackForMsg = new Color(57, 71, 101);

    public Client(String serverAddress, int serverPort, String username, Color themeColor) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.username = username;
        this.themeColor = themeColor;
        this.fileChooser = new JFileChooser();

        initializeGUI(username);
    }

    private void initializeGUI(String username) {
        frame = new JFrame(username);
        topPanel = new JPanel();
        msgField = new JTextField();
        sendButton = new JButton("Send");
        fileButton = new JButton("File");
        textArea = new JPanel();
        vertical = Box.createVerticalBox();

        frame.getContentPane().setBackground(blackForBg);
        frame.setLayout(null);
        frame.setSize(450, 700);
        frame.setLocation(10, 50);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setupWindowClosing();

        // Top panel setup
        topPanel.setLayout(null);
        topPanel.setBackground(themeColor);
        topPanel.setBounds(0, 0, 450, 70);

        // Add icons and labels
        setupTopPanelComponents();

        // Chat area setup
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(0, 70, 431, 545);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Message field setup
        msgField.setBorder(BorderFactory.createEmptyBorder());
        msgField.setBackground(blackForMsg);
        msgField.setForeground(Color.WHITE);
        msgField.setBounds(0, 615, 250, 40);
        msgField.setFont(new Font("SAN_SERIF", Font.PLAIN, 16));

        // Button setup
        fileButton.setBounds(250, 615, 60, 40);
        fileButton.setForeground(Color.WHITE);
        fileButton.setBackground(themeColor);
        fileButton.addActionListener(this);

        sendButton.setBounds(310, 615, 123, 40);
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(themeColor);
        sendButton.addActionListener(this);

        frame.getRootPane().setDefaultButton(sendButton);

        // Add components to frame
        frame.add(topPanel);
        frame.add(scrollPane);
        frame.add(msgField);
        frame.add(fileButton);
        frame.add(sendButton);

        textArea.setBackground(blackForBg);
        frame.setVisible(true);
    }

    private void setupWindowClosing() {
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    if (dataOutputStream != null) dataOutputStream.close();
                    if (dataInputStream != null) dataInputStream.close();
                    if (socket != null) socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                frame.dispose();
            }
        });
    }

    private void setupTopPanelComponents() {
        ImageIcon backIcon = new ImageIcon("icons/3.png");
        Image backIconSized = backIcon.getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT);
        JLabel label1 = new JLabel(new ImageIcon(backIconSized));
        label1.setBounds(5, 17, 30, 30);

        ImageIcon dpIcon = new ImageIcon("icons/grp_icon.png");
        Image dpIconSized = dpIcon.getImage().getScaledInstance(60, 60, Image.SCALE_DEFAULT);
        JLabel label2 = new JLabel(new ImageIcon(dpIconSized));
        label2.setBounds(40, 5, 60, 60);

        JLabel name = new JLabel("ChatterBox");
        name.setFont(new Font("SAN_SERIF", Font.BOLD, 12));
        name.setForeground(Color.WHITE);
        name.setBounds(110, 15, 100, 18);

        JLabel activeStatus = new JLabel("You, other members");
        activeStatus.setFont(new Font("SAN_SERIF", Font.PLAIN, 14));
        activeStatus.setForeground(Color.WHITE);
        activeStatus.setBounds(110, 35, 110, 20);

        topPanel.add(label1);
        topPanel.add(label2);
        topPanel.add(name);
        topPanel.add(activeStatus);
    }

    public void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            Thread thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Could not connect to server: " + e.getMessage());
        }
    }

    private void sendFile() {
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Send file metadata
                dataOutputStream.writeUTF("FILE");
                dataOutputStream.writeUTF(selectedFile.getName());
                dataOutputStream.writeLong(selectedFile.length());

                // Send file content
                FileInputStream fis = new FileInputStream(selectedFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dataOutputStream.write(buffer, 0, bytesRead);
                }
                dataOutputStream.flush();
                fis.close();

                // Save the file message to the database
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                DatabaseHelper.saveMessage(username, null, selectedFile.getName(), fileData);

                // Display file sent message
                displayMessage("<b>" + username + "</b> sent a file: " + selectedFile.getName(), true);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error sending file: " + e.getMessage());
            }
        }
    }


    private void receiveFile(String fileName, long fileSize) {
        try {
            JFileChooser saveChooser = new JFileChooser();
            saveChooser.setSelectedFile(new File(fileName));
            int result = saveChooser.showSaveDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                File saveFile = saveChooser.getSelectedFile();
                FileOutputStream fos = new FileOutputStream(saveFile);

                byte[] buffer = new byte[4096];
                long remainingBytes = fileSize;

                while (remainingBytes > 0) {
                    int bytesToRead = (int) Math.min(buffer.length, remainingBytes);
                    int bytesRead = dataInputStream.read(buffer, 0, bytesToRead);
                    if (bytesRead == -1) break;
                    fos.write(buffer, 0, bytesRead);
                    remainingBytes -= bytesRead;
                }

                fos.close();
                displayMessage("File received and saved: " + fileName, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error receiving file: " + e.getMessage());
        }
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == fileButton) {
            sendFile();
        } else if (ae.getSource() == sendButton) {
            try {
                String message = msgField.getText();
                if (!message.isEmpty()) {
                    dataOutputStream.writeUTF("MESSAGE");
                    String formattedMessage = "<b style=\"color:white;\"><u>" + username + ":</u></b><br>" + message;
                    dataOutputStream.writeUTF(formattedMessage);
                    displayMessage(formattedMessage, true);

                    // Save the message to the database
                    DatabaseHelper.saveMessage(username, message, null, null); // No file sent, so file is null

                    msgField.setText("");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayMessage(String message, boolean isOwnMessage) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        JPanel contentPanel = isOwnMessage ? formatLabel(message, themeColor) : formatLabelReceived(message);

        messagePanel.setBackground(blackForBg);
        contentPanel.setBackground(blackForBg);

        messagePanel.add(contentPanel, isOwnMessage ? BorderLayout.LINE_END : BorderLayout.LINE_START);
        vertical.add(messagePanel);

        textArea.setLayout(new BorderLayout());
        textArea.add(vertical, BorderLayout.PAGE_START);

        frame.validate();
        frame.repaint();
    }

    public static JPanel formatLabel(String out, Color themeColor) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("<html><p style=\"width:150px;\">" + out + "</p></html>");
        label.setBackground(themeColor);
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(15, 15, 15, 50));
        panel.add(label);
        return panel;
    }

    public static JPanel formatLabelReceived(String out) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("<html><p style=\"width:150px;\">" + out + "</p></html>");
        label.setBackground(new Color(57, 71, 101));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(15, 15, 15, 50));
        panel.add(label);
        return panel;
    }

    public void run() {
        try {
            while (true) {
                String messageType = dataInputStream.readUTF();

                if (messageType.equals("FILE")) {
                    String fileName = dataInputStream.readUTF();
                    long fileSize = dataInputStream.readLong();
                    receiveFile(fileName, fileSize);
                } else if (messageType.equals("MESSAGE")) {
                    String message = dataInputStream.readUTF();
                    if (!isOwnMessage(message)) {
                        displayMessage(message, false);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server");
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isOwnMessage(String message) {
        int startIndex = message.indexOf("<u>") + 3;
        int endIndex = message.indexOf(":", startIndex);
        String senderUsername = message.substring(startIndex, endIndex);
        return senderUsername.equals(username);
    }
}
