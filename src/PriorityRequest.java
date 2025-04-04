/**
 * PriorityRequest class represents a request in the Distributed mutual exclusion system
 * Each request consists of
 *  - a host (IP address or hostname of the requesting node)
 *  - a port (port of the requesting node)
 *  - a priority (higher priority requests are processed first)
 *  - a timestamp (used to break ties between requests with the same priority)
 *
 *  implements Comparable to allow for priority based ordering
 *  requests are first compared by priority (higher priority requests first)
 *  if two requests have the same priority, the older request is processed first
 */

public class PriorityRequest implements Comparable<PriorityRequest> {

    private final String host; //IP address or hostname of the requesting node
    private final int port; //port of the requesting node
    private final int priority; //priority of the request - higher value = higher priority
    private final long timestamp; //timestamp of when the request was created

    /**
     * Constructor - initializes a new PriorityRequest
     * @param host the host (IP address or hostname) of the requesting node
     * @param port the port of the requesting node
     * @param priority the priority of the request - higher value = higher priority
     */
    public PriorityRequest(String host, int port, int priority) {
        this.host = host;
        this.port = port;
        this.priority = priority;
        this.timestamp = System.currentTimeMillis(); //creation time of the request
    }


    /**
     * compares a PriorityRequest to another based on priority
     * if the prioritys are equal, it compares the timestamps for FIFO ordering
     * @param other the PriorityRequest to be compared.
     * @return a negative integer, zero, or a positive integer if this request has a higher, equal or lower priority than the other request
     */
    @Override
    public int compareTo(PriorityRequest other) {
        //compare priority levels (higher priority comes first)
        int priorityCompare = Integer.compare(other.priority, this.priority);

        //if the priorities are different, return the result of the priority comparison.
        if(priorityCompare != 0){
            return priorityCompare;
        }else{
            //if the priorities are the same, compare timestamps - earlier requests come first.
            return Long.compare(this.timestamp, other.timestamp);
        }
    }

    /**
     *retrieves the host - ip or host name - of the requesting node
     * @return the host of the requesting node
     */
    public String getHost() {
        return host;
    }

    /**
     *retrieves the port of the requesting node
     * @return the port of the requesting node
     */
    public int getPort() {
        return port;
    }

    /**
     * retrieves the priority of the requesting node
     * @return the priority of the requesting node
     */
    public int getPriority() {
        return priority;
    }
}
