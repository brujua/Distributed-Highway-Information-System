package common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import network.Message;

public class HWNodeList {

	//ArrayList<StNode> HWNodes;
	private Map<StNode,Instant> HWNodes ;
	
	Position position;
	
	public HWNodeList(Position pos) {
		position = pos;
		Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						aliveControler();
						
					}
					
				});
	}

	public boolean addAll(Map<StNode,Instant> neighs) {
		synchronized(HWNodes) {
			if(HWNodes.isEmpty()) {
				HWNodes = neighs;
				return true;
			}else {
/*				for (StNode HWNode : neighs.keySet()) {
					HWNodes.put(HWNode);
				}*/
				HWNodes.putAll(neighs);
				return true;
			}
		
		}
	}
	
	public boolean add(StNode HWNode) {
		synchronized(HWNodes) {
			HWNodes.put(HWNode,Instant.now());
			return true;
		}
	}
	
	public void aliveControler() {
		while (true) {
			try {
				Thread.sleep(5000);
				//TODO 
				
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			
		}
	}
	
	
	
	public StNode findNear(Position carPosition) {
		//TODO book the best way to do this hehe
		StNode near = HWNodes.keySet().iterator().next();
		
		
		
		for (StNode HWNodes: HWNodes.keySet()) {
			
		}
		
		return near; 
	}
	
	
}
