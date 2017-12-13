package cs455.overlay.node;
/*
 *Author: Tiger Barras
 *MessageNode.java
 *Top level class that generates, routes, and recieves packages from other Nodes
 */

import cs455.overlay.node.Node;
import cs455.overlay.transport.ConnectionCache;
import cs455.overlay.transport.NodeConnectionCache;
import cs455.overlay.transport.Connection;
import cs455.overlay.transport.ServerThread;
import cs455.overlay.transport.RecieverThread;
import cs455.overlay.transport.Sender;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;
import cs455.overlay.wireformats.RegistryReportsDeregistrationStatus;
import cs455.overlay.wireformats.OverlayNodeSendsDeregistration;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;
import cs455.overlay.wireformats.NodeReportsOverlaySetupStatus;
import cs455.overlay.wireformats.RegistryRequestsTaskInitiate;
import cs455.overlay.wireformats.OverlayNodeSendsData;
import cs455.overlay.wireformats.OverlayNodeReportsTaskFinished;
import cs455.overlay.wireformats.OverlayNodeReportsTrafficSummary;
import cs455.overlay.util.InteractiveCommandParser;

import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.Integer;

//Remember that connections between nodes only need to be one way
//Other than the registry
public class MessageNode implements Node{

	//Need a cache for the sockets I'm going to send messages to
	//I'll start receiverThreads to deal with the ones talking to me
	NodeConnectionCache cache;
	//The port that this node's serverThread is listening on
	//Set in the startServer call
	int portNum;
	InetAddress address;

	//Data on how to reach the registry
	InetAddress registryAddress;
	int registryPort;
	int portToRegistry;

	//The id assigned to this node by the registry in RegistryReportsRegistrationStatus
	int id;
	//All the ids in the overlay
	int[] allIds;

	//The routing table for this node
	RoutingTable routingTable = new RoutingTable();

	//Counters for checksums
	private AtomicInteger sendTracker = new AtomicInteger(0);
	private AtomicInteger recieveTracker = new AtomicInteger(0);
	private AtomicInteger relayTracker = new AtomicInteger(0);
	private AtomicLong sendSummation = new AtomicLong(0);
	private AtomicLong recieveSummation = new AtomicLong(0);

	public MessageNode(){
		cache = new NodeConnectionCache();

		try{
			address = InetAddress.getLocalHost();
		}catch(UnknownHostException e){
			System.out.println("MessageNode: Error finding address");
			System.out.println(e);
		}

		//Get the server set up
		try{
			this.startServer(0);//Opening a serverSocket on port 0 automatically finds an open port
		}catch(IOException e){
			System.out.println("MessageNode: Could not start ServerThread");
			System.out.println(e);
		}
	}//End constructor

	//This is what will get called when something happens
	//Such as a message coming in, or a new link being opened
	public void onEvent(Event e){

		//System.out.println(e);

		int messageType = e.getType();

		switch(messageType){
			case 3:
					this.onMessageThree(e);//REGISTRY_REPORTS_REGISTRATION_STATUS
					break;
			case 5:
					this.onMessageFive(e);//REGISTRY_REPORTS_DEREGISTRATION_STATUS
					break;
			case 6:
					this.onMessageSix(e);//REGISTRY_SENDS_NODE_MANIFEST
					break;
			case 8:
					this.onMessageEight(e);//REGISTRY_REQUESTS_TASK_INITIATE
					break;
			case 9:
					this.onMessageNine(e);//OVERLAY_NODE_SENDS_DATA
					break;
			case 11:
					this.onMessageEleven();//RegistryRequestsTrafficSummary
					break;
			default: break;
		}

	}//End onEvent

	public void onMessageThree(Event e){
		RegistryReportsRegistrationStatus message3 = null;
		try{
			message3 = new RegistryReportsRegistrationStatus(e.getBytes());
		}catch(UnknownHostException exception){
			System.out.println("MessageNode: Error reading RegistryReportsRegistrationStatus");
			System.out.println(exception);
		}


		int status = message3.getStatus();
		String information = message3.getInformation();

		if(status == -1){
			System.out.println("MessageNode: Error in registration!\n" + information);
		}else{
			this.id = status;
			System.out.println("Registration successful. ID: " + id);
		}
	}//End onMessageThree

