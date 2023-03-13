package be.chrverviers.stockmanager.Domain.Models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Report {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private int id;
	
	@OneToOne
	private Intervention intervention;
	
	private String description;

	public Report() {
		super();
	}
	
	public Report(int id, Intervention intervention, String description) {
		super();
		this.id = id;
		this.intervention = intervention;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Intervention getIntervention() {
		return intervention;
	}

	public void setIntervention(Intervention intervention) {
		this.intervention = intervention;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
