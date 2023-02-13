package be.chrverviers.stockmanager.Domain.Models;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class Privilege {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	
	@ManyToMany(fetch=FetchType.LAZY, mappedBy = "privileges")
	@JsonBackReference(value="privilege-role-reference")
	private Set<Role> roles;
	
	public Privilege() {
		super();
	}
	
	public Privilege(String name, Set<Role> roles) {
		super();
		this.name = name;
		this.roles = roles;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
}
