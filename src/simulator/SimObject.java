package simulator;

import java.awt.*;

public interface SimObject {

	String getID();

	void tick();

	void render(Graphics g);

    void setSimModeOn(boolean on);
}
