package be.chrverviers.stockmanager.Repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.LicenceType;
import be.chrverviers.stockmanager.Domain.Models.Room;
import be.chrverviers.stockmanager.Domain.Models.Unit;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
public class LicenceRepository implements IRepository<Licence>{

	private JdbcTemplate template;
	
	public LicenceRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}
	
	
	RowMapper<Licence> rowMapper = (rs, rowNum) -> {
		Licence licence = new Licence();
		licence.setId(rs.getInt(1));
		licence.setDescription(rs.getString(2));
		licence.setValue(rs.getString(3));
		licence.setReference(rs.getString(7));
		licence.setPurchasedAt(rs.getDate(8));
		Item item = rs.getInt(6)==0?null:new Item(rs.getInt(6));
		licence.setItem(item);
		LicenceType type = new LicenceType();
		type.setId(rs.getInt(9));
		type.setName(rs.getString(10));
		type.setAlias(rs.getString(11));
		licence.setType(type);
		return licence;
	};
	
	RowMapper<Licence> withUserRowMapper = (rs, rowNum) -> {
		Licence licence = new Licence();
		licence.setId(rs.getInt(1));
		licence.setDescription(rs.getString(2));
		licence.setValue(rs.getString(3));
		licence.setReference(rs.getString(7));
		licence.setPurchasedAt(rs.getDate(8));
		Item item = rs.getInt(6)==0?null:new Item(rs.getInt(6));
		licence.setItem(item);
		LicenceType type = new LicenceType();
		type.setId(rs.getInt(9));
		type.setName(rs.getString(10));
		type.setAlias(rs.getString(11));
		licence.setType(type);
		User user = mapUser(rs,rowNum);
		licence.setUser(user);
		return licence;
	};
	
	RowMapper<Licence> withUserAndItemRowMapper = (rs, rowNum) -> {
		Licence licence = new Licence();
		licence.setId(rs.getInt(1));
		licence.setDescription(rs.getString(2));
		licence.setValue(rs.getString(3));
		licence.setReference(rs.getString(7));
		licence.setPurchasedAt(rs.getDate(8));
		LicenceType type = new LicenceType();
		type.setId(rs.getInt(9));
		type.setName(rs.getString(10));
		type.setAlias(rs.getString(11));
		licence.setType(type);
		User user = mapUser(rs,rowNum);
		licence.setUser(user);
		Item item = mapItem(rs, rowNum);
		licence.setItem(item);
		Room room = mapRoom(rs, rowNum);
		item.setRoom(room);
		return licence;
	};
	
	@Override
	public List<Licence> findAll() {
		String query = "SELECT * FROM CCLIB.LICENCE l JOIN CCLIB.LICENCE_TYPE lt ON l.TYPE_ID=lt.ID LEFT JOIN CCLIB.USER u ON l.USER_ID = u.ID";
		return template.query(query, withUserRowMapper);
	}

	@Override
	public Optional<Licence> findById(int id) {
		String query = "SELECT * FROM CCLIB.LICENCE l JOIN CCLIB.LICENCE_TYPE t ON l.TYPE_ID = t.ID LEFT JOIN CCLIB.USER u on l.USER_ID = u.ID LEFT JOIN CCLIB.ITEM i on l.ITEM_ID = i.ID LEFT JOIN ROOM r ON i.ROOM_ID = r.ID LEFT JOIN UNIT un ON r.UNIT_ID = un.ID WHERE l.ID = ?";
		Licence licence = new Licence();
		try { 
			licence = template.queryForObject(query, withUserAndItemRowMapper, id);
		} catch(DataAccessException e) {
			e.getMessage();
			//This means that there is no data, but we don't do anything because in this case we return an Optional
		}
		return Optional.ofNullable(licence);
	}
	
	public List<Licence> findForItem(Item item){
		String query = "SELECT * FROM CCLIB.LICENCE l JOIN CCLIB.LICENCE_TYPE t ON l.TYPE_ID = t.ID LEFT JOIN CCLIB.USER u on l.USER_ID = u.ID LEFT JOIN CCLIB.ITEM i on l.ITEM_ID = i.ID LEFT JOIN ROOM r ON i.ROOM_ID = r.ID LEFT JOIN UNIT un ON r.UNIT_ID = un.ID WHERE l.ITEM_ID = ?";
		List<Licence> licences = new ArrayList<Licence>();
		try { 
			licences = template.query(query, withUserAndItemRowMapper, item.getId());
		} catch(DataAccessException e) {
			e.printStackTrace();
		}
		return licences;
	}
	
	public List<Licence> findForIntervention(Intervention intervention){
		String query = "SELECT * FROM CCLIB.LICENCE l JOIN CCLIB.LICENCE_TYPE t ON l.TYPE_ID = t.ID LEFT JOIN CCLIB.USER u on l.USER_ID = u.ID LEFT JOIN INTERVENTION_LICENCE il ON il.LICENCE_ID = l.ID LEFT JOIN CCLIB.INTERVENTION i on il.INTERVENTION_ID = i.ID WHERE i.ID = ?";
		List<Licence> licences = new ArrayList<Licence>();
		try { 
			licences = template.query(query, withUserAndItemRowMapper, intervention.getId());
		} catch(DataAccessException e) {
			e.printStackTrace();
		}
		return licences;
	}

	@Override
	public int create(Licence t) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

		String query = "INSERT INTO CCLIB.LICENCE (ID, DESCRIPTION, VALUE, TYPE_ID, REFERENCE, PURCHASED_AT) VALUES (DEFAULT, ?, ?, ?, ?, ?)";
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getDescription());
			ps.setString(2, t.getValue());
			ps.setInt(3, t.getType().getId());
			ps.setString(4, t.getReference());
			ps.setDate(5, new Date(t.getPurchasedAt().getTime()));
			return ps;
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}
	
	public void attach(Licence l, Item i) {
		Integer userId = l.getUser()!=null ? l.getUser().getId() : null;
		String query = "UPDATE CCLIB.LICENCE l SET l.ITEM_ID = ?, l.USER_ID = ? WHERE l.ID = ?";
		template.update(query, i.getId(), userId, l.getId());
	}
	
	public void detach(Licence l) {
		Integer userId = l.getUser()!=null ? l.getUser().getId() : null;
		String query = "UPDATE CCLIB.LICENCE l SET l.ITEM_ID = ?, l.USER_ID = ? WHERE l.ID = ?";
		template.update(query, null, userId, l.getId());
	}
	
	public void attachAll(Collection<Licence> list, Item i) {
		for(Licence l:list) {
			this.attach(l, i);
		}
	}

	public void detachAll(Collection<Licence> list) {
		for(Licence l:list) {
			this.detach(l);
		}
	}
	
	@Override
	public Licence save(Licence t, int id) {
		Integer userId = t.getUser()!=null ? t.getUser().getId() : null;
		Integer itemId = t.getItem()!=null ? t.getItem().getId() : null;
		
		String query = "UPDATE CCLIB.LICENCE l SET l.DESCRIPTION = ?, l.VALUE = ?, l.USER_ID = ?, l.TYPE_ID = ?, l.ITEM_ID = ? WHERE l.ID = ?";
		
		template.update(query, t.getDescription(), t.getValue(), userId, t.getType().getId(), itemId, id);
		return null;
	}
	
	@Override
	public List<Licence> saveAll(List<Licence> list) {
		List<Licence> tmp = new ArrayList<>();
		for(Licence l : list) {
			tmp.add(this.save(l, l.getId()));
		}
		return tmp;
	}

	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
	
	public int getCountForCurrentMonth() {
		String query = "SELECT COUNT(*) FROM CCLIB.LICENCE WHERE MONTH(PURCHASED_AT) = MONTH(CURRENT_DATE) AND YEAR(PURCHASED_AT) = YEAR(CURRENT_DATE)";
	    return template.queryForObject(query, intMapper);	
	}
	
	public int getCountForCurrentMonthForType(LicenceType type) {
		return this.getCountForCurrentMonthForType(type.getId());
	}
	
	public int getCountForCurrentMonthForType(int typeId) {
		String query = "SELECT COUNT(*) FROM CCLIB.LICENCE WHERE MONTH(PURCHASED_AT) = MONTH(CURRENT_DATE) AND YEAR(PURCHASED_AT) = YEAR(CURRENT_DATE) AND TYPE_ID = ?";
	    
		return template.queryForObject(query, intMapper, typeId);	
	}
	
	private User mapUser(ResultSet rs, int rowNum) {
		User user = new User();
		try {
			user.setId(rs.getInt(12));
			user.setUsername(rs.getString(13));
			user.setFirstname(rs.getString(14));
			user.setLastname(rs.getString(15));
			user.setIsActive(rs.getBoolean(16));
			user.setEmail(rs.getString(17));
		} catch (SQLException e) {
			return null;
		}
		return user.getId()==0 ? null : user;
	}
	
	private Item mapItem(ResultSet rs, int rowNum) {
		Item item = new Item();
		try {
			item.setId(rs.getInt(18));
			item.setReference(rs.getString(19));
			item.setSerialNumber(rs.getString(20));
			item.setDescription(rs.getString(21));
			item.setPurchasedAt(rs.getDate(22));
			item.setReceivedAt(rs.getDate(23));
			item.setWarrantyExpiresAt(rs.getDate(24));
			item.setPrice(rs.getDouble(25));
			item.setIsAvailable(rs.getBoolean(26));
			item.setIsPlaced(rs.getBoolean(27));
			item.setLastCheckupAt(rs.getDate(29));
			item.setCheckupInterval(rs.getInt(30));
			item.setProvider(rs.getString(31));
		} catch (SQLException e) {
			return new Item();
		}
		return item.getId()==0 ? new Item() : item;
	}
	
	private Room mapRoom(ResultSet rs, int rowNum) {
		Room room = new Room();
		try {
			room.setId(rs.getInt(35));
			room.setName(rs.getString(36));
			Unit unit = new Unit();
			unit.setId(rs.getInt(38));
			unit.setName(rs.getString(39));
			room.setUnit(unit);		
		} catch (SQLException e) {
			return null;
		}
		return room;


	}
	
	RowMapper<Integer> intMapper = (rs, rowNum) -> {
		return rs.getInt(1);
	};

}
