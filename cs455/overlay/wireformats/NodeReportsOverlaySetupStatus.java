package cs455.overlay.wireformats;
/*
 *Author: Tiger Barras
 *NodeReportsOverlaySetupStatus.java
 *Wireformat the messageNodes use to communicate that they have finished setting up their overlay
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


public class NodeReportsOverlaySetupStatus implements Event{

	private byte[] message;
	private int messageType = 7;

	private int successStatus;
	private int length;
	private String informationString;
	private byte[] infoStringBytes;


	public NodeReportsOverlaySetupStatus(byte[] data)/*throws UnknownHostException*/{
		//System.out.println("unmarshalling NodeReportsOverlaySetupStatus");
		message = data;

		try{
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			din.readInt();//Read past messageType, since that's already set
			successStatus = din.readInt();
			length = din.readInt();
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
	}//End incoming constructor


	public NodeReportsOverlaySetupStatus(int ss, String infoString){
		successStatus = ss;
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
			dout.writeInt(successStatus);
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


	public int getSuccessStatus(){
		return successStatus;
	}//End getSuccessStatus

	public String getInformationString(){
		return informationString;
	}//End getInformationString

	public int getType(){
		return 7;
	}//End getType

	public byte[] getBytes(){
		return message;
	}//End getBytes

	public String toString(){
		String toReturn = "NodeReportsOverlaySetupStatus: \n";
		toReturn = toReturn.concat("  Success Status -> " + this.successStatus + "\n");
		toReturn = toReturn.concat("  Information String -> " + this.informationString + "\n");
		return toReturn;
	}//End getString

	public Socket getSocket(){
		return null;
	}//End getSocket

}
