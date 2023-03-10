package be.chrverviers.stockmanager.Domain.DTO;

import java.time.LocalDate;

public class OrderCreationItemDTO {
	private String description;
	private double price;
	private LocalDate purchasedAt;
	private LocalDate warrantyExpiresAt;
	private int checkupInterval;
	private String provider;
	private int quantity;
	
	public OrderCreationItemDTO() {
		super();
	}

	public OrderCreationItemDTO(String description, double price, LocalDate purchasedAt, LocalDate warrantyExpiresAt, 
			int checkupInterval, String provider, int quantity) {
		super();
		this.description = description;
		this.price = price;
		this.purchasedAt = purchasedAt;
		this.warrantyExpiresAt = warrantyExpiresAt;
		this.checkupInterval = checkupInterval;
		this.provider = provider;
		this.quantity = quantity;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public LocalDate getPurchasedAt() {
		return purchasedAt;
	}
	
	public void setPurchasedAt(LocalDate purchasedAt) {
		this.purchasedAt = purchasedAt;
	}
	
	public LocalDate getWarrantyExpiresAt() {
		return warrantyExpiresAt;
	}
	
	public void setWarantyExpiresAt(LocalDate warranty_expires_at) {
		this.warrantyExpiresAt = warranty_expires_at;
	}
	
	public int getCheckupInterval() {
		return checkupInterval;
	}
	
	public void setCheckupInterval(int checkupInterval) {
		this.checkupInterval = checkupInterval;
	}
	
	public String getProvider() {
		return provider;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
