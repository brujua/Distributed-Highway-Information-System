package cars;

import java.util.List;

import common.CarMonitor;
import common.Pulse;
import common.StNode;
import network.Message;
import network.MsgHandler;
import network.MsgType;

public class PulseEmiter implements Runnable, MotionObserver{
	
	private CarMonitor carMonitor;
	private MsgHandler msgHandler;
	private StNode source;
	
	public PulseEmiter(MotionObservable pulseSource, CarMonitor monitor, MsgHandler msgHandler, StNode source) {
		this.carMonitor = monitor;
		this.msgHandler = msgHandler;
		this.source = source;
		pulseSource.addObserver(this);
	}

	@Override
	public void run() {
		List<StNode> nodes = carMonitor.getList();
		Message msg = new Message(MsgType.PULSE,source.getId(),source.getPort(),source);
		for (StNode node : nodes) {
			msgHandler.sendMsg(node, msg);
		}
		
	}

	@Override
	public void notify(Pulse pulse) {
		source = source.changePulse(pulse);
	}
	
}
