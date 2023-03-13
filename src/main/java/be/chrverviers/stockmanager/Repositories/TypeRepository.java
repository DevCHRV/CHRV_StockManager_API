package be.chrverviers.stockmanager.Repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import be.chrverviers.stockmanager.Domain.Models.Order;
import be.chrverviers.stockmanager.Domain.Models.Type;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
public class TypeRepository implements IRepository<Type> {
	
	private JdbcTemplate template;

	public TypeRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}
	
	@Override
	public List<Type> findAll() {
		String query = "SELECT * FROM CCLIB.TYPE";
		return template.query(query, rowMapper);
	}
	@Override
	public Optional<Type> findById(int id) {
		String query = "SELECT * FROM CCLIB.TYPE t WHERE t.ID = ?";
		Type type = null;
		try { 
			type = template.queryForObject(query, rowMapper, id);
		} catch(DataAccessException e) {
			//This means that the item is null, so we do nothing as it is nullable
		}
		return Optional.ofNullable(type);
	}
	
	public Map<Type,Integer> findForOrderId(int id){
		String query = "SELECT t.*, oit.quantity as ORDER_QUANTITY FROM CCLIB.TYPE t JOIN CCLIB.ORDER_ITEM_TYPE oit ON oit.TYPE_ID = t.ID WHERE oit.ORDER_ID = ?";
		return template.query(query, (rs)-> {
			Map<Type,Integer> tmp = new HashMap<Type, Integer>();
			while(rs.next()) {
				Type type = new Type();
				type.setId(rs.getInt(1));
				type.setName(rs.getString(2));
				type.setDescription(rs.getString(3));
				type.setExpectedLifetime(rs.getInt(4));
				type.setTotalQuantity(rs.getInt(5));
				type.setAvailableQuantity(rs.getInt(6));
				type.setAlias(rs.getString(7));
				tmp.put(type, rs.getInt(8));	
			}
			return tmp;
		}, id);
	}
	
	public Map<Type,Integer> findForOrder(Order o){
		return this.findForOrderId(o.getId());
	}

	@Override
	public int create(Type t) {
		// TODO Module de remplacement de méthode auto-généré
		return 0;
	}

	@Override
	public Type save(Type t, int id) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public List<Type> saveAll(List<Type> t) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
	
	RowMapper<Type> rowMapper = (rs, rowNum) -> {
		Type type = new Type();
		type.setId(rs.getInt(1));
		type.setName(rs.getString(2));
		type.setDescription(rs.getString(3));
		type.setExpectedLifetime(rs.getInt(4));
		type.setTotalQuantity(rs.getInt(5));
		type.setAvailableQuantity(rs.getInt(6));
		type.setAlias(rs.getString(7));
		return type;
	};
	
}
