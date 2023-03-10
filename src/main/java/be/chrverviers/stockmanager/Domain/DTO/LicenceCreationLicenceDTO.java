package be.chrverviers.stockmanager.Domain.DTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.LicenceType;

public class LicenceCreationLicenceDTO {
	
	private String reference;
	
	private String description;
	
	private String value;
	
	private LocalDate purchasedAt;
	
	private LicenceType type;
	
	public int quantity;

	public LicenceCreationLicenceDTO() {
		super();
	}

	public LicenceCreationLicenceDTO(String reference, String description, String value, LocalDate purchasedAt, LicenceType type, int quantity) {
		super();
		this.reference = reference;
		this.description = description;
		this.value = value;
		this.purchasedAt = purchasedAt;
		this.quantity = quantity;
		this.type = type;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public LocalDate getPurchasedAt() {
		return purchasedAt;
	}

	public void setPurchasedAt(LocalDate purchasedAt) {
		this.purchasedAt = purchasedAt;
	}

	public LicenceType getType() {
		return type;
	}

	public void setType(LicenceType type) {
		this.type = type;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public Licence toLicence() {
		return new Licence(this.description, this.value, this.type, new Date(
				this.purchasedAt
				.atStartOfDay()
				.atZone(ZoneId.systemDefault())
				.toInstant().toEpochMilli()
			));
	}
}
