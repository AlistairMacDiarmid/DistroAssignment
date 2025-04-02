import java.io.*;
import java.net.Socket;

public class C_Connection_r extends Thread {

	private static final int NODE = 0; //node IP index
	private static final int PORT = 1; //node port index

	private final C_buffer buffer; //shared buffer for storing requests
	private final Socket socket; //socket connected to the requesting node
	private BufferedReader reader; //buffered reader for reading input

	private static final Object lock = new Object(); // lock object to synchronize thread access
	private static final String LOG_FILE = "distro_log.txt";


	/**
	 * constructor to initialise the connection handler
	 * @param socket the socket connected to the requesting node
	 * @param buffer the shared buffer for storing requests
	 */
	public C_Connection_r(Socket socket, C_buffer buffer) {
		this.socket = socket;
		this.buffer = buffer;
	}

	private synchronized void logToFile(String message) {
		try (FileWriter fw = new FileWriter(LOG_FILE, true);
			 BufferedWriter bw = new BufferedWriter(fw);
			 PrintWriter out = new PrintWriter(bw)) {
			out.println(java.time.LocalDateTime.now() + " | " + message);
		} catch (IOException e) {
			System.err.println("Logging failed: " + e.getMessage());
		}
	}

	/**
	 * main execution method of the thread
	 * reads a request, saves it, logs it, and closes the connection
	 */
	public void run() {
		System.out.println("C:connection IN  dealing with request from socket " + socket);
		try {
			// Read the request from the socket
			String[] request = readRequest();
			if (request != null) {
				// Synchronize access to the shared buffer to preserve FIFO order
				synchronized (lock) {
					saveRequest(request);  // Store the request in the buffer
					logRequest(request);    // Log the received request
				}
			}
		} catch (IOException e) {
			System.err.println("C:connection ERROR: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Close the socket connection after processing
			closeConnection();
		}
		// Show the current state of the buffer (you can optionally log this as well)
		buffer.show();
	}

	/**
	 * reads the request (node IP and port) from the socket.
	 * @return a string array containing node IP and nodePort [node IP, node port]
	 * @throws IOException if there is any issue reading from the socket
	 */
	private String[] readRequest() throws IOException {
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		String[] request = new String[2];
		request[NODE] = reader.readLine(); // Read node host/ip
		request[PORT] = reader.readLine(); // Read node port

		if (request[NODE] == null || request[PORT] == null) {
			System.err.println("C:connection ERROR: Invalid request received!");
			return null;
		}

		return request;
	}

	/**
	 * stores the received request in the shared buffer.
	 * @param request the request array
	 */
	private void saveRequest(String[] request) {
		buffer.saveRequest(request);
	}

	/**
	 * closes the socket connection after processing the request.
	 */
	private void closeConnection() {
		try {
			if (socket != null) {
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
		String logMsg = "COORDINATOR: Received request from " + request[0] + ":" + request[1] + " | Queue size: " + (buffer.size()/2);
		logToFile(logMsg);
		System.out.println("C:connection OUT " + logMsg);
	}
}

