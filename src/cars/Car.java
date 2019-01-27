package cars;

import common.*;
import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Car implements MsgListener, MotionObservable{

	private Logger logger;

	// -- temporary constants --
	public final String ip = "localhost";
	public final int tentativePort = 5555;
    public static final String CONFIG_NODES_PATH = "config-cars";
	// range for the monitors
    public final double PRIMARY_RANGE = 200; //near cars


	public final int pulseRefreshTime = 1000;
	public final TimeUnit pRefreshTimeUnit = TimeUnit.MILLISECONDS;
    public final double SECONDARY_RANGE = 2000; // far cars


	private String id;
	private String name = null;
	private int port;
	private Position position;
	private double velocity;
    private ReentrantReadWriteLock positionLock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock neighsLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock selectedHWNodeLock = new ReentrantReadWriteLock();

    private final List<MT_Broadcast> broadcastMsgs = new ArrayList<>();
    private MsgHandler msgHandler;
    private List<StNode> possibleHWNodes;
	private StNode selectedHWNode;
    private CarMonitor primaryMonitor; //near cars
    private CarMonitor secondaryMonitor; // far cars
    private HWNodeMonitor hwNodeMonitor;
	private PulseEmiter pulseEmiter;
	private List<MotionObserver> motionObservers = new ArrayList<>();
	private ScheduledExecutorService pulseScheduler = Executors.newSingleThreadScheduledExecutor();
	private ExecutorService threadService = Executors.newCachedThreadPool();

    public Car(Position position, double velocity) {
        id = UUID.randomUUID().toString();
        logger = LoggerFactory.getLogger(getName());
        this.position = position;
        this.velocity = velocity;
        this.port = Util.getAvailablePort(tentativePort);
        this.possibleHWNodes = readConfig();
        primaryMonitor = new CarMonitor("PrimaryMonitor: " + getName());
        secondaryMonitor = new CarMonitor("SecondaryMonitor: " + getName());
        hwNodeMonitor = new HWNodeMonitor(getName());
        hwNodeMonitor.startMonitoring(this::handleHWNodeDown);
        //initialize the MsgHandler
        msgHandler = new MsgHandler(this.port);
        msgHandler.addMsgListener(this);

        pulseEmiter = new PulseEmiter(this, primaryMonitor, msgHandler, getCarStNode());

    }

    public Car(Position position, double velocity, String name) {
        this(position, velocity);
        this.name = name;
	    this.logger = LoggerFactory.getLogger(getName());
    }

    public Car(Position position, double velocity, List<StNode> highwayNodes) {
        this(position, velocity);
        this.possibleHWNodes = highwayNodes;

    }

	public Car(Position position, double velocity, List<StNode> highwayNodes, String name){
        this(position, velocity, name);
        this.possibleHWNodes = highwayNodes;
	}

	/**
	 * This method reads the configuration file for the cars, named 'config-cars.properties' under resources.
	 * From there, extracts the list of possible locations (ip and range of ports) for HWNodes
	 *
	 * @return the list of possible hwnodes
	 */
	private List<StNode> readConfig() {
		List<StNode> nodes = new ArrayList<>();
		try {
			nodes = Util.readNodeConfigFile(CONFIG_NODES_PATH);
		} catch (MissingResourceException e) {
			logger.error("Config file for car corrupted: " + e.getMessage());
		}
		return nodes;
	}

    public Car listenForMsgs() {
        port = Util.getAvailablePort(tentativePort);
        msgHandler = new MsgHandler(port, getName());
        msgHandler.addMsgListener(this);
        msgHandler.listenForUDPMsgs();
        return this;
    }

    /**
     * @return this (Fluid Syntax)
     * @throws NoPeersFoundException if could not register in any HWNode, there is no HWNode, or the config file is corrupted.
     */
	public Car registerInNetwork() throws NoPeersFoundException {
		boolean registered = false;
		StNode highwNode;
        Iterator<StNode> nodIterator = possibleHWNodes.iterator();
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
     * @param hwNode node to which the registration will be attempted
	 * @return  returns true on success, false on failure.
	 */
	private boolean tryRegister(StNode hwNode) {
		CompletableFuture<Message> response;
		Message responseMsg;
		try {
			if(hwNode==null)
				return false;
            // send hello and wait for response
			Message msg = new Message(MsgType.HELLO, this.ip, this.port, getCarStNode());
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
		        logger.info("Node did not respond: "+hwNode.getPort());
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

    private void handleHWNodeDown() {
        logger.error("Selected hwnode down. Attempting to re-register in network...");
        selectedHWNodeLock.writeLock().lock();
        selectedHWNode = null;
        pulseEmiter.setHighwayNode(null);
        selectedHWNodeLock.writeLock().unlock();
        boolean registered = false;
        //indefinitely attempt to find a hwnode to register
        while (!Thread.currentThread().isInterrupted() && !registered) {
            try {
                registerInNetwork();
                registered = true;
            } catch (NoPeersFoundException e) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
                continue;
            }
        }
    }

	private void updateHWNode(StNode hwNode) {
        selectedHWNodeLock.writeLock().lock();
		selectedHWNode = hwNode;
		pulseEmiter.setHighwayNode(hwNode);
        hwNodeMonitor.setNode(hwNode);
        selectedHWNodeLock.writeLock().unlock();
	}


    private void updateNeigh(CarStNode neigh) {
	    //if its not this same car
	    if (!neigh.getId().equals(id)) {
		    positionLock.readLock().lock();
		    double distance = position.distance(neigh.getPosition());
		    if (distance <= PRIMARY_RANGE)
			    primaryMonitor.update(neigh);
		    else if (distance <= SECONDARY_RANGE)
			    secondaryMonitor.update(neigh);

		    positionLock.readLock().unlock();
	    }
	}



	public Car emitPulses() {
		pulseScheduler.scheduleWithFixedDelay(pulseEmiter, pulseRefreshTime, pulseRefreshTime, pRefreshTimeUnit);
		return this;
	}

	public boolean containBroadcast(MT_Broadcast broadcast) {
		//TODO is only test method dont use
		return broadcastMsgs.contains(broadcast);
	}




	
	private void monitorFarCars() {
		// TODO Auto-generated method stub
		
	}

	public Pulse getPulse() {
        positionLock.readLock().lock();
        Pulse pulse = new Pulse(position, velocity, Instant.now());
        positionLock.readLock().unlock();
        return pulse;
	}

    public String getName() {
	    return (name != null) ? name + '#' + this.id.substring(0, 5) : this.id.substring(0, 5);
	}
	
	@Override
	public void msgReceived(Message m) {
		// The logic of the received msg will be handled on a different thread
        if (!threadService.isShutdown()) {
            threadService.execute(new Runnable() {
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
                            case ERROR: {
                                logger.error("Received error message: " + m);
                                break;
                            }
	                        case BROADCAST: {
                                handleBroadcast(m);
		                        break;
	                        }
                            case ALIVE: {
                                handleAlive(m);
                                break;
                            }
                            default: {
                                logger.error("Received message of wrong type: " + m.getType().toString());
                            }
                        }
                    } catch (CorruptDataException cde) {
                        logger.error("Corrupt data exception on message: " + m + " /n exception msg: " + cde.getMessage());
                    }
                }
            });
        } else {
            logger.info("Message receive while shut down");
        }
	}

    private void handleAlive(Message m) throws CorruptDataException {
        if (m.getType() != MsgType.ALIVE || !(m.getData() instanceof StNode)) {
            throw new CorruptDataException();
        }
        StNode node = (StNode) m.getData();
        logger.debug("Received Alive from: " + node);

        selectedHWNodeLock.readLock().lock();
        if (selectedHWNodeLock != null && selectedHWNode.equals(node))
            hwNodeMonitor.update(node);
        selectedHWNodeLock.readLock().unlock();
    }

    private void handleHello(Message m) throws CorruptDataException {
		if(m.getType() != MsgType.HELLO || ! (m.getData() instanceof StNode )){
			throw new CorruptDataException();
		}
		StNode car = (StNode) m.getData();
        logger.debug("Hello received from node: " + car);
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
        logger.info("Hello Response received from node: " + helloRsp.getStNode());

		//check new cars that i dont know of, and send them a Hello msg
		List<CarStNode> knownCars = primaryMonitor.getList();
		knownCars.addAll(secondaryMonitor.getList());
		for (CarStNode car : helloRsp.getCars()) {
			//check if its not this same car
			if (!car.getId().equals(id)) {
				updateNeigh(car);
				if (!knownCars.contains(car)) {
					sendHello(car.getStNode(), false);
				}
			}
		}
	}



	private void handlePulse(Message m) throws CorruptDataException {
		if (m.getType() != MsgType.PULSE || !(m.getData() instanceof CarStNode)) {
			throw new CorruptDataException();
		}
		CarStNode car = (CarStNode) m.getData();
        updateNeigh(car);
        logger.debug("Pulse received from node: " + car);
	}

    public void handleBroadcast(Message msg) throws CorruptDataException {
		if (msg.getType() != MsgType.BROADCAST || !(msg.getData() instanceof MT_Broadcast)) {
			throw new CorruptDataException();
		}
		MT_Broadcast broadcast = (MT_Broadcast) msg.getData();
        synchronized (broadcastMsgs) {
            if (!broadcastMsgs.contains(broadcast) && broadcast.getTTL() > 0) {
                logger.info("Broadcast Message received: " + broadcast.getMsg());
                broadcastMsgs.add(broadcast);
                //resend the msg to flood
                sendBroadcast(broadcast.decrementTTL());
            }
        }
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

    public void sendBroadcast(MT_Broadcast broadcastMsg) {
        broadcastMsg.setFromCar(true);
        Message msg = new Message(MsgType.BROADCAST, ip, port, broadcastMsg);
        for (CarStNode node : getNeighs()) {
            msgHandler.sendUDP(node, msg);
        }
        selectedHWNodeLock.readLock().lock();
        if (selectedHWNode != null)
            msgHandler.sendUDP(selectedHWNode, msg);
        selectedHWNodeLock.readLock().unlock();

        synchronized (broadcastMsgs) {
            if (!broadcastMsgs.contains(broadcastMsg))
                broadcastMsgs.add(broadcastMsg);
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
        positionLock.writeLock().lock();
        velocity = newVelocity;
        position = new Position(position.getCordx() + velocity, position.getCordy());
        positionLock.writeLock().unlock();
		notifyMotionObservers();
	}

	public void setPosition(Position position) {
		this.position = position;
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
        return new CarStNode(id, ip, port, getPulse());
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
     * Disconects car from the network, stopping all the threads and tasks
	 */
	public void shutdown() {
        threadService.shutdownNow();
		pulseScheduler.shutdownNow();
		primaryMonitor.shutdown();
		secondaryMonitor.shutdown();
        hwNodeMonitor.shutdown();
		msgHandler.close();
	}

    public void setVelocity(double velocity) {
        positionLock.writeLock().lock();
        this.velocity = velocity;
        positionLock.writeLock().unlock();
    }
}
