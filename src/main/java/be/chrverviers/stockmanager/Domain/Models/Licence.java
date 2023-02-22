package be.chrverviers.stockmanager.Domain.Models;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(
		  scope=Licence.class,
		  generator = ObjectIdGenerators.PropertyGenerator.class, 
		  property = "id")
public class Licence {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private int id;
	private String description;
	private String value;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User user;
	
	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="type_id", referencedColumnName="id")
	private LicenceType type;
	
	@ManyToOne
	private Item item;
	
	public Licence() {
		super();
	}
	
	public Licence(String description) {
		super();
		this.description = description;
	}
	
	public Licence(String description, String value, LicenceType type) {
		super();
		this.description = description;
		this.value = value;
		this.type = type;
	}
	
	public Licence(int id, String description, String value, User user, LicenceType type, Item item) {
		super();
		this.id = id;
		this.description = description;
		this.value = value;
		this.user = user;
		this.type = type;
		this.item = item;
	}

	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LicenceType getType() {
		return type;
	}

	public void setType(LicenceType type) {
		this.type = type;
	}
	
	public Item getItem() {
		return item;
	}
	
	public void setItem(Item item) {
		this.item = item;
	}
	
}
