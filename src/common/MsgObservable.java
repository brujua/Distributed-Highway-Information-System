package common;

public interface MsgObservable {

	public void addListener(MsgListener l);
	public void removeListener(MsgListener l);
}
