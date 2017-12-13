package cs455.overlay.exception;
/*
*Author: Tiger Barras
*ConnectionCacheException
*This will be thrown when the cache does not function properly
*/

public class ConnectionCacheException extends RuntimeException{

	String error = "Error in ConnectionCache.\nIs the connection you're looking for in this cache?";

	public ConnectionCacheException(){}

	public ConnectionCacheException(String e){
		error = e;
	}//End constructor

	public String toString(){
		return error;
	}//End toString

}//End class
