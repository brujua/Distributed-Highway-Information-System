package cars;

import java.util.ArrayList;
import java.util.List;

public class MsgHandler implements MsgObservable{

	private String ip;
	private int port;
	private ArrayList<MsgListener> listeners;
	
	
	public MsgHandler(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
		listenForMsgs();
	}

	/*
	 * Starts a thread and listen for msgs
	*/
	private void listenForMsgs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(MsgListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListener(MsgListener l) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Emits the msg to each of the targets on the list	
	*/
	public void emitMessage(List<Messageable> dest, Message msg) {
		return ;
		//TODO
	};

}
