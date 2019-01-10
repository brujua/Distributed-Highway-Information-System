package simulator;

public interface SimController {

    void addCar(String name, int startPosition, double velocity, int delay);

    void addHWNode(String name, int delay);

    void setSimModeOn(boolean on);
}
