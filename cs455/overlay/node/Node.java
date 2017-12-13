package cs455.overlay.node;

//Author: Tiger Barras
//Node class. Will be inherited by Registry and MessagingNode classes

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.ArrayList;
import cs455.overlay.wireformats.Event;
import cs455.overlay.transport.ConnectionCache;

public interface Node{

  //This is what will get called when something happens
  //Such as a message coming in, or a new link being opened
  public void onEvent(Event e);

  //Listens at a specific port, and creates new RecieverThreads when
  //  it creates sockets
  public void startServer(int portNum) throws IOException;

  //Spans a Reciever thread that is linked to the specified socket
  public void spawnRecieverThread(Socket socket);

  //Returns the ConnectionCache used by this node
  public ConnectionCache getConnectionCache();

  public void setPortNum(int pn);

}
