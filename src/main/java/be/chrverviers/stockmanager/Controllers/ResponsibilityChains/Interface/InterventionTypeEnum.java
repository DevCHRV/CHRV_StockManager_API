package be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface;

/**
 * Enum that represent all the Intervention Types
 * @author colincyr
 * The value of the Enum's occurrence MUST be the id of the corresponding type in the database
 */
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
