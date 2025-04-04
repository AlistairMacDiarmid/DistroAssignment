import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * C_Connection_r handles the connection from a requesting node.
 * it will read the request from the socket, save it to a shared buffer, logs the operation and finally closes the connection
 * class extends Thread to allow each connection to be processed concurrently
 */

public class C_Connection_r extends Thread {

	//constants for request indices: Node IP, Node Port and Node Priority
	private static final int NODE = 0; //node IP index
	private static final int PORT = 1; //node port index
	private static final int PRIORITY = 2; // priority index

	//shared buffer for storing requests
	private final C_buffer buffer;

	//socket connected to the requesting node
	private final Socket socket;

	//buffered reader for reading input from the socket
	private BufferedReader reader;

	//logger for logging actions and errors
	private static final Logger logger = LogManager.getLogger();

	/**
	 * constructor to initialise the connection handler
	 * @param socket the socket connected to the requesting node
	 * @param buffer the shared buffer for storing requests
	 */
	public C_Connection_r(Socket socket, C_buffer buffer) {
		this.socket = socket;
		this.buffer = buffer;

	}

	/**
	 * main execution method of the thread
	 * reads a request from the socket, saves it to the buffer, logs the action
	 * and finally closes the socket connection
	 */
	public void run() {
		System.out.println("C:connection IN dealing with request from socket " + socket);
		try {
			//read the request from the socket
			String[] request = readRequest();
			if (request != null) {
				//log the state of the queue before adding the new request to the queue
				logger.info("[COORD] PRE_ADD_QUEUE: Size=" + buffer.size() + " | Current=" + buffer.getQueueState());

				//save the request to the buffer
				buffer.saveRequest(request);

				//log the new request after adding it to the queue
				logger.info("[COORD] NEW_REQUEST: " + request[NODE] + ":" + request[PORT] +
						" Priority: " + request[PRIORITY]);
				logger.info("[COORD] POST_ADD_QUEUE: Added " + request[PORT] + "(P" + request[PRIORITY] +
						") | New size=" + buffer.size() + " | Queue=" + buffer.getQueueState());
			}
		} catch (IOException e) {
			//log any errors
			logger.severe("[COORD] ERROR: " + e.getMessage());
		} finally {
			//close the connection, even if an error occurred
			closeConnection();
		}
	}

	/**
	 * reads the request (node IP, port and priority) from the socket.
	 * @return a string array containing node IP, nodePort and the priority [node IP, node port, node priority]
	 * @throws IOException if there are any issue reading from the socket
	 */
	private String[] readRequest() throws IOException {
		//initialise the reader for reading input from the sockets input stream
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		//create an array to store the node IP, port and priority
		String[] request = new String[3];
		request[NODE] = reader.readLine();//read node host/IP
		request[PORT] = reader.readLine(); //read node port
		request[PRIORITY] = reader.readLine(); //read node priority

		//check if the request data is valid (not null)
		if (request[NODE] == null || request[PORT] == null || request[PRIORITY] == null) {
			System.err.println("C:connection ERROR: Invalid request received!");
			return null;
		}

		//return the request data as a string array
		return request;
	}



	/**
	 * closes the socket connection after processing the request.
	 * ensures that resources are released and the connection is properly terminated
	 */
	private void closeConnection() {
		try {
			//if the socket is not null, close it
			if (socket != null) {
				socket.close();
				System.out.println("C:connection OUT    socket closed");
			}
		} catch (IOException e) {
			//log error if there is an issue closing socket
			System.err.println("C:connection ERROR closing socket: " + e.getMessage());
		}
	}

}

