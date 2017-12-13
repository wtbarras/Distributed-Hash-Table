package cs455.overlay.node;
/*
 *Author: Tiger Barras
 *Registry.java
 *Top level class that creates the overlay that all the MessagingNodes reside in
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.Math;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import cs455.overlay.transport.Connection;
import cs455.overlay.transport.ConnectionCache;
import cs455.overlay.transport.RegisterConnectionCache;
import cs455.overlay.transport.ServerThread;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;
import cs455.overlay.wireformats.OverlayNodeSendsDeregistration;
import cs455.overlay.wireformats.RegistryReportsDeregistrationStatus;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;
import cs455.overlay.wireformats.NodeReportsOverlaySetupStatus;
import cs455.overlay.wireformats.RegistryRequestsTaskInitiate;
import cs455.overlay.wireformats.OverlayNodeReportsTaskFinished;
import cs455.overlay.wireformats.RegistryRequestsTrafficSummary;
import cs455.overlay.wireformats.OverlayNodeReportsTrafficSummary;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.util.InteractiveCommandParser;
import cs455.overlay.util.SummaryAggregator;
import cs455.overlay.exception.ConnectionCacheException;


public class Registry implements Node{

	int portNum;//Port that the registry listens on
	RegisterConnectionCache cache;//Short term storage to hold connections before they are error checked
	RoutingTable routingTable;//Long term storage for legit connections
	//ArrayList<Integer> registry = new ArrayList<Integer>();//Why did I make this?
	private ConcurrentHashMap<Integer, RoutingTable> messagingNodeRoutingTables = new ConcurrentHashMap<Integer, RoutingTable>();

	private final AtomicInteger countOfNodesSuccessfullySetup = new AtomicInteger(0);
	private final AtomicInteger countOfNodesFinished = new AtomicInteger(0);
	private final AtomicInteger countOfSummariesRecieved = new AtomicInteger(0);

	SummaryAggregator summaryAggregator = new SummaryAggregator();

	public Registry(int pn){
		portNum = pn;
		cache = new RegisterConnectionCache();
		routingTable = new RoutingTable();

		try{
			this.startServer(portNum);
		}catch(IOException e){
			System.out.println("Registry: Could not start ServerThread");
			System.out.println(e);
		}
	}//End constructor

	//Called by the reciever thread whenever it recieves a message
	//This calls the appropriate method for the message type
	public synchronized void onEvent(Event e){
		//System.out.println("Registry.onEvent()");
		//System.out.println(e);
		//System.out.println(e.getBytes().length);

		int type = e.getType();
		switch(type){
			 case 2: this.onMessageTwo(e);//OVERLAY_NODE_SENDS_REGISTRATION
							 break;
			 case 4: this.onMessageFour(e);//OVERLAY_NODE_SENDS_DEREGISTRATION
							 break;
			 case 7: this.onMessageSeven(e);//NODE_REPORTS_OVERLAY_SETUP_STATUS
							 break;
			 case 10:this.onMessageTen(e);//OVERLAY_NODE_REPORTS_TASK_FINISHED
							 break;
			 case 12:this.onMessageTwelve(e);//OVERLAY_NODE_SENDS_TRAFFIC_SUMMARY
							 break;
			default: break;
		}
		//System.out.println(e);
	}//End onEvent

	//----Methods called from onEvent----
	public void onMessageTwo(Event sendsReg){
		//Turn the Event into the right message type
		OverlayNodeSendsRegistration onsr = null;
		try{
			onsr = new OverlayNodeSendsRegistration(sendsReg.getBytes(), sendsReg.getSocket());
		}catch(UnknownHostException e){
			System.out.println("Registry: Error converting event into OverlayNodeSendsRegistration");
			System.out.println(e);
		}

		//Generate response message
		RegistryReportsRegistrationStatus response;
		response = checkRegistration(onsr);
		System.out.println(response);

		//Send response
		InetAddress address = onsr.getIP();
		int port = onsr.getPort();
		String key = address.getHostAddress()/*.concat(String.valueOf(port))*/;
		RoutingEntry responseEntry = routingTable.getEntry(key);
		Connection responseConnection = responseEntry.getConnection();
		//System.out.println(response.getBytes().length);
		responseConnection.write(response.getBytes());

	}//End onMessageTwo

	public void onMessageFour(Event e){
		OverlayNodeSendsDeregistration onsd = new OverlayNodeSendsDeregistration(e.getBytes(), e.getSocket());

		//Build the key from the actual address/socket that sent the deregistration
		Socket socket = onsd.getSocket();
		InetAddress socketAddress = socket.getInetAddress();
		//(TO REMOVE)int socketPort = socket.getPort();
		String socketKey = socketAddress.getHostAddress();
		//(TO REMOVE)socketKey = socketKey.concat(String.valueOf(socketPort));

		//Pull relevant data from the message for error checking
		InetAddress messageAddress = onsd.getIP();
		//(TO REMOVE)int messagePort = onsd.getPort();
		//Build key to search cache
		String addressKey = messageAddress.getHostAddress();
		//(TO REMOVE)addressKey = addressKey.concat(String.valueOf(messagePort)); //At some point, make this it's own method so that all the keys are generated the exact same way

		//Set up reply information
		int successStatus = -1;
		String information = "Deregistration request successfull";

		//Check to make sure that the information in the cache matches
			//what is in the message
		if(!socketKey.equals(addressKey)){
			System.out.printf("socket:%s\nmessage:%s\n",socketAddress.getHostAddress(),messageAddress.getHostAddress());
			//This means that the address or the port in the message is wrong
			information = "Deregistration failed: Information in message did not match actual";
			successStatus = -1;
		}else{
			//The information was correct!
			successStatus = 1;
		}
		//Check to see if this node is already in the Routing Table
		//At this point, the successStatus is either -1 cause the info was messed up,
			//Or it's !-1 because the information was correct
		if(!routingTable.contains(onsd.getId())){
			//This node has already deregistered!
			//System.out.println(addressKey);
			//System.out.println(onsd.getId());
			//System.out.println(routingTable.contains(addressKey));
			//System.out.println(routingTable.contains(onsd.getId()));
			information = "Registration failed: Node has already deregistered";
			successStatus = -1;
		}

		//If it's all good, add this node to the table
		if(successStatus == 1){
			System.out.println("Removing routing entry from table");
			routingTable.removeEntry(onsd.getId(), addressKey);
		}

		RegistryReportsDeregistrationStatus statusMessage;
		statusMessage = new RegistryReportsDeregistrationStatus(successStatus, information);

		//Send response
		Connection responseConnection = null;
		try{
				responseConnection = cache.get(addressKey);
		}catch(ConnectionCacheException error){
			System.out.println("Registry: Error sending deregistration response");
			System.out.println(error);
		}
		//System.out.println(response.getBytes().length);
		responseConnection.write(statusMessage.getBytes());

	}//End onMessageFour

	public void onMessageSeven(Event event){
		NodeReportsOverlaySetupStatus nross = new NodeReportsOverlaySetupStatus(event.getBytes());

		String infoString = nross.getInformationString();

		System.out.println("Recieved NODE_REPORTS_OVERLAY_SETUP_STATUS: ");
		System.out.println("  " + infoString + "\n");

		if(nross.getSuccessStatus() > -1){
		countOfNodesSuccessfullySetup.getAndIncrement();
		}

		if(countOfNodesSuccessfullySetup.intValue() == routingTable.getSize()){
			System.out.println("Registry is now ready to initiate task\n\n");
		}

	}//End onMessageSeven

	public void onMessageTen(Event event){
		OverlayNodeReportsTaskFinished onrtf;
		onrtf = new OverlayNodeReportsTaskFinished(event.getBytes());

		int id = onrtf.getId();

		System.out.printf("Node %d reports task finished\n", id);

		countOfNodesFinished.getAndIncrement();

		if(countOfNodesFinished.get() == countOfNodesSuccessfullySetup.get()){
			System.out.println("All nodes finished sending");
			System.out.println("Giving nodes time to finish recieving. . .");
			try{
				Thread.sleep(20000);
			}catch(InterruptedException e){
				System.out.println("Registry: Error sleeping while waiting for messages to finish routing");
				System.out.println(e);
			}
			System.out.println("Requesting traffic summary");
			this.requestTrafficSummary();
		}
	}//End onMessageTen

	public synchronized void onMessageTwelve(Event event){
		OverlayNodeReportsTrafficSummary onrt = new OverlayNodeReportsTrafficSummary(event.getBytes());

		countOfSummariesRecieved.getAndIncrement();

		summaryAggregator.addSummary(onrt);

		if(countOfSummariesRecieved.get() == countOfNodesSuccessfullySetup.get()){
			System.out.println("All summaries recieved");
			System.out.println(summaryAggregator);
		}
	}//End onMessgeTwelve

	public void requestTrafficSummary(){
		//Generate array for the hashmap, which will be easier to go though
		RoutingEntry[] routingArray = routingTable.getAllEntriesCollection().toArray(new RoutingEntry[0]);

		for(RoutingEntry entry : routingArray){
			Connection connection = entry.getConnection();
			RegistryRequestsTrafficSummary rrts = new RegistryRequestsTrafficSummary();

			connection.write(rrts.getBytes());
		}
	}//End requestTrafficSummary

	//Generates a new, unique identifier between 0 & 127.
	private synchronized int generateId(){
		//We'll just assign them sequentially
		return routingTable.getSize();
	}//End generateID

	//Does error checking on registration messages
	//Returns message with appropriate info to be sent back to messageNode
	private RegistryReportsRegistrationStatus checkRegistration(OverlayNodeSendsRegistration onsr){
		//Get the actual Address information from the socket associated with this message
		Socket socket = onsr.getSocket();
		InetAddress socketAddress = socket.getInetAddress();
		//(TO REMOVE)int socketPort = socket.getPort();
		String socketKey = socketAddress.getHostAddress();
		//(TO REMOVE)socketKey = socketKey.concat(String.valueOf(socketPort));

		//Pull Address infomation from message for error checking
		InetAddress messageAddress = onsr.getIP();
		//(TO REMOVE)int messagePort = onsr.getPort();
		//Build key to search cache
		String addressKey = messageAddress.getHostAddress();
		//(TO REMOVE)addressKey = addressKey.concat(String.valueOf(messagePort)); //At some point, make this it's own method so that all the keys are generated the exact same way
		int successStatus = -1;
		//Set up info string so it's ready to go if registration is successful
		String information = "Registration request successfull ";
		information = information.concat("The number of messaging nodes currently in the overlay is ");
		information = information.concat(String.valueOf(cache.size()));

		//Check to make sure that the information in the cache matches
			//what is in the message
		if(!socketKey.equals(addressKey)){
			System.out.printf("socket:%s\nmessage:%s\n",socketAddress.getHostAddress(),messageAddress.getHostAddress());
			//This means that the address or the port in the message is wrong
			information = "Registration failed: Information in message did not match actual";
			successStatus = -1;
		}else{
			//The information was correct!
			successStatus = generateId();
		}
		//Check to see if this node is already in the Routing Table
		//At this point, the successStatus is either -1 cause the info was messed up,
			//Or it's !-1 because the information was correct
		if(routingTable.contains(addressKey)){
			//This node has already registered!
			information = "Registration failed: Node has already registered";
			successStatus = -1;
		}

		//If it's all good, add this node to the table
		if(successStatus != -1){
			System.out.println("Adding routing entry to table");
			Connection connection = cache.get(addressKey);
			int messagePort = onsr.getPort();
			RoutingEntry entry = new RoutingEntry(messageAddress, successStatus, messagePort, connection);
			routingTable.addEntry(entry);
		}

		RegistryReportsRegistrationStatus statusMessage;
		statusMessage = new RegistryReportsRegistrationStatus(successStatus, information);
		return statusMessage;

	}//End checkRegistration

	public void startServer(int portNum) throws IOException{
		ServerThread server = new ServerThread(portNum, this);
		server.start();
	}//End startServer

	public void spawnRecieverThread(Socket socket){
		System.out.println("I didn't implement Registry.spawnRecieverThread b/c I don't think it's neaded anymore");
	}//End spawnRecieverThread

	public ConnectionCache getConnectionCache(){
		return this.cache;
	}//End getConnection

	public void setPortNum(int pn){
		System.out.println("Registry pn: " + pn);
		portNum = pn;
	}//End setPortNum



	//These are methods called by InteractiveCommandParser
	public void listMessagingNodes(){
		Enumeration<RoutingEntry> tableElements = routingTable.getAllEntriesEnumeration();

		while(tableElements.hasMoreElements()){
			System.out.println(tableElements.nextElement());
		}
	}//End listMessagingNodes

	public void setupOverlay(int numberOfTableEntries){
		//Turn routing table into array that's easier to navigate
		RoutingEntry[] routingArray = routingTable.getAllEntriesCollection().toArray(new RoutingEntry[0]);
		//Sort array, so it can be traversed in order of the entry IDs
		Arrays.sort(routingArray);

		//Array of all the node IDs, to be sent to messagingNodes
		int[] allIds = new int[routingArray.length];
		for(int i = 0; i < routingArray.length; i++){
			allIds[i] = routingArray[i].getId();
		}

		//Go through each element in the routing table.
		for(int i = 0; i < routingArray.length; i++){
			// System.out.println(routingArray[i].getId());

			//Build list of nodes that are in this nodes routingTable
			RoutingEntry[] entryManifest;//The list of entries used to build this nodes routingTable
			entryManifest = new RoutingEntry[numberOfTableEntries];
			RoutingTable messagingNodeRoutingTable = new RoutingTable();//Routing table that will hold all these entries for later

			//Build entryManifest using routingArray's entries
			int routingEntryIndexBase = i;
			for(int n = 0; n < numberOfTableEntries; n++){
				int offset = (int)Math.pow(2,n);//1,2,4,8,16...
				//System.out.println("n: " + n + " Offset: " + offset);
				int routingEntryIndex = (routingEntryIndexBase+offset)%routingArray.length;
				entryManifest[n] = routingArray[routingEntryIndex];
				messagingNodeRoutingTable.addEntry(entryManifest[n]);
			}

			//Build ID and Address arrays for message
			int id[] = new int[numberOfTableEntries];
			InetAddress address[] = new InetAddress[numberOfTableEntries];
			int port[] = new int[numberOfTableEntries];
			for(int n = 0; n < entryManifest.length; n++){
				id[n] = entryManifest[n].getId();
				address[n] = entryManifest[n].getAddress();
				port[n] = entryManifest[n].getPort();
			}

			//Store this nodes routing table to be printed out later
			messagingNodeRoutingTables.put(routingArray[i].getId() ,messagingNodeRoutingTable);

			//Build REGISTRY_NODE_SENDS_MANIFEST message to that node
			RegistrySendsNodeManifest rsnm = new RegistrySendsNodeManifest(numberOfTableEntries
																																			, id
																																			, address
																																			, port
																																			, allIds);

			//Send message
			Connection messagingNodeConnection = routingArray[i].getConnection();
			messagingNodeConnection.write(rsnm.getBytes());
		}
	}//End setupOverlay

	public void listRoutingTables(){
		Iterator<Map.Entry<Integer, RoutingTable> > routingTableIterator = messagingNodeRoutingTables.entrySet().iterator();

		while(routingTableIterator.hasNext()){
			Map.Entry<Integer, RoutingTable> keyValue = routingTableIterator.next();
			int id = keyValue.getKey();
			System.out.println("Printing routing table for MessageNode: " + id);
			System.out.println(keyValue.getValue() + "\n\n");
		}

	}//End listRoutingTables

	public void start(int numberOfMessages){
		//An array of the routing entries from routingTable(easier to navigate than HashMap)
		RoutingEntry[] routingArray = routingTable.getAllEntriesCollection().toArray(new RoutingEntry[0]);
		//Message containing the number of messages to send
		//Will be send to every node in routingArray
		RegistryRequestsTaskInitiate rrti = new RegistryRequestsTaskInitiate(numberOfMessages);

		//Iterate over all the routingEntries
		for(RoutingEntry entry : routingArray){
			Connection connection = entry.getConnection();
			//Send message
			connection.write(rrti.getBytes());
		}

	}//End start


	public static void main(String args[]){
		//The arguement for this is the portnumber to run on
		Registry registry = new Registry(Integer.parseInt(args[0]));//args[0] being the portnum

		InteractiveCommandParser parser = new InteractiveCommandParser(registry);

		/*InetAddress addr = InetAddress.getLoopbackAddress();
		try{
			addr = InetAddress.getLocalHost();
			//addrByte = addr.getAddress();
		}catch(UnknownHostException e){
			System.out.println("Error finding address");
		}
		*/
	}//End main



}//End class
