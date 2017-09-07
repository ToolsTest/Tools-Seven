package MyOwn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.sun.corba.se.spi.orbutil.fsm.Action;
import com.sun.corba.se.spi.orbutil.fsm.FSM;
import com.sun.corba.se.spi.orbutil.fsm.Input;

/**
 * This class represents the Window for chat client.
 * Contains window components, layout and methods to update on client side.
 * @author Psionatix
 *
 */
public class ClientWindow extends JFrame {

  private JTextArea userText; // text input area
  private JTextArea chatWindow; // chat display area
  private JButton sendButton; // the send button
  private JPanel contentPane; // the windows panel, positions graphic elements
  private JMenuBar menuBar; // just a file menu bar test
  private JList<String> list; // list used to store users.
  private DefaultListModel<String> listModel; // the list model used for the list
  private Login connectWindow;
  

  public ClientWindow(final MyClient client) {
    super("Chat Window : - Disconnected");

    // window settings
    this.setSize(880, 580);
    this.setLocationRelativeTo(null);
    this.contentPane = new JPanel();
    this.contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    this.setContentPane(contentPane);

    // window layout settings
    GridBagLayout gbl_contentPane = new GridBagLayout();
    gbl_contentPane.columnWidths = new int[]{20, 760, 100};
    gbl_contentPane.rowHeights = new int[]{20, 35, 445, 50};
    gbl_contentPane.columnWeights = new double[]{1.0, 1.0};
    gbl_contentPane.rowWeights = new double[]{1.0, Double.MIN_VALUE};
    contentPane.setLayout(gbl_contentPane);

    // MenuBar
    menuBar = new JMenuBar();
    JMenu menu = new JMenu("File");
    JMenuItem menuItem = new JMenuItem("Login");
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          connectWindow = new Login(client);
          connectWindow.setVisible(true);
          connectWindow.toFront();
      }
    });
    menu.add(menuItem);
    menuBar.add(menu);
    this.setJMenuBar(menuBar);

    // window scroll chat area
    chatWindow = new JTextArea();
    chatWindow.setLineWrap(true);
    chatWindow.setEditable(false);
    JScrollPane scroll = new JScrollPane(chatWindow);
    GridBagConstraints gbc_chatWindow = new GridBagConstraints();
    gbc_chatWindow.fill = GridBagConstraints.BOTH;
    gbc_chatWindow.insets = new Insets(2, 2, 2, 2);
    gbc_chatWindow.gridx = 0;
    gbc_chatWindow.gridy = 0;
    gbc_chatWindow.gridwidth = 2;
    gbc_chatWindow.gridheight = 3;
    
    //gbc_chatWindow.insets = new Insets(20, 20, 20, 20);
    contentPane.add(scroll, gbc_chatWindow);

    // User list, list type clickable elements, scrollable
    listModel = new DefaultListModel<String>();
    list = new JList<String>(listModel);
    list.setSelectedIndex(0);
    list.setVisibleRowCount(5);
    JScrollPane listScrollPane = new JScrollPane(list);
    GridBagConstraints gbc_listScroll = new GridBagConstraints();
    gbc_listScroll.fill = GridBagConstraints.BOTH;
    gbc_listScroll.insets = new Insets(2, 2, 2, 2);
    gbc_listScroll.gridx = 2;
    gbc_listScroll.gridy = 1;
    gbc_listScroll.gridwidth = 1;
    gbc_listScroll.gridheight = 2;
    contentPane.add(listScrollPane, gbc_listScroll);

    // userText type area
    userText = new JTextArea();
    userText.setLineWrap(true);
    userText.setEditable(true);
    JScrollPane sendScroll = new JScrollPane(userText);
    GridBagConstraints gbc_sendScroll = new GridBagConstraints();
    // gbc_userText.insets = new Insets(0, 5, 0, 0);
    gbc_sendScroll.fill = GridBagConstraints.BOTH;
    gbc_sendScroll.insets = new Insets(2, 2, 2, 2);
    gbc_sendScroll.gridx = 0;
    gbc_sendScroll.gridy = 3;
    gbc_sendScroll.gridheight = 1;
    gbc_sendScroll.gridwidth = 2;
    contentPane.add(sendScroll, gbc_sendScroll);
    userText.setColumns(10);

    // send button
    sendButton = new JButton("Send");

    GridBagConstraints gbc_sendButton = new GridBagConstraints();
    // gbc_sendButton.insets = new Insets(0, 0, 0, 5);
    gbc_sendButton.gridx = 2;
    gbc_sendButton.gridy = 3;
    contentPane.add(sendButton, gbc_sendButton);
    
    connectWindow = new Login(client);
    connectWindow.setVisible(true);
    this.setVisible(true);
    connectWindow.toFront();
  }

  // This method sets up the input event listeners
  // Send button click event
  // Press Enter event
  private void setSendListener(final MyClient client, final String name) {
    sendButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            sendText(client, name);
          }
        }
        );

    // sets up event for 'Enter' on text Area.'
    userText.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        // TODO Auto-generated method stub
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendText(client, name);
        }
      }
    });
  }

  private void sendText(MyClient client, String name) {
    if(!userText.getText().isEmpty()) {
      final String msg = name + " : " + userText.getText().trim() + "\n\r";
      try {
        client.sendMessage(msg);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      userText.setText("");
    }
  }

  public void updateChat(final String msg) {
    SwingUtilities.invokeLater(
        new Runnable() {
          public void run() {
            chatWindow.append(msg);
          }
        }
        );
  }
  
  public void addUser(String name) {
    if (!listModel.contains(name)) {
      listModel.addElement(name);
    }
  }
  
  public void removeUser(String name) {
    if (listModel.contains(name)) {
      listModel.removeElement(name);
    }
  }
  
  public void connected(MyClient client, String name) {
    this.setSendListener(client, name);
    this.setTitle("Chat Client : " + name + " - Connected");
    
  }
  
  public void disconnect(String name) {
    this.setTitle("Chat Client : " + name + " - Disconnected");
    this.listModel.clear();
  }

}
