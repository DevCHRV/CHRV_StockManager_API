package be.chrverviers.stockmanager.Domain.DTO;

import java.util.Collections;
import java.util.List;

import be.chrverviers.stockmanager.Domain.Models.Type;

public class OrderCreationTypeDTO {
	
	private int id;
	private String alias;
	private String name;
	private String description;
	private List<OrderCreationItemDTO> items =  Collections.emptyList();
	
	public OrderCreationTypeDTO() {
		super();
	}

	public OrderCreationTypeDTO(int id, String name, String description, List<OrderCreationItemDTO> items) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.items = items;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<OrderCreationItemDTO> getItems() {
		return items;
	}

	public void setItems(List<OrderCreationItemDTO> items) {
		this.items = items;
	}
	
	public Type toItemType() {
		return new Type(id, alias, name, description);
	}
}
