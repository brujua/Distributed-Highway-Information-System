package cars;

import common.Pulse;
import common.StNode;
import network.Message;
import network.MsgHandler;
import network.MsgType;

import java.util.List;

public class PulseEmiter implements Runnable, MotionObserver{
	
	private CarMonitor carMonitor;
	private MsgHandler msgHandler;
	private CarStNode source;

	private StNode highwayNode;

	public PulseEmiter(MotionObservable pulseSource, CarMonitor monitor, MsgHandler msgHandler, CarStNode source) {
		this.carMonitor = monitor;
		this.msgHandler = msgHandler;
		this.source = source;
		this.highwayNode = null;
		pulseSource.addObserver(this);
	}

	public void setHighwayNode(StNode highwayNode) {
		synchronized (highwayNode){
			this.highwayNode = highwayNode;
		}
	}

	@Override
	public void run() {
		Message msg;
		List<StNode> nodes = carMonitor.getList();
		synchronized (source){
			 msg = new Message(MsgType.PULSE,source.getId(),source.getPort(),source);
		}
		for (StNode node : nodes) {
			msgHandler.sendUDP(node, msg);
		}
		synchronized (highwayNode){
			if(highwayNode != null)
				msgHandler.sendUDP(highwayNode, msg);
		}
	}

	@Override
	public void notify(Pulse pulse) {
		synchronized (source){
			source = source.changePulse(pulse);
		}
	}
	
}
