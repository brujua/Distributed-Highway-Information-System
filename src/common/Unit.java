package common;

public enum Unit {
	KiloMeters, Meters, Miles;   

	
	public double normalizeUnity(Unit patron, Position guetPosition) {
		double resultUnits = 0;
		Unit compareUnity = guetPosition.getUnity();
		if(patron == Unit.KiloMeters) {
			if(compareUnity == Unit.KiloMeters) {
				resultUnits = 1;
			}else {
				if(compareUnity == Unit.Meters) {
					resultUnits = 0.001;
				}else {
					if(compareUnity == Unit.Miles) {
						resultUnits = 0.621;
					}
				}
				
			}
		}else {
			if(patron == Unit.Meters) {
				if(compareUnity == Unit.KiloMeters) {
					resultUnits = 1000;
				}else {
					if(compareUnity == Unit.Meters) {
						resultUnits = 1;
					}else {
						if(compareUnity == Unit.Miles) {
							resultUnits = 0.000621;
						}
					}
					
				}
				
			}else {
				if(patron == Unit.Miles) {
					if(compareUnity == Unit.KiloMeters) {
						resultUnits = 1.609;
					}else {
						if(compareUnity == Unit.Meters) {
							resultUnits = 1609.34;
						}else {
							if(compareUnity == Unit.Miles) {
								resultUnits = 1;
							}
						}
						
					}
					
				}
			}
		}
		
		
		return resultUnits;
	}
		

}

