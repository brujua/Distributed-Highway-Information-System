package cars;

import common.*;
import network.CorruptDataException;
import network.Message;
import network.MsgHandler;
import network.MsgListener;
import network.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Car implements MsgListener, MotionObservable{

    private static final long SLEEP_BETWEEN_TRIES_REG_IN_NET = 5000;
    private Logger logger;

    public static final String CONFIG_FILE_NAME = "config-cars";
    public final TimeUnit standardTimeUnit = TimeUnit.MILLISECONDS;

    // configurable parameters
    public int tentativePort = 5555;
    public double PRIMARY_RANGE = 2000; //near cars
    public double SECONDARY_RANGE = 9000; // far cars
    public int pulseRefreshTime = 1000;
    public int timeoutTime = 5000;
    public int timeoutCheckFrequency = 2000;

    private String id;

	private String name = null;
	private int port;
	private Position position;
	private double velocity;
    private ReentrantReadWriteLock positionLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock selectedHWNodeLock = new ReentrantReadWriteLock();

    private List<StNode> possibleHWNodes = new ArrayList<>();
    private final List<BroadcastMessage> broadcastMsgs = new ArrayList<>();
    private MsgHandler msgHandler;
	private StNode selectedHWNode;
    private CarMonitor primaryMonitor; //near cars
    private SingleNodeMonitor hwNodeMonitor;
	private PulseEmiter pulseEmiter;
	private List<MotionObserver> motionObservers = new ArrayList<>();
	private ScheduledExecutorService pulseScheduler = Executors.newSingleThreadScheduledExecutor();
	private ExecutorService threadService = Executors.newCachedThreadPool();

    public Car(Position position, double velocity) {
        id = UUID.randomUUID().toString();
        logger = LoggerFactory.getLogger(getName());
        this.position = position;
        this.velocity = velocity;
        initConfigProperties();
        this.port = Util.getAvailablePort(tentativePort);
        primaryMonitor = new CarMonitor("PrimaryMonitor: " + getName(), timeoutTime, timeoutCheckFrequency);
        hwNodeMonitor = new SingleNodeMonitor(timeoutTime, timeoutCheckFrequency, getName());
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
     * if at any point the configuration fails, the configurable parameters pending will remain untouched.
     */
    private void initConfigProperties() {
        try {
            ConfigReader configReader = new ConfigReader(CONFIG_FILE_NAME);
            tentativePort = configReader.getIntProperty("tentative-port");
            PRIMARY_RANGE = configReader.getIntProperty("primary-range");
            SECONDARY_RANGE = configReader.getIntProperty("secondary-range");
            pulseRefreshTime = configReader.getIntProperty("pulse-frequency");
            timeoutTime = configReader.getIntProperty("timeout-time");
            timeoutCheckFrequency = configReader.getIntProperty("timeout-check-frequency");
            possibleHWNodes = configReader.getNodes();
        } catch (MissingResourceException e) {
            logger.error("config file its missing or corrupt: " + e.getMessage());
        }

    }

    /**
     * Starts a thread that listens for UDP messages
     *
     * @return this (Fluid Syntax)
     */
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
            Message msg = new HelloMessage(getCarStNode());
			response = msgHandler.sendUDPWithResponse(hwNode, msg);

			responseMsg = response.get();

        	switch(responseMsg.getType()) {
            	case HELLO_RESPONSE: {
                    handleHelloResponse(responseMsg);
                    updateHWNode(responseMsg.getSender());
		            return true;
                }
            	case REDIRECT: {
            		return handleRedirect(responseMsg);
            	}
            	default:{
                    logger.error("Response from hw-node of wrong type, type: {} ", responseMsg.getType());
            		return false;
            	}
        	}
        }
		catch (CorruptDataException e) {
			logger.error("corrupt data on hw-node response");
			return false;
        } catch (ExecutionException e) { // from the CompletableFuture
        	try {
        		throw e.getCause();
        	} catch(TimeoutException toe) {
                logger.info("Node did not respond, tried on port: {}", hwNode.getPort());
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
                    Thread.sleep(SLEEP_BETWEEN_TRIES_REG_IN_NET);
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
		    positionLock.readLock().unlock();
        }
    }

    /**
     * starts a thread that periodically sends pulses to neighbours
     *
     * @return this (Fluid Syntax)
     */
	public Car emitPulses() {
        pulseScheduler.scheduleWithFixedDelay(pulseEmiter, pulseRefreshTime, pulseRefreshTime, standardTimeUnit);
		return this;
	}

    public boolean containBroadcast(BroadcastMessage broadcast) {
		//TODO is only test method dont use
		return broadcastMsgs.contains(broadcast);
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
            threadService.execute(() -> {
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
                            handleError(m);
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
            });
        } else {
            logger.info("Message receive while shut down");
        }
	}

    private void handleError(Message m) throws CorruptDataException {
        if (m.getType() != MessageType.ERROR || !(m instanceof ErrorMessage))
            throw new CorruptDataException();
        ErrorMessage error = (ErrorMessage) m;
        logger.error("Received error message: {}" + error.getErrorMsg());
    }

    private void handleAlive(Message m) throws CorruptDataException {
        if (m.getType() != MessageType.ALIVE) {
            throw new CorruptDataException();
        }
        StNode node = m.getSender();
        logger.debug("Received Alive from: {}", node);

        selectedHWNodeLock.readLock().lock();
        if (selectedHWNodeLock != null && selectedHWNode.equals(node))
            hwNodeMonitor.update(node);
        selectedHWNodeLock.readLock().unlock();
    }

    private void handleHello(Message m) throws CorruptDataException {
        if (m.getType() != MessageType.HELLO) {
			throw new CorruptDataException();
		}
        StNode car = m.getSender();
        logger.info("Hello received from node: " + car);
		sendHelloResponse(m);

	}

	/**
	 * @param redirectMsg - message containing the redirect information
	 * @return boolean - if the redirect was successful
     * @throws CorruptDataException when the message its of wrong type, or the containing data isn´t of type RedirectMessage
	 */
	private boolean handleRedirect(Message redirectMsg) throws CorruptDataException {
        if (redirectMsg.getType() != MessageType.REDIRECT || !(redirectMsg instanceof RedirectMessage))
			throw new CorruptDataException();
        RedirectMessage redi = (RedirectMessage) redirectMsg;
        logger.info("Redirected received to: {}", redi.getRedirectedNode());
		return tryRegister(redi.getRedirectedNode());
	}

	/**
	 * @param responseMsg - message received
     * @throws CorruptDataException when the message its of wrong type, or the contained data isn´t of type HelloResponseMessage
     */
    private void handleHelloResponse(Message responseMsg) throws CorruptDataException {
        if (responseMsg.getType() != MessageType.HELLO_RESPONSE || !(responseMsg instanceof HelloResponseMessage))
			throw new CorruptDataException();
        HelloResponseMessage helloRsp = (HelloResponseMessage) responseMsg;
        logger.info("Hello Response received from node: {}", helloRsp.getSender());

		//check new cars that i dont know of, and send them a Hello msg
		List<CarStNode> knownCars = primaryMonitor.getList();
		for (CarStNode car : helloRsp.getCars()) {
			//check if its not this same car
			if (!car.getId().equals(id)) {
				updateNeigh(car);
				if (!knownCars.contains(car)) {
                    sendHelloToCar(car.getStNode());
				}
			}
		}
	}



	private void handlePulse(Message m) throws CorruptDataException {
        if (m.getType() != MessageType.PULSE || !(m instanceof PulseMessage)) {
			throw new CorruptDataException();
		}
        CarStNode car = ((PulseMessage) m).getCarNode();
        updateNeigh(car);
        logger.debug("Pulse received from node: {}", car);
    }

    private void handleBroadcast(Message msg) throws CorruptDataException {
        if (msg.getType() != MessageType.BROADCAST || !(msg instanceof BroadcastMessage)) {
			throw new CorruptDataException();
		}
        BroadcastMessage broadcast = (BroadcastMessage) msg;
        synchronized (broadcastMsgs) {
            if (!broadcastMsgs.contains(broadcast) && broadcast.getTTL() > 0) {
                logger.info("Broadcast Message received: {}", broadcast.getMsg());
                broadcastMsgs.add(broadcast);
                //resend the msg to flood
                broadcast.setSender(getStNode(), true);
                broadcast.decrementTTL();
                floodBroadcast(broadcast);
            }
        }
    }

    private boolean sendHelloToCar(StNode node){
        CompletableFuture<Message> msgHelloRsp = msgHandler.sendUDPWithResponse(node, new HelloMessage(getCarStNode()));
        try {
            handleHelloResponse(msgHelloRsp.get());
			return true;
		} catch (InterruptedException e) {
			logger.error("Interrupted while waiting hello response");
			return false;
		} catch (ExecutionException e) {
			if (e.getCause() instanceof TimeoutException) {
				logger.error("Node" + node + " does not respond to Hello");
			}
			return false;
        } catch (CorruptDataException e) {
            logger.error("hello response corrupted");
            return false;
        }
	}

    private void floodBroadcast(BroadcastMessage broadcastMsg) {
        for (CarStNode node : getNeighs()) {
            msgHandler.sendUDP(node, broadcastMsg);
        }
        selectedHWNodeLock.readLock().lock();
        if (selectedHWNode != null)
            msgHandler.sendUDP(selectedHWNode, broadcastMsg);
        selectedHWNodeLock.readLock().unlock();

        synchronized (broadcastMsgs) {
            if (!broadcastMsgs.contains(broadcastMsg))
                broadcastMsgs.add(broadcastMsg);
        }
	}

    private void sendHelloResponse(Message m) {
        if (!(m.getType() == MessageType.HELLO)) {
            throw new InvalidParameterException();
        }
        StNode car = m.getSender();
        HelloResponseMessage response = new HelloResponseMessage(m.getId(), getStNode(), primaryMonitor.getList());
        msgHandler.sendUDP(car, response);
    }

    public BroadcastMessage sendBroadcast(String message) {
        BroadcastMessage broadcast = new BroadcastMessage(getStNode(), message, true);
        floodBroadcast(broadcast);
        return broadcast;
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
        return new CarStNode(id, port, getPulse());
	}

	public StNode getStNode() {
        return new StNode(id, port);
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
        hwNodeMonitor.shutdown();
		msgHandler.close();
	}

    public void setVelocity(double velocity) {
        positionLock.writeLock().lock();
        this.velocity = velocity;
        positionLock.writeLock().unlock();
    }
}
