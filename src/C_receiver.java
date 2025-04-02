import java.io.IOException;
import java.net.*;

/**
 * listens for incoming connection requests from nodes
 * when a request is received, it spawns a new thread (c_connection_r)
 * to habd the request asynchronously
 */
public class C_receiver extends Thread{

    private final C_buffer 	buffer; // shared buffer for request storage
    private final int port; // port to listen for requests
    private ServerSocket 	serverSocket; //server socket that listesns for connections


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
		if(!initialiseServerSocket()){
			return; //exit if socket init fails
		}
		acceptConnections(); //listen for client communication
	}

	/**
	 * initialises the server socket to listen on a specified port
	 * @return TRUE if successful, FALSE otherwise
	 */
	private boolean initialiseServerSocket() {
		try{
			serverSocket = new ServerSocket(port);
			System.out.println("C:receiver - listening on port " + port);
			return true;
		} catch (IOException e) {
			System.err.println("C:receiver error - failed to create server socket: " + e.getMessage());
			return false;
		}
	}

	/**
	 * continuously listens for incoming connection and spawns a new thread to handle each request
	 */
	private void acceptConnections() {
		while (true) {
			try {
				//accept incoming connection
				Socket clientSocket = serverSocket.accept();
				System.out.println("C:receiver - Received a request from a node");

				//handle request in a separate thread
				handleClientRequest(clientSocket);
			} catch (IOException e) {
				System.err.println("C:receiver ERROR - Exception while accepting connection: " + e.getMessage());
			}
		}
	}

	/**
	 * handles incoming client request by spawning a new thread
	 * @param clientSocket the socket connection from a node
	 */
	private void handleClientRequest(Socket clientSocket) {
		C_Connection_r connectionThread = new C_Connection_r(clientSocket, buffer);
		connectionThread.start();
	}

//    public void run () {
//	// >>> create the socket the server will listen to
//		try{
//			s_socket = new ServerSocket(port);
//			System.out.println("C:receiver - Listening on port " + port);
//
//			while (true) {
//				try{
//					//get a new connection
//					socketFromNode = s_socket.accept();
//					System.out.println("C:receiver - Coordinator has received a request");
//
//					//create a separate thread to service the request
//					connect = new C_Connection_r(socketFromNode, buffer);
//					connect.start();
//				}catch(IOException e){
//					System.out.println("Exception when creating a connection "+e);
//				}
//			}//end while
//		}catch(IOException e){
//			System.err.println("Error creating server socket: " + e);
//		}
//    }//end run
}
