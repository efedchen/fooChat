import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Server implements Runnable {

    private List<ClientData> clients = new ArrayList<ClientData>();
    private DatagramSocket socket;
    private Thread run, manage, send, receive;
    private static AtomicLong idCounter = new AtomicLong();

    private int port;
    private boolean running;

    public Server(int port) {
        this.port = port;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        run = new Thread(this, "Server");
        run.start();
    }

    public void run() {
        running = true;
        System.out.println("Server started on port " + port);
        manageClients();
        receive();
    }

    public void manageClients(){
        manage = new Thread("Manage"){
            public void run(){
                while (running){
                    //managing
                }
            }
        };
        manage.start();
    }

    private void send(final byte [] data, final InetAddress address, final int port){
        send = new Thread("Send"){
            public void run(){
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    System.out.println("Doesnt sent to " + address + ":" + port);
                }
            }
        };
        send.start();
    }

    public void receive(){
        receive = new Thread("Receive"){
             public void run(){
                 while(running){
                    byte [] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                     try {
                         socket.receive(packet);
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     process(packet);
                     clients.add(new ClientData("Eugene", createId(), packet.getPort(), packet.getAddress()));
                     System.out.println(clients.get(0).address.toString() + ":" + clients.get(0).port); //do i need it?
                 }
             }
        };
        receive.start();
    }

    private void process(DatagramPacket packet){
        String str = new String(packet.getData());
        if (str.startsWith("/connect/")){
            clients.add(new ClientData(str.substring(9,str.length()), createId(), packet.getPort(), packet.getAddress()));
            System.out.println(str.substring(9,str.length())); //?? do i need it?
        }else if (str.startsWith("/broadcast/")){
            String message = str.substring(11,str.length());
            broadcasting(message);
        }else {
            System.out.println(str);
        }
    }

    private void broadcasting(String message){
        for (int i = 0; i < clients.size(); i++) {
            ClientData clientData = clients.get(i);
            send(message.getBytes(), clientData.address, clientData.port);
        }
    }

    private long createId(){
        return idCounter.getAndIncrement();
    }
}
