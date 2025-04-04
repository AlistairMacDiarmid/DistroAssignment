import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;


/**
 * C_buffer class implements a priority queue to manage the requests.
 * requests are then stored in a PriorityBlockingQueue and are processed based on priority
 */
public class C_buffer {

    private static final Logger logger = LogManager.getLogger();
    //the queue to hold PriorityRequests objects in priority order
    private final PriorityBlockingQueue<PriorityRequest> queue;

    /**
     * constructor - initializes the buffer with a PriorityBlockingQueue
     */
    public C_buffer() {
        queue = new PriorityBlockingQueue<>();
    }


    /**
     * returns the current size of the queue
     *
     * @return the number of requests in the queue
     */
    public int size() {
        return queue.size();
    }

    /**
     * saves a new request into the queue. The request is created from an array of strings
     * the array elements represent the host, port and priority of the request
     *
     * @param r the request data as an array of strings - r[0] - host, r[1] - port, r[2] - priority
     */
    public void saveRequest(String[] r) {
        //create a new  PriorityRequest and add it to the queue
        queue.put(new PriorityRequest(
                r[0],
                Integer.parseInt(r[1]),
                Integer.parseInt(r[2])
        ));
    }

    /**
     * retrieves and removes the highest priority request from the queue
     * method blocks when the queue is empty
     *
     * @return a string array containing the host, port and priority of the request
     * @throws InterruptedException if the current thread is interrupted whilst waiting
     */
    public String[] getRequest() throws InterruptedException {
        try {
            //take the highest priority request from the queue
            PriorityRequest request = queue.take();

            long waitTime = System.currentTimeMillis() - request.getTimestamp();
            if (waitTime > PriorityRequest.STARVATION_THRESHOLD) {
                logger.info("[COORD] STARVATION_PREVENTION: Prioritizing node " +
                        request.getPort() + " after " + (waitTime / 1000) + "s wait");
            }

            return new String[]{
                    request.getHost(),
                    String.valueOf(request.getPort()),
                    String.valueOf(request.getPriority())
            };
        } catch (InterruptedException e) {
            //re-interrupt the current thread if interrupted
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * returns the current state of the queue as a string
     * each request in the queue is represented by its port and priority
     *
     * @return a string representation of the queue state, or "EMPTY" if the queue is empty
     */
    public String getQueueState() {
        if (queue.isEmpty()) return "EMPTY";

        StringBuilder sb = new StringBuilder();
        long currentTime = System.currentTimeMillis();

        for (PriorityRequest request : queue) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            boolean starved = (currentTime - request.getTimestamp()) > PriorityRequest.STARVATION_THRESHOLD;
            sb.append(request.getPort())
                    .append("(Priority:")
                    .append(request.getPriority())
                    .append(starved ? "*" : "")
                    .append(")");
        }

        return sb.toString();
    }
}








