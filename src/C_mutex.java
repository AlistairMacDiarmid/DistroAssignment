import java.net.*;
import java.io.*;
import java.util.Arrays;

public class C_mutex extends Thread {
    final C_buffer buffer;
    int returnPort;

    public C_mutex(C_buffer b, int p) {
        buffer = b;
        returnPort = p;
    }

    public void run() {
        try {
            ServerSocket returnSocket = new ServerSocket(returnPort);
            System.out.println("C:mutex - Waiting for token returns on port " + returnPort);

            while (true) {
                synchronized (buffer) {
                    while (buffer.size() < 2) {  // Wait until we have a valid request
                        System.out.println("C:mutex - Waiting for requests... Buffer size: " + buffer.size());
                        buffer.wait();
                    }

                    // DEBUG: Print buffer contents before fetching the request
                    System.out.println("C:mutex - Buffer contents before fetching request: " + buffer.toString());

                    // Fetch next request
                    String[] request = buffer.getRequest();
                    if (request != null && request.length == 2) {
                        String nodeHost = request[0];
                        int nodePort = Integer.parseInt(request[1]);

                        System.out.println("C:mutex - Granting token to " + nodeHost + ":" + nodePort);

                        try {
                            // Grant token to requesting node
                            Socket nodeSocket = new Socket(nodeHost, nodePort);
                            PrintWriter out = new PrintWriter(nodeSocket.getOutputStream(), true);
                            out.println("TOKEN_GRANTED");
                            nodeSocket.close();
                            System.out.println("C:mutex - Token granted to " + nodeHost + ":" + nodePort);
                        } catch (IOException e) {
                            System.err.println("Error granting token: " + e.getMessage());
                            continue;
                        }

                        // Wait for token return
                        try {
                            Socket returnConnection = returnSocket.accept();
                            BufferedReader in = new BufferedReader(new InputStreamReader(returnConnection.getInputStream()));
                            String response = in.readLine();
                            if ("TOKEN_RETURNED".equals(response)) {
                                System.out.println("C:mutex - Token returned by " + nodeHost + ":" + nodePort);
                            }
                            returnConnection.close();
                        } catch (IOException e) {
                            System.err.println("Error waiting for token return: " + e.getMessage());
                        }
                    } else {
                        System.err.println("C:mutex ERROR: Retrieved request is invalid! Request contents: " + Arrays.toString(request));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Mutex error: " + e.getMessage());
        }
    }


}