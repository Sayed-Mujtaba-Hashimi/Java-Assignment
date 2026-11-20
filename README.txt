NETWORK CLIENT - JAVA SWING

OVERVIEW
This is a desktop Network Client interface built with Java Swing. It demonstrates
hostname/IP and port validation, TCP/UDP selection, simulated connection and
disconnection, sending messages, and clearing a scrollable activity log.

IMPORTANT
The connection is simulated. The program does not create a real TCP or UDP socket
or contact the entered host. Sent messages and responses are written to the log
as demonstration events.

REQUIREMENTS
- Java JDK 17 or later

COMPILE
  javac NetworkClient.java

RUN (normal disconnected start)
  java NetworkClient

RUN (connected preview with sample log entries)
  java NetworkClient --demo

FEATURES
- Host validation accepts hostnames, IPv4 addresses, and IPv6 literals.
- Port validation accepts numeric values from 1 through 65535.
- Connect changes the status to Connected and disables the connection fields.
- Disconnect returns the status to Disconnected.
- Send logs the entered message and a simulated acknowledgement.
- Clear log empties the log area.

COMPONENT AND LAYOUT DESIGN
- JFrame: top-level application window.
- BorderLayout: divides the window into a header and main workspace.
- BorderLayout panels: organize the connection card and log/composer sections.
- GridBagLayout: aligns the host, port, and protocol input fields in one row.
- FlowLayout: arranges Connect and Disconnect buttons.
- JTextArea inside JScrollPane: displays a scrollable, timestamped activity log.
- JTextField, JComboBox, JButton, and JLabel: provide the inputs, actions, and status.
- ActionListeners: handle connect, disconnect, send, clear, and Enter-key actions.
