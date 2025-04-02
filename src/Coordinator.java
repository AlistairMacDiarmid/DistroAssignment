import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;

public class Coordinator {


	private int requestPort = 7001;  // default port for receiving requests
	private int returnPort = 7002;   // port for returning the token
	private C_buffer buffer;
	private C_receiver receiver;
	private C_mutex mutex;

	private static final String LOG_FILE = "distro_log.txt";



	/**
	 * constructor - initalises the coordinator with optional request port.
	 * @param port the port number for receiving requests (default is 7001)
	 */
	public Coordinator (int port) {

		// Reset log file when coordinator starts
		try (FileWriter fw = new FileWriter(LOG_FILE, false)) {
			fw.write(""); // Truncate the file
			logToFile("COORDINATOR STARTED | Port: " + port + " | Time: " + java.time.LocalDateTime.now());
		} catch (IOException e) {
			System.err.println("Failed to reset log file: " + e.getMessage());
		}

		this.requestPort = port;
		this.buffer = new C_buffer();
		this.receiver = new C_receiver(buffer, requestPort);
		this.mutex = new C_mutex(buffer, returnPort);
    }

	private synchronized void logToFile(String message) {
		try (FileWriter fw = new FileWriter(LOG_FILE, true);
			 BufferedWriter bw = new BufferedWriter(fw);
			 PrintWriter out = new PrintWriter(bw)) {
			out.println(java.time.LocalDateTime.now() + " | " + message);
		} catch (IOException e) {
			System.err.println("Error writing to log file: " + e.getMessage());
		}
	}

	/**
	 * display the hostname and ip of the coordinator
	 */
	private void displayCoordinatorInfo(){
		try {
			InetAddress c_addr = InetAddress.getLocalHost();
			String c_name = c_addr.getHostName();
			System.out.println ("Coordinator address is "+c_addr);
			System.out.println ("Coordinator host name is "+c_name+"\n\n");
		}
		catch (Exception e) {
			System.err.println("Error retrieving coordinator details");
		}

	}

	/**
     * starts the threads for receiving requests and managing the mutex
     */
	private void startThreads() {
		receiver.start();
		mutex.start();
	}

	/**
     * waits for the threads to finish before exiting the main thread
     */
	private void waitForThreads(){
		try{
			receiver.join();
			mutex.join();
		}catch (InterruptedException e){
			System.out.println("Coordinator - Main thread interrupted");
		}
	}

	/**
	 * main method - create a coordinator instance and runs it
	 * @param args optional command-line arguments to specify the request port
	 */
    public static void main (String args[]){
		int port = 7001;  // Default request port

		// allow user to specify a different port via command-line argument
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}

		// initialize and run the Coordinator
		Coordinator coordinator = new Coordinator(port);
		coordinator.displayCoordinatorInfo();
		coordinator.startThreads();
		coordinator.waitForThreads();

    }
    
}
