import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class C_mutex extends Thread {
    final C_buffer buffer;
    int returnPort;
    private ServerSocket returnSocket;

    private static final Logger logger = LogManager.getLogger();

    /**
     * Constructor - initalises the mutex process
     * @param b the shared buffer for storing requests
     * @param p the port for receiving token returns
     */
    public C_mutex(C_buffer b, int p) {
        buffer = b;
        returnPort = p;
    }



    /**
     * main execution loop for handling mutual exclusion
     */
    public void run(){
        try{
            initialiseServerSocket();
            processRequests();
        }catch(Exception e){
            System.err.println("C:Mutex error: " + e.getMessage());
        }
    }

    /**
     * initialises the server socket for receiving token returns
     * @throws IOException if an I/O error occurs
     */
    private void initialiseServerSocket() throws IOException {
        returnSocket = new ServerSocket(returnPort);
        System.out.println("C:mutex - Initialised server socket on port " + returnPort);
    }

    /**
     * continuously process incoming requests and grants tokens
     * @throws IOException if I/O error occurs
     */
    private void processRequests() throws IOException, InterruptedException {
        while (true) {
            String[] request;
            synchronized (buffer) {
                // Wait until at least one full request exists
                while (buffer.size() < 2) {
                    buffer.wait();  // Releases lock and waits
                }
                request = buffer.getRequest();
            }  // Lock released here

            if (isValidRequest(request)) {
                grantToken(request[0], Integer.parseInt(request[1]));
                waitForReturnToken(request[0], Integer.parseInt(request[1]));
            }
        }
    }

    /**
     * waits fot the token to be returned by the node
     * @param nodeHost the host of the returning node
     * @param nodePort the port of the returning node
     */
    private void waitForReturnToken(String nodeHost, int nodePort) {
        try {
            Socket returnConnection = returnSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(returnConnection.getInputStream()));
            String response = in.readLine();
            if ("TOKEN_RETURNED".equals(response)) {
                logger.info("COORDINATOR: TOKEN RETURNED by " + nodeHost + ":" + nodePort);
                System.out.println("C:mutex - Token returned by " + nodeHost + ":" + nodePort);
            }
        } catch (IOException e) {
            System.err.println("C:mutex ERROR: Failed to receive token return - " + e.getMessage());
        }
    }


    /**
     * grants the token to the requesting node
     * @param nodeHost the host of the requesting node
     * @param nodePort the port of the requesting node.
     */
    private void grantToken(String nodeHost, int nodePort) {
        try {
            Socket nodeSocket = new Socket(nodeHost, nodePort);
            PrintWriter out = new PrintWriter(nodeSocket.getOutputStream(), true);
            out.println("TOKEN_GRANTED");
            logger.info("COORDINATOR: GRANTED token to " + nodeHost + ":" + nodePort);
            System.out.println("C:mutex - Token granted to " + nodeHost + ":" + nodePort);
        } catch (IOException e) {
            System.err.println("Error granting token to: " + nodeHost + ":" + nodePort);

        }
    }

    /**
     * wait until the buffer has a valid request
     */
    private void waitForRequests() {
        while(buffer.size()<2){
            try{
                System.out.println("C:mutex - waiting for requests\nBuffer size: " + buffer.size());
                buffer.wait();
            } catch (InterruptedException e) {
                System.err.println("C:mutex error: Interrupted while waiting for requests: " + e.getMessage());
            }
        }
        System.out.println("C:mutex - Buffer contents before fetching request: " + buffer);
    }

    /**
     * checks if the request is valid
     * @param request the request array containing the node host and port
     * @return TRUE if valid, FALSE otherwise
     */
    private boolean isValidRequest(String[] request) {
        return request!=null && request.length==2;
    }
}