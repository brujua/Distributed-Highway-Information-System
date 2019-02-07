package network;

import common.StNode;

public class MT_Redirect extends Message {

	private StNode redirectedNode;

	public MT_Redirect(StNode sender, String responseId, StNode redirectedNode) {
		super(MsgType.REDIRECT, sender, responseId);
		this.redirectedNode = redirectedNode;
	}
	public StNode getRedirectedNode() {
		return redirectedNode;
	}

}
