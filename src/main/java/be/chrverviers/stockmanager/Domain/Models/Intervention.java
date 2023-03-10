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
	
	@ManyToOne
	private Room room;
	
	private String ticketNumber;

	public Intervention() {
		super();
	}
	
	public Intervention(int id, InterventionType type, String description, User notifier, User user,
			Item item, List<Licence> licences, Date expectedDate, Date actualDate, Room room,
			String ticketNumber) {
		super();
		this.id = id;
		this.type = type;
		this.description = description;
		this.notifier = notifier;
		this.user = user;
		this.item = item;
		this.licences = licences;
		this.expectedDate = expectedDate;
		this.actualDate = actualDate;
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

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
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

	@Override
	public String toString() {
		return "Intervention [id=" + id + ", ticketNumber=" + ticketNumber + "]";
	}
}
