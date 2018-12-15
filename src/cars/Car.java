package cars;

import common.*;
import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class Car implements MsgListener, MotionObservable{

	private Logger logger;

	// -- temporary constants --
	public final String ip = "localhost";
	public final int tentativePort = 5555;
	// range for the monitors
	public final double PRIMARY_RANGE = 200;
	public final double SECONDARY_RANGE = Double.MAX_VALUE;

	public final int pulseRefreshTime = 1000;
	public final TimeUnit pRefreshTimeUnit = TimeUnit.MILLISECONDS;

	private String id;
	private String name = null;
	private int port;
	private Position position;
	private double velocity;
	
	private MsgHandler msgHandler;
	private ArrayList<StNode> highWayNodes; // centralized part of the network
	private StNode selectedHWNode;
	private CarMonitor primaryMonitor;
	private CarMonitor secondaryMonitor;
	private PulseEmiter pulseEmiter;
	private List<MotionObserver> motionObservers = new ArrayList<>();
	private ScheduledExecutorService pulseScheduler = Executors.newSingleThreadScheduledExecutor();
	private ExecutorService threadService = Executors.newCachedThreadPool();

	public Car(Position position, double velocity, List<StNode> highwayNodes){
		super();
		id = UUID.randomUUID().toString();
		logger = LoggerFactory.getLogger(getName());
		this.position = position;
		this.velocity = velocity;
		this.port = Util.getAvailablePort(tentativePort);
		highWayNodes = new ArrayList<>(highwayNodes);
		primaryMonitor = new CarMonitor(getName());
		secondaryMonitor = new CarMonitor(getName());

		//initialize the MsgHandler
		msgHandler = new MsgHandler(this.port);
		msgHandler.addMsgListener(this);

		pulseEmiter = new PulseEmiter(this, primaryMonitor, msgHandler, getCarStNode());

	}

	public Car(Position position, double velocity, List<StNode> highwayNodes, String name){
		this(position,velocity,highwayNodes);
		this.name = name;
		this.logger = LoggerFactory.getLogger(name);
	}

	public Car (Position position, double velocity) {
		this(position, velocity, new ArrayList<>());
	}
		


	public Car registerInNetwork() throws NoPeersFoundException {
		boolean registered = false;
		StNode highwNode;
		Iterator<StNode> nodIterator = highWayNodes.iterator();
		while(!registered) {
			if(nodIterator.hasNext()) {
				highwNode = nodIterator.next();
				logger.info("Intentando registro en nodo: " + highwNode);

				registered = tryRegister(highwNode);
			} else 
				throw new NoPeersFoundException(); 
		}
		logger.info("Registered in node:" + selectedHWNode);
		return this;
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
			response = msgHandler.sendUDPWithResponse(hwNode, msg);
			responseMsg = response.get();
			
        	switch(responseMsg.getType()) {
            	case HELLO_RESPONSE:{
            		handleHelloResponse(responseMsg,true);
		            updateHWNode(((MT_HelloResponse) responseMsg.getData()).getStNode());
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
			return false;
		} 
        catch (ExecutionException  e) {
        	try {
        		throw e.getCause();
        	} catch(TimeoutException toe) {
		        logger.info("Node did not respond");
            	return false;
        		
        	} catch (Throwable e1) {
		        logger.error("Error while waiting for highway response");
				return false;
			}       	
        } catch (InterruptedException e) {
			logger.info("Interrupted while trying to register and waiting response");
			return false;
		}
	}

	private void updateHWNode(StNode hwNode) {
		selectedHWNode = hwNode;
		pulseEmiter.setHighwayNode(hwNode);
	}


	public Car listenForMsgs() {
		port = Util.getAvailablePort(tentativePort);
		msgHandler = new MsgHandler(port, getName());
		msgHandler.addMsgListener(this);
		msgHandler.listenForUDPMsgs();
		return this;
	}
	

	/**
	 * @param cars
	 */
	private void updateMultipleCars(Iterable<StNode> cars) {
		for(StNode car : cars) {
			updateNeigh(car);
		}
		
	}

	private void updateNeigh(CarStNode car) {
		boolean updated = primaryMonitor.update(car);
		if(!updated)
			secondaryMonitor.update(car);
	}



	public Car emitPulses() {
		pulseScheduler.scheduleWithFixedDelay(pulseEmiter, 0, pulseRefreshTime, pRefreshTimeUnit);
		return this;
	}
	
	private void monitorFarCars() {
		// TODO Auto-generated method stub
		
	}

	public Pulse getPulse() {
		return new Pulse(position, velocity, Instant.now());
	}

	private String getName() {
		return (name != null)? name : this.id.substring(0,5);
	}
	
	@Override
	public void msgReceived(Message m) {
		// The logic of the received msg will be handled on a different thread
		threadService.execute( new Runnable() {
			public void run() {
				try {
					switch (m.getType()) {
						case HELLO: {
							handleHello(m);
							break;
						}
						case PULSE: {
							handlePulse(m);
							break;
						}
						case REDIRECT: {
							handleRedirect(m);
							break;
						}
						default: {
							logger.error("Received message of wrong type");
						}
					}
				}catch(CorruptDataException cde){
					logger.error("Corrupt data exception on message: "+ m +" /n exception msg: "+cde.getMessage());
				}
			}
		});
	}

	private void handleHello(Message m) throws CorruptDataException {
		if(m.getType() != MsgType.HELLO || ! (m.getData() instanceof StNode )){
			throw new CorruptDataException();
		}
		StNode car = (StNode) m.getData();
		logger.info("Hello received on node: " + getStNode() + "from node: " + car);
		sendHelloResponse(m);

	}

	/**
	 * @param redirectMsg - message containing the redirect information
	 * @return boolean - if the redirect was successful
	 * @throws CorruptDataException when the message its of wrong type, or the containing data isn´t of type MT_Redirect
	 */
	private boolean handleRedirect(Message redirectMsg) throws CorruptDataException {
		if (redirectMsg.getType() != MsgType.REDIRECT || !(redirectMsg.getData() instanceof MT_Redirect))
			throw new CorruptDataException();
		MT_Redirect redi = (MT_Redirect) redirectMsg.getData();
		logger.info("Redirected received to: " + redi.getRedirectedNode());
		return tryRegister(redi.getRedirectedNode());
	}

	/**
	 * @param responseMsg - message received
	 * @param isHWNode - flag that marks if it is a conversation with a HWNode
	 * @throws CorruptDataException when the message its of wrong type, or the containing data isn´t of type MT_HelloResponse
	 */
	private void handleHelloResponse(Message responseMsg, boolean isHWNode) throws CorruptDataException {
		if(responseMsg.getType() != MsgType.HELLO_RESPONSE || ! (responseMsg.getData() instanceof MT_HelloResponse) )
			throw new CorruptDataException();
		MT_HelloResponse helloRsp = (MT_HelloResponse) responseMsg.getData();
		logger.info("Hello Response received on node: " + getStNode() + "from node: " + helloRsp.getStNode());
		//check new cars that i dont know of, and send them a Hello msg
		List<StNode> knownCars = primaryMonitor.getList();
		knownCars.addAll(secondaryMonitor.getList());
		for (CarStNode car : helloRsp.getCars()) {
			if(! knownCars.contains(car)){
				sendHello(car.getStNode(), false);
			}
		}
	}



	private void handlePulse(Message m) throws CorruptDataException {
		if (m.getType() != MsgType.PULSE || !(m.getData() instanceof CarStNode)) {
			throw new CorruptDataException();
		}
		CarStNode car = (CarStNode) m.getData();
		if (isInRange(car)) {
			updateNeigh(car);
		}
		logger.info("Pulse received on node: " + getStNode() + " from node: " + car);
	}

	private boolean isInRange(CarStNode car) {
		Position pos = car.getPosition();
		return pos.distance(this.position) <= PRIMARY_RANGE;
	}

	private boolean sendHello(StNode node, boolean isHWNode) throws CorruptDataException {
		CompletableFuture<Message> msgHelloRsp = msgHandler.sendUDPWithResponse(node, new Message(MsgType.HELLO, ip, port, getStNode()));
		try {
			handleHelloResponse(msgHelloRsp.get(), isHWNode);
			return true;
		} catch (InterruptedException e) {
			logger.error("Interrupted while waiting hello response");
			return false;
		} catch (ExecutionException e) {
			if (e.getCause() instanceof TimeoutException) {
				logger.error("Node" + node + " does not respond to Hello");
			}
			return false;
		}
	}

	private void sendHelloResponse(Message m) throws CorruptDataException {
		if(! (m.getData() instanceof StNode )){
			throw new CorruptDataException();
		}
		StNode car = (StNode) m.getData();
		MT_HelloResponse response = new MT_HelloResponse(m.getId(),getStNode(),primaryMonitor.getList());
		Message msg = new Message(MsgType.HELLO_RESPONSE,ip,port, response);
		msgHandler.sendUDP(car, msg);
	}


	public void move() {
		//move with current velocity
		move(velocity);
	}

	/**
	 * Method provided to do simple simulation, in a real environment the position of the car must be read from gps.
	 *
	 * @param newVelocity - current velocity, represented as a single value for simplicity
	 */
	public void move(double newVelocity) {
		// moves only on the x axis for simplicity
		velocity = newVelocity;
		position = new Position(position.getCordx() + velocity, position.getCordy());
		notifyMotionObservers();
	}
	
	private void notifyMotionObservers() {
		for (MotionObserver obs : motionObservers) {
			obs.notify(this.getPulse());
		}

	}

	public List<CarStNode> getNeighs(){
		return primaryMonitor.getList();
	}
	
	public StNode getSelectedHWnode() {
		return selectedHWNode;
	}

	public CarStNode getCarStNode() {
		return new CarStNode(id, ip, port, new Pulse(position, velocity, Instant.now()));
	}

	public StNode getStNode() {
		return new StNode(id, ip, port);
	}

	public String getID() {
		return id;
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

	/**
	 * Disconects car from the network, stopping all the threads
	 */
	public void shutdown() {
		pulseScheduler.shutdownNow();
		primaryMonitor.shutdown();
		secondaryMonitor.shutdown();
		msgHandler.close();
	}

}
