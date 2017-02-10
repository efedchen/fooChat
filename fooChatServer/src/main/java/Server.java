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
//                    String s = new String (packet.getData());
//                    System.out.println(s);
                    socket.send(packet);
                } catch (IOException e) {
                    System.out.println("Doesn't sent to " + address + ":" + port);
                }
            }
        };
        send.start();
    }
    private void send (String message, InetAddress address, int port){
        message +="/end/";
//        System.out.println("method send, message " + message + " length " + message.length());
        send(message.getBytes(), address, port);
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
//                     String st = new String(packet.getData());
//                     System.out.println(st);
                 }
             }
        };
        receive.start();
    }

    private void process(DatagramPacket packet){
        String str = new String(packet.getData());
        if (str.startsWith("/connect/")){
            ClientData cd = new ClientData(str.substring(9,str.length()), createId(), packet.getPort(), packet.getAddress());
//            clients.add(new ClientData(str.substring(9,str.length()), createId(), packet.getPort(), packet.getAddress()));
            clients.add(cd);
            System.out.println("New connect from : " + str.substring(9,str.length()) +", address: " +
                    packet.getAddress() + ":" + packet.getPort()+ ". Identifier: " + idCounter + " id" + cd.getId());
            String ID = "/connect/"+idCounter;
//            System.out.println(ID + " length "+ ID.length());
            send(ID, packet.getAddress(), packet.getPort());
        }else if (str.startsWith("/broadcast/")){
            System.out.println("New broadcasting message: " + str + " length " + str.length());
            broadcasting(str);
        }else {
            System.out.println(str);
        }
    }

    private void broadcasting(String message){
        for (int i = 0; i < clients.size(); i++) {
            ClientData clientData = clients.get(i);
            send(message.getBytes(), clientData.address, clientData.getPort());

            String s = clientData.getName();
            System.out.println(s + " " + s.length());
        }
    }

    private long createId(){
        return idCounter.incrementAndGet();
    }
}
