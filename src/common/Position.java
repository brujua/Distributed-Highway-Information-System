package common;

public class Position {

	private String cordx;
	private String cordy;
	//max distance to a near node
	private final double maxDistance = 10;
	
	
	
	public Position(String coordX, String coordY) {
		super();
		coordX = cordx;
		coordY = cordy;
		
		
		
	}

	public String getCordx() {
		return cordx;
	}

	public String getCordy() {
		return cordy;
	}
	
	public void isNear(Position position) {
		
		//Return if the parameter position is near this position 
		//TODO
		return ;
		
	}
	
	
}
