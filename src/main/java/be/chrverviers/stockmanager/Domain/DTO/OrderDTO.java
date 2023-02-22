package be.chrverviers.stockmanager.Domain.DTO;

import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Order;
import be.chrverviers.stockmanager.Domain.Models.User;

@Entity
public class OrderDTO {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private int id;		
	
	@OneToMany
	private List<Item> items;
	
	@ManyToOne
	private User user;
	
	private Date date;
	
	private boolean isReceived;
	
	public OrderDTO() {
		super();
	}

	public OrderDTO(int id, List<Item> items, User user, Date date, boolean isReceived) {
		super();
		this.id = id;
		this.items = items;
		this.user = user;
		this.date =date;
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
	
	public Order toOrder() {
		Order order = new Order();
		order.setId(id);
		order.setDate(date);
		order.setItems(items);
		order.setIsReceived(isReceived);
		order.setTypes(null);
		order.setUser(user);
		return order;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderDTO other = (OrderDTO) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
