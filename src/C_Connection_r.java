import java.net.*;
import java.io.*;



/**
 * handles an incoming request from a node
 * reads the nodes IP and port, stores them in the buffer, and logs the request
 */
public class C_Connection_r extends Thread{

	//constants for request array indexing
	private static final int NODE = 0; //node IP index
	private static final int PORT = 1; //node port index

    // instance variables
    private final C_buffer buffer; //shared buffer for storing requests
	private final Socket socket; //socket connected to the requesting node
    InputStream    in;
  	private BufferedReader reader; //buffered reader for reading input


	/**
	 * constructor to initialise the connection handler
	 * @param socket the socket connected to the requesting node
	 * @param buffer the shared buffer for storing requests
	 */
	public C_Connection_r(Socket socket, C_buffer buffer){
    	this.socket = socket;
    	this.buffer = buffer;
    }

	/**
	 * main execution method of the thread
	 * reads a request, saves it, logs it, and closes the connection
	 */
	public void run() {
		System.out.println("C:connection IN  dealing with request from socket " + socket);
		try {
			String[] request = readRequest(); //read request from socket
			if (request != null) {
				saveRequest(request);  //store request in buffer
				logRequest(request);   //log received request
			}
		} catch (IOException e) {
			System.err.println("C:connection ERROR: " + e.getMessage());
			e.printStackTrace();
		} finally {
			closeConnection(); // Ensure socket is closed
		}
		buffer.show(); // Display current buffer state
	}

	/**
	 * reads the request from the socket (node IP and port)
	 * @return a string array containing node IP and nodePort [node IP, node port]
	 * @throws IOException if there is any issue reading from the socket
	 */
	private String[] readRequest() throws IOException {
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		String[] request = new String[2];
		request[NODE] = reader.readLine(); //read node host/ip
		request[PORT] = reader.readLine(); //read node port

		if (request[NODE] == null || request[PORT] == null) {
			System.err.println("C:connection ERROR: Invalid request received!");
			return null;
		}

		return request;
	}

	/**
	 * stores the received request in the shared buffer
	 * @param request the request array
	 */
	private void saveRequest(String[] request) {
		buffer.saveRequest(request);
	}

	/**
	 * closes the socket connection after processing the request
	 */
	private void closeConnection() {
		try{
			if(socket!=null){
				socket.close();
                System.out.println("C:connection OUT    socket closed");
			}
		} catch (IOException e) {
			System.err.println("C:connection ERROR closing socket: " + e.getMessage());
		}
	}


	/**
	 * logs details about the received request.
	 * @param request the request array
	 */
	private void logRequest(String[] request) {
		System.out.println("C:connection OUT received and recorded request from " +
				request[NODE] + ":" + request[PORT] + " (socket closed)");
	}

//	public void run() {
//		final int NODE = 0;
//		final int PORT = 1;
//
//		String[] request = new String[2];
//
//		System.out.println("C:connection IN  dealing with request from socket "+ socket);
//		try {
//
//		    // >>> read the request, i.e. node ip and port from the socket s
//		    in = socket.getInputStream();
//		    reader = new BufferedReader(new InputStreamReader(in));
//
//		    request[NODE] = reader.readLine(); //read node host/ip
//		    request[PORT] = reader.readLine(); //read node port
//
//			// >>> save it in a request object and save the object in the buffer (see C_buffer's methods).
//			buffer.saveRequest(request);
//
//		    socket.close();
//		    System.out.println("C:connection OUT    received and recorded request from "+ request[NODE]+":"+request[PORT]+ "  (socket closed)");
//
//		}
//		catch (java.io.IOException e){
//				System.out.println(e);
//				System.exit(1);
//		}
//		buffer.show();
//
// 	}
}
