package cs455.overlay.routing;

/*
 *Author: Tiger Barras
 *RoutingEntry.java
 *Holds RoutingEntries in long term storage
 */

import java.util.concurrent.ConcurrentHashMap;
import java.util.Enumeration;
import java.util.Collection;
import cs455.overlay.routing.RoutingEntry;

public class RoutingTable{

	//Gets the cache passed in
	//Links the addresses in there to the routingEntries that it also holds
	//This lets us associate IDs with addresses

	/*
	Made a design choice here
	I wanted to be able to index my table by both ID and address+port
	Two hash maps may take more memory, but saves on wasted search cycles
	*/
	ConcurrentHashMap<Integer, RoutingEntry> idTable = new ConcurrentHashMap<Integer, RoutingEntry>();
	ConcurrentHashMap<String,  RoutingEntry> addressTable = new ConcurrentHashMap<String, RoutingEntry>();

	//Don't need a constructor other than null constructor

	//Associate an Id with an entry in the table
	public synchronized void addEntry(RoutingEntry entry){
		//Key for idTable
		int idKey = entry.getId();
		//Key for addressTable
		String addressKey = entry.getAddress().getHostAddress();
		//String port = String.valueOf(entry.getPort());
		//String addressKey = address.concat(port);

		//System.out.println("Added entry with id " + idKey + " and address " + addressKey);

		idTable.put(idKey, entry);
		addressTable.put(addressKey, entry);
	}//End addEntry

	public void removeEntry(Integer id, String addressKey){
		idTable.remove(id);
		addressTable.remove(addressKey);
	}//End removeEntry

	public synchronized int getSize(){
		return idTable.size();
	}

	//Check if a connection is in the table, searching by id
	public boolean contains(int idKey){
		return idTable.containsKey(idKey);
	}//End contains
	//Check if a connection is in the table, searching by address+port
	public boolean contains(String addressKey){
		return addressTable.containsKey(addressKey);
	}//End contains

	public RoutingEntry getEntry(int idKey){
		return idTable.get(idKey);
	}//End getEntry
	public RoutingEntry getEntry(String addressKey){
		return addressTable.get(addressKey);
	}//End getEntry

	public Enumeration<RoutingEntry> getAllEntriesEnumeration(){
		return addressTable.elements();
	}//End getAllEntries

	public Collection<RoutingEntry> getAllEntriesCollection(){
		return addressTable.values();
	}//End getAllEntries

	public String toString(){
		String toReturn = "";
		Enumeration<RoutingEntry> elements = addressTable.elements();

		while(elements.hasMoreElements()){
			toReturn = toReturn.concat(elements.nextElement().toString() + "\n");
		}

		return toReturn;
	}//End toString

}//End class
