package highway;

import java.math.BigInteger;
import java.util.ArrayList;

import common.Message;
import common.MsgHandler;
import common.MsgListener;
import common.Position;
import common.StNode;

public class HighWay implements MsgListener{

	public final String ip = "localhost";
	public final int port = 9000;
	
	//node identificator
	private String id;
	
	// Postion node data
	private Position position;
	
	//MSG tools
	private BigInteger msgCounter;
	private MsgHandler msgHandler;
	
	//Negighs HW nodes
	private ArrayList<StNode> neighs; //othrs node highway tolking to me 
	private ArrayList<StNode> carNodes; //cars in my zone to shared
	
	public HighWay ( ArrayList<StNode> neighs,Position position) {
		super();
		this.position= position;
		this.neighs = neighs;
				
		msgHandler = new MsgHandler(this.ip, this.port);
		msgHandler.addListener(this);
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	@Override
	public void notify(Message m) {
		// TODO Auto-generated method stub
		
	}
	
	
}
