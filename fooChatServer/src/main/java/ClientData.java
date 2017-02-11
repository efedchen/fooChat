import java.net.InetAddress;

public class ClientData {

    private String name;
    private final long id;
    private int port;
    public int attempt = 0;

    public InetAddress address;

    public ClientData(String name, long id, int port, InetAddress address) {
        this.name = name;
        this.id = id;
        this.port = port;
        this.address = address;
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getName() {

        return name;
    }

    public int getPort() {
        return port;
    }

    public long getId() {
        return id;
    }
}
