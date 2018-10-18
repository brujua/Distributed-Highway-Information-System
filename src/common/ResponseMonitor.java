package common;


import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResponseMonitor{

	public ExecutorService service = Executors.newCachedThreadPool();
	
	
	//maps responseID of the msgs pending and a boolean that marks if it was received
	public Map<String, CompletableFuture<Message>> pendingMsgs = new ConcurrentHashMap<String, CompletableFuture<Message>>();
	
	public ResponseMonitor() {
		// TODO Auto-generated constructor stub
	}
	
	public void addMsg(Message m, CompletableFuture<Message> future) {
		pendingMsgs.put(m.getId(), future);
	}
	
	public boolean check(Message m) {
		//if it is a message we were waiting, complete its future.
		String responseId = m.getResponseId();
		if(responseId!=null) 
			if(pendingMsgs.containsKey(responseId)) {
				CompletableFuture<Message> fut = pendingMsgs.get(responseId);
				fut.complete(m);
				return true;
			}
		return false;
		
	}

}
