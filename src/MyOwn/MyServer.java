package MyOwn;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class MyServer {
  private ServerWindow window;
  private static ArrayList<ClientThread> threads = 
      new ArrayList<ClientThread>();
  private static ArrayList<String> users = 
      new ArrayList<String>();

  // Server socket
  private static ServerSocket serverSocket;
  private boolean online;

  public MyServer() {
    window = new ServerWindow("MyServer - IM", "SERVER", this);
    window.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        disconnect();
      }
    });
    try {
      serverSocket = new ServerSocket(6789);
      online = true;
      System.out.println("Online: " + online);
    } catch (IOException e) {
      online = false;
    }
    window.setVisible(true);
  }

  public void startRunning() {
    if (online) {
      updateServer("Server socket is open. Pending connections.\n\r");
    }
    while (online) {
      try {
        Socket connection = serverSocket.accept();
        if (connection != null) {
          System.out.println("Attempting to make a new Connection.");
          ObjectOutputStream tempOut = new ObjectOutputStream(connection.getOutputStream());
          ObjectInputStream tempIn = new ObjectInputStream(connection.getInputStream());
          System.out.println("Streams created.");
          System.out.println(tempIn);
          
          // Client receives the Connect: + name string from client
            String init = (String) tempIn.readObject();
            
          // To receive a Message Object, change the code here.
          // read in an Object:
          // Object obj = tempIn.readObject();
          // This is where the server code will hang, but works locally.. sometimes.
          // Check this obj is instanceof Message.
          // If it is set the String name below = ((Message)obj).getSender();
          
          System.out.println("Object received.");

          // Checks string syntax is correct, validate name, connects client 100%.
          if (init.startsWith("Connect")) {
            String name = init.split(":")[1];
            if (validateName(name)) {
              System.out.println("name is valid.");
              tempOut.writeObject(name + "accepted");
              tempOut.flush();
              ClientThread newClient = new ClientThread(tempOut, tempIn, threads, this, name);
              users.add(name);
              this.sendToAll("NewName:" + name);
              this.sendSingle("SERVER : You have successfully connected.\n\r", newClient);
              window.addUserList(name);
              this.sendSingle("Retrieving user list...\n\r", newClient);
              for(ClientThread c : threads) {
                this.sendSingle("NewName:" + c.getClientName(), newClient);
              }
              this.sendSingle("User list receieved.\n\r", newClient);
              this.sendSingle("Use /quit to exit, or X to disconnect.\n\r", newClient);
              threads.add(newClient);
              newClient.start();
            } else {
              tempOut.writeObject("nameistaken");
              tempOut.flush();
              tempIn.close();
              tempOut.close();
              connection.close();
            }
          }
        }

        for (ClientThread c : threads) {
          if (c == null) {
            threads.remove(c);
          }
        }
      } catch (IOException | ClassNotFoundException e) {

      }
    }
  }

  public void sendToAll(final String msg) {
    for (ClientThread c : threads) {
      try {
        c.sendMessage(msg);
      } catch (IOException e) {
        System.out.println(c.getClientName());
        System.out.println("Error user text event");
      }
    }
  }
  
  public void sendToAll(final String msg, ClientThread except) {
    for (ClientThread c : threads) {
      try {
        c.sendMessage(msg);
      } catch (IOException e) {
        System.out.println(c.getClientName());
        System.out.println("Error user text event");
      }
    }
  }

  public void sendSingle(String msg, ClientThread c) throws IOException {
    c.sendMessage(msg);
  }

  public void updateServer(String msg) {
    final String rec = msg;
    window.updateReceived(rec);
  }

  public void removeServerUser(String name) {
    users.remove(name);
    window.removeUserList(name);
  }

  // Makes sure the name is unique.
  private boolean validateName(String name) {
    return !users.contains(name);
  }

  // This is used to close any open connections.
  // This prevents memory leaks, ends all threads.
  // This sends a message to all clients, alerting them to disconnect.
  private void disconnect() {
    sendToAll("/quit");
    for (int i = threads.size(); i > 0; i--) {
      try {
        threads.get(i -1).close();
      } catch (IOException e) {
        System.out.println(threads.get(i - 1).getClientName() + " did not close correctly.");
      }
    }
    online = false;
    window.dispose();
  }
  
  public static void main(String[] args) {
    MyServer serv = new MyServer();
    serv.startRunning();
  }
}
