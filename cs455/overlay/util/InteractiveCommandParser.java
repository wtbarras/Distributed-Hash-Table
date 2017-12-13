package cs455.overlay.util;

/*
 *Author: Tiger Barras
 *InteractiveCommandParser.java
 *Parses the commands for nodes
 */

import cs455.overlay.node.Node;
import cs455.overlay.node.Registry;
import cs455.overlay.node.MessageNode;

import java.util.Scanner;
import java.lang.NumberFormatException;

public class InteractiveCommandParser{

	Registry registry;
	MessageNode messageNode;
	int flag;//Lets the parser know whether it's parsing for a MessageNode or a Registry
				//Zero for Register, one for MessageNode

	public InteractiveCommandParser(Registry r){
		registry = r;

		this.registryListen();
	}//End constructor

	public InteractiveCommandParser(MessageNode mn){
		messageNode = mn;

		this.messageNodeListen();
	}//End constructor

	public void registryListen(){
		System.out.println("Starting command parser");
		Scanner sc = new Scanner(System.in);

		String input;

		while(true){//Just sit in this loop and listen for input
			System.out.print(">> ");
			input = sc.next();
			parseRegistry(input, sc);
		}
	}//End listen

	//The scanner gets passed in here so command arguements can be read
	public void parseRegistry(String input, Scanner sc){
		switch(input){
			//Registry commands
			case "list-messaging-nodes":
					this.registry.listMessagingNodes();
					break;
			case "setup-overlay":
					int numberOfRoutingTableEntries = this.readArgument(sc, "setup-overlay");
					this.registry.setupOverlay(numberOfRoutingTableEntries);
					break;
			case "list-routing-tables":
					this.registry.listRoutingTables();
					break;
			case "start":
					int numberOfMessages = this.readArgument(sc, "start");
					this.registry.start(numberOfMessages);
					break;
			default:
					System.out.println("Not a valid command");
		}
	}//End parseRegistry

	public void messageNodeListen(){
		System.out.println("Starting command parser");
		Scanner sc = new Scanner(System.in);

		String input;

		while(true){//Just sit here and listen for input
			System.out.print(">> ");
			input = sc.next();
			parseMessageNode(input);
		}
	}//End MessageNodeListen

	public void parseMessageNode(String input){
		switch(input){
			//Registry commands
			case "exit-overlay":
					this.messageNode.exitOverlay();
					break;
			case "pcad": //Shorthand for nest case
			case "print-counters-and-diagnostics":
					this.messageNode.printSummary();
					break;
			default:
					System.out.println("Not a valid command");
		}
	}//End parseMessageNode

	//Reads a single integer arguement from the command line
	//The string arguement is the name of the command this is reading for
	private int readArgument(Scanner sc, String callingCommand){
		int arguement = -1;
		try{
			arguement = Integer.parseInt(sc.next());
		}catch(NumberFormatException e){
			System.out.printf("The next token after '%s' needs to be an integer\n", callingCommand);
		}

		return arguement;
	}



}
