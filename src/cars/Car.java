package cars;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.UUID;

public class Car implements MsgListener{
	// temporary constants
	public final String ip = "localhost";
	public final int port = 5555;
	
	private String id;
	private String position;
	private double velocity;
	private BigInteger msgCounter;
	//private Thread listeningThread;
	private MsgHandler msgHandler;
	private ArrayList<StNode> highWayNodes; // centralized part of the network
	private ArrayList<StNode> neighs; //other cars near by
	
	public Car (String position, double velocity) {
		super();
		id = UUID.randomUUID().toString();
		this.position = position;
		this.velocity = velocity;
		msgCounter = BigInteger.valueOf(0);
		highWayNodes = new ArrayList<>();
		neighs = new ArrayList<>();
		msgHandler = new MsgHandler(this.ip, this.port);
		msgHandler.addListener(this);
		
		registerInNetwork();
		emitPulses();
	}
	
	private void registerInNetwork() {
		
		
	}
	
	private void emitPulses() {
		
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}



	@Override
	public void notify(Message m) {
		// TODO Auto-generated method stub
		
	}

}
