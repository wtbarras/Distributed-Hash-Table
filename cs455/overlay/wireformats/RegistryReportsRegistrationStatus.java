package cs455.overlay.wireformats;
/*
*Author: Tiger Barras
*RegistryReportsRegistrationStatus.java
*Wireformat for reply from registry to messagenode after registration attempt
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
import java.io.UnsupportedEncodingException;

/*----------FORMAT-----------
*Byte: Message Type (REGISTRY_REPORTS_REGISTRATION_STATUS)
*int : Success Status; Assigned ID if succesful, -1 in case of failure
*Byte: Length of following "Information string" field
*Byte[^^]: InformationString
*/
public class RegistryReportsRegistrationStatus implements Event{

	byte[] message;
	int messageType = 3;
	int status;
	int length;
	byte[] infoStringBytes;
	String informationString;

	//Constructor to make an object out of an incoming byte array
	public RegistryReportsRegistrationStatus(byte[] data)throws UnknownHostException{
		//System.out.println("unmarshalling RRRS");
		System.out.println(data.length);
		message = data;

		try{
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			din.readInt();//Read past messageType, since that's already set
			status = din.readInt();
			length = din.readInt();
			//System.out.println(status);
			infoStringBytes = new byte[length];
			din.readFully(infoStringBytes);
			try{
				informationString = new String(infoStringBytes, "US-ASCII");
			}catch(UnsupportedEncodingException e){
				System.out.println("RRRS: Error, US-ASCII not supported");
				System.out.println(e);
			}
			baInputStream.close();
			din.close();
		}catch(IOException e){
			System.out.println("RRRS: Error Unmarshalling");
			System.out.println(e);
		}

		System.out.println(informationString);
	}//End incoming constructor

	//Another constructor to make an object out of the data fields
	public RegistryReportsRegistrationStatus(int ss, String infoString){
		status = ss;
		informationString = infoString;
		try{
			infoStringBytes = infoString.getBytes("US-ASCII");
		}catch(UnsupportedEncodingException e){
			System.out.println("RRRS: Error, US-ASCII not supported");
			System.out.println(e);
		}
		length = infoStringBytes.length;

		try{
			message = null;
			ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
			dout.writeInt(messageType);
			dout.writeInt(status);
			dout.writeInt(length);
			dout.write(infoStringBytes);
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

	public String toString(){
		String toReturn = "RegistryReportsRegistrationStatus: \n";
		toReturn = toReturn.concat("  Success Status -> " + this.status + "\n");
		toReturn = toReturn.concat("  Information String -> " + this.informationString + "\n");
		return toReturn;
	}//End toString

	public Socket getSocket(){
		return null;
	}

	public int getStatus(){
		return status;
	}//End getStatus

	public String getInformation(){
		return informationString;
	}

}
