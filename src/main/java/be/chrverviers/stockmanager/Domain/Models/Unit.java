package be.chrverviers.stockmanager.Domain.Models;

import java.util.Collections;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Entity
public class Unit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String name;
	
	@OneToMany
	@JoinColumn(name="unit_id")
	private List<Room> rooms = Collections.emptyList();
	
	public Unit() {
		super();
	}
	
	public Unit(int id) {
		super();
		this.id = id;
	}
	
	public Unit(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public Unit(int id, String name, List<Room> rooms) {
		super();
		this.id = id;
		this.name = name;
		this.rooms = rooms;
	}

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

	public List<Room> getRooms() {
		return rooms;
	}

	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
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
		Unit other = (Unit) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Unit [id=" + id + ", name=" + name + "]";
	}
}
