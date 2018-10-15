package common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MsgWaiter implements MsgListener{

	public ExecutorService service = Executors.newCachedThreadPool();
	
	public Map<String, Boolean> pendingMsgs = new HashMap<String, Boolean>();
	
	public MsgWaiter() {
		// TODO Auto-generated constructor stub
	}
	
	public addPendingMsg();
	
	@Override
	public void notify(Message m) {
		// TODO Auto-generated method stub
		
	}

}
