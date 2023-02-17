package be.chrverviers.stockmanager.Domain.Models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class Intervention {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private int id;
	
	@ManyToOne
	private InterventionType type;
	
	@OneToOne
	private Report report = null;
	
	private String description;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User notifier = null;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Item item;
	
	@ManyToMany(fetch=FetchType.LAZY)
	private List<Licence> licences;
	
	private Date expectedDate;
	
	private Date actualDate;
	
	private String unit;
	
	private String room;
	
	private String ticketNumber;

	public Intervention() {
		super();
	}
	
	public Intervention(int id, InterventionType type, Report report, String description, User notifier, User user,
			Item item, List<Licence> licences, Date expectedDate, Date actualDate, String unit, String room,
			String ticketNumber) {
		super();
		this.id = id;
		this.type = type;
		this.report = report;
		this.description = description;
		this.notifier = notifier;
		this.user = user;
		this.item = item;
		this.licences = licences;
		this.expectedDate = expectedDate;
		this.actualDate = actualDate;
		this.unit = unit;
		this.room = room;
		this.ticketNumber = ticketNumber;
	}



	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public InterventionType getType() {
		return type;
	}

	public void setType(InterventionType type) {
		this.type = type;
	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getNotifier() {
		return notifier;
	}

	public void setNotifier(User notifier) {
		this.notifier = notifier;
	}

	public User getUser() {
		return user;
	}
	
	public Item getItem() {
		return this.item;
	}
	
	public void setItem(Item item) {
		this.item = item;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getExpectedDate() {
		return expectedDate;
	}

	public void setExpectedDate(Date expectedDate) {
		this.expectedDate = expectedDate;
	}

	public Date getActualDate() {
		return actualDate;
	}

	public void setActualDate(Date actualDate) {
		this.actualDate = actualDate;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getTicketNumber() {
		return ticketNumber;
	}

	public void setTicketNumber(String ticketNumber) {
		this.ticketNumber = ticketNumber;
	}

	public List<Licence> getLicences() {
		return licences;
	}

	public void setLicences(List<Licence> licences) {
		this.licences = licences;
	}
}
