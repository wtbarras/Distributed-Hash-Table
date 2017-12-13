package cs455.overlay.transport;
/*
*Author: Tiger Barras
*NodeConnectionCache.java
*Wrapper for a port:Connection HashMap that holds all the nodes that this node can talk to
*/

//import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import cs455.overlay.transport.Connection;
import cs455.overlay.exception.ConnectionCacheException;

public class NodeConnectionCache implements ConnectionCache{

	ConcurrentHashMap<String,Connection> cache = new ConcurrentHashMap<String, Connection>();

	public void add(String index, Connection c)throws ConnectionCacheException{
		if(cache.size() > 4){
			throw new ConnectionCacheException("Cannot have more that 4 nodes in NodeConnectionCache");
		}else{
			cache.put(index,c);
		}
	}//End add

	//Make this throw a ConnectionCacheException
	public Connection get(String index) throws ConnectionCacheException{
		if(!cache.containsKey(index)){
			throw new ConnectionCacheException("Index not found in cache");
		}else{
			Connection toReturn = cache.get(index);
			return toReturn;
		}
		//return new Socket();
	}//End get

	public int size(){
		return cache.size();
	}//End size


}//End class
