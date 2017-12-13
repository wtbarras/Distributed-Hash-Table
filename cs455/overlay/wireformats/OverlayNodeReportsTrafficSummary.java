package cs455.overlay.wireformats;
/*
 *Author: Tiger Barras
 *OverlayNodeReportsTrafficSummary.java
 *Wireformat the Registry uses to tell the message nodes to start sending messages
 * as well as how many to send
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


public class OverlayNodeReportsTrafficSummary implements Event{

	private byte[] message;
	private int messageType = 12;

	private int id;
	private int sent;
	private int relayed;
	private long sentDataSum;
	private int recieved;
	private long recievedDataSum;


	public OverlayNodeReportsTrafficSummary(byte[] data){
		message = data;

		try{
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

			din.readInt();//Read past messageType, since that's already set
			id = din.readInt();
			sent = din.readInt();
			relayed = din.readInt();
			sentDataSum = din.readLong();
			recieved = din.readInt();
			recievedDataSum = din.readLong();

			baInputStream.close();
			din.close();
		}catch(IOException e){
			System.out.println("OVRTS: Error Unmarshalling");
			System.out.println(e);
		}
	}//End unmarshall constructor

	public OverlayNodeReportsTrafficSummary(int _id, int _sent, int _relayed, long _sentDataSum, int _recieved, long _recievedDataSum){
		id = _id;
		sent = _sent;
		relayed = _relayed;
		sentDataSum = _sentDataSum;
		recieved = _recieved;
		recievedDataSum = _recievedDataSum;

		try{
			message = null;
			ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

			dout.writeInt(messageType);
			dout.writeInt(id);
			dout.writeInt(sent);
			dout.writeInt(relayed);
			dout.writeLong(sentDataSum);
			dout.writeInt(recieved);
			dout.writeLong(recievedDataSum);

			dout.flush();
			message = baOutputStream.toByteArray();
			baOutputStream.close();
			dout.close();
		}catch(IOException e){
			System.out.println("RRTI: Error Marshalling");
			System.out.println(e);
		}
	}//End marshall constructor


	public int getId(){
		return id;
	}//End getId

	public int getSent(){
		return sent;
	}//End getSent

	public int getRelayed(){
		return relayed;
	}//End getRelayed

	public int getRecieved(){
		return recieved;
	}//End getRecieved

	public long getSentDataSum(){
		return sentDataSum;
	}//End getSentDataSum

	public long getRecievedDataSum(){
		return recievedDataSum;
	}//End get RecievedDataSum

	public byte[] getBytes(){
		return message;
	}//End getBytes

	public int getType(){
		return messageType;
	}//End getType

	public String toString(){
		String toReturn = "Node " + id;
		toReturn = toReturn.concat("  " + sent);
		toReturn = toReturn.concat("  " + recieved);
		toReturn = toReturn.concat("  " + relayed);
		toReturn = toReturn.concat("  " + sentDataSum);
		toReturn = toReturn.concat("  " + recievedDataSum);

		return toReturn;
	}//End toString

	public Socket getSocket(){
		return null;
	}//End getSocket


}//End class
