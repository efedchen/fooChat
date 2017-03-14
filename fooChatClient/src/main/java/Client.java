import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import net.*;

import static java.awt.GridBagConstraints.RELATIVE;

public class Client extends JFrame implements Runnable {
 	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
    private JTextArea history;
    private JTextArea onlineUsersArea;
    private JTextField txtMessage;
    private Thread listen, run;
    private boolean connected = false;
    private boolean running = false;
    private ArrayList<String> onlineUsers;
    private Net net = null;


 	public Client(String name, String address, int port) {
        net = new Net(name, address, port);

        connected = net.openConnection(address);
        if (!connected) {
            System.out.println("Connection failed..");
            console("Connection failed..");
        }
        createWindow();
        console("Attempting a connection: " + address + ":" + port + ", user: " + name + "\n" );

        String connection = "/connect/" + name + "/end/";

        //sends handshake to the server
        net.send(connection.getBytes() );

		running = true;
		run = new Thread(this,"Running");
		run.start();
    }

     public void run(){
         listen();
     }

 	private void createWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		setTitle("fooChat@Messenger Client");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(980, 550);
 		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{16, 557, 7};
 		gbl_contentPane.rowHeights = new int[]{25, 485, 40};
 		contentPane.setLayout(gbl_contentPane);
 		
 		history = new JTextArea();
		history.setEditable(false);
		history.setFont(new Font("Arial", Font.PLAIN, 14));
		JScrollPane scroll = new JScrollPane(history);
 		GridBagConstraints scrollConstraints = new GridBagConstraints();
		scrollConstraints.insets = new Insets(0, 0, 5, 5);
 		scrollConstraints.fill = GridBagConstraints.BOTH;
 		scrollConstraints.gridx = 0;
 		scrollConstraints.gridy = 0;
		scrollConstraints.gridwidth = 3;
		scrollConstraints.gridheight = 2;
		scrollConstraints.weightx = 1;
		scrollConstraints.weighty = 1;
		scrollConstraints.insets = new Insets(0, 5, 0, 0);
 		contentPane.add(scroll, scrollConstraints);

		onlineUsersArea = new JTextArea();
		onlineUsersArea.setEditable(false);
		onlineUsersArea.setFont(new Font("Arial", Font.PLAIN, 14));
		JScrollPane onlineUsersScroll = new JScrollPane(onlineUsersArea);
		GridBagConstraints onlineUsersScrollConstraints = new GridBagConstraints();
		onlineUsersScrollConstraints.insets = new Insets(0, 0, 5, 5);
		onlineUsersScrollConstraints.fill = GridBagConstraints.BOTH;
		onlineUsersScrollConstraints.gridx = RELATIVE;
		onlineUsersScrollConstraints.gridy = 0;
		onlineUsersScrollConstraints.gridwidth = 1;
		onlineUsersScrollConstraints.gridheight = 4;
		onlineUsersScrollConstraints.weightx = 1;
		onlineUsersScrollConstraints.weighty = 1;
		contentPane.add(onlineUsersScroll, onlineUsersScrollConstraints);


		txtMessage = new JTextField();
		txtMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					send(txtMessage.getText(),true);
				}
			}
		});
		GridBagConstraints gbc_txtMessage = new GridBagConstraints();
		gbc_txtMessage.insets = new Insets(0, 0, 0, 5);
		gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMessage.gridx = 0;
		gbc_txtMessage.gridy = 2;
		gbc_txtMessage.gridwidth = 2;
		gbc_txtMessage.weightx = 1;
		contentPane.add(txtMessage, gbc_txtMessage);
		txtMessage.setColumns(10);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send(txtMessage.getText(),true);
			}
		});
		GridBagConstraints gbc_btnSend = new GridBagConstraints();
		gbc_btnSend.insets = new Insets(0, 0, 0, 5);
		gbc_btnSend.gridx = 2;
		gbc_btnSend.gridy = 2;
		gbc_btnSend.weightx = 0;
		gbc_btnSend.weighty = 0;
		contentPane.add(btnSend, gbc_btnSend);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				String disconnect = "/disconnect/" + net.getId() + "/end/";
				net.send(disconnect.getBytes());
				System.out.println("closed");
				running = false;
				net.close();
			}
		});

		setVisible(true);
 		
 		txtMessage.requestFocusInWindow();
 	}
 	
 	public void send(String message, boolean bool) {
 		if (message.equals("")) return;
 		if(bool){
			message = net.getName() + ": " + message;
			message = "/broadcast/" + message + "/end/";
		}

		//sends data to the server
		net.send(message.getBytes());

		txtMessage.setText("");
 	}

 	public void listen() {
        listen = new Thread("Listen") {
            public void run() {
                while (running) {
                	int k=0;
                    String message = net.receive();
					if(message.startsWith("/connect/")){

						long id = Long.parseLong(message.split("/connect/|/end/")[1]);
						net.setId(id);

                        System.out.println("Successfully connected to server! Id: " + net.getId());
                        console("Successfully connected to server! Id: " + net.getId());
                    }else if(message.startsWith("/broadcast/")){

						//allows user to send messages that starts with /broadcast/
						String text = message.substring(11);
						text = text.split("/end/")[0];

						console(text);
					} else if(message.startsWith("/ping/")){

						message = "/ping/"+net.getId()+"/end/";
						net.send(message.getBytes());
					} else if(message.startsWith("/onlineU/")){
						onlineUsers.add(message.split("/onlineU/|/end/")[1]);
						//TODO: print out online useres
					}
                }
            }
        }; listen.start();
    }

    public void console(String message) {
        history.setCaretPosition(history.getDocument().getLength());
        history.append(message + "\n\r");
    }
 }