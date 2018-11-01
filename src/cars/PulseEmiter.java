package cars;

import java.util.List;

import common.StNode;
import network.MsgHandler;

public class PulseEmiter implements Runnable{
	
	private CarMonitor carMonitor;
	private MsgHandler msgHandler;
	
	public PulseEmiter(CarMonitor monitor, MsgHandler msgHandler) {
		this.carMonitor = monitor;
		this.msgHandler = msgHandler;
	}

	@Override
	public void run() {
		List<StNode> nodes = carMonitor.getList();
		for (StNode stNode : nodes) {
			//TODO make this class listen to the car motion?
		}
		
	}
	
}
