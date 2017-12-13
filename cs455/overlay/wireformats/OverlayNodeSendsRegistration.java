package cs455.overlay.wireformats;
/*
*Author: Tiger Barras
*OverlayNodeSendsRegistration.java
*Wireformat for when a node sends its registration to the register
*/

import cs455.overlay.wireformats.Event;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/*----------FORMAT-----------
 *Byte: Message Type (OVERLAY_NODE_SENDS_REGISTRATION)
 *Byte: Length of following "IP Address" field
 *Byte[^^]: IP Address; from InetAddress.getAddress()
 *int: Port Number
 */
public class OverlayNodeSendsRegistration implements Event{

  //This is all infomation pulled from the message
  byte[] message;
  int messageType = 2;
	int length;
  byte[] IPByte;
	InetAddress IP;
	int portNumber;

  Socket socket = null;

  //Constructor to make an object out of an incoming byte array
	public OverlayNodeSendsRegistration(byte[] data, Socket s)throws UnknownHostException{
    //System.out.println("Creating ONSR");

    socket = s;
    message = data;

    //System.out.println(data.length);

    try{
      ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
      DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
      din.readInt();//Read past messageType, since that's already set
      length = din.readInt();
      IPByte = new byte[length];
      din.readFully(IPByte);
      IP = InetAddress.getByAddress(IPByte);//Turns the byte array into an actual InetAddress
      portNumber = din.readInt();
      baInputStream.close();
      din.close();
    }catch(IOException e){
      System.out.println("ONSR: Error Unmarshalling");
      System.out.println(e);
    }
	}//End incoming constructor

  //Another constructor to make an object out of the data fields
  public OverlayNodeSendsRegistration(InetAddress addr, int port){
    //Populate fields
    IP = addr;
    IPByte = addr.getAddress();
    length = IPByte.length;
    portNumber = port;

    try{
      message = null;
      ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
      DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
      dout.writeInt(messageType);
      dout.writeInt(length);
      dout.write(IPByte);
      dout.writeInt(port);
      dout.flush();
      message = baOutputStream.toByteArray();
      baOutputStream.close();
      dout.close();
    }catch(IOException e){
      System.out.println("ONSR: Error Marshalling");
      System.out.println(e);
    }
  }//End outgoing constructor

  public int getType(){
    return messageType;
  }//End getType

  //Method to return the byte array
  public byte[] getBytes(){
    return message;
  }//End getBytes

  public InetAddress getIP(){
    return IP;
  }//End getIP

  public int getPort(){
    return portNumber;
  }//End getPort

  public Socket getSocket(){
    return socket;
  }

  public String toString(){
    String toReturn = "OverlayNodeSendsRegistration: \n";
    toReturn = toReturn.concat("  Length -> " + length + "\n");
    toReturn = toReturn.concat("  Sender's Address -> " + IP + "\n");
    toReturn = toReturn.concat("  Sender's Port Number -> " + portNumber + "\n");
    return toReturn;
  }//End toString

}
