import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.net.IDN;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * A Swing-based Network Client demonstration.
 *
 * Connection behavior is simulated: this program validates the entered host
 * and port and updates the UI state, but it does not open a network socket.
 * Run with --demo to show a pre-populated connected-state preview.
 */
public class NetworkClient extends JFrame {
    private static final Color NAVY = new Color(19, 37, 61);
    private static final Color NAVY_LIGHT = new Color(31, 56, 85);
    private static final Color ACCENT = new Color(43, 111, 235);
    private static final Color BG = new Color(242, 246, 251);
    private static final Color TEXT = new Color(35, 49, 66);
    private static final Color MUTED = new Color(111, 128, 148);
    private static final Color GREEN = new Color(32, 166, 111);
    private static final Color RED = new Color(218, 81, 91);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final JTextField hostField = new JTextField("example.com", 22);
    private final JTextField portField = new JTextField("8080", 8);
    private final JComboBox<String> protocolBox = new JComboBox<>(new String[]{"TCP", "UDP"});
    private final JTextField messageField = new JTextField();
    private final JTextArea logArea = new JTextArea();
    private final JLabel statusText = new JLabel("Disconnected");
    private final StatusDot statusDot = new StatusDot(RED);
    private final JButton connectButton = new JButton("Connect");
    private final JButton disconnectButton = new JButton("Disconnect");
    private final JButton sendButton = new JButton("Send  ➤");
    private final JButton clearButton = new JButton("Clear log");
    private boolean connected = false;

    public NetworkClient(boolean demoMode) {
        super("NetLink | Network Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 620));
        setSize(1050, 720);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(22, 26, 24, 26));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildWorkspace(), BorderLayout.CENTER);
        setContentPane(root);

        configureComponents();
        wireEvents();
        appendLog("Network Client initialized. Ready to connect.");

