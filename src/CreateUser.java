import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CreateUser extends JFrame implements ActionListener {
    private JTextField nameField;
    private JButton createButton;
    private JButton chooseColorButton;
    private Color selectedColor;
    private JPanel previewPanel;
    private JLabel previewLabel;

    // Theme colors
    private final static Color BACKGROUND_COLOR = new Color(43, 43, 43);
    private final static Color COMPONENT_BG = new Color(60, 60, 60);
    private final static Color TEXT_COLOR = new Color(200, 200, 200);

    // Predefined theme colors
    private final static Color PURPLE = new Color(138, 37, 196);
    private final static Color PINKISH = new Color(245, 48, 94);
    private final static Color YELLOW = new Color(246, 181, 0);
    private final static Color BLUE = new Color(0, 120, 212);
    private final static Color GREEN = new Color(16, 124, 16);

    public CreateUser() {
        setTitle("ChatterBox - Create New User");
        setSize(450, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Set default color
        selectedColor = PURPLE;

        // Main panel setup
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add sections to main panel
        mainPanel.add(createColorsPanel(), BorderLayout.NORTH);
        mainPanel.add(createInputPanel(), BorderLayout.CENTER);
        mainPanel.add(createPreviewPanel(), BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);
    }

    private JPanel createColorsPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout(10, 10));
        containerPanel.setBackground(BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Quick Colors");
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Sans Serif", Font.BOLD, 14));
        containerPanel.add(titleLabel, BorderLayout.NORTH);

        // Colors panel
        JPanel colorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        colorsPanel.setBackground(BACKGROUND_COLOR);

        addColorButton(colorsPanel, PURPLE, "Purple");
        addColorButton(colorsPanel, PINKISH, "Pink");
        addColorButton(colorsPanel, YELLOW, "Yellow");
        addColorButton(colorsPanel, BLUE, "Blue");
        addColorButton(colorsPanel, GREEN, "Green");

        containerPanel.add(colorsPanel, BorderLayout.CENTER);
        return containerPanel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username label
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setFont(new Font("Sans Serif", Font.BOLD, 14));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(10));

        // Username field
        nameField = new JTextField();
        nameField.setBackground(COMPONENT_BG);
        nameField.setForeground(TEXT_COLOR);
        nameField.setCaretColor(TEXT_COLOR);
        nameField.setFont(new Font("Sans Serif", Font.PLAIN, 14));
        nameField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        nameField.setMaximumSize(new Dimension(350, 40));
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(20));

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        chooseColorButton = createStyledButton("Custom Color");
        chooseColorButton.setPreferredSize(new Dimension(150, 40));
        buttonsPanel.add(chooseColorButton);

        createButton = createStyledButton("Create User");
        createButton.setPreferredSize(new Dimension(150, 40));
        buttonsPanel.add(createButton);

        panel.add(buttonsPanel);

        return panel;
    }

    private JPanel createPreviewPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout(10, 10));
        containerPanel.setBackground(BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Preview");
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Sans Serif", Font.BOLD, 14));
        containerPanel.add(titleLabel, BorderLayout.NORTH);

        // Preview panel
        previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        previewPanel.setBackground(BACKGROUND_COLOR);

        previewLabel = new JLabel("Theme Preview");
        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setFont(new Font("Sans Serif", Font.PLAIN, 16));
        previewLabel.setOpaque(true);
        previewLabel.setBorder(BorderFactory.createLineBorder(TEXT_COLOR, 2));
        previewLabel.setPreferredSize(new Dimension(350, 50));
        updatePreview();

        previewPanel.add(previewLabel, BorderLayout.CENTER);
        containerPanel.add(previewPanel, BorderLayout.CENTER);

        return containerPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(COMPONENT_BG);
        button.setForeground(TEXT_COLOR);
        button.setFont(new Font("Sans Serif", Font.BOLD, 14));
        button.setBorder(BorderFactory.createLineBorder(TEXT_COLOR));
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(TEXT_COLOR);
                button.setForeground(COMPONENT_BG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(COMPONENT_BG);
                button.setForeground(TEXT_COLOR);
            }
        });
        button.addActionListener(this);
        return button;
    }

    private void addColorButton(JPanel panel, Color color, String tooltip) {
        JButton colorButton = new JButton();
        colorButton.setPreferredSize(new Dimension(40, 40));
        colorButton.setBackground(color);
        colorButton.setBorder(BorderFactory.createLineBorder(TEXT_COLOR, 1));
        colorButton.setToolTipText(tooltip);
        colorButton.addActionListener(e -> {
            selectedColor = color;
            updatePreview();
        });
        panel.add(colorButton);
    }

    private void updatePreview() {
        previewLabel.setBackground(selectedColor);
        previewLabel.setForeground(isDarkColor(selectedColor) ? Color.WHITE : Color.BLACK);
        previewPanel.repaint();
    }

    private boolean isDarkColor(Color color) {
        int brightness = (int) Math.sqrt(
                color.getRed() * color.getRed() * 0.241 +
                        color.getGreen() * color.getGreen() * 0.691 +
                        color.getBlue() * color.getBlue() * 0.068);
        return brightness < 130;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createButton) {
            String username = nameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a username",
                        "Input Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create new client and connect to server
            SwingUtilities.invokeLater(() -> {
                Client client = new Client("127.0.0.1", 6001, username, selectedColor);
                client.connectToServer();
                // Clear the input field after successful creation
                nameField.setText("");
                // Reset color to default
                selectedColor = PURPLE;
                updatePreview();
            });

        } else if (e.getSource() == chooseColorButton) {
            Color newColor = JColorChooser.showDialog(
                    this,
                    "Choose Theme Color",
                    selectedColor);

            if (newColor != null) {
                selectedColor = newColor;
                updatePreview();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CreateUser createUser = new CreateUser();
            createUser.setVisible(true);
        });
    }
}