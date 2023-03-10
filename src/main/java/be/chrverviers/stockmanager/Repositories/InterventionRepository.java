package be.chrverviers.stockmanager.Repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;import org.aspectj.apache.bcel.generic.Type;
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
import be.chrverviers.stockmanager.Domain.Models.Room;
import be.chrverviers.stockmanager.Domain.Models.Unit;
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
		String query = "SELECT i.*, it.*, u.*, t.id, t.reference, ro.*, un.* FROM CCLIB.INTERVENTION i LEFT JOIN CCLIB.INTERVENTION_TYPE it ON i.TYPE_ID=it.ID LEFT JOIN CCLIB.USER u ON i.USER_ID = u.ID LEFT JOIN CCLIB.ITEM t ON i.ITEM_ID = t.ID JOIN CCLIB.ROOM ro ON i.ROOM_ID = ro.ID JOIN CCLIB.UNIT un ON ro.UNIT_ID = un.ID ORDER BY EXPECTED_DATE DESC";
		return template.query(query, rowMapper);
	}
	
	public List<Intervention> findAllPending() {
		String query = "SELECT i.*, it.*, u.*, t.id, t.reference, ro.*, un.* FROM CCLIB.INTERVENTION i LEFT JOIN CCLIB.INTERVENTION_TYPE it ON i.TYPE_ID=it.ID LEFT JOIN CCLIB.USER u ON i.USER_ID = u.ID LEFT JOIN CCLIB.ITEM t ON i.ITEM_ID = t.ID JOIN CCLIB.ROOM ro ON i.ROOM_ID = ro.ID JOIN CCLIB.UNIT un ON ro.UNIT_ID = un.ID WHERE i.ACTUAL_DATE IS NULL";
		return template.query(query, rowMapper);
	}

	@Override
	public Optional<Intervention> findById(int id) {
		String query = "SELECT i.*, it.*, u.*, n.*, t.id, t.name, t.reference, ro.*, un.* FROM CCLIB.INTERVENTION i LEFT JOIN CCLIB.INTERVENTION_TYPE it ON i.TYPE_ID = it.ID LEFT JOIN CCLIB.USER u ON i.USER_ID = u.ID LEFT JOIN CCLIB.USER n ON i.NOTIFIER_ID = n.ID LEFT JOIN CCLIB.ITEM t ON i.ITEM_ID = t.ID LEFT JOIN CCLIB.REPORT r ON r.INTERVENTION_ID = i.ID LEFT JOIN CCLIB.ROOM ro ON i.ROOM_ID = ro.ID LEFT JOIN CCLIB.UNIT un ON ro.UNIT_ID = un.ID WHERE i.ID = ?";
		Intervention intervention = new Intervention();
		try { 
			intervention = template.queryForObject(query, withNotifierAndItemRowMapper, id);
		} catch(DataAccessException e) {
			//This means that there is no data, but we don't do anything because in this case we return an Optional
		}
		return Optional.ofNullable(intervention);
	}
	
	public List<Intervention> findForItem(Item item){
		String query = "SELECT i.*, it.*, u.*, t.id, t.reference, ro.*, un.* FROM CCLIB.INTERVENTION i LEFT JOIN CCLIB.INTERVENTION_TYPE it ON i.TYPE_ID=it.ID LEFT JOIN CCLIB.USER u ON i.USER_ID = u.ID LEFT JOIN ITEM t ON i.ITEM_ID = t.ID LEFT JOIN CCLIB.ROOM ro ON i.ROOM_ID = ro.ID LEFT JOIN CCLIB.UNIT un ON ro.UNIT_ID = un.ID WHERE i.ITEM_ID = ? ORDER BY EXPECTED_DATE DESC";
		List<Intervention> interventions = new ArrayList<Intervention>();
		try { 
			interventions = template.query(query, rowMapper, item.getId());
		} catch(DataAccessException e) {
			e.printStackTrace();
		}
		return interventions;
	}
	
	public List<Intervention> findForItemId(int itemId){
		String query = "SELECT i.*, it.*, u.*, t.id, t.reference, ro.*, un.* FROM CCLIB.INTERVENTION i LEFT JOIN CCLIB.INTERVENTION_TYPE it ON i.TYPE_ID=it.ID LEFT JOIN CCLIB.USER u ON i.USER_ID = u.ID LEFT JOIN ITEM t ON i.ITEM_ID = t.ID LEFT JOIN CCLIB.ROOM ro ON i.ROOM_ID = ro.ID LEFT JOIN CCLIB.UNIT un ON ro.UNIT_ID = un.ID WHERE i.ITEM_ID = ? ORDER BY EXPECTED_DATE DESC";
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
		String query = "INSERT INTO CCLIB.INTERVENTION (ID, DESCRIPTION, EXPECTED_DATE, ACTUAL_DATE, ROOM_ID, USER_ID, TYPE_ID, TICKET_NUMBER, ITEM_ID) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getDescription());
			ps.setDate(2, new Date(t.getExpectedDate().getTime()));
			ps.setDate(3, t.getActualDate()!=null ? new Date(t.getActualDate().getTime()) : null);
			if(t.getRoom()!=null)
				ps.setInt(4, t.getRoom().getId());
			else
				ps.setNull(4, Types.INTEGER);
			ps.setInt(5, t.getUser().getId());
			ps.setInt(6, t.getType().getId());
			ps.setString(7, t.getTicketNumber());
			ps.setInt(8,  t.getItem().getId());
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
	
	public void detachAll(Collection<Licence> list, Intervention i) {
		for(Licence l:list) {
			this.detach(l, i);
		}
	}
	
	public void detach(Licence l, Intervention i) {
		String query = "DELETE FROM CCLIB.INTERVENTION_LICENCE WHERE LICENCE_ID = ? AND INTERVENTION_ID = ?";
		template.update(query, l.getId(), i.getId());
	}

	@Override
	public Intervention save(Intervention t, int id) {
		String query = "UPDATE CCLIB.INTERVENTION t SET t.DESCRIPTION = ?, t.EXPECTED_DATE = ?, t.ACTUAL_DATE = ?, t.ROOM_ID = ?, t.NOTIFIER_ID = ?, t.TYPE_ID = ?, t.TICKET_NUMBER = ? WHERE t.ID = ?";
		Integer notifierId = (t.getNotifier()!=null&&t.getNotifier().getId()!=0) ? t.getNotifier().getId() : null;
		template.update(query, t.getDescription(), t.getExpectedDate(), t.getActualDate(), t.getRoom().getId(), notifierId, t.getType().getId(), t.getTicketNumber(), id);
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
		intervention.setTicketNumber(rs.getString(8));
		InterventionType type = new InterventionType();
		type.setId(rs.getInt(11));
		type.setName(rs.getString(12));
		type.setShouldSendMailHelpline(rs.getBoolean(13));
		type.setShouldSendMailUser(rs.getBoolean(14));
		intervention.setType(type);
		User user = new User();
		user.setId(rs.getInt(15));
		user.setUsername(rs.getString(16));
		user.setFirstname(rs.getString(17));
		user.setLastname(rs.getString(18));
		user.setIsActive(rs.getBoolean(19));
		user.setEmail(rs.getString(20));
		intervention.setUser(user);
		Item item = new Item();
		item.setId(rs.getInt(21));
		item.setReference(rs.getString(22));
		intervention.setItem(item);
		Room room = new Room();
		room.setId(rs.getInt(23));
		room.setName(rs.getString(24));
		intervention.setRoom(room);
		Unit unit= new Unit();
		unit.setId(rs.getInt(26));
		unit.setName(rs.getString(27));
		room.setUnit(unit);
		return intervention;
	};
	
	RowMapper<Intervention> withNotifierRowMapper = (rs, rowNum) -> {
		Intervention intervention = new Intervention();
		intervention.setId(rs.getInt(1));
		intervention.setDescription(rs.getString(2));
		intervention.setExpectedDate(rs.getDate(3));
		intervention.setActualDate(rs.getDate(4));
		intervention.setTicketNumber(rs.getString(8));
		InterventionType type = new InterventionType();
		type.setId(rs.getInt(11));
		type.setName(rs.getString(12));
		type.setShouldSendMailHelpline(rs.getBoolean(13));
		type.setShouldSendMailUser(rs.getBoolean(14));
		intervention.setType(type);
		User user = new User();
		user.setId(rs.getInt(15));
		user.setUsername(rs.getString(16));
		user.setFirstname(rs.getString(17));
		user.setLastname(rs.getString(18));
		user.setIsActive(rs.getBoolean(19));
		user.setEmail(rs.getString(20));
		intervention.setUser(user);
		User notifier = new User();
		notifier.setId(rs.getInt(21));
		notifier.setUsername(rs.getString(22));
		notifier.setFirstname(rs.getString(23));
		notifier.setLastname(rs.getString(24));
		notifier.setIsActive(rs.getBoolean(25));
		notifier.setEmail(rs.getString(26));
		intervention.setNotifier(notifier);
		Item item = new Item();
		item.setId(rs.getInt(27));
		item.setName(rs.getString(28));
		item.setReference(rs.getString(29));
		Room room = new Room();
		room.setId(rs.getInt(30));
		room.setName(rs.getString(31));
		intervention.setRoom(room);
		Unit unit= new Unit();
		unit.setId(rs.getInt(33));
		unit.setName(rs.getString(34));
		room.setUnit(unit);
		return intervention;
	};
	
	RowMapper<Intervention> withNotifierAndItemRowMapper = (rs, rowNum) -> {
		Intervention intervention = new Intervention();
		intervention.setId(rs.getInt(1));
		intervention.setDescription(rs.getString(2));
		intervention.setExpectedDate(rs.getDate(3));
		intervention.setActualDate(rs.getDate(4));
		intervention.setTicketNumber(rs.getString(8));
		InterventionType type = new InterventionType();
		type.setId(rs.getInt(11));
		type.setName(rs.getString(12));
		type.setShouldSendMailHelpline(rs.getBoolean(13));
		type.setShouldSendMailUser(rs.getBoolean(14));
		intervention.setType(type);
		User user = new User();
		user.setId(rs.getInt(15));
		user.setUsername(rs.getString(16));
		user.setFirstname(rs.getString(17));
		user.setLastname(rs.getString(18));
		user.setIsActive(rs.getBoolean(19));
		user.setEmail(rs.getString(20));
		intervention.setUser(user);
		User notifier = new User();
		notifier.setId(rs.getInt(21));
		notifier.setUsername(rs.getString(22));
		notifier.setFirstname(rs.getString(23));
		notifier.setLastname(rs.getString(24));
		notifier.setIsActive(rs.getBoolean(25));
		notifier.setEmail(rs.getString(26));
		intervention.setNotifier(notifier);
		Item item = new Item();
		item.setId(rs.getInt(27));
		item.setName(rs.getString(28));
		item.setReference(rs.getString(29));
		intervention.setItem(item);
		Room room = new Room();
		room.setId(rs.getInt(30));
		room.setName(rs.getString(31));
		intervention.setRoom(room);
		Unit unit= new Unit();
		unit.setId(rs.getInt(33));
		unit.setName(rs.getString(34));
		room.setUnit(unit);
		return intervention;
	};

}