        if (demoMode) {
            hostField.setText("192.0.2.44");
            portField.setText("8080");
            protocolBox.setSelectedItem("TCP");
            setConnected(true);
            appendLog("Demo preview: simulated connection only; no socket was opened.");
            appendLog("TX  >  PING /status");
            appendLog("RX  <  ACK - connection healthy (simulated)");
        }
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(20, 23, 20, 23));

        JPanel identity = new JPanel(new BorderLayout(14, 0));
        identity.setOpaque(false);
        JLabel mark = new JLabel("N", SwingConstants.CENTER);
        mark.setForeground(Color.WHITE);
        mark.setFont(new Font("SansSerif", Font.BOLD, 23));
        mark.setOpaque(true);
        mark.setBackground(ACCENT);
        mark.setPreferredSize(new Dimension(46, 46));
        mark.setBorder(new EmptyBorder(3, 3, 3, 3));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Network Client");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel subtitle = new JLabel("A simple interface for testing client connections");
        subtitle.setForeground(new Color(185, 200, 219));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);
        identity.add(mark, BorderLayout.WEST);
        identity.add(titles, BorderLayout.CENTER);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 10));
        status.setOpaque(false);
        statusDot.setPreferredSize(new Dimension(10, 10));
        status.add(statusDot);
        statusText.setForeground(new Color(227, 235, 245));
        statusText.setFont(new Font("SansSerif", Font.BOLD, 13));
        status.add(statusText);

        header.add(identity, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);
        return header;
    }

    private JComponent buildWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(0, 16));
        workspace.setOpaque(false);
        workspace.add(buildConnectionCard(), BorderLayout.NORTH);
        workspace.add(buildLogCard(), BorderLayout.CENTER);
        return workspace;
    }

    private JComponent buildConnectionCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 14));

        JPanel heading = sectionTitle("Connection settings", "Enter a destination and choose the transport protocol");
        card.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;

        addField(fields, gbc, 0, "HOST / IP ADDRESS", hostField, 0.55);
        addField(fields, gbc, 1, "PORT", portField, 0.20);
        addField(fields, gbc, 2, "PROTOCOL", protocolBox, 0.25);
        card.add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        actions.setOpaque(false);
        styleButton(connectButton, ACCENT, Color.WHITE);
        styleButton(disconnectButton, new Color(232, 237, 244), TEXT);
        actions.add(connectButton);
        actions.add(disconnectButton);
        JLabel note = new JLabel("Simulation mode  ·  No real network socket is opened");
        note.setForeground(MUTED);
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.setBorder(new EmptyBorder(0, 8, 0, 0));
        actions.add(note);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int column,
                          String labelText, JComponent field, double weight) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 7));
        wrapper.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        wrapper.add(label, BorderLayout.NORTH);
        styleInput(field);
        wrapper.add(field, BorderLayout.CENTER);

        gbc.gridx = column;
        gbc.weightx = weight;
        panel.add(wrapper, gbc);
    }

    private JComponent buildLogCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 13));

        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);
        logHeader.add(sectionTitle("Activity log", "Connection events and sent messages appear here"), BorderLayout.WEST);
        styleButton(clearButton, new Color(232, 237, 244), TEXT);
        clearButton.setMargin(new Insets(7, 13, 7, 13));
        logHeader.add(clearButton, BorderLayout.EAST);
        card.add(logHeader, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setForeground(new Color(210, 223, 238));
        logArea.setBackground(new Color(18, 31, 49));
        logArea.setCaretColor(Color.WHITE);
        logArea.setBorder(new EmptyBorder(15, 16, 15, 16));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new LineBorder(new Color(29, 48, 70), 1, true));
        scrollPane.getViewport().setBackground(new Color(18, 31, 49));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scrollPane, BorderLayout.CENTER);

        JPanel composer = new JPanel(new BorderLayout(10, 0));
        composer.setOpaque(false);
        styleInput(messageField);
        messageField.setToolTipText("Type a message to send while connected");
        messageField.setText("Hello from Network Client");
        messageField.setForeground(TEXT);
        styleButton(sendButton, ACCENT, Color.WHITE);
        sendButton.setPreferredSize(new Dimension(120, 40));
        composer.add(messageField, BorderLayout.CENTER);
        composer.add(sendButton, BorderLayout.EAST);
        card.add(composer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 232, 240), 1, true),
                new EmptyBorder(17, 18, 17, 18)));
        return panel;
    }

    private JPanel sectionTitle(String titleText, String subtitleText) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(titleText);
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        return panel;
    }

    private void configureComponents() {
        hostField.setToolTipText("Enter a hostname, IPv4 address, or IPv6 address");
        portField.setToolTipText("Port must be between 1 and 65535");
        protocolBox.setFocusable(false);
        disconnectButton.setEnabled(false);
        sendButton.setEnabled(false);
    }

    private void wireEvents() {
        connectButton.addActionListener(e -> connect());
        disconnectButton.addActionListener(e -> disconnect());
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        clearButton.addActionListener(e -> {
            logArea.setText("");
        });
    }

    private void connect() {
        String host = hostField.getText().trim();
        String portValue = portField.getText().trim();

        if (!isValidHost(host)) {
            showValidationError("Enter a valid hostname or IP address.", hostField);
            return;
        }

        int port;
        try {
            if (!portValue.matches("\\d{1,5}")) {
                throw new NumberFormatException();
            }
            port = Integer.parseInt(portValue);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            showValidationError("Port must be a number from 1 to 65535.", portField);
            return;
        }

        appendLog("Connecting to " + host + ":" + port + " using " + protocolBox.getSelectedItem() + "...");
        setConnected(true);
    }

    private boolean isValidHost(String host) {
        if (host == null || host.isBlank() || host.length() > 253 || host.contains(" ")) {
            return false;
        }

        // A colon indicates an IPv6 literal. Parsing it does not require DNS lookup.
        if (host.contains(":")) {
            try {
                return InetAddress.getByName(host) instanceof Inet6Address;
            } catch (Exception ex) {
                return false;
            }
        }

        // Strictly validate dotted IPv4 literals instead of treating them as DNS names.
        if (host.matches("[0-9.]+")) {
            String[] parts = host.split("\\.", -1);
            if (parts.length != 4) return false;
            for (String part : parts) {
                if (part.isEmpty() || part.length() > 3) return false;
                try {
                    int value = Integer.parseInt(part);
                    if (value < 0 || value > 255) return false;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
            return true;
        }

        try {
            String ascii = IDN.toASCII(host, IDN.USE_STD3_ASCII_RULES);
            if (ascii.isEmpty() || ascii.length() > 253) return false;
            String[] labels = ascii.split("\\.", -1);
            for (String label : labels) {
                if (label.isEmpty() || label.length() > 63 ||
                        !label.matches("[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?")) {
                    return false;
                }
            }
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private void disconnect() {
        if (!connected) return;
        appendLog("Disconnected from " + hostField.getText().trim() + ".");
        setConnected(false);
    }

    private void sendMessage() {
        if (!connected) {
            JOptionPane.showMessageDialog(this, "Connect before sending a message.",
                    "Not connected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Type a message before sending.",
                    "Empty message", JOptionPane.WARNING_MESSAGE);
            messageField.requestFocusInWindow();
            return;
        }
        appendLog("TX  >  " + message);
        appendLog("RX  <  Message accepted (simulated response)");
        messageField.selectAll();
    }

    private void setConnected(boolean value) {
        connected = value;
        statusText.setText(value ? "Connected" : "Disconnected");
        statusDot.setDotColor(value ? GREEN : RED);
        connectButton.setEnabled(!value);
        disconnectButton.setEnabled(value);
        sendButton.setEnabled(value);
        hostField.setEnabled(!value);
        portField.setEnabled(!value);
        protocolBox.setEnabled(!value);
        if (value) {
            appendLog("Connected to " + hostField.getText().trim() + ":" + portField.getText().trim() +
                    " (" + protocolBox.getSelectedItem() + ") - simulated.");
        }
    }

    private void showValidationError(String message, JComponent focusTarget) {
        JOptionPane.showMessageDialog(this, message, "Input validation", JOptionPane.ERROR_MESSAGE);
        focusTarget.requestFocusInWindow();
    }

    private void appendLog(String message) {
        logArea.append("[" + LocalTime.now().format(TIME_FORMAT) + "]  " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void styleInput(JComponent component) {
        component.setFont(new Font("SansSerif", Font.PLAIN, 13));
        component.setForeground(TEXT);
        component.setBackground(new Color(250, 252, 255));
        component.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(215, 224, 235), 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        if (component instanceof JTextField field) {
            field.setCaretColor(ACCENT);
        }
    }

    private void styleButton(AbstractButton button, Color background, Color foreground) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(background.equals(ACCENT) ? ACCENT : new Color(220, 228, 237), 1, true),
                new EmptyBorder(9, 17, 9, 17)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static class StatusDot extends JComponent {
        private Color dotColor;
        StatusDot(Color color) { this.dotColor = color; }
        void setDotColor(Color color) { this.dotColor = color; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(dotColor);
            g2.fillOval(1, 1, Math.max(0, getWidth() - 3), Math.max(0, getHeight() - 3));
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        boolean demoMode = Arrays.asList(args).contains("--demo");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep Java's default Swing look and feel if the system one is unavailable.
            }
            new NetworkClient(demoMode).setVisible(true);
        });
    }
}
