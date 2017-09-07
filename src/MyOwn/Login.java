package MyOwn;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Login extends JFrame {

  private JPanel contentPane;
  private JTextField txtName;
  private JTextField txtAddress;
  private JLabel lblIpAddress;
  private JLabel lblPort;
  private JTextField txtPort;
  private MyClient client;


  /**
   * Create the frame.
   */
  public Login(MyClient client) {

    this.client = client;
    // sets login window settings
    setTitle("login");
    setResizable(false);
    setSize(300, 380);
    setLocationRelativeTo(null);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);

    txtName = new JTextField();
    txtName.setBounds(67, 73, 165, 28);
    contentPane.add(txtName);
    txtName.setColumns(10);

    JLabel lblName = new JLabel("Name:");
    lblName.setBounds(125, 57, 49, 16);
    contentPane.add(lblName);

    txtAddress = new JTextField();
    txtAddress.setBounds(67, 129, 165, 28);
    contentPane.add(txtAddress);
    txtAddress.setColumns(10);

    lblIpAddress = new JLabel("IP Address:");
    lblIpAddress.setBounds(110, 113, 79, 16);
    contentPane.add(lblIpAddress);

    lblPort = new JLabel("Port:");
    lblPort.setBounds(128, 174, 43, 16);
    contentPane.add(lblPort);

    txtPort = new JTextField();
    txtPort.setColumns(10);
    txtPort.setBounds(67, 190, 165, 28);
    contentPane.add(txtPort);

    JButton btnLogin = new JButton("Login");
    btnLogin.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String name = txtName.getText();
        String address = txtAddress.getText();
        int port = Integer.parseInt(txtPort.getText());
        login(name, address, port);
      }
    });
    btnLogin.setBounds(91, 310, 117, 29);
    contentPane.add(btnLogin);

    // sets the text fields default text.
    // makes cursor focus name field.
    txtName.requestFocusInWindow();
    txtPort.setText("6789");
    txtAddress.setText("25.35.67.159");
    
    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
       we.getWindow().dispose();
      }
    });
  }

  /**
   * This is called when login is pressed.
   * It takes the name, address and port, attempts to connect.
   * It assumes the server will return hard coded responses.
   */
  private void login(String name, String address, int port) {

    // this is only a client sided check at the moment.
    if (name.isEmpty() || name.contains(" ")) {
      JOptionPane.showConfirmDialog(this, "Invalid name, no space, no empty.");
    } else if (client.isConnected()) {
      JOptionPane.showConfirmDialog(this, "Already connected.", "Fail", 
          JOptionPane.OK_OPTION);
    } else {
      try {
        if (InetAddress.getByName(address).isReachable(600)) {
          Socket connection = new Socket(InetAddress.getByName(address), port);
          ObjectOutputStream tempOut = new ObjectOutputStream(connection.getOutputStream());
          ObjectInputStream tempIn = new ObjectInputStream(connection.getInputStream());
       
          // This is sent to the server to validate the name.
          // If you set this up to send a 'Message' object.
          //The Message object is sent.
          // If you then set up the server to read in a Message object
          // The message object will read in fine if connecting locally.
          // If Message is sent over hamachi/LAN, the server never receives
          // the Message Object.
          tempOut.writeObject("Connecting:" + name);
          tempOut.flush();
          
          /*// Send Message Code.
           * tempOut.writeObject(new Message(name + " is trying to connect.", name, "REQUEST");
           * tempOut.flush();
           */

          // We then wait for the server to send it's first response.
          String accept = (String) tempIn.readObject();

          while (accept == null) {
            accept = (String) tempIn.readObject();
          }

          System.out.println("accept is: " + accept);
          // Once input is received, it can only be one of 2 things:
          if (accept.equals(name + "accepted")) {
            // Create the client, start it's running.
            System.out.println(name + connection + tempOut + tempIn);
            if(client.connect(name, connection, tempOut, tempIn)) {
              System.out.println("Start running begin");
              client.startRunning();
              this.dispose();
            }
            else {
              JOptionPane.showConfirmDialog(this, "Connection failed. Try again.", "Fail", 
                  JOptionPane.OK_OPTION);
            }

          } else if (accept.equals("nameistaken")) {
            JOptionPane.showConfirmDialog(this, "Invalid name. Try again.", "Name Error", 
                JOptionPane.OK_OPTION);
            tempIn.close();
            tempOut.close();
            connection.close();
          }
        } else {
          JOptionPane.showConfirmDialog(this, "Invalid IP. Try again.", "IP Error", 
              JOptionPane.OK_OPTION);
        }
      } catch (UnknownHostException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
