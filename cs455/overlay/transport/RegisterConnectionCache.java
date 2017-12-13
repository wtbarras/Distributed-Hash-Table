package cs455.overlay.transport;
/*
 *Author: Tiger Barras
 *RegisterConnectionCache.java
 *Wrapper for a socket array that holds all the nodes that talk to the register
 */

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import cs455.overlay.exception.ConnectionCacheException;
import cs455.overlay.transport.Connection;

public class RegisterConnectionCache implements ConnectionCache{

	ConcurrentHashMap<String,Connection> cache = new ConcurrentHashMap<String, Connection>();

	public void add(String index, Connection c){
		cache.put(index,c);
	}//End add

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

	public boolean contains(String key){
		return cache.contains(key);
	}


}//End class
