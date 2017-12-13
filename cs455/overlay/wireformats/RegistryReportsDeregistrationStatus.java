package cs455.overlay.wireformats;
/*
*Author: Tiger Barras
*RegistryReportsDeregistrationStatus.java
*Wireformat for reply from registry to messagenode after deregistration attempt
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


public class RegistryReportsDeregistrationStatus implements Event{

	byte[] message;
	int messageType = 5;
	int status;
	int length;
	byte[] infoStringBytes;
	String informationString;


	public RegistryReportsDeregistrationStatus(byte data[]){
		//System.out.println("unmarshalling RRDS");

		message = data;

		try{
			//Set up streams to parse byte array
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

			//Read information from byte array
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

			//Close up
			baInputStream.close();
			din.close();
		}catch(IOException e){
			System.out.println("RRRS: Error Unmarshalling");
			System.out.println(e);
		}
	}//End unmarshall constructor

	public RegistryReportsDeregistrationStatus(int ss, String infoString){
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
			//Set up streams to form byte array
			message = null;
			ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

			//Write information into stream
			dout.writeInt(messageType);
			dout.writeInt(status);
			dout.writeInt(length);
			dout.write(infoStringBytes);

			//Pull byte array from stream
			dout.flush();
			message = baOutputStream.toByteArray();

			//Close up
			baOutputStream.close();
			dout.close();
		}catch(IOException e){
			System.out.println("ONSR: Error Marshalling");
			System.out.println(e);
		}
	}//End marshall constructo


	public int getStatus(){
		return this.status;
	}//End getStatus

	public String getInformationString(){
		return informationString;
	}//End getInformationString

	public int getType(){
		return messageType;
	}//End getType

	//Method to return the byte array
	public byte[] getBytes(){
		return message;
	}//End getBytes

	public String toString(){
		String toReturn = "RegistryReportsDeregistrationStatus: \n";
		toReturn = toReturn.concat("  Success Status -> " + this.status + "\n");
		toReturn = toReturn.concat("  Information String -> " + this.informationString + "\n");
		return toReturn;
	}//End toString

	public Socket getSocket(){
		return null;
	}
}
