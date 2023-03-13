package be.chrverviers.stockmanager.Domain.Models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class LicenceType {
	
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private int id;
	
	private String name;
	
	private String alias;
	
	public LicenceType() {
		super();
	}
	
	public LicenceType(String name) {
		super();
		this.name = name;
	}
	
	public LicenceType(String name, String alias) {
		super();
		this.name = name;
		this.alias = alias;
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
	
	public String getAlias() {
		return this.alias;
	}
	
	public void setAlias(String alias) {
		this.alias = alias;
	}
}
