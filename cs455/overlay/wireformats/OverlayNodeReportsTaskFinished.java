package cs455.overlay.wireformats;
/*
 *Author: Tiger Barras
 *NodeReportsOverlaySetupStatus.java
 *Wireformat the messageNodes use to communicate that they have finished sending all their messages
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



public class OverlayNodeReportsTaskFinished implements Event{

	private byte[] message;
	private int messageType = 10;

	int length;
	private byte[] addressBytes;
	private InetAddress address;
	private int portNumber;
	private int id;


	public OverlayNodeReportsTaskFinished(byte[] data){
		message = data;

		try{
			//Open stream to read out information from data[]
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

			//Read data
			din.readInt();//Read past messageType, since that's already set
			length = din.readInt();
			addressBytes = new byte[length];
			din.readFully(addressBytes);
			address = InetAddress.getByAddress(addressBytes);
			portNumber = din.readInt();
			id = din.readInt();

			//Close Streams
			baInputStream.close();
			din.close();
		}catch(IOException e){
			System.out.println("ONRTF: Error Unmarshalling");
			System.out.println(e);
		}
	}//End unmarshall constructor

	public OverlayNodeReportsTaskFinished(InetAddress a, int pn, int i){
		address = a;
		addressBytes = address.getAddress();
		length = addressBytes.length;
		portNumber = pn;
		id = i;

		try{
			message = null;
			ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

			dout.writeInt(messageType);
			dout.writeInt(length);
			dout.write(addressBytes);
			dout.writeInt(portNumber);
			dout.writeInt(id);

			dout.flush();
			message = baOutputStream.toByteArray();
			baOutputStream.close();
			dout.close();
		}catch(IOException e){
			System.out.println("ONSR: Error Marshalling");
			System.out.println(e);
		}
	}//End marshall constructor

	public InetAddress getAddress(){
		return address;
	}//End getAddress

	public int getPort(){
		return portNumber;
	}//End getPort

	public int getId(){
		return id;
	}//End getId

	public int getType(){
		return messageType;
	}//End getType

	public byte[] getBytes(){
		return message;
	}//End getBytes

	public String toString(){
		String toReturn = "OverlayNodeReportsTaskFinished: \n";
		toReturn = toReturn.concat("  Address -> " + this.address + "\n");
		toReturn = toReturn.concat("  ID -> " + this.id + "\n");
		return toReturn;
	}//End getString

	public Socket getSocket(){
		return null;
	}//End getSocket

}
