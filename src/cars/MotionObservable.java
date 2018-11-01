package cars;

public interface MotionObservable {
	public void addObserver(MotionObserver mo);
	public void removeObserver(MotionObserver mo);
}
