package be.chrverviers.stockmanager.Repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.LicenceType;
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
		Item item = rs.getInt(6)==0?null:new Item(rs.getInt(6));
		licence.setItem(item);
		LicenceType type = new LicenceType();
		type.setId(rs.getInt(7));
		type.setName(rs.getString(8));
		licence.setType(type);
		return licence;
	};
	
	RowMapper<Licence> withUserRowMapper = (rs, rowNum) -> {
		Licence licence = new Licence();
		licence.setId(rs.getInt(1));
		licence.setDescription(rs.getString(2));
		licence.setValue(rs.getString(3));
		Item item = rs.getInt(6)==0?null:new Item(rs.getInt(6));
		licence.setItem(item);
		LicenceType type = new LicenceType();
		type.setId(rs.getInt(7));
		type.setName(rs.getString(8));
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
		LicenceType type = new LicenceType();
		type.setId(rs.getInt(7));
		type.setName(rs.getString(8));
		licence.setType(type);
		User user = mapUser(rs,rowNum);
		licence.setUser(user);
		Item item = mapItem(rs, rowNum);
		licence.setItem(item);
		return licence;
	};
	
	@Override
	public List<Licence> findAll() {
		String query = "SELECT * FROM CCLIB.LICENCE l JOIN CCLIB.LICENCE_TYPE lt ON l.TYPE_ID=lt.ID";
		return template.query(query, rowMapper);
	}

	@Override
	public Optional<Licence> findById(int id) {
		String query = "SELECT * FROM CCLIB.LICENCE l JOIN CCLIB.LICENCE_TYPE t ON l.TYPE_ID = t.ID LEFT JOIN CCLIB.USER u on l.USER_ID = u.ID LEFT JOIN CCLIB.ITEM i on l.ITEM_ID = i.ID WHERE l.ID = ?";
		Licence licence = new Licence();
		try { 
			licence = template.queryForObject(query, withUserAndItemRowMapper, id);
		} catch(DataAccessException e) {
			e.printStackTrace();
		}
		return Optional.ofNullable(licence);
	}
	
	public List<Licence> findForItem(Item item){
		String query = "SELECT * FROM CCLIB.LICENCE l JOIN CCLIB.LICENCE_TYPE t ON l.TYPE_ID = t.ID LEFT JOIN CCLIB.USER u on l.USER_ID = u.ID LEFT JOIN CCLIB.ITEM i on l.ITEM_ID = i.ID WHERE l.ITEM_ID = ?";
		List<Licence> licences = new ArrayList<Licence>();
		try { 
			licences = template.query(query, withUserAndItemRowMapper, item.getId());
		} catch(DataAccessException e) {
			e.printStackTrace();
		}
		return licences;
	}

	@Override
	public int create(Licence t) {
		Integer userId = t.getUser()!=null ? t.getUser().getId() : null;
		Integer itemId = t.getItem()!=null ? t.getItem().getId() : null;
		
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

		String query = "INSERT INTO CCLIB.LICENCE (ID, DESCRIPTION, VALUE, TYPE_ID) VALUES (DEFAULT, ?, ?, ?)";
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getDescription());
			ps.setString(2, t.getValue());
			ps.setInt(3, t.getType().getId());
			return ps;
		}, keyHolder);
		return keyHolder.getKey().intValue();
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
	
	private User mapUser(ResultSet rs, int rowNum) {
		User user = new User();
		try {
			user.setId(rs.getInt(9));
			user.setUsername(rs.getString(10));
			user.setFirstname(rs.getString(11));
			user.setLastname(rs.getString(12));
		} catch (SQLException e) {
			return null;
		}
		return user.getId()==0 ? null : user;
	}
	
	private Item mapItem(ResultSet rs, int rowNum) {
		Item item = new Item();
		try {
			item.setId(rs.getInt(13));
			item.setReference(rs.getString(14));
			item.setSerial_number(rs.getString(15));
			item.setDescription(rs.getString(16));
			item.setPurchased_at(rs.getDate(17));
			item.setReceived_at(rs.getDate(18));
			item.setWarranty_expires_at(rs.getDate(19));
			item.setPrice(rs.getDouble(20));
			item.setIs_available(rs.getBoolean(21));
			item.setIs_placed(rs.getBoolean(22));
			item.setUnit(rs.getString(23));
			item.setRoom(rs.getString(24));
			item.setLast_checkup_at(rs.getDate(25));
			item.setCheckup_interval(rs.getInt(26));
			item.setProvider(rs.getString(27));
		} catch (SQLException e) {
			return null;
		}
		return item.getId()==0 ? null : item;
	}
}
