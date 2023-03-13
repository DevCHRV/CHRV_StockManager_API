package be.chrverviers.stockmanager.Domain.Models;

import java.util.Collections;
import java.util.Date;
import java.util.List;
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
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@Cascade(CascadeType.DETACH)
	private Type type;
	private String name;
	private String reference;
	private String serialNumber;
	private String description;
	private String provider;
	private Date purchasedAt = new Date();
	private Date receivedAt = new Date();
	private Date warrantyExpiresAt = new Date();
	private double price;
	
	@ManyToOne
	private Room room;
	
	private Date lastCheckupAt = new Date();
	private int checkupInterval;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=javax.persistence.CascadeType.ALL)
	@JoinColumn(name="item_id")
	@Cascade(CascadeType.ALL)
	//We trick the API to only send them as ID because otherwise it would send the first occurence as a full JSON object
	//and the next as id.
	//Which is a pain because the front-end can't really know when it's going to receive an id or an object
	private List<Licence> licence = Collections.emptyList();
	
	@OneToMany(fetch=FetchType.LAZY, cascade=javax.persistence.CascadeType.ALL)
	@JoinColumn(name="item_id")
	@Cascade(CascadeType.ALL)
	private List<Intervention> interventions = Collections.emptyList();
	
	//For some reason Spring doesn't want to deserialize the Order class...
	@JsonIgnore
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Order order;

	private boolean isAvailable = true;
	
	private boolean isPlaced = false;
	
	public Item() {
		super();
	}
	
	public Item(int id) {
		this.id=id;
	}
	
	public Item(int id, String name) {
		this.id=id;
		this.name = name;
	}
	
	public Type getType() {
		return type;
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
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

	public Date getPurchasedAt() {
		return purchasedAt;
	}

	public void setPurchasedAt(Date purchasedAt) {
		this.purchasedAt = purchasedAt;
	}

	public Date getReceivedAt() {
		return receivedAt;
	}

	public void setReceivedAt(Date receivedAt) {
		this.receivedAt = receivedAt;
	}

	public Date getWarrantyExpiresAt() {
		return warrantyExpiresAt;
	}

	public void setWarrantyExpiresAt(Date warrantyExpiresAt) {
		this.warrantyExpiresAt = warrantyExpiresAt;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Date getLastCheckupAt() {
		return lastCheckupAt;
	}

	public void setLastCheckupAt(Date lastCheckupAt) {
		this.lastCheckupAt = lastCheckupAt;
	}

	public int getCheckupInterval() {
		return checkupInterval;
	}

	public void setCheckupInterval(int checkupInterval) {
		this.checkupInterval = checkupInterval;
	}

	public List<Licence> getLicence() {
		return licence;
	}

	public void setLicence(List<Licence> licence) {
		this.licence = licence;
	}

	public boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public boolean getIsPlaced() {
		return isPlaced;
	}

	public void setIsPlaced(boolean isPlaced) {
		this.isPlaced = isPlaced;
	}
	
	public void setInterventions(List<Intervention> interventions) {
		this.interventions = interventions;
	}
	
	public Order getOrder() {
		return this.order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}
	
	public List<Intervention> getInterventions(){
		return this.interventions;
	}

	@Override
	public String toString() {
		return String.format("ITEM [ID=%s, SN=%s, REF=%s, DESC=%s, TYPE=%s, LICENCES=%s ]", this.id, this.serialNumber, this.reference, this.description, this.type.getName()+this.type.getId(), this.licence);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
