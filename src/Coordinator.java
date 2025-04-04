import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Coordinator classs is the central coordinator in the distributed mutual exclusion system
 * it manages request reception, token distribution and synchronization between processes
 */
public class Coordinator {

	//default ports for communication
	private int requestPort = 7001;  // default port for receiving requests from processes
	private int returnPort = 7002;   // port for receiving returned tokens


	//coordinator components
	private C_buffer buffer;// buffer to store requests
	private C_receiver receiver; //thread for receiving incoming requests
	private C_mutex mutex; // thread for managing mutual exclusion and tokens granting

	//logger for logging events and errors
	private static final Logger logger = LogManager.getLogger();

	/**
	 * constructor - initialises the coordinator with optional request port.
	 * @param port the port number for receiving requests (default is 7001)
	 */
	public Coordinator (int port) {
		LogManager.clearLogs(); //clear the log each time the coordinator is initialized
		this.requestPort = port; //set the request port
		this.buffer = new C_buffer(); //initialise the shared buffer
		this.receiver = new C_receiver(buffer, requestPort); //initialise the receiver thread
		this.mutex = new C_mutex(buffer, returnPort); //initialise the mutex thread

		//log that the coordinator has been started
		logger.info("[COORD] STARTED: port=" + port);

	}


	/**
	 * display the hostname and ip of the coordinator
	 */
	private void displayCoordinatorInfo(){
		try {
			//get the local address and hostname of the coordinator
			InetAddress c_addr = InetAddress.getLocalHost();
			String c_name = c_addr.getHostName();

			//log and display the coordinators information
			logger.info("[COORD] HOST_INFO: addr=" + c_addr.getHostAddress()
					+ ", name=" + c_addr.getHostName());
			System.out.println ("Coordinator address is "+c_addr);
			System.out.println ("Coordinator host name is "+c_name+"\n\n");
		}
		catch (Exception e) {
			//log any errors that occur while getting the local address and hostname
			logger.severe("[COORD] HOST_INFO_ERROR: " + e.getMessage());
		}

	}

	/**
     * starts the threads for receiving requests and managing the mutex
     */
	private void startThreads() {
		//start the threads for receiving requests and managing the mutex
		receiver.start();
		mutex.start();
	}

	/**
     * waits for the threads to finish before exiting the main thread
     */
	private void waitForThreads(){
		try{
			//wait for the threads to complete
			receiver.join();
			mutex.join();
		}catch (InterruptedException e){
			//log interruption of the main thread
			System.out.println("Coordinator - Main thread interrupted");
		}
	}

	/**
	 * main method - create a coordinator instance and runs it
	 * @param args optional command-line arguments to specify the request port
	 */
    public static void main (String args[]){
		int port = 7001;//default request port

		// allow user to specify a different port via command-line argument
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}

		// initialize and run the Coordinator with specified port
		Coordinator coordinator = new Coordinator(port);
		coordinator.displayCoordinatorInfo(); //display coordinator information
		coordinator.startThreads(); //start threads for handling requests and mutex
		coordinator.waitForThreads(); // wait for threads to finish before exiting

    }
    
}