	public void onMessageFive(Event event){
		RegistryReportsDeregistrationStatus rrds = new RegistryReportsDeregistrationStatus(event.getBytes());

		if(rrds.getStatus() <= -1){
			System.out.println("MessageNode: Error deregistering");
			System.out.println("Information: " + rrds.getInformationString());
		}else{
			System.out.println("Successfully Deregistered");
			System.exit(-1);
		}
	}//end onMessageFive

	public void onMessageSix(Event event){
		RegistrySendsNodeManifest rsnm = new RegistrySendsNodeManifest(event.getBytes());

		int[] id = rsnm.getIds();
		InetAddress[] address = rsnm.getAddress();
		int[] port = rsnm.getPorts();
		allIds = rsnm.getAllIds();

		//Build routing Entries
		for(int i = 0; i < id.length; i++){
			Connection overlayConnection = null;
			//Open socket to node in overlay in order to create a Connection
			Socket overlaySocket = new Socket();
			try{
				//Create a socket connection with the messageNode
				overlaySocket = new Socket(address[i], port[i]);
				overlayConnection = new Connection(this, overlaySocket);
			}catch(IOException e){
				System.out.println("Error opening socket to node in overlay");
				System.out.println("  Node ID: " + id[i]);
				System.out.println(e);
			}

			RoutingEntry entry = new RoutingEntry(address[i], id[i], port[i], overlayConnection);
			routingTable.addEntry(entry);
		}

		try{
			this.sendSetupStatus(this.id);
		}catch(UnknownHostException e){
			System.out.println("MessageNode: Error sending Overlay Setup Status");
			System.out.println(e);
		}

	}//End onMessageSix

	public void onMessageEight(Event event){
		RegistryRequestsTaskInitiate rrti = new RegistryRequestsTaskInitiate(event.getBytes());

		int numberMessagesToSend = rrti.getNumberMessagesToSend();
		int messagesSent = 0;
		//System.out.println("Number of messages to send: " + rrti.getNumberMessagesToSend());

		while(messagesSent < numberMessagesToSend){//Loop until all messages are sent
			Random numberGenerator = new Random();

			//Pick random node
			int destinationId = id;
			while(destinationId == id){//Loop until a node is picked that is not this
				//System.out.println("Selecting node to send message to");
				int randomIndex = numberGenerator.nextInt(allIds.length);//Generate random int
				destinationId = allIds[randomIndex];
			}

			System.out.println("I want to send a message to node " + destinationId);

			//Generate payload
			int min = -2147483647;
			int max =  2147483647;
			float fullPayload = numberGenerator.nextFloat()*(max - min + 1) + min;
			int payload = (int)fullPayload;
			System.out.printf("Payload: %d   fullPayload: %f", payload, fullPayload);
			//if((float)payload != fullPayload){
			//	System.out.println("Payload Overflow!");
			//	System.exit(-1);
			//}
			//int payload = 100;//Only use this for test purposes
			//It works just fine when this is constant...

			//Generate Message
			//System.out.println("creating message");
			OverlayNodeSendsData onsd = new OverlayNodeSendsData(destinationId, id, payload, new ArrayList<Integer>());

			//Figure out which node this is actually being sent to
			//System.out.println("Selecting next node");
			int nextNode = this.selectNextNode(destinationId);

			//Send message to that node
			//System.out.println("About to send packet to node " + destinationId + " via " + nextNode);
			this.sendToOverlayNode(nextNode, onsd);

			//UpdateData
			messagesSent++;
			sendSummation.getAndAdd(payload);
			sendTracker.getAndIncrement();
			System.out.println("Sent " + messagesSent + " messages");
		}

		// System.out.println("<<<<  I've sent all my messages. YAY!  >>>>");
		this.reportTaskFinished();
	}//End onMessageEight

