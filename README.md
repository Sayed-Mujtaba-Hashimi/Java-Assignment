# Network Models, Protocols, and Hostname Lookup

## 1. OSI model (7 layers)

```text
+-------------------------------+
| 7. Application                | User-facing network services
+-------------------------------+
| 6. Presentation               | Data format, encryption, compression
+-------------------------------+
| 5. Session                    | Starts, manages, ends sessions
+-------------------------------+
| 4. Transport                  | End-to-end delivery, ports, TCP/UDP
+-------------------------------+
| 3. Network                    | Logical addressing and routing (IP)
+-------------------------------+
| 2. Data Link                  | Frames, MAC addresses, local-link delivery
+-------------------------------+
| 1. Physical                   | Signals and bits over the medium
+-------------------------------+
```

## 2. TCP/IP model (4 layers)

```text
+-------------------------------+
| Application                   | HTTP, HTTPS, DNS, SMTP, POP3, IMAP
+-------------------------------+
| Transport                     | TCP and UDP; port numbers
+-------------------------------+
| Internet                      | IP addressing and routing
+-------------------------------+
| Network Access / Link         | Frames, MAC addresses, physical media
+-------------------------------+
```

### OSI-to-TCP/IP mapping

| OSI | TCP/IP |
|---|---|
| Application (7), Presentation (6), Session (5) | Application |
| Transport (4) | Transport |
| Network (3) | Internet |
| Data Link (2), Physical (1) | Network Access / Link |

## 3. Protocol purposes and default ports

| Protocol | Application purpose | Default port |
|---|---|---|
| HTTP | Transfers web pages/resources without TLS encryption by itself | TCP 80 |
| HTTPS | HTTP protected with TLS | TCP 443 |
| DNS | Resolves domain names and provides domain information | UDP 53 and TCP 53 |
| SMTP | Sends and relays outgoing email | TCP 25 (mail transfer); TCP 587 commonly for message submission |
| POP3 | Retrieves/downloads email from a mail server | TCP 110 |
| IMAP | Accesses and synchronizes email while keeping it on the server | TCP 143 |

Encrypted variants commonly use POP3S TCP 995 and IMAPS TCP 993. DNS uses both UDP and TCP port 53.

## 4. Java program

`HostIPLookup.java` accepts a hostname, uses `InetAddress.getAllByName()` to print all returned IP addresses, and handles empty input and `UnknownHostException`.

Compile and run in Windows PowerShell:

```powershell
javac HostIPLookup.java
java HostIPLookup
```

Example hostname: `google.com`. Actual returned addresses can vary by DNS resolver, location, and time.

## 5. Required command outputs — capture on your computer

Run these in Windows PowerShell or Command Prompt and take screenshots showing the command and output:

```powershell
ipconfig /all
nslookup google.com
tracert -d google.com
```

Use traceroute/tracert only where permitted by your instructor or network administrator. Some networks block or rate-limit its responses.

**Do not submit fabricated outputs.** Your IP configuration, DNS server, and route are specific to your computer. Add your screenshots to a `Command-Outputs` folder:
- `ipconfig.png`
- `nslookup.png`
- `tracert.png`

See [Network_Layers_Explanation.md](Network_Layers_Explanation.md) for the one-page explanation connecting the command outputs to network layers.
