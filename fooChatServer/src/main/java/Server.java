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
    private List<Long> clientResponse = new ArrayList<Long>();
    private DatagramSocket socket;
    private Thread run, manage, send, receive;
    private static AtomicLong idCounter = new AtomicLong();
    private final int Max_ATTEMPTS = 5;

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
        receive();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        manageClients();
    }

    private void manageClients(){
        manage = new Thread("Manage"){
            public void run(){
                while (running){
                broadcasting("/ping/");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < clients.size(); i++) {
                        ClientData c = clients.get(i);
                        if(!clientResponse.contains(c.getId())){
                            if(c.attempt >= Max_ATTEMPTS){
                                disconnect(c.getId(),false);
                            }else{
                                c.attempt++;
                            }
                        } else{
                            clientResponse.remove(c.getId());
                            c.attempt = 0;
                        }
                    }
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
                    System.out.println("Doesn't sent to " + address + ":" + port);
                }
            }
        };
        send.start();
    }
    private void send (String message, InetAddress address, int port){
        message +="/end/";
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
                 }
             }
        };
        receive.start();
    }

    private void process(DatagramPacket packet){
        String str = new String(packet.getData());
        if (str.startsWith("/connect/")){
            ClientData cd = new ClientData(str.substring(9,str.length()), createId(), packet.getPort(), packet.getAddress());
            clients.add(cd);
            System.out.println("New connect from : " + str.substring(9,str.length()) +", address: " +
                    packet.getAddress() + ":" + packet.getPort()+ ". Identifier: " + cd.getId());
            String ID = "/connect/"+idCounter;
            send(ID, packet.getAddress(), packet.getPort());
        }else if (str.startsWith("/broadcast/")){
            System.out.println("New broadcasting message: " + str + " length " + str.length());
            broadcasting(str);
        }else if(str.startsWith("/disconnect/")){
            String id = str.split("/disconnect/|/end/")[1];
            disconnect(Long.parseLong(id), true);
        }else if(str.startsWith("/ping/")){
            clientResponse.add(Long.parseLong(str.split("/ping/|/end/")[1]));
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

    private void disconnect(long id, boolean status){
        ClientData c = null;
        for (int i = 0; i < clients.size(); i++){
            if(clients.get(i).getId() == id){
                c = clients.get(i);
                clients.remove(i);
                break;
            }
        }
        String message;
        if(status){
            message = "Client " + c.getName() + "(" + c.getId() + ")" + c.address.toString() + ":" + " disconnected";
        }else {
            message = "Client " + c.getName() + "(" + c.getId() + ")" + c.address.toString() + ":" + " disconnected due to time out";
        }
        System.out.println(message);
    }

    private long createId(){
        return idCounter.incrementAndGet();
    }
}
