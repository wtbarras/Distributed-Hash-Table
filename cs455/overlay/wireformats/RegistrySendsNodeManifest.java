package cs455.overlay.wireformats;
/*
 *Author: Tiger Barras
 *RegistrySendsNodeManifest.java
 *Wireformat the registry uses to send a MessageNode it's routing table entries
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



public class RegistrySendsNodeManifest implements Event{

	private byte[] message;
	private int messageType = 6;
	private int routingTableSize;

	//Arrays to hold data about nodes in routing table
	//They correspond, so id[0] refers to the same node as address[0]
	private int[] id;
	private byte[][] addressBytes;
	private InetAddress[] address;
	private int[] port;

	//The list of add the ID's in the overlay
	int[] allIds;

	public RegistrySendsNodeManifest(byte[] data){
		message = data;

		try{
			//Create streams
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(message);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

			//Read metadata
			din.readInt();//Read past messageType, since that's already set
			routingTableSize = din.readInt();

			//Set up arrays for entries
			id = new int[routingTableSize];
			addressBytes = new byte[routingTableSize][];
			address = new InetAddress[routingTableSize];
			port = new int[routingTableSize];

			//entry data
			int length;
			for(int i = 0; i < routingTableSize; i++){
				id[i] = din.readInt();//Node ID
				length = din.readInt();//Length of address field
				addressBytes[i] = new byte[length];
				din.readFully(addressBytes[i]);//Address
				address[i] =InetAddress.getByAddress(addressBytes[i]);
				port[i] = din.readInt();
			}//Done reading entry data

			//Overlay data
			int numberNodesInOverlay = din.readInt();
			allIds = new int[numberNodesInOverlay];
			for(int i = 0; i < numberNodesInOverlay; i++){
				allIds[i] = din.readInt();
			}

		}catch(IOException e){
			System.out.println("RSNM: Error unmarshalling data");
			System.out.println(e);
		}
	}//End constructor

	public RegistrySendsNodeManifest(int size, int[] _id, InetAddress[] _address, int[] _port, int[] _allIds){
		routingTableSize = size;
		id = _id;
		address = _address;
		addressBytes = new byte[size][];
		allIds = _allIds;
		port = _port;


		try{
			message = null;
			//Initialize streams
			ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

			//Build up byte stream
			dout.writeInt(messageType);
			dout.writeInt(routingTableSize);
			//loop through arrays to build entry for each node in routing table
			//System.out.println(id.length);
			//System.out.println(address.length);
			for(int i = 0; i < size; i++){
				//System.out.println(i);
				//Write info of node 2^i hops away
				dout.writeInt(id[i]);
				//Turn address for this node into byte[]
				addressBytes[i] = address[i].getAddress();
				int addressLength = addressBytes[i].length;
				dout.writeInt(addressLength);//Length of following address
				dout.write(addressBytes[i]);//Actual address in byte[] form
				dout.writeInt(port[i]);
			}//Done writing routing table entries
			//write the list of all the nodes in the overlay
			dout.writeInt(allIds.length);//Number of node IDs
			for(int entry : allIds){
				dout.writeInt(entry);
			}//Done writing list of entries

			//Pull byte array from stream
			dout.flush();
			message = baOutputStream.toByteArray();

			//Close streams
			baOutputStream.close();
			dout.close();
		}catch(IOException e){
			System.out.println("RSNM: Error marshalling message");
			System.out.println(e);
		}

	}//End constructor


	public int getType(){
		return messageType;
	}//End getType

	//Method to return the byte array
	public byte[] getBytes(){
		return message;
	}//End getBytes

	public String toString(){
		String toReturn = "RegistrySendsNodeManifest: \n";
		toReturn = toReturn.concat("  Nodes in manifest: " + Arrays.toString(id) + "\n");
		toReturn = toReturn.concat("  All node IDs in overlay: " + Arrays.toString(allIds) + "\n");
		return toReturn;
	}//End toString

	public Socket getSocket(){
		return null;
	}//End getSocket

	public int[] getIds(){
		return id;
	}//End getIds

	public InetAddress[] getAddress(){
		return address;
	}//End getAddress

	public int[] getPorts(){
		return port;
	}//End getPorts

	public int[] getAllIds(){
		return allIds;
	}//End getAllIds
}
