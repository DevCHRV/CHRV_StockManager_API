package be.chrverviers.stockmanager.Repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Order;
import be.chrverviers.stockmanager.Domain.Models.Type;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
public class OrderRepository implements IRepository<Order>{
	
	private JdbcTemplate template;
	
	public OrderRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}

	@Override
	public List<Order> findAll() {
		String query = "SELECT * FROM CCLIB.ORDER o LEFT JOIN CCLIB.USER u ON o.USER_ID = u.ID ORDER BY DATE";
		return template.query(query, rowMapper);
	}

	@Override
	public Optional<Order> findById(int id) {
		String query = "SELECT * FROM CCLIB.ORDER i JOIN CCLIB.USER t ON i.USER_ID = t.ID WHERE i.ID = ?";
		Order order = null;
		try { 
			order = template.queryForObject(query, rowMapper, id);
		} catch(DataAccessException e) {
			//This means that the item is null, so we do nothing as it is nullable
		}
		return Optional.ofNullable(order);
	}

	@Override
	public int create(Order t) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		String query = "INSERT INTO CCLIB.ORDER (ID, USER_ID, DATE, IS_RECEIVED) VALUES (DEFAULT, ?, ?, ?)";
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, t.getUser().getId());
			ps.setDate(2, new Date(t.getDate().getTime()));
			ps.setBoolean(3, t.getIsReceived());
			return ps;
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}
	
	public void attachAll(Order t, List<Item> i) {
		for(Item item:i) {
			this.attach(t, item);
		}
	}
	
	public void attachAllItemId(Order t, List<Integer> i) {
		for(int id:i) {
			this.attachItemId(t, id);
		}
	}
	
	public void attach(Order t, Item i) {
		String query = "UPDATE CCLIB.ITEM t SET t.ORDER_ID = ? WHERE t.ID = ?";
		template.update(query, t.getId(), i.getId());
	}
	
	public void attachItemId(Order t, int id) {
		String query = "UPDATE CCLIB.ITEM t SET t.ORDER_ID = ? WHERE t.ID = ?";
		template.update(query, t.getId(), id);
	}
	
	
	public void attachAll(Order t, Map<Type,Integer> i) {
		for(Iterator<Type> it = i.keySet().iterator(); it.hasNext(); ) {
			Type tmp = it.next();
		    this.attach(t, tmp, i.get(tmp));
		}
	}
	
	public void attach(Order o, Type t, int quantity) {
		String query = "INSERT INTO CCLIB.ORDER_ITEM_TYPE (TYPE_ID, ORDER_ID, QUANTITY) VALUES(?,?,?)";
		template.update(query, t.getId(), o.getId(), quantity);
	}

	@Override
	public Order save(Order t, int id) {
		String query = "UPDATE CCLIB.ORDER t SET t.USER_ID = ?, t.DATE = ?, t.IS_RECEIVED = ? WHERE t.ID = ?";
		template.update(query, t.getUser().getId(), t.getDate(), t.getIsReceived()?'1':'0', id);
		return t;
	}

	@Override
	public List<Order> saveAll(List<Order> t) {
		for(Order o : t) {
			this.save(o, o.getId());
		}
		return t;
	}

	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
	
	RowMapper<Order> rowMapper = (rs, rowNum) -> {
		Order order = new Order();
		order.setId(rs.getInt(1));
		order.setDate(rs.getDate(3));
		order.setIsReceived(rs.getBoolean(4));
		User user = new User();
		user.setId(rs.getInt(5));
		user.setUsername(rs.getString(6));
		user.setFirstname(rs.getString(7));
		user.setLastname(rs.getString(8));
		order.setUser(user);
		return order;
	};

}
