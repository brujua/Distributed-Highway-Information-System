package network;

import java.io.Serializable;

import cars.CarStNode;
import common.StNode;

public class MT_HelloResponse implements Serializable{
	
	private String responseId;
	private StNode stNode;
	private Iterable<CarStNode> cars;
	
	/**
	 * @param responseId
	 * @param stNode
	 * @param cars Must implement serializable
	 */
	public MT_HelloResponse(String responseId, StNode stNode, Iterable<CarStNode> cars) {
		super();
		this.responseId = responseId;
		this.stNode = stNode;
		this.cars = cars;
	}

	public String getResponseId() {
		return responseId;
	}

	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}

	public StNode getStNode() {
		return stNode;
	}

	public void setStNode(StNode stNode) {
		this.stNode = stNode;
	}

	public Iterable<CarStNode> getCars() {
		return cars;
	}

	public void setCars(Iterable<CarStNode> cars) {
		this.cars = cars;
	}
	
	
}
