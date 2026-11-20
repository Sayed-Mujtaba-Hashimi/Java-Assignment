import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class HostIPLookup {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a hostname: ");
            String hostname = scanner.nextLine().trim();

            if (hostname.isEmpty()) {
                System.out.println("Error: Hostname cannot be empty.");
                return;
            }

            try {
                InetAddress[] addresses = InetAddress.getAllByName(hostname);
                System.out.println("\nIP addresses for " + hostname + ":");
                for (int i = 0; i < addresses.length; i++) {
                    System.out.println((i + 1) + ". " + addresses[i].getHostAddress());
                }
                System.out.println("\nTotal addresses found: " + addresses.length);
            } catch (UnknownHostException e) {
                System.out.println("Error: Unknown host '" + hostname
                        + "'. Check the hostname or your DNS/network connection.");
            }
        }
    }
}
