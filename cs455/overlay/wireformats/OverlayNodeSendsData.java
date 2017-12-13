package cs455.overlay.wireformats;
/*
 *Author: Tiger Barras
 *OverlayNodeSendsData.java
 *Wireformat to wrap data that is being routed over network
 */

import cs455.overlay.wireformats.Event;

import java.util.Arrays;
import java.util.ArrayList;
import java.net.InetAddress;
import java.net.Socket;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;


public class OverlayNodeSendsData implements Event{

	byte[] message;
	int messageType = 9;
	int destinationId;
	int sourceId;
	int payload;
	int numberOfHops;
	ArrayList<Integer> disseminationTrace = new ArrayList<Integer>();


	public OverlayNodeSendsData(byte[] data){
		message = data;

		try{
			//Set up streams to read byte array
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

			//Read all the information from the byte array
			din.readInt();//Read past messageType, since that's already set
			destinationId = din.readInt();
			sourceId = din.readInt();

			payload = din.readInt();

			numberOfHops = din.readInt();
			for(int i = 0; i < numberOfHops; i++){
				disseminationTrace.add(din.readInt());
			}

			//Close up
			baInputStream.close();
			din.close();
		}catch(IOException e){
			System.out.println("ONSD: Error Unmarshalling");
			System.out.println(e);
		}
	}//End unmarshal constructor

	public OverlayNodeSendsData(int dest, int src, int payld, ArrayList<Integer> dt){
		//Set instance variables
		destinationId = dest;
		sourceId = src;
		payload = payld;
		disseminationTrace = dt;
		numberOfHops = dt.size();

		try{
			//Set up streams to read into byte array
			message = null;
			ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

			//Write data
			dout.writeInt(messageType);
			dout.writeInt(destinationId);
			dout.writeInt(sourceId);
			dout.writeInt(payload);
			dout.writeInt(numberOfHops);
			for(int hop : disseminationTrace){
				dout.writeInt(hop);
			}

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


public void addHop(int id){
	disseminationTrace.add(id);
	numberOfHops++;

	OverlayNodeSendsData updateData = new OverlayNodeSendsData(destinationId, sourceId, payload, disseminationTrace);

	this.message = updateData.getBytes();
}//End addHop

public int getDestination(){
	return destinationId;
}//End getDeistination

public int getSource(){
	return sourceId;
}//End getSource

public int getPayload(){
	return payload;
}//End getPayload

public int getNumberOfHops(){
	return numberOfHops;
}//End getNumberOFHops

public ArrayList<Integer> getDisseminationTrace(){
	return disseminationTrace;
}

public int getType(){
	return 9;
}//End getType

public byte[] getBytes(){
	return message;
}//End getBytes

public String toString(){
		String toReturn = "OverlayNodeSendsData";
		toReturn = toReturn.concat("Destination ID: " + destinationId);
		toReturn = toReturn.concat("Source ID: " + sourceId);
		toReturn = toReturn.concat("Payload: " + payload);
		toReturn = toReturn.concat("Dissemination trace: " + Arrays.toString(disseminationTrace.toArray()));

		return toReturn;
}//End toString

public Socket getSocket(){
	return null;
}//End getSocket

}//End class
