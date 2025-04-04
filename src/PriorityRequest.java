public class PriorityRequest implements Comparable<PriorityRequest> {

    private final String host;
    private final int port;
    private final int priority;
    private final long timestamp;

    public PriorityRequest(String host, int port, int priority) {
        this.host = host;
        this.port = port;
        this.priority = priority;
        this.timestamp = System.currentTimeMillis();
    }


    /**
     * @param other the object to be compared.
     * @return
     */
    @Override
    public int compareTo(PriorityRequest other) {
        int priorityCompare = Integer.compare(other.priority, this.priority);
        if(priorityCompare != 0){
            return priorityCompare;
        }else{
            return Long.compare(this.timestamp, other.timestamp);
        }
    }

    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public int getPriority() {
        return priority;
    }
}
