package be.chrverviers.stockmanager.Domain.Models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class InterventionType {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private int id;
	
	private String name;
	
	private Boolean shouldSendMailHelpline;
	
	private Boolean shouldSendMailUser;
	
	public InterventionType() {
		super();
	}
	
	public InterventionType(String name) {
		super();
		this.name = name;
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
	
	public Boolean getShouldSendMailHelpline() {
		return shouldSendMailHelpline;
	}

	public void setShouldSendMailHelpline(Boolean shouldSendMailHelpline) {
		this.shouldSendMailHelpline = shouldSendMailHelpline;
	}

	public Boolean getShouldSendMailUser() {
		return shouldSendMailUser;
	}

	public void setShouldSendMailUser(Boolean shouldSendMailUser) {
		this.shouldSendMailUser = shouldSendMailUser;
	}	
	
}
