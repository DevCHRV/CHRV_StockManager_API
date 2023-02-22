package be.chrverviers.stockmanager.Domain.Models;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.data.relational.core.mapping.Table;

@Entity
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Cascade(CascadeType.DETACH)
	private Type type;

	
	private String reference;
	private String serial_number;
	private String description;
	private String provider;
	private Date purchased_at = new Date();
	private Date received_at = new Date();
	private Date warranty_expires_at = new Date();
	private double price;
	private String unit;
	private String room;
	private Date last_checkup_at = new Date();
	private int checkup_interval;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=javax.persistence.CascadeType.ALL)
	@JoinColumn(name="item_id")
	@Cascade(CascadeType.ALL)
	//We trick the API to only send them as ID because otherwise it would send the first occurence as a full JSON object
	//and the next as id.
	//Which is a pain because the front-end can't really know when it's going to receive an id or an object
	private List<Licence> licence = Collections.emptyList();

	private boolean is_available;
	
	private boolean is_placed;
	
	public Item() {
		super();
	}
	
	public Item(int id) {
		this.id=id;
	}
	
	public Type getType() {
		return type;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getSerial_number() {
		return serial_number;
	}

	public void setSerial_number(String serial_number) {
		this.serial_number = serial_number;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public Date getPurchased_at() {
		return purchased_at;
	}

	public void setPurchased_at(Date purchased_at) {
		this.purchased_at = purchased_at;
	}

	public Date getReceived_at() {
		return received_at;
	}

	public void setReceived_at(Date received_at) {
		this.received_at = received_at;
	}

	public Date getWarranty_expires_at() {
		return warranty_expires_at;
	}

	public void setWarranty_expires_at(Date warranty_expires_at) {
		this.warranty_expires_at = warranty_expires_at;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public Date getLast_checkup_at() {
		return last_checkup_at;
	}

	public void setLast_checkup_at(Date last_checkup_at) {
		this.last_checkup_at = last_checkup_at;
	}

	public int getCheckup_interval() {
		return checkup_interval;
	}

	public void setCheckup_interval(int checkup_interval) {
		this.checkup_interval = checkup_interval;
	}

	public List<Licence> getLicence() {
		return licence;
	}

	public void setLicence(List<Licence> licence) {
		this.licence = licence;
	}

	public boolean getIs_available() {
		return is_available;
	}

	public void setIs_available(boolean is_available) {
		this.is_available = is_available;
	}

	public boolean getIs_placed() {
		return is_placed;
	}

	public void setIs_placed(boolean is_placed) {
		this.is_placed = is_placed;
	}
	
//	public Order getOrder() {
//		return this.order;
//	}
//	
//	public void setOrder(Order order) {
//		this.order = order;
//	}

	@Override
	public String toString() {
		return String.format("ITEM {ID:%s, SN:%s, REF: %s, DESC:%s, TYPE:%s, LICENCES:%s }", this.id, this.serial_number, this.reference, this.description, this.type.getName()+this.type.getId(), this.licence);
	}

}
