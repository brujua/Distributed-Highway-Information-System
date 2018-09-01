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
		//first register in the highway network
		registerInNetwork();
		//then start listening for msgs with the MsgHandler
		msgHandler = new MsgHandler(this.ip, this.port);
		msgHandler.addListener(this);
		
		
		emitPulses();
	}
	
	private void registerInNetwork() {
		//send hello to first highwayNode
		// if it doenst reply in some time, try next one
		
		//send and receive
		
	}
	
	private void emitPulses() {
		//call to method emitMessage() of the msgHandler
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}



	@Override
	public void notify(Message m) {
		// TODO
		switch(m.getType()) {
			case HELLO: {
				break;
			}
			case PULSE: {
				break;
			}
			case REDIRECT: {
				break;
			}
			default: {
				//TODO log message of unknown type
			}
		}
		
	}

}
