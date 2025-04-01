import java.net.*;

public class Coordinator {
	
    public static void main (String args[]){
		int port = 7001;
		
		Coordinator c = new Coordinator ();
		
		try {    
		    InetAddress c_addr = InetAddress.getLocalHost();
		    String c_name = c_addr.getHostName();
		    System.out.println ("Coordinator address is "+c_addr);
		    System.out.println ("Coordinator host name is "+c_name+"\n\n");    
		}
		catch (Exception e) {
		    System.err.println(e);
		    System.err.println("Error in corrdinator");
		}
				
		// allows defining port at launch time
		if (args.length == 1) port = Integer.parseInt(args[0]);
	
		// Create and run a C_receiver and a C_mutex object sharing a C_buffer object
		C_buffer buffer = new C_buffer();
		C_receiver receiver = new C_receiver(buffer, port);
		C_mutex mutex = new C_mutex(buffer, 7002); //use 7001 for token returns

        receiver.start();
		mutex.start();

		// Wait for the threads to finish
		try{
			receiver.join();
            mutex.join();
		}catch (InterruptedException e){
			System.out.println("Coordinator - Main thread interrupted");
		}

    }
    
}
