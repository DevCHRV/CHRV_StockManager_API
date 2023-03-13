package be.chrverviers.stockmanager.Domain.DTO;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import be.chrverviers.stockmanager.Domain.Models.Order;
import be.chrverviers.stockmanager.Domain.Models.Type;
import be.chrverviers.stockmanager.Domain.Models.User;

public class OrderCreationDTO {
	
	private int id;		
	
	private List<OrderCreationTypeDTO> types = Collections.emptyList();
	
	private User user;
	
	private Date date;
	
	private boolean isReceived;
	
	public OrderCreationDTO() {
		super();
	}

	public OrderCreationDTO(int id, List<OrderCreationTypeDTO> types, User user, Date date, boolean isReceived) {
		super();
		this.id = id;
		this.types = types;
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

	public List<OrderCreationTypeDTO> getTypes() {
		return types;
	}

	public void setTypes(List<OrderCreationTypeDTO> types) {
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
	
	public Order toOrder() {
		Order order = new Order();
		order.setId(id);
		order.setDate(date);
		order.setTypes(getTypesAsOrderTypes());
		order.setIsReceived(isReceived);
		order.setUser(user);
		return order;
	}
	
	private Map<Type, Integer> getTypesAsOrderTypes(){
		Map<Type, Integer> map = new HashMap<Type, Integer>();
		
		for(OrderCreationTypeDTO type: this.getTypes()) {
			for(OrderCreationItemDTO item: type.getItems()) {
				Type tmp = new Type(type.getId(), type.getAlias(), type.getName(), type.getDescription());
				if(map.containsKey(tmp)){
					map.merge(tmp, item.getQuantity(), Integer::sum);
				} else {
					map.put(tmp, item.getQuantity());
				}
			}
		}
		
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderCreationDTO other = (OrderCreationDTO) obj;
		if (id != other.id)
			return false;
		return true;
	}
}