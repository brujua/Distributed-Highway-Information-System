package network;

public interface MsgObservable {

	void addMsgListener(MsgListener l);

	void removeListener(MsgListener l);
}
