package be.chrverviers.stockmanager.Repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.ResponsibilityChain;
import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.InterventionType;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
public class InterventionRepository implements IRepository<Intervention> {

	private JdbcTemplate template;
		
	public InterventionRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}
	
	@Override
	public List<Intervention> findAll() {
		String query = "SELECT i.*, it.*, u.*, t.id, t.reference FROM CCLIB.INTERVENTION i LEFT JOIN CCLIB.INTERVENTION_TYPE it ON i.TYPE_ID=it.ID LEFT JOIN CCLIB.USER u ON i.USER_ID = u.ID LEFT JOIN CCLIB.ITEM t ON i.ITEM_ID = t.ID ORDER BY EXPECTED_DATE DESC";
		return template.query(query, rowMapper);
	}

	@Override
	public Optional<Intervention> findById(int id) {
		String query = "SELECT i.*, it.*, u.*, n.*, t.id, t.reference, t.serial_number FROM CCLIB.INTERVENTION i LEFT JOIN CCLIB.INTERVENTION_TYPE it ON i.TYPE_ID = it.ID LEFT JOIN CCLIB.USER u ON i.USER_ID = u.ID LEFT JOIN CCLIB.USER n ON i.NOTIFIER_ID = n.ID LEFT JOIN CCLIB.ITEM t ON i.ITEM_ID = t.ID LEFT JOIN CCLIB.REPORT r ON r.INTERVENTION_ID = i.ID WHERE i.ID = ?";
		Intervention intervention = new Intervention();
		try { 
			intervention = template.queryForObject(query, withNotifierAndItemRowMapper, id);
		} catch(DataAccessException e) {
			//This means that there is no data, but we don't do anything because in this case we return an Optional
		}
		return Optional.ofNullable(intervention);
	}
	
	public List<Intervention> findForItem(Item item){
		String query = "SELECT i.*, it.*, u.*, t.id, t.reference FROM CCLIB.INTERVENTION i LEFT JOIN CCLIB.INTERVENTION_TYPE it ON i.TYPE_ID=it.ID LEFT JOIN CCLIB.USER u ON i.USER_ID = u.ID LEFT JOIN ITEM t ON i.ITEM_ID = t.ID WHERE i.ITEM_ID = ? ORDER BY EXPECTED_DATE DESC";
		List<Intervention> interventions = new ArrayList<Intervention>();
		try { 
			interventions = template.query(query, rowMapper, item.getId());
		} catch(DataAccessException e) {
			e.printStackTrace();
		}
		return interventions;
	}
	
	public List<Intervention> findForItemId(int itemId){
		String query = "SELECT i.*, it.*, u.*, t.id, t.reference FROM CCLIB.INTERVENTION i LEFT JOIN CCLIB.INTERVENTION_TYPE it ON i.TYPE_ID=it.ID LEFT JOIN CCLIB.USER u ON i.USER_ID = u.ID LEFT JOIN ITEM t ON i.ITEM_ID = t.ID WHERE i.ITEM_ID = ? ORDER BY EXPECTED_DATE DESC";
		List<Intervention> interventions = new ArrayList<Intervention>();
		try { 
			interventions = template.query(query, rowMapper, itemId);
		} catch(DataAccessException e) {
			e.printStackTrace();
		}
		return interventions;
	}

	@Override
	public int create(Intervention t) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		String query = "INSERT INTO CCLIB.INTERVENTION (ID, DESCRIPTION, EXPECTED_DATE, ACTUAL_DATE, UNIT, ROOM, USER_ID, TYPE_ID, TICKET_NUMBER, ITEM_ID) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getDescription());
			ps.setDate(2, new Date(t.getExpectedDate().getTime()));
			ps.setDate(3, new Date(t.getActualDate().getTime()));
			ps.setString(4, t.getUnit());
			ps.setString(5, t.getRoom());
			ps.setInt(6, t.getUser().getId());
			ps.setInt(7, t.getType().getId());
			ps.setString(8, t.getTicketNumber());
			ps.setInt(9,  t.getItem().getId());
			return ps;
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}
	
	public void attach(User notifier, Intervention i) {
		Integer notifierId = (notifier!=null&&notifier.getId()!=0) ? notifier.getId() : null;
		String query = "UPDATE CCLIB.INTERVENTION i SET i.NOTIFIER_ID = ? WHERE i.ID = ?";
		template.update(query, notifierId, i.getId());
	}
	
	public void attach(Licence l, Intervention i) {
		String query = "INSERT INTO CCLIB.INTERVENTION_LICENCE (INTERVENTION_ID, LICENCE_ID) VALUES(?,?)";
		template.update(query, i.getId(), l.getId());
	}
	
	public void attachAll(Collection<Licence> list, Intervention i) {
		for(Licence l:list) {
			this.attach(l, i);
		}
	}

	@Override
	public Intervention save(Intervention t, int id) {
		String query = "UPDATE CCLIB.INTERVENTION t SET t.DESCRIPTION = ?, t.EXPECTED_DATE = ?, t.ACTUAL_DATE = ?, t.UNIT = ?, t.ROOM = ?, t.NOTIFIER_ID = ?, t.TYPE_ID = ?, t.TICKET_NUMBER = ? WHERE t.ID = ?";
		Integer notifierId = (t.getNotifier()!=null&&t.getNotifier().getId()!=0) ? t.getNotifier().getId() : null;
		template.update(query, t.getDescription(), t.getExpectedDate(), t.getActualDate(), t.getUnit(), t.getRoom(), notifierId, t.getType().getId(), t.getTicketNumber(), id);
		return null;
	}

	@Override
	public List<Intervention> saveAll(List<Intervention> t) {
		List<Intervention> tmp = new ArrayList<>();
		for(Intervention l : t) {
			tmp.add(this.save(l, l.getId()));
		}
		return tmp;
	}

	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
	
	RowMapper<Intervention> rowMapper = (rs, rowNum) -> {
		Intervention intervention = new Intervention();
		intervention.setId(rs.getInt(1));
		intervention.setDescription(rs.getString(2));
		intervention.setExpectedDate(rs.getDate(3));
		intervention.setActualDate(rs.getDate(4));
		intervention.setUnit(rs.getString(5));
		intervention.setRoom(rs.getString(6));
		intervention.setTicketNumber(rs.getString(10));
		InterventionType type = new InterventionType();
		type.setId(rs.getInt(12));
		type.setName(rs.getString(13));
		intervention.setType(type);
		User user = new User();
		user.setId(rs.getInt(14));
		user.setUsername(rs.getString(15));
		user.setFirstname(rs.getString(16));
		user.setLastname(rs.getString(17));
		intervention.setUser(user);
		Item item = new Item();
		item.setId(rs.getInt(18));
		item.setReference(rs.getString(19));
		intervention.setItem(item);
		return intervention;
	};
	
	RowMapper<Intervention> withNotifierRowMapper = (rs, rowNum) -> {
		Intervention intervention = new Intervention();
		intervention.setId(rs.getInt(1));
		intervention.setDescription(rs.getString(2));
		intervention.setExpectedDate(rs.getDate(3));
		intervention.setActualDate(rs.getDate(4));
		intervention.setUnit(rs.getString(5));
		intervention.setRoom(rs.getString(6));
		intervention.setTicketNumber(rs.getString(10));
		InterventionType type = new InterventionType();
		type.setId(rs.getInt(12));
		type.setName(rs.getString(13));
		intervention.setType(type);
		User user = new User();
		user.setId(rs.getInt(14));
		user.setUsername(rs.getString(15));
		user.setFirstname(rs.getString(16));
		user.setLastname(rs.getString(17));
		intervention.setUser(user);
		User notifer = new User();
		user.setId(rs.getInt(18));
		user.setUsername(rs.getString(19));
		user.setFirstname(rs.getString(20));
		user.setLastname(rs.getString(21));
		intervention.setNotifier(notifer);
		Item item = new Item();
		item.setId(rs.getInt(22));
		item.setReference(rs.getString(23));
		return intervention;
	};
	
	RowMapper<Intervention> withNotifierAndItemRowMapper = (rs, rowNum) -> {
		Intervention intervention = new Intervention();
		intervention.setId(rs.getInt(1));
		intervention.setDescription(rs.getString(2));
		intervention.setExpectedDate(rs.getDate(3));
		intervention.setActualDate(rs.getDate(4));
		intervention.setUnit(rs.getString(5));
		intervention.setRoom(rs.getString(6));
		intervention.setTicketNumber(rs.getString(10));
		InterventionType type = new InterventionType();
		type.setId(rs.getInt(12));
		type.setName(rs.getString(13));
		intervention.setType(type);
		User user = new User();
		user.setId(rs.getInt(14));
		user.setUsername(rs.getString(15));
		user.setFirstname(rs.getString(16));
		user.setLastname(rs.getString(17));
		intervention.setUser(user);
		User notifer = new User();
		notifer.setId(rs.getInt(18));
		notifer.setUsername(rs.getString(19));
		notifer.setFirstname(rs.getString(20));
		notifer.setLastname(rs.getString(21));
		intervention.setNotifier(notifer);
		Item item = new Item();
		item.setId(rs.getInt(22));
		item.setReference(rs.getString(23));
		item.setSerial_number(rs.getString(24));
		intervention.setItem(item);
//		Report report = new Report();
//		report.setId(rs.getInt(25));
//		report.setDescription(rs.getString(26));
//		report.setIntervention(intervention);
//		intervention.setReport(report);
		return intervention;
	};

}