	private void onMessageNine(Event event){
		OverlayNodeSendsData onsd = new OverlayNodeSendsData(event.getBytes());

		int destinationId = onsd.getDestination();
		int sourceId = onsd.getSource();
		int payload = onsd.getPayload();
		int numberOfHops = onsd.getNumberOfHops();
		ArrayList<Integer> disseminationTrace = onsd.getDisseminationTrace();

		if(sourceId == id){//If a message this node sent returns to it, that's very bad
			System.out.println("MessageNode: Error, Message returned to source");
			System.out.println(Arrays.toString(disseminationTrace.toArray()));
			System.exit(-1);
		}

		if(destinationId == id){//The packet has reached its destination
			recieveTracker.getAndIncrement();
			recieveSummation.getAndAdd((long)payload);
			System.out.println("Recieved " + recieveTracker.get() + " messages");
			System.out.println("Dissemination Trace: " + Arrays.toString(disseminationTrace.toArray()));
		}else{//Need to route the packet on
			//Update data
			relayTracker.getAndIncrement();

			onsd.addHop(id);//Add itself to the dissemination trace

			int nextNode = selectNextNode(destinationId);//Figure out where the packet goes from here

			System.out.println("About to route packet to node " + destinationId + " via " + nextNode);
			this.sendToOverlayNode(nextNode, onsd);//Send to next node
		}

	}//End onMessageNine

	public void onMessageEleven(){
		OverlayNodeReportsTrafficSummary onrts;
		onrts = new OverlayNodeReportsTrafficSummary(this.id
																									, this.sendTracker.get()
																									, this.relayTracker.get()
																									, this.sendSummation.longValue()
																									, this.recieveTracker.get()
																									, this.recieveSummation.longValue());

		try{
			this.sendToRegistry(onrts);
		}catch(UnknownHostException e){
			System.out.println("MessageNode: Error sending traffic summary");
			System.out.println(e);
		}
	}//End onMessageEleven


	private void reportTaskFinished(){
		OverlayNodeReportsTaskFinished onrtf = new OverlayNodeReportsTaskFinished(address, portNum, id);

		try{
			this.sendToRegistry(onrtf);
		}catch(UnknownHostException e){
			System.out.println("MessageNode: Error sending ONRTF to Registry");
			System.out.println(e);
		}
	}//End reportTaskFinished

	private int selectNextNode(int destinationNode){

		//Turn routing table into array that's easier to navigate
		RoutingEntry[] routingArray = routingTable.getAllEntriesCollection().toArray(new RoutingEntry[0]);
		//Sort array, so it can be traversed in order of the entry IDs
		Arrays.sort(routingArray);

		//Initialize two 'pointers' to move through the array
		//If one of them lands on the destination, then we're done
		//If the destination ends up between them, rout to the previousNode
		//Else, route to the last node in the table
		int previousIndex = 0;
		int currentIndex = 1;
		int previousNode = routingArray[previousIndex].getId();
		int currentNode = routingArray[currentIndex].getId();

		//Loop through whole array
		while(currentIndex < routingArray.length){
			//Update Nodes
			previousNode = routingArray[previousIndex].getId();
			currentNode = routingArray[currentIndex].getId();

			//Check to make sure we haven't already found the right node
			if(previousNode == destinationNode) return previousNode;
			if(currentNode == destinationNode) return currentNode;
			if((previousNode < destinationNode) && (currentNode > destinationNode)) return previousNode;

			//Update indices
			previousIndex++;
			currentIndex++;
		}

		//The destiatio was not in the scope of our routing table
		//Send the packet to the last node in the table to get it as close as possible
		return routingArray[routingArray.length-1].getId();
	}//End selectNextNode

	public void sendSetupStatus(int status) throws UnknownHostException{
		InetAddress local = InetAddress.getLocalHost();
		String infoString = String.format("Overlay setup status: %d", status);
		NodeReportsOverlaySetupStatus nross = new NodeReportsOverlaySetupStatus(status, infoString);

		this.sendToRegistry(nross);
	}//End sendSetupStatus

	//Listens at a specific port, and then passes out a Socket
	public void startServer(int pn) throws IOException{
		ServerThread server = new ServerThread(pn, this);
		server.getPortNum();
		server.start();
	}//End startServer

	//Spans a Reciever thread that is linked to the specified socket
	public void spawnRecieverThread(Socket socket){
		RecieverThread reciever = new RecieverThread(this, socket);
		reciever.start();
	}//End spawnRecieverThread

	public ConnectionCache getConnectionCache(){
		return cache;
	}//End getConnectionCache

