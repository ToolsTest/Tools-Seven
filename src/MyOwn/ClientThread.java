package MyOwn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JTextArea;

/**
 * Once a client connection is accepted by the server, 
 * that clients connection and in/out streams are passed to a ClientThread.
 * The ClientThread runs on it's own, seperate thread.
 * This means each clients in/out streams are each running on a seperate thread.
 * This is because the server has a seperate in/out stream for each client connected.
 * @author Psionatix
 *
 */
public class ClientThread extends Thread {

  private ObjectInputStream input;
  private ObjectOutputStream output;
  
  // This ArrayList is passed from the server.
  // It stores ALL of the accepted client connection threads.
  // The code is set up in a way that once a thread dies, it is removed.
  private static ArrayList<ClientThread> threads;
  // private Message message;
  private String name;
  private MyServer server;
  
  // We already have the connection, so we just pass it all.
  public ClientThread(ObjectOutputStream output, ObjectInputStream input, ArrayList<ClientThread> threads, MyServer server, String name) {
    this.output = output;
    this.input = input;
    ClientThread.threads = threads;
    this.server = server;
    this.name = name;
  }

  // As this is a runnable, once the threads .start() is called from the server.
  // This run() method runs, once the run method is complete, the ClientThread
  // is terminated.
  public void run() {
    try {
      
      // Now the client is 100% connected, we can broadcast this to all users.
      // When a message is received by a client, it is also sent back to the client.
      // This means that, even if a user sends a message, it won't appear on their end
      // unless it has been sent back to them from the server.
      server.sendToAll("*** " + name + " has entered! ***\n\r");
      server.updateServer("*** " + name + " has entered! ***\n\r");

      while (true) {
        String line = (String) input.readObject();
        if (line != null) {
          if (line.startsWith("Rem:")) {
            server.sendSingle("/quit", this);
            server.sendToAll(name + " has disconnected.\n\r");
            server.updateServer(name + " has disconnected.\n\r");
            server.sendToAll(line);
            server.removeServerUser(line.split(":")[1]);
            break;
          }
          // send the received message (line) to all clients.
          server.sendToAll(line);
          // Update the server chat window.
          server.updateServer(line);
        }
      }
    } catch (IOException e) {

    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // remove a user from the server user list.
    server.removeServerUser(this.getName());
    // remove thread from the array list before it terminates.
    threads.remove(this);
  }

  public void sendMessage(String msg) throws IOException {
    this.output.writeObject(msg);  
    this.output.flush();
  }
  
  public void sendMessage(Object msg) throws IOException {
    this.output.writeObject(msg);  
    this.output.flush();
  }
  
  public String getClientName() {
    return name;
  }
  
  public void close() throws IOException {
    input.close();
    output.close();
    threads.remove(this);
  }

}
