package MyOwn;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class MyClient implements Runnable {

  private String serverIP;
  private int serverPort;

  // client socket
  private Socket connection = null;

  // output stream
  private ObjectOutputStream output;
  //input stream
  private ObjectInputStream input;
  // client name
  private String name;
  // connection closed?
  private boolean isConnected;
  // the GUI
  private ClientWindow window;
  
  private Thread thread;

  public MyClient() {
    this.name = null;
    this.connection = null;
    this.output = null;
    this.input = null;
    this.isConnected = false;

    this.window = new ClientWindow(this);
    
    this.window.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        /** Can't use 'this' as a prefix here. 
         * this in this context is part of the WindowAdapter
         * object being initialised above.
         * What is happening here, A new class is being created from the
         * abstract WindowAdapter() class, upon initialising, we are also
         * overriding the necessary abstract method windowClosing at the same time.
         * This allows us to create an anonymous object of an abstract class
         * without extending or creating another class and/or object first.
         */
        if (isConnected) {
        disconnect();
        }
        while (isConnected);
        window.dispose();
      }
    });
    
    // At this point the constructor ends.
    // Once 'Login' is pressed from the login window.
    // This classes connect method is called and startRunning() thereafter.
  }

  // This class implements runnable, so the .start() here fires up run()
  public void startRunning() {
    (this.thread = new Thread(this)).start();
  }

  // This is where the beef of it happens.
  // This object is destroyed once the run() completes.
  @Override
  public void run() {
    // closed is set to false within the constructor.
    while (isConnected) {
      try {
        Object obj;
        if ((obj = this.input.readObject()) != null) {

          String message = (String) obj;
          // Checks if the message received from server is equal.
          if (message.startsWith("NewName:")) {
            this.window.addUser(message.split(":")[1]);
          } else if (message.startsWith("Rem:")) {
            this.window.removeUser(message.split(":")[1]);
          }  else if (message.startsWith("/quit")) {
            this.isConnected = false;
            this.disconnect();
            this.window.updateChat("Unexpected disconnection - Sever shutdown.\n\r");
          } else {
            this.window.updateChat(message);
          }
        }

      } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
      } catch (IOException e) {
        if (input == null) {
          this.window.updateChat("Server input lost. Disconnecting...\n\r");
          this.window.updateChat(e.getMessage());
        }
      }
    }
    System.out.println("Connected: " + isConnected);
    System.out.println("Closing runnable thread.");
  }

  // Used by the ClientWindow to send a message.
  public void sendMessage(String msg) throws IOException {
    if (msg.contains("/quit")) {
      if (this.isConnected) {
        this.disconnect();
      }
    } else {
      this.output.writeObject(msg);
      this.output.flush();
    }
  }

  // If the client is connected and suddenly closes.
  // This alerts the server that the client is disconnecting.
  // The server receives this message and forwards it to all other clients.
  // Upon being received, the server and clients remove this client from user list.
  private void disconnect() {
    System.out.println("Disconnect was called.");
    try {
      if (isConnected) {
        this.sendMessage("Rem:" + name);
        this.isConnected = false;
      }
      this.input.close();
      this.output.close();
      this.connection.close();
      this.window.disconnect(name);
      this.window.updateChat("Client : DISCONNECTED.\n\r");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public boolean connect(String name, Socket connection, ObjectOutputStream out, 
      ObjectInputStream in) {
    if (!(isConnected)) {
      this.name = name;
      this.connection = connection;
      this.output = out;
      this.input = in;
      this.window.addUser(name);
      this.window.setTitle("Chat Client : " + name + " - Connected");
      this.window.setFocusable(true);
      System.out.println("connect complete");
      this.window.connected(this, name);
      this.isConnected = true;
      return isConnected;
      } else {
      return false;
    }
  }
  
  public boolean isConnected() {
    return isConnected;
  }

  public static void main(String[] args) {
    MyClient client = new MyClient();
  }
}
