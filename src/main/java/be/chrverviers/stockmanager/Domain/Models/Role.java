package be.chrverviers.stockmanager.Domain.Models;

import java.util.Collections;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.JoinColumn;

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	private String name;
	
//    @ManyToMany(fetch=FetchType.LAZY, mappedBy = "roles")
//	private Set<User> users;
	
	@ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "roles_privileges", 
            joinColumns = @JoinColumn(
              name = "role_id", referencedColumnName = "id"), 
            inverseJoinColumns = @JoinColumn(
              name = "privilege_id", referencedColumnName = "id"))
	private Set<Privilege> privileges = Collections.emptySet();
	
	public int getId() {
		return id;
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

//	public Set<User> getUsers() {
//		return users;
//	}
//
//	public void setUsers(Set<User> users) {
//		this.users = users;
//	}

	public Set<Privilege> getPrivileges() {
		return privileges;
	}

	public void setPrivileges(Set<Privilege> privileges) {
		this.privileges = privileges;
	}

}