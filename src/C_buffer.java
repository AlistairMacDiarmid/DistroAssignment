import java.util.*;

public class C_buffer {

    private Vector<Object> data;

    public C_buffer (){
    	data = new Vector<Object>();
    }

    public int size(){
    	return data.size();
    }

    public synchronized void saveRequest (String[] r){
    	data.add(r[0]); //host
    	data.add(r[1]); //port
		notifyAll(); //notify the waiting threads
    }

	public synchronized String[] getRequest() {
		while (data.size() < 2) { // Ensure we have both IP and Port before proceeding
			try {
				wait(); // Wait for requests to be available
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}

		// Retrieve and remove the request (IP and Port)
		String[] request = new String[2];
		request[0] = (String) data.remove(0);  // Get the IP
		request[1] = (String) data.remove(0);  // Get the Port

		return request;
	}

    public void show(){
		for (int i=0; i<data.size();i++)
		    System.out.print(" "+data.get(i)+" ");
		System.out.println(" ");
    }

    public void add(Object o){
    	data.add(o);
    }

    synchronized public Object  get(){
    	Object o = null;

		if (data.size() > 0){
		    o = data.get(0);
		    data.remove(0);
		}
		return o;
    }

	@Override
	public String toString() {
        return "C_buffer [data=" + data + "]";
    }




}
