package network;


import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseMonitor{
	
	//maps responseID of the msgs pending and a boolean that marks if it was received
	public Map<String, CompletableFuture<Message>> pendingMsgs = new ConcurrentHashMap<String, CompletableFuture<Message>>();
	
	public void addMsg(Message m, CompletableFuture<Message> future) {
		pendingMsgs.put(m.getId(), future);
	}
	
	public void remove(Message m) {
		pendingMsgs.remove(m);
	}
	
	/**
	 * Checks if a message its a response expected to be received, and completes its associated future
	 * 
	 * @param msg Message to check
	 * @return true if it was a expected response
	 */
	public boolean check(Message msg) {
		//if it is a message we were waiting, complete its future.
		String responseId = msg.getResponseId();
		if(responseId!=null) 
			if(pendingMsgs.containsKey(responseId)) {
				CompletableFuture<Message> fut = pendingMsgs.get(responseId);
				fut.complete(msg);
				return true;
			}
		return false;
		
	}

}
