package network.messages;

import common.StNode;
import network.Message;

public class RedirectMessage extends Message {

	private StNode redirectedNode;

	public RedirectMessage(StNode sender, String responseId, StNode redirectedNode) {
		super(MessageType.REDIRECT, sender, responseId);
		this.redirectedNode = redirectedNode;
	}
	public StNode getRedirectedNode() {
		return redirectedNode;
	}

}
