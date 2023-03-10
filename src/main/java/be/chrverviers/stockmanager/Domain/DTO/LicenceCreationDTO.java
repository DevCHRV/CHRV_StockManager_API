package be.chrverviers.stockmanager.Domain.DTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.LicenceType;

public class LicenceCreationDTO {
	
	private String reference;
	
	private String description;
	
	private String value;
	
	private LocalDate purchasedAt;
	
	private LicenceType type;
	
	public int quantity;
	
	public LicenceCreationDTO() {
		super();
	}
	
	public LicenceCreationDTO(LicenceCreationLicenceDTO licence) {
		super();
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
	
	public List<LicenceCreationDTO> toLicenceDTOList(){
		List<LicenceCreationDTO> list = new ArrayList<LicenceCreationDTO>();
		for(int i = 0; i<quantity; i++) {
			list.add(this);
		}
		return list;
	}
	
	public List<Licence> toLicenceList(){
		List<Licence> list = new ArrayList<Licence>();
		for(int i = 0; i<quantity; i++) {
			list.add(new Licence(description, value, type,  new Date(
					this.purchasedAt
					.atStartOfDay()
					.atZone(ZoneId.systemDefault())
					.toInstant().toEpochMilli()
				)));
		}
		return list;
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
