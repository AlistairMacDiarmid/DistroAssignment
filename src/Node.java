import java.net.*;
import java.io.*;
import java.util.*;

public class Node{

    private Random ra;
    private Socket	s;
    private PrintWriter pout = null;
    private ServerSocket n_ss;
    private Socket	n_token;
    String	c_host = "127.0.0.1";
    int 	c_request_port = 7001;
    int 	c_return_port = 7002;
    String	n_host = "127.0.0.1";
    String 	n_host_name;
    int     n_port;
    
    public Node(String nam, int por, int sec){
		ra = new Random();
		n_host_name = nam;
		n_port = por;

		System.out.println("Node " + n_host + ":" + n_port + " of DME is active ....");

		try{
			n_ss = new ServerSocket(n_port);
            System.out.println("Node " + n_host + ":" + n_port + " is listening for connections");
		}catch(IOException e){
			System.err.println("Error creating server socket: " + e);
            System.exit(1);
		}

		while(true){
			try{
				//sleep
				Thread.sleep(ra.nextInt(sec)*1000L);

				//send request to coordinator
				Socket s = new Socket(c_host, c_request_port);
				PrintWriter pout = new PrintWriter(s.getOutputStream(), true);
				pout.println(n_host);  // Send node IP
				pout.println(n_port);  // Send node Port
				s.close();
				System.out.println("Node " + n_port + " sent request to coordinator");

				//wait for the token
				System.out.println("Node " + n_port + ": Waiting for token...");
				n_token = n_ss.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(n_token.getInputStream()));
				String token = in.readLine();
				n_token.close();


					System.out.println("Node " + n_host + ":" + n_port + " received token from coordinator");

					//CRITICAL SECTION
					System.out.println("Node " + n_port + ": ENTERING critical section");
					Thread.sleep(ra.nextInt(sec)*1000L);
					System.out.println("Node " + n_port + ": LEAVING critical section");

					//return the token
					s = new Socket(c_host, c_return_port);
                    pout = new PrintWriter(s.getOutputStream(),true);
                    pout.println("TOKEN_RETURNED");
                    s.close();
                    System.out.println("Node " + n_host + ":" + n_port + " sent token return to coordinator");


			}catch(InterruptedException e){
				Thread.currentThread().interrupt();
			} catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

	}

    public static void main (String args[]){
		String n_host_name = ""; 
		int n_port;
		
		// port and millisec (average waiting time) are specific of a node
		if ((args.length < 1) || (args.length > 2)){
		    System.out.print("Usage: Node [port number] [millisecs]");
		    System.exit(1);
		}
		
		// get the IP address and the port number of the node
	 	try{ 
		    InetAddress n_inet_address =  InetAddress.getLocalHost() ;
		    n_host_name = n_inet_address.getHostName();
		    System.out.println ("node hostname is " +n_host_name+":"+n_inet_address);
	    	}
	    	catch (java.net.UnknownHostException e){
		    System.out.println(e);
		    System.exit(1);
	    	} 
		
		n_port = Integer.parseInt(args[0]);
		System.out.println ("node port is "+n_port);
	    Node n = new Node(n_host_name, n_port, Integer.parseInt(args[1]));
    }
    
    
}
