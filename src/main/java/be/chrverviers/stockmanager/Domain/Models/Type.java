package be.chrverviers.stockmanager.Domain.Models;

import java.util.Collections;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class Type {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	private String description;
	private int expectedLifetime;
	private int totalQuantity;
	private int availableQuantity;
	
    @OneToMany(fetch=FetchType.LAZY, mappedBy="type")
	private Set<Item> items = Collections.emptySet();
	
	public Type() {
		super();
	}
	
	public Type(String name, String description, int expectedLifetime, int totalQuantity, int availableQuantity) {
		super();
		this.name = name;
		this.description = description;
		this.expectedLifetime = expectedLifetime;
		this.totalQuantity = totalQuantity;
		this.availableQuantity = availableQuantity;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
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
	
	public int getExpectedLifetime() {
		return expectedLifetime;
	}
	
	public void setExpectedLifetime(int expectedLifetime) {
		this.expectedLifetime = expectedLifetime;
	}
	
	public int getTotalQuantity() {
		return totalQuantity;
	}
	
	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}
	
	public int getAvailableQuantity() {
		return availableQuantity;
	}
	
	public void setAvailableQuantity(int availableQuantity) {
		this.availableQuantity = availableQuantity;
	}
	
	@Override
	public String toString() {
		return String.format("ITEM {id:%s, name:%s, }", this.id, this.name);
	}

	
}
