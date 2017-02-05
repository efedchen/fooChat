import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server implements Runnable {

    private DatagramSocket socket;
    private Thread run, manage, send, receive;
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
                     String str = new String(packet.getData());
                     System.out.println(str);
                 }
             }
        };
        receive.start();
    }
}
