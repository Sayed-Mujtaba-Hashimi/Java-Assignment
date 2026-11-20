import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Swing-based TCP Network Connection Tester.
 *
 * The application resolves a host with InetAddress and attempts a TCP
 * connection using the user-configured connect timeout. It does not send
 * application data after connecting.
 */
@SuppressWarnings("this-escape")
public class NetworkConnectionTester extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color BG = new Color(14, 23, 38);
    private static final Color PANEL = new Color(24, 37, 56);
    private static final Color PANEL_ALT = new Color(19, 31, 48);
    private static final Color TEXT = new Color(232, 239, 248);
    private static final Color MUTED = new Color(155, 171, 192);
    private static final Color ACCENT = new Color(38, 194, 163);
    private static final Color DANGER = new Color(244, 102, 112);
    private static final Color BORDER = new Color(51, 69, 92);

    private final JTextField hostField = new JTextField("example.com", 22);
    private final JTextField portField = new JTextField("443", 8);
    private final JTextField timeoutField = new JTextField("5000", 8);
    private final JButton testButton = new JButton("Test connection");
    private final JButton clearButton = new JButton("Clear results");
    private final JLabel statusPill = new JLabel(" READY ");
    private final JLabel resultTitle = new JLabel("Ready to test");
    private final JTextArea userOutput = new JTextArea();
    private final JTextArea diagnosticsOutput = new JTextArea();
    private final JProgressBar progressBar = new JProgressBar();
    private String captureFile; // Used only by the optional screenshot/demo mode.

    public NetworkConnectionTester() {
        super("Network Connection Tester");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(940, 690));
        setSize(1120, 790);
        setLocationRelativeTo(null);
        setContentPane(buildInterface());
        styleInputs();
        setInitialOutput();
        wireEvents();
    }

    private JPanel buildInterface() {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(26, 30, 22, 30));

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);
        center.add(buildConnectionCard(), BorderLayout.NORTH);
        center.add(buildOutputArea(), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("NETWORK UTILITIES  /  TCP DIAGNOSTICS");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));
        eyebrow.setForeground(ACCENT);

        JLabel title = new JLabel("Network Connection Tester");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT);
        title.setBorder(new EmptyBorder(7, 0, 4, 0));

        JLabel subtitle = new JLabel("Resolve a host, verify a TCP port, and inspect connection diagnostics.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        titleStack.add(eyebrow);
        titleStack.add(title);
        titleStack.add(subtitle);
        header.add(titleStack, BorderLayout.WEST);

        JLabel mode = new JLabel("TCP  •  SOCKET CONNECT");
        mode.setFont(new Font("SansSerif", Font.BOLD, 11));
        mode.setForeground(new Color(159, 218, 210));
        mode.setOpaque(true);
        mode.setBackground(new Color(26, 73, 75));
        mode.setBorder(new EmptyBorder(9, 13, 9, 13));
        header.add(mode, BorderLayout.EAST);
        return header;
    }

    private JPanel buildConnectionCard() {
        JPanel card = cardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 8, 7, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JLabel sectionTitle = sectionLabel("CONNECTION TARGET");
        c.gridx = 0; c.gridy = 0; c.gridwidth = 3;
        card.add(sectionTitle, c);

        c.gridy = 1; c.gridwidth = 1;
        addField(card, c, 0, "Hostname or IP address", hostField, 1.55);
        addField(card, c, 1, "TCP port (1–65535)", portField, 0.65);
        addField(card, c, 2, "Timeout (ms)", timeoutField, 0.65);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        stylePrimaryButton(testButton);
        styleSecondaryButton(clearButton);
        actions.add(testButton);
        actions.add(clearButton);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 3;
        c.insets = new Insets(13, 8, 2, 8);
        card.add(actions, c);

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(100, 4));
        progressBar.setForeground(ACCENT);
        progressBar.setBackground(PANEL);
        c.gridy = 3;
        c.insets = new Insets(6, 8, 0, 8);
        card.add(progressBar, c);
        return card;
    }

    private void addField(JPanel parent, GridBagConstraints base, int column, String labelText,
                          JTextField field, double weight) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(MUTED);
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);

        GridBagConstraints c = (GridBagConstraints) base.clone();
        c.gridx = column;
        c.weightx = weight;
        c.insets = new Insets(8, 8, 7, 8);
        parent.add(wrapper, c);
    }

    private JPanel buildOutputArea() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);

        JPanel resultHeader = new JPanel(new BorderLayout(10, 0));
        resultHeader.setOpaque(false);
        JPanel statusBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusBlock.setOpaque(false);
        statusPill.setFont(new Font("SansSerif", Font.BOLD, 10));
        statusPill.setOpaque(true);
        statusPill.setForeground(new Color(196, 211, 226));
        statusPill.setBackground(new Color(53, 67, 86));
        statusPill.setBorder(new EmptyBorder(6, 9, 6, 9));
        resultTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        resultTitle.setForeground(TEXT);
        statusBlock.add(statusPill);
        statusBlock.add(resultTitle);
        resultHeader.add(statusBlock, BorderLayout.WEST);
        outer.add(resultHeader, BorderLayout.NORTH);

        JPanel outputCards = new JPanel(new GridLayout(1, 2, 14, 0));
        outputCards.setOpaque(false);
        outputCards.add(buildTextCard("USER-FRIENDLY RESULT", userOutput, false));
        outputCards.add(buildTextCard("DEVELOPER DIAGNOSTICS", diagnosticsOutput, true));
        outer.add(outputCards, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildTextCard(String heading, JTextArea area, boolean mono) {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(0, 12));
        JLabel label = sectionLabel(heading);
        card.add(label, BorderLayout.NORTH);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setForeground(mono ? new Color(174, 198, 220) : TEXT);
        area.setFont(new Font(mono ? Font.MONOSPACED : "SansSerif", Font.PLAIN, mono ? 12 : 14));
        area.setBorder(new EmptyBorder(2, 2, 2, 2));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL);
        scroll.setBackground(PANEL);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JLabel note = new JLabel("NOTE  This checks TCP reachability only; no application payload is transmitted.");
        note.setFont(new Font("SansSerif", Font.PLAIN, 11));
        note.setForeground(MUTED);
        footer.add(note, BorderLayout.WEST);
        JLabel version = new JLabel("JAVA SWING  •  TCP CLIENT");
        version.setFont(new Font("SansSerif", Font.BOLD, 10));
        version.setForeground(new Color(110, 132, 158));
        footer.add(version, BorderLayout.EAST);
        return footer;
    }

    private JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true), new EmptyBorder(16, 17, 16, 17)));
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(new Color(139, 163, 190));
        return label;
    }

    private void styleInputs() {
        for (JTextField f : new JTextField[]{hostField, portField, timeoutField}) {
            f.setFont(new Font("SansSerif", Font.PLAIN, 14));
            f.setForeground(TEXT);
            f.setCaretColor(ACCENT);
            f.setBackground(PANEL_ALT);
            f.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, true), new EmptyBorder(10, 11, 10, 11)));
        }
    }

    private void stylePrimaryButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(new Color(9, 29, 36));
        b.setBackground(ACCENT);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(11, 19, 11, 19));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(TEXT);
        b.setBackground(new Color(49, 66, 88));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(11, 17, 11, 17));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void setInitialOutput() {
        userOutput.setText("Enter a hostname or IP address, a TCP port, and a timeout.\n\n"
                + "Select ‘Test connection’ to resolve the host and check whether the remote TCP port accepts a connection.");
        diagnosticsOutput.setText("No test has been run.\n\nValidation rules:\n"
                + "• Hostname/IP must be valid and non-empty\n"
                + "• Port: integer from 1 to 65535\n"
                + "• Timeout: integer from 100 to 120000 milliseconds");
    }

    private void wireEvents() {
        testButton.addActionListener(e -> runTest());
        clearButton.addActionListener(e -> {
            setInitialOutput();
            setStatus("READY", "Ready to test", MUTED, new Color(53, 67, 86));
        });
        hostField.addActionListener(e -> runTest());
        portField.addActionListener(e -> runTest());
        timeoutField.addActionListener(e -> runTest());
    }

    private void runTest() {
        final String host = hostField.getText().trim();
        final String portText = portField.getText().trim();
        final String timeoutText = timeoutField.getText().trim();
        final int port;
        final int timeout;

        String validationProblem = validateHost(host);
        if (validationProblem != null) {
            showValidationFailure("Invalid hostname or IP address", validationProblem, "HOST_VALIDATION");
            return;
        }
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                showValidationFailure("Invalid TCP port", "Port must be between 1 and 65535.", "PORT_VALIDATION");
                return;
            }
        } catch (NumberFormatException ex) {
            showValidationFailure("Invalid TCP port", "Enter a whole-number port between 1 and 65535.", "PORT_VALIDATION");
            return;
        }
        try {
            timeout = Integer.parseInt(timeoutText);
            if (timeout < 100 || timeout > 120000) {
                showValidationFailure("Invalid timeout", "Timeout must be between 100 and 120000 milliseconds.", "TIMEOUT_VALIDATION");
                return;
            }
        } catch (NumberFormatException ex) {
            showValidationFailure("Invalid timeout", "Enter a whole-number timeout between 100 and 120000 milliseconds.", "TIMEOUT_VALIDATION");
            return;
        }

        setBusy(true);
        setStatus("TESTING", "Resolving host and testing TCP port…", new Color(255, 208, 122), new Color(86, 65, 35));
        userOutput.setText("Working…\n\nResolving " + host + " and attempting a TCP connection on port " + port + ".");
        diagnosticsOutput.setText("Test started: " + now() + "\nHost: " + host + "\nPort: " + port
                + "\nTCP connect timeout: " + timeout + " ms\n\nWaiting for DNS resolution and socket result…");

        SwingWorker<TestResult, Void> worker = new SwingWorker<>() {
            @Override protected TestResult doInBackground() {
                return performTest(host, port, timeout);
            }
            @Override protected void done() {
                setBusy(false);
                try {
                    TestResult r = get();
                    userOutput.setText(r.userMessage);
                    diagnosticsOutput.setText(r.diagnostics);
                    if (r.success) {
                        setStatus("SUCCESS", "TCP connection established", ACCENT, new Color(25, 76, 67));
                    } else {
                        setStatus("FAILED", r.shortTitle, DANGER, new Color(88, 42, 52));
                    }
                    scheduleDemoCapture();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    showUnexpectedFailure(cause);
                }
            }
        };
        worker.execute();
    }

    private String validateHost(String host) {
        if (host == null || host.isBlank()) return "Hostname cannot be empty.";
        if (host.length() > 253) return "Hostname is too long (maximum 253 characters).";
        if (host.chars().anyMatch(Character::isWhitespace)) return "Hostname cannot contain spaces or whitespace.";
        if (host.contains(":") || host.startsWith("[")) {
            String literal = host.startsWith("[") && host.endsWith("]")
                    ? host.substring(1, host.length() - 1) : host;
            if (!literal.matches("[0-9a-fA-F:.%]+") || !literal.contains(":"))
                return "Enter a valid IPv6 literal, hostname, or IPv4 address.";
            try {
                if (!(InetAddress.getByName(literal) instanceof Inet6Address))
                    return "The supplied address is not a valid IPv6 literal.";
            } catch (UnknownHostException ex) {
                return "The supplied IPv6 literal is not valid on this system.";
            }
            return null;
        }
        String candidate = host.endsWith(".") ? host.substring(0, host.length() - 1) : host;
        if (candidate.isEmpty()) return "Enter a valid hostname or IP address.";
        if (candidate.matches("[0-9.]+")) {
            String[] octets = candidate.split("\\.", -1);
            if (octets.length != 4) return "IPv4 addresses must contain four octets.";
            for (String octet : octets) {
                if (octet.isEmpty() || octet.length() > 3) return "The IPv4 address is not valid.";
                try {
                    int value = Integer.parseInt(octet);
                    if (value > 255) return "Each IPv4 octet must be between 0 and 255.";
                } catch (NumberFormatException ex) {
                    return "The IPv4 address is not valid.";
                }
            }
            return null;
        }
        try {
            String ascii = IDN.toASCII(candidate, IDN.USE_STD3_ASCII_RULES);
            if (ascii.length() > 253) return "Hostname is too long after internationalized-name conversion.";
            for (String label : ascii.split("\\.")) {
                if (label.isEmpty() || label.length() > 63 || label.startsWith("-") || label.endsWith("-"))
                    return "Hostname labels must be 1–63 characters and cannot start or end with a hyphen.";
            }
        } catch (IllegalArgumentException ex) {
            return "Hostname contains invalid characters.";
        }
        return null;
    }

    private TestResult performTest(String host, int port, int timeout) {
        long started = System.nanoTime();
        List<String> ips = new ArrayList<>();
        StringBuilder diagnostic = new StringBuilder();
        diagnostic.append("Timestamp: ").append(now()).append('\n')
                .append("Host input: ").append(host).append('\n')
                .append("TCP port: ").append(port).append('\n')
                .append("Configured connect timeout: ").append(timeout).append(" ms\n")
                .append("Protocol: TCP\n\n");
        try {
            String lookupHost = host.startsWith("[") && host.endsWith("]")
                    ? host.substring(1, host.length() - 1) : host;
            long dnsStarted = System.nanoTime();
            InetAddress[] addresses = InetAddress.getAllByName(lookupHost);
            long dnsElapsed = elapsedMs(dnsStarted);
            for (InetAddress address : addresses) ips.add(address.getHostAddress());
            diagnostic.append("DNS resolution: SUCCESS ( ").append(dnsElapsed).append(" ms )\nResolved addresses:\n");
            for (int i = 0; i < ips.size(); i++) diagnostic.append("  ").append(i + 1).append(". ").append(ips.get(i)).append('\n');

            IOException bestFailure = null;
            String connectedAddress = null;
            long connectStarted = System.nanoTime();
            long connectDeadline = connectStarted + timeout * 1_000_000L;
            for (InetAddress address : addresses) {
                long remainingNanos = connectDeadline - System.nanoTime();
                if (remainingNanos <= 0) {
                    bestFailure = new SocketTimeoutException("TCP connection attempts exceeded the configured total timeout of " + timeout + " ms");
                    break;
                }
                int remainingMillis = (int) Math.max(1L, Math.min(Integer.MAX_VALUE, (remainingNanos + 999_999L) / 1_000_000L));
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(address, port), remainingMillis);
                    connectedAddress = address.getHostAddress();
                    break;
                } catch (IOException ex) {
                    if (bestFailure == null || failureRank(ex) > failureRank(bestFailure)) bestFailure = ex;
                }
            }
            long elapsed = elapsedMs(started);
            long connectElapsed = elapsedMs(connectStarted);
            if (connectedAddress != null) {
                diagnostic.append("\nTCP connect: SUCCESS\nConnected address: ").append(connectedAddress)
                        .append('\n').append("TCP connection elapsed: ").append(connectElapsed).append(" ms\n")
                        .append("Total elapsed (including DNS): ").append(elapsed).append(" ms\n")
                        .append("Socket closed automatically by try-with-resources.");
                String user = "SUCCESS — TCP connection established\n\n"
                        + "The host resolved successfully and accepted a TCP connection.\n\n"
                        + "Destination: " + host + ":" + port + "\n"
                        + "Connected IP: " + connectedAddress + "\n"
                        + "TCP connect time: " + connectElapsed + " ms\n"
                        + "Total elapsed time: " + elapsed + " ms\n\n"
                        + "The test socket has been closed. No application data was sent.";
                return new TestResult(true, "TCP connection established", user, diagnostic.toString());
            }
            if (bestFailure != null) throw bestFailure;
            throw new IOException("No resolved address could be tested.");
        } catch (UnknownHostException ex) {
            long elapsed = elapsedMs(started);
            diagnostic.append("DNS resolution: FAILED\nException: ").append(ex.getClass().getSimpleName())
                    .append("\nMessage: ").append(safeMessage(ex)).append("\nElapsed time: ").append(elapsed).append(" ms");
            return failed("Host not found", "The hostname could not be resolved. Check the spelling and DNS/network settings.", diagnostic, ex);
        } catch (SocketTimeoutException ex) {
            long elapsed = elapsedMs(started);
            diagnostic.append("\nTCP connect: TIMEOUT\nException: ").append(ex.getClass().getName())
                    .append("\nMessage: ").append(safeMessage(ex)).append("\nElapsed time: ").append(elapsed).append(" ms")
                    .append("\nConfigured timeout: ").append(timeout).append(" ms");
            return failed("Connection timed out", "The destination did not complete the TCP handshake within " + timeout
                    + " ms. The service may be slow, filtered, or unreachable.", diagnostic, ex);
        } catch (NoRouteToHostException ex) {
            long elapsed = elapsedMs(started);
            diagnostic.append("\nTCP connect: NO ROUTE TO HOST\nException: ").append(ex.getClass().getName())
                    .append("\nMessage: ").append(safeMessage(ex)).append("\nElapsed time: ").append(elapsed).append(" ms");
            return failed("No route to host", "Your device cannot find a network route to the destination. Check routing, connectivity, or firewall rules.", diagnostic, ex);
        } catch (ConnectException ex) {
            long elapsed = elapsedMs(started);
            diagnostic.append("\nTCP connect: REFUSED OR FAILED\nException: ").append(ex.getClass().getName())
                    .append("\nMessage: ").append(safeMessage(ex)).append("\nElapsed time: ").append(elapsed).append(" ms");
            return failed("Connection refused", "The host was reached, but the TCP port refused the connection. Verify the port and whether a service is listening.", diagnostic, ex);
        } catch (SocketException ex) {
            long elapsed = elapsedMs(started);
            diagnostic.append("\nTCP socket error\nException: ").append(ex.getClass().getName())
                    .append("\nMessage: ").append(safeMessage(ex)).append("\nElapsed time: ").append(elapsed).append(" ms");
            return failed("Socket error", "The operating system reported a socket/network error while testing the destination.", diagnostic, ex);
        } catch (SecurityException ex) {
            long elapsed = elapsedMs(started);
            diagnostic.append("\nPermission failure\nException: ").append(ex.getClass().getName())
                    .append("\nMessage: ").append(safeMessage(ex)).append("\nElapsed time: ").append(elapsed).append(" ms");
            return failed("Permission denied", "The application is not permitted to perform this network operation.", diagnostic, ex);
        } catch (IOException ex) {
            long elapsed = elapsedMs(started);
            diagnostic.append("\nI/O failure\nException: ").append(ex.getClass().getName())
                    .append("\nMessage: ").append(safeMessage(ex)).append("\nElapsed time: ").append(elapsed).append(" ms");
            return failed("Network I/O error", "The connection test failed because of a network or socket I/O problem.", diagnostic, ex);
        }
    }

    private int failureRank(IOException ex) {
        if (ex instanceof SocketTimeoutException) return 5;
        if (ex instanceof NoRouteToHostException) return 4;
        if (ex instanceof ConnectException) return 3;
        if (ex instanceof SocketException) return 2;
        return 1;
    }

    private TestResult failed(String title, String explanation, StringBuilder diagnostic, Exception ex) {
        diagnostic.append("\n\nDeveloper detail: ").append(ex.getClass().getName())
                .append(" — ").append(safeMessage(ex));
        return new TestResult(false, title, "FAILED — " + title + "\n\n" + explanation
                + "\n\nReview the developer diagnostics for the exception type and details.", diagnostic.toString());
    }

    private void showValidationFailure(String title, String explanation, String code) {
        setBusy(false);
        setStatus("INVALID INPUT", title, DANGER, new Color(88, 42, 52));
        userOutput.setText("INPUT ERROR — " + title + "\n\n" + explanation
                + "\n\nCorrect the value and run the test again.");
        diagnosticsOutput.setText("Timestamp: " + now() + "\nValidation: FAILED\nDiagnostic code: " + code
                + "\nNo DNS lookup or TCP connection was attempted.");
        scheduleDemoCapture();
    }

    private void showUnexpectedFailure(Throwable ex) {
        setStatus("ERROR", "Unexpected test error", DANGER, new Color(88, 42, 52));
        userOutput.setText("The test could not be completed because of an unexpected application error.");
        diagnosticsOutput.setText("Exception: " + ex.getClass().getName() + "\nMessage: " + safeMessage(ex));
        scheduleDemoCapture();
    }

    private void scheduleDemoCapture() {
        if (captureFile == null || captureFile.isBlank()) return;
        Timer timer = new Timer(500, e -> {
            try {
                JComponent view = (JComponent) getContentPane();
                BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                view.printAll(graphics);
                graphics.dispose();
                File destination = new File(captureFile);
                File parent = destination.getAbsoluteFile().getParentFile();
                if (parent != null) parent.mkdirs();
                ImageIO.write(image, "png", destination);
            } catch (Exception ex) {
                System.err.println("Unable to capture demo screenshot: " + ex.getMessage());
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void setBusy(boolean busy) {
        testButton.setEnabled(!busy);
        clearButton.setEnabled(!busy);
        progressBar.setVisible(busy);
    }

    private void setStatus(String badge, String title, Color foreground, Color background) {
        statusPill.setText(" " + badge + " ");
        statusPill.setForeground(foreground);
        statusPill.setBackground(background);
        resultTitle.setText(title);
    }

    private static String safeMessage(Throwable ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? "(no message supplied)" : ex.getMessage();
    }

    private static long elapsedMs(long start) {
        return (System.nanoTime() - start) / 1_000_000L;
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(new Date());
    }

    private static final class TestResult {
        final boolean success;
        final String shortTitle;
        final String userMessage;
        final String diagnostics;
        TestResult(boolean success, String shortTitle, String userMessage, String diagnostics) {
            this.success = success;
            this.shortTitle = shortTitle;
            this.userMessage = userMessage;
            this.diagnostics = diagnostics;
        }
    }

    public static void main(String[] args) {
        String demo = null;
        String capture = null;
        for (int i = 0; i < args.length; i++) {
            if ("--demo-scenario".equals(args[i]) && i + 1 < args.length) demo = args[++i];
            else if ("--capture-file".equals(args[i]) && i + 1 < args.length) capture = args[++i];
        }
        final String scenario = demo;
        final String capturePath = capture;
        SwingUtilities.invokeLater(() -> {
            NetworkConnectionTester app = new NetworkConnectionTester();
            app.captureFile = capturePath;
            app.setVisible(true);
            if (scenario != null) {
                switch (scenario) {
                    case "invalid-port" -> {
                        app.hostField.setText("127.0.0.1"); app.portField.setText("70000"); app.timeoutField.setText("3000");
                    }
                    case "dns-failure" -> {
                        app.hostField.setText("no-such-hostname.invalid"); app.portField.setText("443"); app.timeoutField.setText("3000");
                    }
                    case "connection-refused" -> {
                        app.hostField.setText("127.0.0.1"); app.portField.setText("1"); app.timeoutField.setText("3000");
                    }
                    case "success" -> {
                        app.hostField.setText("127.0.0.1"); app.portField.setText("18888"); app.timeoutField.setText("3000");
                    }
                    default -> { /* Normal interactive mode. */ }
                }
                app.hostField.setCaretPosition(0);
                app.testButton.requestFocusInWindow();
                SwingUtilities.invokeLater(app::runTest);
            }
        });
    }
}
