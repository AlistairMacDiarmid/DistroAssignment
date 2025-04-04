import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


/**
 * C_mutex handles the mutual exclusion for the coordination process
 * it grants tokens to requesting nodes and waits for token returns.
 * this class runs in a separate thread to continuously process incoming requests and manage the token granting and returning process
 */
public class C_mutex extends Thread {
    //shared buffer to store requests
    final C_buffer buffer;

    //port for received tokens from the node
    int returnPort;

    //server socket to listen for token return connections
    private ServerSocket returnSocket;

    //logger to log actions and errors
    private static final Logger logger = LogManager.getLogger();

    /**
     * Constructor - initialises the mutex process
     * @param b the shared buffer for storing requests
     * @param p the port for receiving token returns
     */
    public C_mutex(C_buffer b, int p) {
        buffer = b;
        returnPort = p;
    }



    /**
     * main execution loop for handling mutual exclusion
     * continuously process requests and grants tokens to nodes
     */
    public void run(){
        try{
            //initialise the server socket for receiving token returns
            initialiseServerSocket();

            //process incoming requests and manage token granting and returning process
            processRequests();
        }catch(Exception e){
            //log any errors that occur
            System.err.println("C:Mutex error: " + e.getMessage());
        }
    }

    /**
     * initialises the server socket to listen for token returns on the given port
     * @throws IOException if there are any issues initialising the server socket
     */
    private void initialiseServerSocket() throws IOException {
        returnSocket = new ServerSocket(returnPort);
        System.out.println("C:mutex - Initialised server socket on port " + returnPort);
    }

    /**
     * continuously process incoming requests from the shared buffer and grants tokens to nodes
     * after granting the token, wait for the token to be returned from the node
     * @throws IOException if there is an I/O error during the request processing
     * throws InterruptedException if the thread is interrupted while waiting
     */
    private void processRequests() throws IOException, InterruptedException  {
        while (true) {
            try{
                //get request from the buffer
                String[] request = buffer.getRequest();

                //grant token to the requesting node
                grantToken(request[0], Integer.parseInt(request[1]), Integer.parseInt(request[2]));

                //wait for the token to be returned from the node
                waitForReturnToken(request[0], Integer.parseInt(request[1]));
            }catch (InterruptedException e){
                //handle thread interruption and exit loop
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * waits fot the token to be returned by the node after it has been granted
     * @param nodeHost the host of the returning node
     * @param nodePort the port of the returning node
     */
    private void waitForReturnToken(String nodeHost, int nodePort) {
        try {
            //accept the return connection from the node
            Socket returnConnection = returnSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(returnConnection.getInputStream()));

           //read the response from the node
            String response = in.readLine();

            //if the response indicates a token was returned, log and print success.
            if ("TOKEN_RETURNED".equals(response)) {
                logger.info("[COORD] TOKEN_RETURNED: " + nodeHost + ":" + nodePort
                        + " | QUEUE: " + buffer.getQueueState());                System.out.println("C:mutex - Token returned by " + nodeHost + ":" + nodePort);
            }
        } catch (IOException e) {
            //log any errors that occur while waiting for the token return
            logger.severe("[COORD] TOKEN_RETURN_ERROR: " + nodeHost + ":" + nodePort
                    + " | " + e.getMessage());        }
    }


    /**
     * grant the token to the requesting node
     * @param nodeHost the host of the requesting node
     * @param nodePort the port of the requesting node
     * @param priority the priority of the requesting node
     */
    private void grantToken(String nodeHost, int nodePort, int priority) {
        try {
            //establish a connection to the requesting node
            Socket nodeSocket = new Socket(nodeHost, nodePort);
            PrintWriter out = new PrintWriter(nodeSocket.getOutputStream(), true);

            //send the token grant message to the node
            out.println("TOKEN_GRANTED");

            //log and print the token granting action
            logger.info("[COORD] TOKEN_GRANTED: " + nodeHost + ":" + nodePort
            + " (Priority " + priority
            + ") | QUEUE: " + buffer.getQueueState());
            System.out.println("C:mutex - Token granted to " + nodeHost + ":" + nodePort);
        } catch (IOException e) {
            //log any errors that occur while granting the token
            logger.severe("[COORD] TOKEN_GRANT_ERROR: " + nodeHost + ":" + nodePort
                    + " | " + e.getMessage());
        }
    }


}