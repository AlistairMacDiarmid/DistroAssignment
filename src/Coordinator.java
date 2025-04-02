import java.net.*;

public class Coordinator {


	private int requestPort = 7001;  // default port for receiving requests
	private int returnPort = 7002;   // port for returning the token
	private C_buffer buffer;
	private C_receiver receiver;
	private C_mutex mutex;


	/**
	 * constructor - initalises the coordinator with optional request port.
	 * @param port the port number for receiving requests (default is 7001)
	 */
	public Coordinator (int port) {
		this.requestPort = port;
		this.buffer = new C_buffer();
		this.receiver = new C_receiver(buffer, requestPort);
		this.mutex = new C_mutex(buffer, returnPort);
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
