import java.net.*;
import java.io.*;
import java.util.Arrays;

public class C_mutex extends Thread {
    final C_buffer buffer;
    int returnPort;
    private ServerSocket returnSocket;

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
    private void processRequests() throws IOException {
        while(true){
                synchronized (buffer){
                    waitForRequests();
                    String[] request = buffer.getRequest();
                    if(isValidRequest(request)){
                        String nodeHost = request[0];
                        int nodePort = Integer.parseInt(request[1]);
                        grantToken(nodeHost, nodePort);
                        waitForReturnToken(nodeHost,nodePort);
                    }else{
                        System.err.println("C:mutex error: invalid request!\nContents: " + Arrays.toString(request));
                    }

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


//    public void run() {
//        try {
//            ServerSocket returnSocket = new ServerSocket(returnPort);
//            System.out.println("C:mutex - Waiting for token returns on port " + returnPort);

//            while (true) {
//                synchronized (buffer) {
//                    while (buffer.size() < 2) {  // Wait until we have a valid request
//                        System.out.println("C:mutex - Waiting for requests... Buffer size: " + buffer.size());
//                        buffer.wait();
//                    }
//
//                    // DEBUG: Print buffer contents before fetching the request
//                    System.out.println("C:mutex - Buffer contents before fetching request: " + buffer.toString());
//
//                    // Fetch next request
//                    String[] request = buffer.getRequest();
//                    if (request != null && request.length == 2) {
//                        String nodeHost = request[0];
//                        int nodePort = Integer.parseInt(request[1]);
//
//                        System.out.println("C:mutex - Granting token to " + nodeHost + ":" + nodePort);
//
//                        try {
//                            // Grant token to requesting node
//                            Socket nodeSocket = new Socket(nodeHost, nodePort);
//                            PrintWriter out = new PrintWriter(nodeSocket.getOutputStream(), true);
//                            out.println("TOKEN_GRANTED");
//                            nodeSocket.close();
//                            System.out.println("C:mutex - Token granted to " + nodeHost + ":" + nodePort);
//                        } catch (IOException e) {
//                            System.err.println("Error granting token: " + e.getMessage());
//                            continue;
//                        }
//
//                        // Wait for token return
//                        try {
//                            Socket returnConnection = returnSocket.accept();
//                            BufferedReader in = new BufferedReader(new InputStreamReader(returnConnection.getInputStream()));
//                            String response = in.readLine();
//                            if ("TOKEN_RETURNED".equals(response)) {
//                                System.out.println("C:mutex - Token returned by " + nodeHost + ":" + nodePort);
//                            }
//                            returnConnection.close();
//                        } catch (IOException e) {
//                            System.err.println("Error waiting for token return: " + e.getMessage());
//                        }
//                    } else {
//                        System.err.println("C:mutex ERROR: Retrieved request is invalid! Request contents: " + Arrays.toString(request));
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Mutex error: " + e.getMessage());
//        }
//    }


}