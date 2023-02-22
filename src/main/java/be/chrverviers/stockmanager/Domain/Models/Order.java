package be.chrverviers.stockmanager.Domain.Models;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
public class Order {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private int id;		
	
	@OneToMany
	private List<Item> items;
	
	@JsonSerialize(using=OrderTypeSerializer.class)
	@ManyToMany(targetEntity=Type.class)
	private Map<Type,Integer> types;
	
	@ManyToOne
	private User user;
	
	private Date date;
	
	private boolean isReceived;
	
	public Order() {
		super();
	}

	public Order(int id, List<Item> items, Map<Type, Integer> types, User user, Date date, boolean isReceived) {
		super();
		this.id = id;
		this.items = items;
		this.types = types;
		this.user = user;
		this.date = date;
		this.isReceived = isReceived;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> item) {
		this.items = item;
	}
	
	public Map<Type, Integer> getTypes() {
		return types;
	}

	public void setTypes(Map<Type, Integer> types) {
		this.types = types;
	}


	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public boolean getIsReceived() {
		return isReceived;
	}
	
	public void setIsReceived(boolean isReceived) {
		this.isReceived = isReceived;
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
		Order other = (Order) obj;
		if (id != other.id)
			return false;
		return true;
	}
}

class OrderTypeSerializer extends JsonSerializer<Map<Type,Integer>> {

    @Override
    public void serialize(Map<Type,Integer> value, 
      JsonGenerator gen,
      SerializerProvider serializers) 
      throws IOException, JsonProcessingException {
    	gen.writeStartArray();
    		for(Type t:value.keySet()) {
    	    	gen.writeStartObject();
    	        gen.writeFieldName("type");
    	    	gen.writeObject(t);
    	    	gen.writeNumberField("quantity", value.get(t));
    	    	gen.writeEndObject();
    		}
    	gen.writeEndArray();
    }
} 
