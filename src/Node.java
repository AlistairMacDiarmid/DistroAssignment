import java.net.*;
import java.io.*;
import java.util.*;

public class Node{

	//random object for genertaing random sleep times
    private Random ra;

	//socket and printwriter for communication
    private Socket	s;
    private PrintWriter pout = null;

	//server socket for listening to token messages
    private ServerSocket n_ss;
    private Socket	n_token;

	//coordinator host and ports
    String	c_host = "127.0.0.1";
    int 	c_request_port = 7001; //port for sending token requests
    int 	c_return_port = 7002; //port for receiving token requests

	//node specific information
    String	n_host = "127.0.0.1"; //localhost address
    String 	n_host_name; //hostname of node
    int     n_port; // post the node listens on

	private static final String LOG_FILE = "distro_log.txt";

	


	/**
	 * Constructor - initializes the node and starts the execution loop
	 * @param nam the host name of the node
	 * @param por the port on which the node listens
	 * @param sec the wait time in milliseconds before making a request
	 */
	public Node(String nam, int por, int sec){
		//DEBUG
		logToFile("NODE " + por + ": STARTING UP");

		ra = new Random();
		n_host_name = nam;
		n_port = por;

		System.out.println("Node " + n_host + ":" + n_port + " of DME is active ....");

		//create server socket for listening to incoming token messages
		try{
			n_ss = new ServerSocket(n_port);
            System.out.println("Node " + n_host + ":" + n_port + " is listening for connections");
		}catch(IOException e){
			System.err.println("Error creating server socket: " + e);
            System.exit(1);
		}


		sleep(sec);

		//infinite loop for distributed mutual exclusion execution
		while(true){

			sleep(sec);
            //send request to coordinator for entering the critical section
            sendRequest(c_host,c_request_port,n_host,n_port);

            //wait for the token to be received by the coordinator
            waitForToken(n_port,n_token,n_ss);

            //enter the critical section
            criticalSection(sec);

            //return the token to the coordinator after execution
            returnToken(s, c_host, c_return_port, pout, n_host, n_port);

			sleep(3);


        }
	}

	private synchronized void logToFile(String message) {
		try (FileWriter fw = new FileWriter(LOG_FILE, true);
			 BufferedWriter bw = new BufferedWriter(fw);
			 PrintWriter out = new PrintWriter(bw)) {
			out.println(java.time.LocalDateTime.now() + " | " + message);
			out.flush();
		} catch (IOException e) {
			System.err.println("Log failed: " + e.getMessage());
		}
	}

	/**
	 * returns the token to the coordinator after finishing the critical section
	 * @param s socket for communication
	 * @param c_host coordinators host address
	 * @param c_return_port coordinators token return port
	 * @param pout printwriter for sending messages
	 * @param n_host node's host address
	 * @param n_port node's host port number
	 */
	public void returnToken(Socket s, String c_host, int c_return_port, PrintWriter pout, String n_host, int n_port){
		try {
			s = new Socket(c_host, c_return_port);
			pout = new PrintWriter(s.getOutputStream(), true);
			pout.println("TOKEN_RETURNED"); // notify coordinator the token is returned
			s.close();
			logToFile("NODE " + n_port + ": RETURNING token to coordinator");
			System.out.println("Node " + n_host + ":" + n_port + " sent token return to coordinator");
		}catch(IOException e) {
			System.out.println("Error returning token " + e);
		}

	}

	/**
	 * simulates execution inside the critical section
	 * @param sec duration to stay inside the critical section
	 */
	public void criticalSection(int sec){
		System.out.println("Node " + n_host + ":" + n_port + " received token from coordinator");
		logToFile("NODE " + n_port + ": ENTERING critical section");
		System.out.println("Node " + n_port + ": ENTERING critical section");
		sleep(sec); //simulate critical section work
		logToFile("NODE " + n_port + ": LEAVING critical section");
		System.out.println("Node " + n_port + ": LEAVING critical section");
	}

	/**
	 * waits for the token to be received by the coordinator
	 * @param n_port node listening port
	 * @param n_token socket for receiving the token
	 * @param n_ss serversocket used to listen for incoming messages
	 */
	public void waitForToken(int n_port, Socket n_token, ServerSocket n_ss){
		try {
			System.out.println("Node " + n_port + ": Waiting for token...");
			n_token = n_ss.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(n_token.getInputStream()));
			String token = in.readLine();
			n_token.close();
		}catch(IOException e){
			System.err.println("Error waiting for token: " + e);
		}


	}

	/**
	 * sends a request to the coordinator for entering the critical section
	 * @param c_host coordinators host address
	 * @param c_request_port coordinators request listening port
	 * @param n_host node's host address
	 * @param n_port node's port number
	 */
	public void sendRequest(String c_host, int c_request_port, String n_host, int n_port) {
		try{
			Socket s = new Socket(c_host, c_request_port);
			PrintWriter pout = new PrintWriter(s.getOutputStream(), true);
			pout.println(n_host);  // Send node IP
			pout.println(n_port);  // Send node Port
			s.close();
			System.out.println("Node " + n_port + " sent request to coordinator");
		}catch(IOException e){
			System.err.println("Error sending request: " + e);
		}

	}

	/**
	 * makes the node sleep for a random amount of time up to a specified limit
	 * @param sec maxmimum sleep time
	 */
	public void sleep(int sec){
        try {
            Thread.sleep(ra.nextInt(sec)*1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

	/**
	 * main method - initalises a node based on command-line arguments
	 * @param args command-line arguments: [port number] [max wait time]
	 */
    public static void main (String args[]){
		String n_host_name = ""; 
		int n_port;
		
		// validate the number of command-line arguments
		if ((args.length < 1) || (args.length > 2)){
		    System.out.print("Usage: Node [port number] [seconds]");
		    System.exit(1);
		}
		
		// get the hostname of the node
	 	try{ 
		    InetAddress n_inet_address =  InetAddress.getLocalHost() ;
		    n_host_name = n_inet_address.getHostName();
		    System.out.println ("node hostname is " +n_host_name+":"+n_inet_address);
	    	}
	    	catch (java.net.UnknownHostException e){
		    System.out.println(e);
		    System.exit(1);
	    	}

		//parse the port number from arguments
		n_port = Integer.parseInt(args[0]);
		System.out.println ("node port is "+n_port);

		//initalise the node with given parameters
	    Node n = new Node(n_host_name, n_port, Integer.parseInt(args[1]));
    }
    
    
}
