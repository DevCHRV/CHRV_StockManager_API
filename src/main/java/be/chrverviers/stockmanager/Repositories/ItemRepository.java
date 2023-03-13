package be.chrverviers.stockmanager.Repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Order;
import be.chrverviers.stockmanager.Domain.Models.Room;
import be.chrverviers.stockmanager.Domain.Models.Type;
import be.chrverviers.stockmanager.Domain.Models.Unit;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
@Transactional
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
		item.setSerialNumber(rs.getString(3));
		item.setDescription(rs.getString(4));
		item.setPurchasedAt(rs.getDate(5));
		item.setReceivedAt(rs.getDate(6));
		item.setWarrantyExpiresAt(rs.getDate(7));
		item.setPrice(rs.getDouble(8));
		item.setIsAvailable(rs.getBoolean(9));
		item.setIsPlaced(rs.getBoolean(10));
		item.setLastCheckupAt(rs.getDate(11));
		item.setCheckupInterval(rs.getInt(12));
		item.setProvider(rs.getString(14));
		item.setOrder(new Order(rs.getInt(15)));
		item.setName(rs.getString(16));
		Type type = new Type();
		type.setId(rs.getInt(18));
		type.setName(rs.getString(19));
		type.setDescription(rs.getString(20));
		type.setExpectedLifetime(rs.getInt(21));
		type.setTotalQuantity(rs.getInt(22));
		type.setAvailableQuantity(rs.getInt(23));
		type.setAlias(rs.getString(24));
		item.setType(type);
		Room room = new Room();
		room.setId(rs.getInt(25));
		room.setName(rs.getString(26));
		item.setRoom(room);
		Unit unit = new Unit();
		unit.setId(rs.getInt(28));
		unit.setName(rs.getString(29));
		room.setUnit(unit);
		return item;
	};
	
	RowMapper<Item> orderItemRowMapper = (rs, rowNum) -> {
		Item item = new Item();
		item.setId(rs.getInt(1));
		item.setReference(rs.getString(2));
		item.setSerialNumber(rs.getString(3));
		item.setDescription(rs.getString(4));
		item.setPurchasedAt(rs.getDate(5));
		item.setWarrantyExpiresAt(rs.getDate(6));
		item.setPrice(rs.getDouble(7));
		item.setLastCheckupAt(rs.getDate(8));
		item.setCheckupInterval(rs.getInt(9));
		item.setProvider(rs.getString(11));
		item.setOrder(new Order(rs.getInt(12)));
		item.setReceivedAt(rs.getDate(13));
		item.setName(rs.getString(14));
		Type type = new Type();
		type.setId(rs.getInt(15));
		type.setName(rs.getString(16));
		type.setDescription(rs.getString(17));
		type.setExpectedLifetime(rs.getInt(18));
		type.setTotalQuantity(rs.getInt(19));
		type.setAvailableQuantity(rs.getInt(20));
		type.setAlias(rs.getString(21));
		item.setType(type);
		return item;
	};
	
	RowMapper<Integer> intMapper = (rs, rowNum) -> {
		return rs.getInt(1);
	};


	@Override
	public List<Item> findAll() {
		String query = "SELECT * FROM CCLIB.ITEM i JOIN CCLIB.TYPE t ON i.TYPE_ID = t.ID LEFT JOIN CCLIB.ROOM r ON i.ROOM_ID = r.ID LEFT JOIN CCLIB.UNIT u ON r.UNIT_ID = u.ID";
		return template.query(query, rowMapper);
	}

	@Override
	public Optional<Item> findById(int id) {
		String query = "SELECT * FROM CCLIB.ITEM i LEFT JOIN CCLIB.TYPE t ON i.TYPE_ID = t.ID LEFT JOIN CCLIB.ROOM r ON i.ROOM_ID = r.ID LEFT JOIN CCLIB.UNIT u ON r.UNIT_ID = u.ID WHERE i.ID = ?";
		Item item = null;
		try { 
			item = template.queryForObject(query, rowMapper, id);
		} catch(DataAccessException e) {
			//This means that the item is null, so we do nothing as it is nullable
		}
		return Optional.ofNullable(item);
	}
	
	public List<Item> findForPendingOrderId(int id) {
		String query = "SELECT * FROM CCLIB.ORDER_ITEM i JOIN CCLIB.TYPE t ON i.TYPE_ID = t.ID WHERE i.ORDER_ID = ?";
		return template.query(query, orderItemRowMapper, id);
	}
	
	public List<Item> findForPendingOrder(Order order) {
		return this.findForPendingOrderId(order.getId());
	}
	
	public List<Item> findForOrderId(int id) {
		String query = "SELECT * FROM CCLIB.ITEM i JOIN CCLIB.TYPE t ON i.TYPE_ID = t.ID LEFT JOIN CCLIB.ROOM r ON i.ROOM_ID = r.ID LEFT JOIN CCLIB.UNIT u ON r.UNIT_ID = u.ID WHERE i.ORDER_ID = ?";
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

	@Override
	public int create(Item t) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		Date d = t.getReceivedAt()!=null?new Date(t.getReceivedAt().getTime()): null;
		Integer orderId = t.getOrder() != null ? t.getOrder().getId() : null;
		Integer roomId = t.getRoom() != null ? t.getRoom().getId() : null;

		String query = "INSERT INTO CCLIB.ITEM (ID, NAME, REFERENCE, SERIAL_NUMBER, DESCRIPTION, PURCHASED_AT, RECEIVED_AT, WARRANTY_EXPIRES_AT, PRICE, IS_AVAILABLE, IS_PLACED, ROOM_ID, LAST_CHECKUP_AT, CHECKUP_INTERVAL, TYPE_ID, PROVIDER, ORDER_ID) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getName());
			ps.setString(2, t.getReference());
			ps.setString(3, t.getSerialNumber());
			ps.setString(4, t.getDescription());
			ps.setDate(5, new Date(t.getPurchasedAt().getTime()));
			ps.setDate(6, d);
			ps.setDate(7, new Date(t.getWarrantyExpiresAt().getTime()));
			ps.setDouble(8, t.getPrice());
			ps.setBoolean(9, t.getIsAvailable());
			ps.setBoolean(10, t.getIsPlaced());
			if(roomId != null)
				ps.setInt(11, t.getRoom().getId());
			else 
				ps.setNull(11, Types.INTEGER);
			ps.setDate(12, new Date(t.getWarrantyExpiresAt().getTime()));
			ps.setInt(13, t.getCheckupInterval());
			ps.setInt(14, t.getType().getId());
			ps.setString(15, t.getProvider());
			if(orderId != null)
				ps.setInt(16, orderId);
			else 
				ps.setNull(16, Types.INTEGER);
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
	
	public List<Integer> createAll(List<Item> list, Order o){
		List<Integer> tmp = new ArrayList();
		for(Item i : list) {
			i.setOrder(o);
			tmp.add(this.create(i));
		}
		return tmp;
	}
	
	public int create(Item t, Order o) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		String query = "INSERT INTO CCLIB.ORDER_ITEM (ID, REFERENCE, DESCRIPTION, PROVIDER, PURCHASED_AT, WARRANTY_EXPIRES_AT, LAST_CHECKUP_AT, PRICE, CHECKUP_INTERVAL, TYPE_ID, ORDER_ID) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getReference());
			ps.setString(2, t.getDescription());
			ps.setString(3, t.getProvider());
			ps.setDate(4, new Date(t.getPurchasedAt().getTime()));
			ps.setDate(5, new Date(t.getWarrantyExpiresAt().getTime()));
			ps.setDate(6,  new Date(t.getPurchasedAt().getTime()));
			ps.setDouble(7, t.getPrice());
			ps.setInt(8, t.getCheckupInterval());
			ps.setInt(9, t.getType().getId());
			ps.setInt(10, o.getId());
			return ps;
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}

	@Override
	public Item save(Item t, int id) {
		String query = "UPDATE CCLIB.ITEM t SET t.REFERENCE = ?, t.NAME = ?, t.SERIAL_NUMBER = ?, t.DESCRIPTION = ?, t.PURCHASED_AT = ?, t.RECEIVED_AT = ?, t.WARRANTY_EXPIRES_AT =?, t.PRICE = ?, t.IS_AVAILABLE = ?, t.IS_PLACED = ?, t.ROOM_ID = ?, t.LAST_CHECKUP_AT = ?, t.CHECKUP_INTERVAL = ?, t.TYPE_ID = ?, t.PROVIDER = ? WHERE t.ID = ?";
		//Note that we are forced to convert boolean to char(1) by hand because the OS/400 doesn't like booleans and JDBC doesn't make the conversion alone		
		template.update(query, t.getReference(), t.getName(), t.getSerialNumber(), t.getDescription(), t.getPurchasedAt(), t.getReceivedAt(), t.getWarrantyExpiresAt(), t.getPrice(), t.getIsAvailable()?'1':'0', t.getIsPlaced()?'1':'0', t.getRoom()!=null?t.getRoom().getId():null, t.getLastCheckupAt(), t.getCheckupInterval(), t.getType().getId(), t.getProvider(), id);
		return null;
	}
		
	public Item save(Item t) {
		String query = "UPDATE CCLIB.ITEM t SET t.REFERENCE = ?, t.NAME = ?, t.SERIAL_NUMBER = ?, t.DESCRIPTION = ?, t.PURCHASED_AT = ?, t.RECEIVED_AT = ?, t.WARRANTY_EXPIRES_AT =?, t.PRICE = ?, t.IS_AVAILABLE = ?, t.IS_PLACED = ?, t.ROOM_ID = ?, t.LAST_CHECKUP_AT = ?, t.CHECKUP_INTERVAL = ?, t.TYPE_ID = ?, t.PROVIDER = ? WHERE t.ID = ?";
		//Note that we are forced to convert boolean to char(1) by hand because the OS/400 doesn't like booleans and JDBC doesn't make the conversion alone		
		template.update(query, t.getReference(), t.getName(), t.getSerialNumber(), t.getDescription(), t.getPurchasedAt(), t.getReceivedAt(), t.getWarrantyExpiresAt(), t.getPrice(), t.getIsAvailable()?'1':'0', t.getIsPlaced()?'1':'0', t.getRoom()!=null?t.getRoom().getId():null, t.getLastCheckupAt(), t.getCheckupInterval(), t.getType().getId(), t.getProvider(), t.getId());
		return null;
	}

	/**
	 * This method should only used to update items of orders that are not yet received
	 * @param t
	 * @param o
	 * @return
	 */
	public Item save(Item t, Order o) {
		if(o.getIsReceived())
			throw new IllegalStateException("Cette méthode ne peut pas être utilisée si la commande est déjà reçue");
		String query = "UPDATE CCLIB.ORDER_ITEM t SET t.REFERENCE = ?, t.NAME = ?, t.SERIAL_NUMBER = ?, t.DESCRIPTION = ?, t.PURCHASED_AT = ?, t.RECEIVED_AT = ?, t.WARRANTY_EXPIRES_AT =?, t.PRICE = ?, t.LAST_CHECKUP_AT = ?, t.CHECKUP_INTERVAL = ?, t.TYPE_ID = ?, t.PROVIDER = ? WHERE t.ID = ?";
		//Note that we are forced to convert boolean to char(1) by hand because the OS/400 doesn't like booleans and JDBC doesn't make the conversion alone		
		template.update(query, t.getReference(), t.getName(), t.getSerialNumber(), t.getDescription(), t.getPurchasedAt(), t.getReceivedAt(), t.getWarrantyExpiresAt(), t.getPrice(), t.getLastCheckupAt(), t.getCheckupInterval(), t.getType().getId(), t.getProvider(), t.getId());
		return null;
	}
	
	/**
	 * This method should only used to update items of orders that are not yet received
	 * @param t
	 * @param o
	 * @return
	 */
	public List<Item> saveAll(List<Item> list, Order o){
		if(o.getIsReceived())
			throw new IllegalStateException("Cette méthode ne peut pas être utilisée si la commande est déjà reçue");
		List<Item> tmp = new ArrayList<>();
		for(Item l : list) {
			tmp.add(this.save(l, o));
		}
		return tmp;
	}

	@Override
	public List<Item> saveAll(List<Item> list){
		List<Item> tmp = new ArrayList<>();
		for(Item l : list) {
			tmp.add(this.save(l, l.getId()));
		}
		return tmp;
	}
	
	public List<Item> findWithUnitLike(String unit){
		return this.findWithUnitAndRoomLike(unit, "");
	}
	
	public List<Item> findWithRoomLike(String room){
		return this.findWithUnitAndRoomLike("", room);
	}
	
	public List<Item> findWithUnitAndRoomLike(String unit, String room){
		String query = String.format("SELECT * FROM CCLIB.ITEM i JOIN CCLIB.TYPE t ON i.TYPE_ID = t.ID WHERE LOWER(i.UNIT) LIKE '%%%s%%' AND LOWER(i.ROOM) LIKE '%%%s%%'", unit, room);
		return template.query(query, rowMapper);
	}
	
	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
	
	public boolean delete(Order order) {
		try {
			String query = "DELETE FROM CCLIB.ORDER_ITEM WHERE ORDER_ID = ?";
			template.update(query, order.getId());
			
			return true;
		} catch(DataAccessException e) {
			return false;
		}
	}
	
	public int getCountForCurrentMonth() {
		String query = "SELECT SUM(c) FROM (SELECT COUNT(*) AS c FROM CCLIB.ITEM WHERE MONTH(PURCHASED_AT) = MONTH(CURRENT_DATE) AND YEAR(PURCHASED_AT) = YEAR(CURRENT_DATE) UNION ALL SELECT COUNT(*) FROM CCLIB.ORDER_ITEM WHERE MONTH(PURCHASED_AT) = MONTH(CURRENT_DATE) AND YEAR(PURCHASED_AT) = YEAR(CURRENT_DATE))";
		
	    return template.queryForObject(query, intMapper);	
    }
	
	public int getCountForCurrentMonthForType(Type type) {
		return this.getCountForCurrentMonthForType(type.getId());
	}
	
	public int getCountForCurrentMonthForType(int typeId) {
		String query = "SELECT SUM(c) FROM (SELECT COUNT(*) AS c FROM CCLIB.ITEM WHERE MONTH(PURCHASED_AT) = MONTH(CURRENT_DATE) AND YEAR(PURCHASED_AT) = YEAR(CURRENT_DATE) AND TYPE_ID = ? UNION ALL SELECT COUNT(*) FROM CCLIB.ORDER_ITEM WHERE MONTH(PURCHASED_AT) = MONTH(CURRENT_DATE) AND YEAR(PURCHASED_AT) = YEAR(CURRENT_DATE) AND TYPE_ID = ?)";
		
	    return template.queryForObject(query, intMapper, typeId, typeId);	
	}
}
