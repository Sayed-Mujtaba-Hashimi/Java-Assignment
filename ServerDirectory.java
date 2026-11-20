import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

class Server {
    private final String name;
    private final String host;
    private final int port;
    private final String protocol;

    public Server(String name, String host, int port, String protocol) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Server name cannot be empty.");
        if (host == null || host.trim().isEmpty())
            throw new IllegalArgumentException("Host cannot be empty.");
        if (port < 1 || port > 65535)
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        if (protocol == null || protocol.trim().isEmpty())
            throw new IllegalArgumentException("Protocol cannot be empty.");

        this.name = name.trim();
        this.host = host.trim();
        this.port = port;
        this.protocol = protocol.trim().toUpperCase();
    }

    public String getName() { return name; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getProtocol() { return protocol; }

    @Override
    public String toString() {
        return String.format("%-16s %-22s %-7d %s",
                name, host, port, protocol);
    }
}

public class ServerDirectory {
    private final ArrayList<Server> servers = new ArrayList<>();
    private final HashMap<String, Server> serversByName = new HashMap<>();

    public ServerDirectory() {
        addServer(new Server("Google DNS", "8.8.8.8", 53, "UDP"));
        addServer(new Server("Cloudflare DNS", "1.1.1.1", 53, "UDP"));
        addServer(new Server("Google Web", "google.com", 443, "HTTPS"));
        addServer(new Server("Example Web", "example.com", 80, "HTTP"));
        addServer(new Server("GitHub", "github.com", 443, "HTTPS"));
    }

    private void addServer(Server server) {
        String key = server.getName().toLowerCase();
        if (serversByName.containsKey(key))
            throw new IllegalArgumentException("Duplicate server name: " + server.getName());
        servers.add(server);
        serversByName.put(key, server);
    }

    private void printHeader() {
        System.out.printf("%-16s %-22s %-7s %s%n", "NAME", "HOST", "PORT", "PROTOCOL");
        System.out.println("------------------------------------------------------------");
    }

    private void displayAllServers() {
        System.out.println("\n--- All Servers ---");
        printHeader();
        for (Server server : servers) System.out.println(server);
    }

    private void searchByProtocol(String protocol) {
        if (protocol == null || protocol.trim().isEmpty()) {
            System.out.println("Error: Protocol cannot be empty.");
            return;
        }
        boolean found = false;
        System.out.println("\n--- Servers using " + protocol.trim().toUpperCase() + " ---");
        printHeader();
        for (Server server : servers) {
            if (server.getProtocol().equalsIgnoreCase(protocol.trim())) {
                System.out.println(server);
                found = true;
            }
        }
        if (!found) System.out.println("No servers found for protocol: " + protocol.trim());
    }

    private void findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: Server name cannot be empty.");
            return;
        }
        Server server = serversByName.get(name.trim().toLowerCase());
        if (server == null) {
            System.out.println("No server found with name: " + name.trim());
        } else {
            System.out.println("\nServer found:");
            printHeader();
            System.out.println(server);
        }
    }

    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;
            System.out.println("================================");
            System.out.println("       SERVER DIRECTORY");
            System.out.println("================================");
            while (running) {
                System.out.println("\n1. Display all servers");
                System.out.println("2. Search servers by protocol");
                System.out.println("3. Find server by name");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1": displayAllServers(); break;
                    case "2":
                        System.out.print("Enter protocol (TCP/UDP/HTTP/HTTPS): ");
                        searchByProtocol(scanner.nextLine());
                        break;
                    case "3":
                        System.out.print("Enter server name: ");
                        findByName(scanner.nextLine());
                        break;
                    case "4":
                        running = false;
                        System.out.println("Exiting Server Directory. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please choose 1, 2, 3, or 4.");
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            new ServerDirectory().run();
        } catch (IllegalArgumentException e) {
            System.out.println("Unable to start Server Directory: " + e.getMessage());
        }
    }
}
