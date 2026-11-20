import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class NetworkInfo {
    public static void main(String[] args) {

        try {
            // Get local host information
            InetAddress localHost = InetAddress.getLocalHost();

            System.out.println("=== Local Network Information ===");
            System.out.println("Local host name: " +
                    localHost.getHostName());
            System.out.println("Local IP address: " +
                    localHost.getHostAddress());

            // Accept hostname from user
            try (Scanner scanner = new Scanner(System.in)) {

                System.out.print("\nEnter a hostname to resolve: ");
                String hostname = scanner.nextLine().trim();

                if (hostname.isEmpty()) {
                    System.out.println(
                            "Error: Hostname cannot be empty.");
                    return;
                }

                try {
                    // Resolve hostname into IP address
                    InetAddress resolvedHost = InetAddress.getByName(hostname);

                    System.out.println("Hostname: " + hostname);
                    System.out.println("Resolved IP address: " +
                            resolvedHost.getHostAddress());

                } catch (UnknownHostException e) {
                    System.out.println(
                            "Error: Could not resolve hostname '" +
                                    hostname +
                                    "'. Please check the name and try again.");
                }
            }

        } catch (UnknownHostException e) {
            System.out.println(
                    "Error: Could not determine the local host name " +
                            "or IP address.");
        }
    }
}