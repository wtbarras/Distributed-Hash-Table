package cs455.overlay.transport;
//Author: Tiger Barras
//ConnectionCache.java
//Holds a collection of Connections.
//This will be inherited by two classes:
//  RegisterConnectionCache
//  NodeConnectionCache
//These will have error/sanity checking
//  e.g. NodeConnectionCache will no let you have more that four connections

//This is SHORT TERM storage. If a connection is valid, it will be added to the
  //node's routing table for long term storage/retrieval

import cs455.overlay.transport.Connection;

public interface ConnectionCache{

	//public ConnectionCache();

	public void add(String index, Connection c);

	public Connection get(String index);

	public int size();

}//End class
