import java.io.IOException;
import java.net.*;
import java.util.logging.Logger;

/**
 * C_receiver listens for incoming connection requests from nodes
 * when a request in received, it spans a new thread - C_Connection_r
 * to handle the request asynchronously
 */
public class C_receiver extends Thread{

	//shared buffer for storing the incoming requests
    private final C_buffer 	buffer;

	//port number to listen for incoming requests
    private final int port;

	//server socket to listen for  connections
    private ServerSocket serverSocket;

	//logger for logging actions and errors
	private static final Logger logger = LogManager.getLogger();


	/**
	 * constructor to initialise receiver with a buffer and port
	 * @param buffer the shared buffer to store incoming requests
	 * @param port the port number on which to listen for incoming connections
	 */
    public C_receiver (C_buffer buffer, int port) {
		this.buffer = buffer;
		this.port = port;

	}

	/**
	 * main execution method of the thread
	 * initialises the server socket and continuously listens for connections
	 */
	public void run(){
		//initialise the server socket and listen for connections
		if(!initialiseServerSocket()){
			return; //exit if socket initialisation fails
		}
		acceptConnections(); //listen for client communication
	}

	/**
	 * initialises the server socket to listen on a specified port
	 * @return TRUE if successful, FALSE otherwise
	 */
	private boolean initialiseServerSocket() {
		try{
			//initialise the server socket on the specified port
			serverSocket = new ServerSocket(port);
			logger.info("[COORD] LISTENING: port=" + port);
			return true;
		} catch (IOException e) {
			//log errors that occur when initialisation fails
			logger.severe("[COORD] SOCKET_ERROR: " + e.getMessage());
			return false;
		}
	}

	/**
	 * continuously listens for incoming connection and spawns a new thread
	 * to handle each request asynchronously
	 */
	private void acceptConnections() {
		while (true) {
			try {
				//accept incoming connection
				Socket clientSocket = serverSocket.accept();
				System.out.println("C:receiver - Received a request from a node");

				//handle client requests by spawning a new thread
				handleClientRequest(clientSocket);
			} catch (IOException e) {
				//log any errors that occur while accepting the connection
				System.err.println("C:receiver ERROR - Exception while accepting connection: " + e.getMessage());
			}
		}
	}

	/**
	 * handles incoming client request by spawning a new thread.
	 * new thread will handle the request asynchronously
	 * @param clientSocket the socket connection from a node
	 */
	private void handleClientRequest(Socket clientSocket) {
		//create new connection handler thread to manage the client request
		C_Connection_r connectionThread = new C_Connection_r(clientSocket, buffer);

		//start the new thread to handle the request
		connectionThread.start();
	}

}
