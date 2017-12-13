package cs455.overlay.wireformats;
/*
 *Author: Tiger Barras
 *RegistryRequestsTaskInitiate.java
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


public class RegistryRequestsTaskInitiate implements Event{

	private byte[] message;
	private int messageType = 8;

	int numberOfMessagesToSend;

	public RegistryRequestsTaskInitiate(byte[] data){
		message = data;

		try{
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

			din.readInt();//Read past messageType, since that's already set
			numberOfMessagesToSend = din.readInt();

			baInputStream.close();
			din.close();
		}catch(IOException e){
			System.out.println("RRTI: Error Unmarshalling");
			System.out.println(e);
		}

	}//End unmarshall constructor

	public RegistryRequestsTaskInitiate(int numMessages){
		numberOfMessagesToSend = numMessages;

		try{
			message = null;
			ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
			dout.writeInt(messageType);
			dout.writeInt(numberOfMessagesToSend);

			dout.flush();
			message = baOutputStream.toByteArray();
			baOutputStream.close();
			dout.close();
		}catch(IOException e){
			System.out.println("RRTI: Error Marshalling");
			System.out.println(e);
		}
	}//End marshall constructor


	public int getNumberMessagesToSend(){
		return numberOfMessagesToSend;
	}//End getNumberMessagesToSend

	public byte[] getBytes(){
		return message;
	}//End getBytes

	public int getType(){
		return 8;
	}//End getType

	public String toString(){
		String toReturn = "RegistryRequestsTaskInitiate: \n";
		toReturn = toReturn.concat("  Number of packets to send: " + numberOfMessagesToSend);

		return toReturn;
	}//End toString

	public Socket getSocket(){
		return null;
	}//End getSocket

}
