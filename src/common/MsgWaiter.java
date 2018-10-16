package common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MsgWaiter implements MsgListener{

	public ExecutorService service = Executors.newCachedThreadPool();
	
	//maps responseID of the msgs pendings and the boolean if it was received
	public Map<String, Boolean> pendingMsgs = new ConcurrentHashMap<String, Boolean>();
	
	public MsgWaiter() {
		// TODO Auto-generated constructor stub
	}
	
	
	
	@Override
	public void notify(Message m) {
		//if it is a message we were waiting, mark it as received.
		String responseId = m.getResponseId();
		if(responseId!=null) 
			if(pendingMsgs.containsKey(responseId))
				pendingMsgs.put(responseId, true);	
	}

}
