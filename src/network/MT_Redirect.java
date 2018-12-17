package network;

import common.StNode;

import java.io.Serializable;

public class MT_Redirect implements Serializable {
	private String responseId;
	private StNode redirectedNode;
	
	public MT_Redirect(String responseId, StNode redirectedNode) {
		super();
		this.responseId = responseId;
		this.redirectedNode = redirectedNode;
	}

	public String getResponseId() {
		return responseId;
	}

	public StNode getRedirectedNode() {
		return redirectedNode;
	}
	
	
	
	
}
