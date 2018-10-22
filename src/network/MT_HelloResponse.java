package network;

import common.StNode;

public class MT_HelloResponse {
	
	private String responseId;
	private StNode stNode;
	private Iterable<StNode> cars;
	
	public MT_HelloResponse(String responseId, StNode stNode, Iterable<StNode> cars) {
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

	public Iterable<StNode> getCars() {
		return cars;
	}

	public void setCars(Iterable<StNode> cars) {
		this.cars = cars;
	}
	
	
}
