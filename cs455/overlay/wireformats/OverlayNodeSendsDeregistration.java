package cs455.overlay.wireformats;
/*
*Author: Tiger Barras
*OverlayNodeSendsDeregistrationStatus.java
*Wireformat to initiate deregistration attempt
*/

import cs455.overlay.wireformats.Event;

import java.net.InetAddress;
import java.net.Socket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

public class OverlayNodeSendsDeregistration implements Event{

	byte[] message;
	int messageType = 4;
	int length;
	byte[] addressBytes;
	InetAddress address;
	int port;
	int id;

	Socket socket = null;

	public OverlayNodeSendsDeregistration(byte[] data, Socket s){
		//System.out.println("Unmarshalling ONSD");

		message = data;
		socket = s;

		try{
			//Set up streams to read byte array
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

			//Read all the information from the byte array
			din.readInt();//Read past messageType, since that's already set
			length = din.readInt();
			addressBytes = new byte[length];
			din.readFully(addressBytes);
			address = InetAddress.getByAddress(addressBytes);
			port = din.readInt();
			id = din.readInt();

			//Close up
			baInputStream.close();
			din.close();
		}catch(IOException e){
			System.out.println("ONSD: Error Unmarshalling");
			System.out.println(e);
		}

	}//End unmarshal constructor

	public OverlayNodeSendsDeregistration(InetAddress a, int p, int i){
		//System.out.println("Marshalling ONSD");

		//Set instance variables
		address = a;
		addressBytes = address.getAddress();
		length = addressBytes.length;
		port = p;
		id = i;

		try{
			//Set up streams to read into byte array
			message = null;
			ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

			//Write data
			dout.writeInt(messageType);
			dout.writeInt(length);
			dout.write(addressBytes);
			dout.writeInt(port);
			dout.writeInt(id);

			//Pull byte array from stream
			dout.flush();
			message = baOutputStream.toByteArray();

			//Close up
			baOutputStream.close();
			dout.close();
		}catch(IOException e){
			System.out.println("ONSD: Error Marshalling");
			System.out.println();
		}
	}//End marshal constructor

	public int getType(){
		return messageType;
	}//End getType

	public byte[] getBytes(){
		return message;
	}//End getBytes

	public String toString(){
		String toReturn = "OverlayNodeSendsDeregistration: \n";
		toReturn = toReturn.concat("  Address: " + address.getHostAddress());
		toReturn = toReturn.concat("\n  port: " + port);
		toReturn = toReturn.concat("\n  id: " + id);

		return toReturn;
	}//End toString

	public Socket getSocket(){
		return socket;
	}//End getSocket

	public int getId(){
		return id;
	}//End get id

	public InetAddress getIP(){
		return address;
	}//End getIP

	public int getPort(){
		return port;
	}//End getPort


}
