package common;

public enum Units {
	KiloMeters, Meters, Miles;   

	
	public double normalizeUnity(Units patron, Position guetPosition) {
		double resultUnits = 0;
		Units compareUnity = guetPosition.getUnity();
		if(patron == Units.KiloMeters) {
			if(compareUnity == Units.KiloMeters) {
				resultUnits = 1;
			}else {
				if(compareUnity == Units.Meters) {
					resultUnits = 0.001;
				}else {
					if(compareUnity == Units.Miles) {
						resultUnits = 0.621;
					}
				}
				
			}
		}else {
			if(patron == Units.Meters) {
				if(compareUnity == Units.KiloMeters) {
					resultUnits = 1000;
				}else {
					if(compareUnity == Units.Meters) {
						resultUnits = 1;
					}else {
						if(compareUnity == Units.Miles) {
							resultUnits = 0.000621;
						}
					}
					
				}
				
			}else {
				if(patron == Units.Miles) {
					if(compareUnity == Units.KiloMeters) {
						resultUnits = 1.609;
					}else {
						if(compareUnity == Units.Meters) {
							resultUnits = 1609.34;
						}else {
							if(compareUnity == Units.Miles) {
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

