package MyOwn;

import java.io.Serializable;
import java.sql.Time;

public class Message implements Serializable {

  private static enum TYPE {MSG, ACCEPT, DC, KICK, REJECT, REQUEST};
  
  private String msg;
  private String sender;
  private TYPE type;
  
  public Message(String msg, String sender) {
    this.msg = msg;
    this.sender = sender;
    type = TYPE.MSG;
  }
  
  public Message(String msg, String sender, String type) {
    this(msg, sender);
    this.setType(type);
  }
  
  public boolean startsWith(String start) {
    return msg.startsWith(start);
  }
  
  public boolean contains(String part) {
    return msg.contains(part);
  }
  
  public String toSendable() {
    return "<" + sender + ">" + " : " + msg;
  }

  public String getSender() {
    return sender;
  }
  
  private void setType(String type) {
    if (type.toUpperCase().equals("ACCEPT")) {
      this.type = TYPE.ACCEPT;
    } else if (type.toUpperCase().equals("DC")) {
      this.type = TYPE.DC;
    } else if (type.toUpperCase().equals("KICK")) {
      this.type = TYPE.KICK;
    } else if (type.toUpperCase().equals("REQUEST")) {
      this.type = TYPE.REQUEST;
    } else if (type.toUpperCase().equals("REJECT")) {
      this.type = TYPE.REJECT;
    }
  }
  
  public String getType() {
    if (this.type == TYPE.ACCEPT) {
      return "ACCEPT";
    } else if (this.type == TYPE.DC) {
      return "DC";
    } else if (this.type == TYPE.KICK) {
      return "KICK";
    } else if (this.type == TYPE.REQUEST) {
      return "REQUEST";
    }
      else if (this.type == TYPE.REJECT) {
      return "REJECT";
    } else {
      return "MSG";
    }
  }

}
