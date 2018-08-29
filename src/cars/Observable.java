package cars;

public interface Observable {

	public void addListener(Listener l);
	public void removeListener(Listener l);
}
