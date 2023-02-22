package be.chrverviers.stockmanager.Repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.Order;
import be.chrverviers.stockmanager.Domain.Models.Type;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
public class ItemRepository implements IRepository<Item> {
	
	private JdbcTemplate template;
	
	
	public ItemRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}
	
	RowMapper<Item> rowMapper = (rs, rowNum) -> {
		Item item = new Item();
		item.setId(rs.getInt(1));
		item.setReference(rs.getString(2));
		item.setSerial_number(rs.getString(3));
		item.setDescription(rs.getString(4));
		item.setPurchased_at(rs.getDate(5));
		item.setReceived_at(rs.getDate(6));
		item.setWarranty_expires_at(rs.getDate(7));
		item.setPrice(rs.getDouble(8));
		item.setIs_available(rs.getBoolean(9));
		item.setIs_placed(rs.getBoolean(10));
		item.setUnit(rs.getString(11));
		item.setRoom(rs.getString(12));
		item.setLast_checkup_at(rs.getDate(13));
		item.setCheckup_interval(rs.getInt(14));
		item.setProvider(rs.getString(16));
		Type type = new Type();
		type.setId(rs.getInt(18));
		type.setName(rs.getString(19));
		type.setDescription(rs.getString(20));
		type.setExpectedLifetime(rs.getInt(21));
		type.setTotalQuantity(rs.getInt(22));
		type.setAvailableQuantity(rs.getInt(23));
		item.setType(type);
		return item;
	};

	@Override
	public List<Item> findAll() {
		String query = "SELECT * FROM CCLIB.ITEM i JOIN CCLIB.TYPE t ON i.TYPE_ID = t.ID";
		return template.query(query, rowMapper);
	}

	@Override
	public Optional<Item> findById(int id) {
		String query = "SELECT * FROM CCLIB.ITEM i LEFT JOIN CCLIB.TYPE t ON i.TYPE_ID = t.ID WHERE i.ID = ?";
		Item item = null;
		try { 
			item = template.queryForObject(query, rowMapper, id);
		} catch(DataAccessException e) {
			//This means that the item is null, so we do nothing as it is nullable
		}
		return Optional.ofNullable(item);
	}
	
	public List<Item> findForOrderId(int id) {
		String query = "SELECT * FROM CCLIB.ITEM i JOIN CCLIB.TYPE t ON i.TYPE_ID = t.ID WHERE i.ORDER_ID = ?";
		return template.query(query, rowMapper, id);
	}
	
	public List<Item> findForOrder(Order order) {
		return this.findForOrderId(order.getId());
	}
	
	public void receiveForOrderId(int id) {
		String query = "UPDATE CCLIB.ITEM i SET i.RECEIVED_AT = ? WHERE i.ORDER_ID = ?";	
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now();
		template.update(query, Date.valueOf(now.toLocalDate()), id);
	}
	
	public void receiveForOrder(Order order) {
		this.receiveForOrderId(order.getId());
	}


	@Override
	public int create(Item t) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		Date d = t.getReceived_at()!=null?new Date(t.getReceived_at().getTime()): null;
		String query = "INSERT INTO CCLIB.ITEM (ID, REFERENCE, SERIAL_NUMBER, DESCRIPTION, PURCHASED_AT, RECEIVED_AT, WARRANTY_EXPIRES_AT, PRICE, IS_AVAILABLE, IS_PLACED, UNIT, ROOM, LAST_CHECKUP_AT, CHECKUP_INTERVAL, TYPE_ID, PROVIDER) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getReference());
			ps.setString(2, t.getSerial_number());
			ps.setString(3, t.getDescription());
			ps.setDate(4, new Date(t.getPurchased_at().getTime()));
			ps.setDate(5, d);
			ps.setDate(6, new Date(t.getWarranty_expires_at().getTime()));
			ps.setDouble(7, t.getPrice());
			ps.setBoolean(8, t.getIs_available());
			ps.setBoolean(9, t.getIs_placed());
			ps.setString(10, t.getUnit());
			ps.setString(11, t.getRoom());
			ps.setDate(12, new Date(t.getLast_checkup_at().getTime()));
			ps.setInt(13, t.getCheckup_interval());
			ps.setInt(14, t.getType().getId());
			ps.setString(15, t.getProvider());
			return ps;
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}
	
	public List<Integer> createAll(List<Item> list){
		List<Integer> tmp = new ArrayList();
		for(Item i : list) {
			tmp.add(this.create(i));
		}
		return tmp;
	}

	@Override
	public Item save(Item t, int id) {
		String query = "UPDATE CCLIB.ITEM t SET t.REFERENCE = ?, t.SERIAL_NUMBER = ?, t.DESCRIPTION = ?, t.PURCHASED_AT = ?, t.RECEIVED_AT = ?, t.WARRANTY_EXPIRES_AT =?, t.PRICE = ?, t.IS_AVAILABLE = ?, t.IS_PLACED = ?, t.UNIT = ?, t.ROOM = ?, t.LAST_CHECKUP_AT = ?, t.CHECKUP_INTERVAL = ?, t.TYPE_ID = ?, t.PROVIDER = ? WHERE t.ID = ?";
		//Note that we are forced to convert boolean to char(1) by hand because the OS/400 doesn't like booleans and JDBC doesn't make the conversion alone		
		template.update(query, t.getReference(), t.getSerial_number(), t.getDescription(), t.getPurchased_at(), t.getReceived_at(), t.getWarranty_expires_at(), t.getPrice(), t.getIs_available()?'1':'0', t.getIs_placed()?'1':'0', t.getUnit(), t.getRoom(), t.getLast_checkup_at(), t.getCheckup_interval(), t.getType().getId(), t.getProvider(), id);
		return null;
	}
	
	public Item save(Item t) {
		String query = "UPDATE CCLIB.ITEM t SET t.REFERENCE = ?, t.SERIAL_NUMBER = ?, t.DESCRIPTION = ?, t.PURCHASED_AT = ?, t.RECEIVED_AT = ?, t.WARRANTY_EXPIRES_AT =?, t.PRICE = ?, t.IS_AVAILABLE = ?, t.IS_PLACED = ?, t.UNIT = ?, t.ROOM = ?, t.LAST_CHECKUP_AT = ?, t.CHECKUP_INTERVAL = ?, t.TYPE_ID = ?, t.PROVIDER = ? WHERE t.ID = ?";
		//Note that we are forced to convert boolean to char(1) by hand because the OS/400 doesn't like booleans and JDBC doesn't make the conversion alone		
		template.update(query, t.getReference(), t.getSerial_number(), t.getDescription(), t.getPurchased_at(), t.getReceived_at(), t.getWarranty_expires_at(), t.getPrice(), t.getIs_available()?'1':'0', t.getIs_placed()?'1':'0', t.getUnit(), t.getRoom(), t.getLast_checkup_at(), t.getCheckup_interval(), t.getType().getId(), t.getProvider(), t.getId());
		return null;
	}


	@Override
	public List<Item> saveAll(List<Item> list){
		List<Item> tmp = new ArrayList<>();
		for(Item l : list) {
			tmp.add(this.save(l, l.getId()));
		}
		return tmp;
	}
	
	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}	
}
