package cs455.overlay.transport;
//Author: Tiger Barras
//ServerThread.java
//Waits for incoming connections, then opens a socket with them

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import cs455.overlay.transport.RecieverThread;
import cs455.overlay.node.Node;

//I think this needs to run on port 0, which will make it find an acceptable port

public class ServerThread extends Thread{

	Node node; //The node that started this ServerThread
	ServerSocket serverSocket;
	int portNum; //Port that the ServerThread object will listen on
	ConnectionCache cache; //Shared object with the node that holds connections

	//These are just used for messageWithId() to give some context to the message
	long id;
	String name;

	public ServerThread(int pn, Node n)throws IOException{ //Port number to listen to, cache to add sockets to
		node = n;
		serverSocket = new ServerSocket(pn);
		this.portNum = serverSocket.getLocalPort();
		System.out.println("ServerThread port: " + this.portNum);
		node.setPortNum(this.portNum);//Report back to node where you are
		cache = node.getConnectionCache();

		id = this.getId();
		name = this.getName();
	}//End constructor

	//This is where execution begins when this thread is created
	public void run(){
		Socket socket;
		try{ //Listen at port portNum, and open socket to an incoming connection
			while(true){
				//messageWithId("Ready to connect. . .");
				socket = serverSocket.accept();
				//messageWithId("Socket Generated");
				Connection connection = new Connection(node, socket);
				//messageWithId("Connection Generated");
				//Key is the address of the sender
				//This will probably break if you have more than one node running on a machine
				String index = socket.getInetAddress().getHostAddress();
				///(TO REMOVE)index = index.concat(String.valueOf(socket.getPort()));
				//System.out.println("ServerThread: Adding connection w/ key: " + index);
				cache.add(index, connection);
				//messageWithId("Connection added to ConnectionCache");
				//Open up new Connection
			}
		}catch(IOException e){
			messageWithId("Error opening Server Socket");
			messageWithId("Error: " + e);
			System.exit(-1);
		}//End try/catch
	}//End run

	public int getPortNum(){
		return portNum;
	}//End getPortNum

	private void messageWithId(String message){
		System.out.printf("Thread (%s:%d): %s\n", name, id, message);
	}//End messageWithId

}//End class
