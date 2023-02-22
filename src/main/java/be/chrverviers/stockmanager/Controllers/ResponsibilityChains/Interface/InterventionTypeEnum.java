package be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface;

public enum InterventionTypeEnum {
	INSTALLATION(1),
	DESINSTALLATION(2),
	LOAN(3),
	RETURN(4),
	DEPANNAGE(5),
	INSTALLATION_LICENCE(6),
	DESINSTALLATION_LICENCE(7);
			
	public int value;
	
	private InterventionTypeEnum(int value) {
		this.value = value;
	}
}
