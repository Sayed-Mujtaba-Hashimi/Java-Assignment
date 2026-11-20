# Java Network Connection Tester

A Java Swing desktop utility that checks whether a TCP port on a host is reachable.
It resolves the supplied host, attempts a TCP socket connection, and displays both
plain-language results and technical diagnostics.

## Features

- Accepts a hostname, IPv4 address, or IPv6 literal.
- Validates the host, TCP port (`1`–`65535`), and timeout (`100`–`120000` ms).
- Resolves hostnames using `InetAddress.getAllByName()` and displays all returned IP addresses.
- Attempts a TCP connection using `Socket.connect(SocketAddress, timeout)`.
- Applies the configured timeout as a total TCP-connect time budget across resolved addresses. DNS lookup occurs before this timer starts and is governed by the operating system's resolver.
- Uses try-with-resources so each test socket is closed automatically.
- Runs the network work in a `SwingWorker` so the interface remains responsive.
- Separates user-friendly explanations from developer diagnostics.

## Exception handling

The program handles these cases separately:

- `UnknownHostException` — DNS could not resolve the hostname.
- `SocketTimeoutException` — the TCP connection did not complete before the configured timeout.
- `NoRouteToHostException` — the system has no route to the target.
- `ConnectException` — the target refused or could not establish the connection (commonly a closed port).
- `SocketException` — another socket-level problem occurred.
- `SecurityException` — the runtime denied the network operation.
- `IOException` — a general network I/O failure.

Input errors are caught before DNS resolution or socket creation.

## Layout and components

- `JFrame` provides the main window.
- `JPanel` sections organize the header, connection form, and output area.
- `BorderLayout` structures the main window and result cards.
- `GridBagLayout` aligns the hostname, port, and timeout fields.
- `GridLayout` places the two result panels side by side.
- `JTextField` collects the host, port, and timeout; `JButton` starts a test or clears output.
- `JTextArea` displays results inside `JScrollPane` components.
- `ActionListener` handles button and Enter-key events.

## Run from source

Requires JDK 17 or newer.

```bash
javac -d classes NetworkConnectionTester.java
java -cp classes NetworkConnectionTester
```

## Run the packaged JAR

```bash
java -jar NetworkConnectionTester.jar
```

## Screenshots included

1. `screenshots/01_invalid_port.png` — port `70000` is rejected by input validation; no DNS lookup or TCP attempt occurs.
2. `screenshots/02_dns_failure.png` — `no-such-hostname.invalid` produces `UnknownHostException`.
3. `screenshots/03_connection_refused.png` — `127.0.0.1:1` resolves but the local TCP port refuses the connection, producing `ConnectException`.
4. `screenshots/04_success_localhost.png` — successful TCP handshake to a temporary local test server (included as an additional verification screenshot).

The screenshots are captured from the actual Swing application. The success screenshot uses a local HTTP server only as a temporary listener; the tester itself sends no application payload.

## Re-create the failure screenshots

After compiling, the source includes optional demo arguments to prefill a scenario and save the visible application content as a PNG:

```bash
java -cp classes NetworkConnectionTester --demo-scenario invalid-port --capture-file screenshots/01_invalid_port.png
java -cp classes NetworkConnectionTester --demo-scenario dns-failure --capture-file screenshots/02_dns_failure.png
java -cp classes NetworkConnectionTester --demo-scenario connection-refused --capture-file screenshots/03_connection_refused.png
```

These flags are only for demonstrating the UI; normal use starts with the standard command above.

## Scope note

This program tests TCP reachability only. A successful connection does not prove that an application protocol (such as HTTP or SSH) is working, and it does not authenticate to the remote service. The configured timeout covers the TCP connection attempts, not DNS resolution.