	public void setPortNum(int pn){
		this.portNum = pn;
		//System.out.println("MessageNode pn: " + this.portNum);
	}//End setPortNum

	public Connection connectToRegistry(String addressString, int registryPort){
		//This is just a dummy address so that the variable is initialized
		InetAddress registryHost = InetAddress.getLoopbackAddress();
		try{
			//Generate the actual InetAddress of the Registry
			registryHost = InetAddress.getByName(addressString);
		}catch(UnknownHostException e){
			System.out.println("Unknown Host exception");
			System.out.println(e);
			System.exit(-1);
		}

		//Default socket to initialize
		Socket registrySocket = new Socket();
		try{
			//Create a socket connection with the Registry
			registrySocket = new Socket(registryHost, registryPort);
			portToRegistry = registrySocket.getLocalPort();
			System.out.println("MessageNode: Using port " + portToRegistry + " to connect to registry");
		}catch(IOException e){
			System.out.println("Error opening socket to Registry");
			System.out.println(e);
		}

		//Create a Connection object witht that socket
		Connection registryConnection = new Connection(this, registrySocket);
		return registryConnection;
	}//End connectToRegistry

	public synchronized void sendToOverlayNode(int nextNode, OverlayNodeSendsData onsd){
		Connection connectionToNextNode = null;
		//Get connection to that node
		if(routingTable.contains(nextNode)){
			connectionToNextNode = routingTable.getEntry(nextNode).getConnection();
		}else{
			System.out.println("MessageNode: Error, tried to rout to node not in routing table");
		}

		//Send message to that node
		connectionToNextNode.write(onsd.getBytes());
	}//End sentToOverlayNode

	public void sendToRegistry(Event event) throws UnknownHostException{
		String RegistryKey = registryAddress.getHostAddress().concat(String.valueOf(registryPort));
		Connection registryConnection = cache.get(RegistryKey);
		registryConnection.write(event.getBytes());
	}//End sendToRegistry

	public void sendRegistration() throws UnknownHostException{
		InetAddress local = InetAddress.getLocalHost();
		OverlayNodeSendsRegistration registration = new OverlayNodeSendsRegistration(local, portNum);

		this.sendToRegistry(registration);

	}//End sendRegistration

	//Sets up instance variables, and registers with the registry
	public void init(String hostName, String port){
		//The address and port of the registry are pulled from the command line
		try{
			this.registryAddress = InetAddress.getByName(hostName);
		}catch(UnknownHostException e){
			System.out.println("MessageNode: Error retrieving registry address");
			System.out.println(e);
		}
		this.registryPort = Integer.parseInt(port);

		//Open connection to registry, and place it in the cache
		Connection registryConnection = this.connectToRegistry(hostName, this.registryPort);
		String key = this.registryAddress.getHostAddress().concat(String.valueOf(this.registryPort));
		System.out.println("MessageNode: Adding connection with key: " + key);
		this.cache.add(key, registryConnection);
		// node.cache.get(key);

		//Register...It's the law
		try{
			this.sendRegistration();
		}catch(UnknownHostException e){
			System.out.println("MessageNode: Error sending registration");
			System.out.println(e);
		}
	}//End init

	//These methods are called from InteractiveCommandParser
	public void exitOverlay(){
		//Create message
		OverlayNodeSendsDeregistration deregistration;
		deregistration = new OverlayNodeSendsDeregistration(this.address, this.portToRegistry, this.id);

		//Send message
		String RegistryKey = registryAddress.getHostAddress().concat(String.valueOf(registryPort));
		Connection registryConnection = cache.get(RegistryKey);
		registryConnection.write(deregistration.getBytes());
	}//End exitOverlay

	public void printSummary(){
		System.out.println("sent: " + sendTracker.get());
		System.out.println("relayed: " + relayTracker.get());
		System.out.println("recieved: " + recieveTracker.get());
		System.out.println("sendSum: " + sendSummation.get());
		System.out.println("recieveSum: " + recieveSummation.get());
	}//End printSummary

	//MAIN
	//Currently only for testing
	public static void main(String args[]){
		//Make a messaging node. It will start our server for us
		MessageNode node = new MessageNode();

		node.init(args[0], args[1]);

		InteractiveCommandParser parser = new InteractiveCommandParser(node);
	}//End main

}
