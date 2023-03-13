package be.chrverviers.stockmanager.Domain.Models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
public class User implements UserDetails {

	/**
	 * 
	 */
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private int id;
	private String username;
	
	private String firstname;
	private String lastname;
	private String email;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="users_roles",
		joinColumns = @JoinColumn(
			name="user_id", referencedColumnName="id"),
		inverseJoinColumns= @JoinColumn(
			name="role_id", referencedColumnName="id"))
	private List<Role> roles = new ArrayList<>();
	
	@OneToMany(mappedBy="user",fetch=FetchType.LAZY)
	@JsonIdentityReference(alwaysAsId=true)
	private Set<Licence> licences;
	
	private boolean isActive;
	
	public User() {
		super();
	}
	
	public User(int id) {
		super();
		this.id = id;
	}

	public User(String username, String firstname, String lastname) {
		super();
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
	}
	
	public User(String username, String firstname, String lastname, String email) {
		super();
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}

	public User(String username, String firstname, String lastname, List<Role> roles) {
		super();
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.roles = roles;
	}
	
	public User(int id, String username, String firstname, String lastname, List<Role> roles, Set<Licence> licences) {
		super();
		this.id = id;
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.roles = roles;
		this.licences = licences;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	} 
	
	public Set<Licence> getLicences() {
		return licences;
	}

	public void setLicences(Set<Licence> licences) {
		this.licences = licences;
	} 

	@JsonIgnore
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
	    List<GrantedAuthority> authorities
	      = new ArrayList<>();
	    for (Role role: this.roles) {
	        authorities.add(new SimpleGrantedAuthority(role.getName()));
	    }
	    return authorities;
	}
	
	public boolean getIsActive() {
		return this.isActive;
	}
	
	public void setIsActive(boolean is) {
		this.isActive = is;
	}

	//Simple Override, we don't really need thos
	@Override
	public boolean isAccountNonExpired() {
		return this.isActive;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.isActive;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.isActive;
	}

	@Override
	public boolean isEnabled() {
		return this.isActive;
	}

	@Override
	public String toString() {
		return "User [ID=" + id + ", USERNAME=" + username + "]";
	}

	@Override
	public String getPassword() {
		return "";
	}
}
