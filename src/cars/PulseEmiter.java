package cars;

import common.CarMonitor;
import common.Pulse;
import common.StNode;
import network.Message;
import network.MsgHandler;
import network.MsgPulse;

import java.util.List;

public class PulseEmiter implements Runnable, MotionObserver{
	
	private CarMonitor carMonitor;
	private MsgHandler msgHandler;
	private CarStNode source;
	private final Object lock = new Object();

	private StNode highwayNode;

	public PulseEmiter(MotionObservable pulseSource, CarMonitor monitor, MsgHandler msgHandler, CarStNode source) {
		this.carMonitor = monitor;
		this.msgHandler = msgHandler;
		this.source = source;
		this.highwayNode = null;
		pulseSource.addObserver(this);
	}

	public void setHighwayNode(StNode highwayNode) {
		synchronized (lock) {
			this.highwayNode = highwayNode;
		}
	}

	@Override
	public void run() {
		Message msg;
		List<CarStNode> nodes = carMonitor.getList();
		synchronized (lock) {
			msg = new MsgPulse(source.getStNode(), source);
		}
		for (CarStNode node : nodes) {
			msgHandler.sendUDP(node, msg);
		}
		synchronized (lock) {
			if(highwayNode != null)
				msgHandler.sendUDP(highwayNode, msg);
		}
	}

	@Override
	public void notify(Pulse pulse) {
		synchronized (lock) {
			source = source.changePulse(pulse);
		}
	}
	
}
