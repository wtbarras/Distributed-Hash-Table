package cs455.overlay.wireformats;
/*
*Author: Tiger Barras
*EventFactory.java
*Creates events out of byte arrays
*/

import cs455.overlay.wireformats.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;

public class EventFactory{

	private static final EventFactory instance = new EventFactory();

	protected EventFactory(){
		//Only exists to defeat instantiation
	}//End constructor

	//This is synchronized because that if statement is not
	  //Thread safe
	public static EventFactory getInstance(){
		return instance;
	}//End getInstance

	public synchronized static Event manufactureEvent(byte[] data, Socket s)throws UnknownHostException{
		Event event = null;

		int type = -1;

		try{
			ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
			DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
			type = din.readInt();
			baInputStream.close();
			din.close();
		}catch(IOException e){
			System.out.println("EventFactor: Error reading event type");
			System.out.println(e);
		}

		//Add logic here to turn the byte stream into an event...homie
		switch(type){
			case 2:	 event = new OverlayNodeSendsRegistration(data, s);
							 break;
			case 3:  event = new RegistryReportsRegistrationStatus(data);
							 break;
			case 4:	 event = new OverlayNodeSendsDeregistration(data, s);
							 break;
			case 5:	 event = new RegistryReportsDeregistrationStatus(data);
							 break;
			case 6:  event = new RegistrySendsNodeManifest(data);
							 break;
			case 7:  event = new NodeReportsOverlaySetupStatus(data);
						 	 break;
			case 8:  event = new RegistryRequestsTaskInitiate(data);
							 break;
			case 9:  event = new OverlayNodeSendsData(data);
							 break;
			case 10: event = new OverlayNodeReportsTaskFinished(data);
							 break;
			case 11: event = new RegistryRequestsTrafficSummary();
							 break;
			case 12: event = new OverlayNodeReportsTrafficSummary(data);
							 break;
			default: System.out.println("That message is not even a real message!");
							 System.exit(-1);
		}

		return event;
	}//End manufactureEvent
}//End class
