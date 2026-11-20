# How Command-Line Network Tools Relate to Network Layers

The OSI model and TCP/IP model describe network communication in layers. Each layer has a different responsibility, and network troubleshooting commands reveal information about more than one layer.

**`ipconfig` (Windows) / `ip addr` (Linux)** displays information about network interfaces. The interface name and MAC address relate to the OSI **Data Link (Layer 2)** layer, while IPv4/IPv6 addresses and subnet information relate to the **Network (Layer 3)** layer. The default gateway is the next-hop router used to reach other networks. These commands show local interface configuration; they do not, by themselves, prove that the internet is reachable.

**`nslookup`** asks a DNS resolver to translate a hostname into IP address records. DNS is an **Application-layer** protocol (OSI Layer 7; TCP/IP Application layer). DNS queries commonly use UDP port 53; TCP port 53 is also used in cases such as large responses and zone transfers. The returned IP address is then used by the **Network layer** to address packets. `nslookup` tests name resolution, not whether the destination web service is running.

**`tracert` (Windows) / `traceroute` (Linux)** discovers routers (hops) toward a destination. It relies on the IP packet's **Time To Live (TTL)** field in IPv4 (called Hop Limit in IPv6). Routers decrease this value; when it reaches zero, a router may return an ICMP Time Exceeded message. This connects the command to the OSI **Network layer (Layer 3)** and TCP/IP Internet layer. Asterisks do not necessarily mean a router is broken: a router may filter or rate-limit probe responses.

Together, the commands provide complementary evidence. `ipconfig`/`ip addr` checks local interface addressing, `nslookup` checks application-level DNS name resolution, and `tracert`/`traceroute` investigates Layer 3 routing. None alone proves that HTTP or HTTPS is working; a separate connection test to the relevant service and port would be needed.
