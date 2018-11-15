package cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import common.*;
import network.CorruptDataException;
import network.MT_HelloResponse;
import network.MT_Redirect;
import network.Message;
import network.MsgHandler;
import network.MsgListener;
import network.MsgType;

public class Car implements MsgListener, MotionObservable{

	private static final Logger logger = LoggerFactory.getLogger(Car.class);

	// -- temporary constants --
	public final String ip = "localhost";
	public final int tentativePort = 5555;	
	// range for the monitors
	public final double PRIMARY_RANGE = 200;
	public final double SECONDARY_RANGE = Double.MAX_VALUE;

	public final int pulseRefreshTime = 1000;
	public final TimeUnit pRefreshTimeUnit = TimeUnit.MILLISECONDS;
	
	private String id;
	private int port;
	private Position position;
	private double velocity;
	
	private MsgHandler msgHandler;
	private ArrayList<StNode> highWayNodes; // centralized part of the network
	private StNode selectedHWNode;
	private CarMonitor primaryMonitor;
	private CarMonitor secondaryMonitor;
	private PulseEmiter pulseEmiter;
	private ScheduledExecutorService pulseScheduler = Executors.newSingleThreadScheduledExecutor();
	private List<MotionObserver> motionObservers = new ArrayList<>();
	
	
	
	public Car(Position position, double velocity, List<StNode> highwayNodes) throws NoPeersFoundException {
		super();
		id = UUID.randomUUID().toString();
		this.position = position;
		this.velocity = velocity;
		this.port = getAvailablePort();
		highWayNodes = new ArrayList<StNode>(highwayNodes);
		primaryMonitor = new CarMonitor(PRIMARY_RANGE, this);
		secondaryMonitor = new CarMonitor(SECONDARY_RANGE, this);
		
		//initialize the MsgHandler
		msgHandler = new MsgHandler(this.port);
		msgHandler.addListener(this);

		pulseEmiter = new PulseEmiter(this,primaryMonitor,msgHandler, getStNode());
		//register in the highway network
		registerInNetwork();
		
		emitPulses();
		monitorFarCars();
	}
	
	public Car (Position position, double velocity) throws NoPeersFoundException {
		this(position, velocity, new ArrayList<>());
	}
		
	private int getAvailablePort() {
		int avPort = this.tentativePort;
		while(Util.isUDPPortOccupied(avPort)) {
			avPort++;
		}
		return avPort;
	}	

	private void registerInNetwork() throws NoPeersFoundException {
		boolean registered = false;
		StNode highwNode;
		Iterator<StNode> nodIterator = highWayNodes.iterator();	
		while(!registered) {
			if(nodIterator.hasNext()) {
				highwNode = nodIterator.next();
				logger.info("Intentando registro en nodo: " + highwNode);
				//System.out.println("Intentando registro en nodo");
				//System.out.println(highwNode);
				
				registered = tryRegister(highwNode);
			} else 
				throw new NoPeersFoundException(); 
		}
		logger.info("Registered in node:" + selectedHWNode);
		//System.out.println("Registered in node:" + selectedHWNode);
	}
	
	
	/**
	 * Attempts to register to a node, if it respond with a Redirect, makes a recursive call to the new node
	 * @param hwNode node to wich the registration will be attempted
	 * @return  returns true on success, false on failure.
	 */
	private boolean tryRegister(StNode hwNode) {
		CompletableFuture<Message> response;
		Message responseMsg;
		try {
			if(hwNode==null)
				return false;
			// send hello and wait for response			
			Message msg = new Message(MsgType.HELLO,this.ip,this.port, getStNode());
			response=msgHandler.sendMsgWithResponse(hwNode, msg);
			responseMsg = response.get();
			
        	switch(responseMsg.getType()) {
            	case HELLO_RESPONSE:{
            		handleHelloResponse(responseMsg);
            		return true;
        			}      	
            	case REDIRECT: {
            		return handleRedirect(responseMsg);
            	}
            	default:{
		            logger.error("Response from hw-node of wrong type, node: "+hwNode+" type: "+responseMsg.getType());
            		//System.out.println("Response from hw-node of wrong type");
            		return false;
            	}
        	}
        }
		catch (CorruptDataException e) {
			logger.error("corrupt data on hw-node response");
			//System.out.println("corrupt data on hw-node response");
			return false;
		} 
        catch (ExecutionException  e) {
        	try {
        		throw e.getCause();
        	} catch(TimeoutException toe) {
        		System.out.println("node doesn't respond");
        		//if it doesn't respond in time, try the next one
            	return false;
        		
        	} catch (Throwable e1) {
				e1.printStackTrace();
				return false;
			}       	
        } catch (InterruptedException e) {
			System.out.println("Interrupted while trying to register and waiting response");
			return false;
			
		}
	}
	
	/**
	 * @param responseMsg
	 * @throws CorruptDataException
	 */
	private void handleHelloResponse(Message responseMsg) throws CorruptDataException {
		Object data = responseMsg.getData();
		//check and cast response, add neighs
		if(!(data instanceof MT_HelloResponse))
			throw new CorruptDataException();             			
		MT_HelloResponse helloRsp = (MT_HelloResponse) data;
		addMultipleCars(helloRsp.getCars());		
		selectedHWNode = helloRsp.getStNode();		
	}
	

	/**
	 * @param cars
	 */
	private void addMultipleCars(Iterable<StNode> cars) {
		for(StNode car : cars) {
			addNeigh(car);
		}
		
	}

	private void addNeigh(StNode car){
		boolean updated = primaryMonitor.update(car);
		if(!updated)
			secondaryMonitor.update(car);
	}

	private boolean handleRedirect(Message redirectMsg) throws CorruptDataException{
		Object data = redirectMsg.getData();
		MT_Redirect redi = null;
		if(!(data instanceof MT_Redirect))
			throw new CorruptDataException();
		redi = (MT_Redirect) data;           	
		return tryRegister(redi.getRedirectedNode());
	}
	
	private void emitPulses() {
		pulseScheduler.scheduleWithFixedDelay(pulseEmiter, 0, pulseRefreshTime, pRefreshTimeUnit);
	}
	
	private void monitorFarCars() {
		// TODO Auto-generated method stub
		
	}

	public Pulse getPulse() {
		return new Pulse(position, velocity, Instant.now());
	}
	
	@Override
	public void notify(Message m) {
		// The logic of the received msg will be handled on a different thread
		Thread thread = new Thread( new Runnable() {
			public void run() {
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
		});
		
		thread.start();
	}
	
	
	public void move() {
		//move with current velocity
		move(velocity);
	}
	
	public void move(double newVelocity) {
		velocity = newVelocity;
		position = new Position(position.getCordx()+velocity, position.getCordy()+velocity);
		notifyMotionObservers();
	}
	
	private void notifyMotionObservers() {
		for (MotionObserver obs : motionObservers) {
			obs.notify(this.getPulse());
		}
		
	}

	public List<StNode> getNeighs(){
		return primaryMonitor.getList();
	}
	
	public StNode getNearestNeigh() {
		List<StNode> neighs = primaryMonitor.getList();
		if(neighs.isEmpty())
			return null;
		return neighs.get(0);
	}
	
	public StNode getSelectedHWnode() {
		return selectedHWNode;
	}
	
	public StNode getStNode() {
		return new StNode(id, ip, port, position);
	}

	@Override
	public void addObserver(MotionObserver mo) {
		motionObservers.add(mo);
		//immediately notify of current pulse
		mo.notify(this.getPulse());
		
	}

	@Override
	public void removeObserver(MotionObserver mo) {
		motionObservers.remove(mo);
		
	}


}
