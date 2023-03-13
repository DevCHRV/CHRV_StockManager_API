package be.chrverviers.stockmanager.Repositories;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import be.chrverviers.stockmanager.Domain.Models.Unit;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
public class UnitRepository implements IRepository<Unit> {
	
	private JdbcTemplate template;

	public UnitRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}
	
	@Override
	public List<Unit> findAll() {
		String query = "SELECT * FROM CCLIB.UNIT";
		return template.query(query, rowMapper);
	}
	@Override
	public Optional<Unit> findById(int id) {
		String query = "SELECT * FROM CCLIB.UNIT WHERE ID = ?";
		Unit unit = null;
		try { 
			unit = template.queryForObject(query, rowMapper, id);
		} catch(DataAccessException e) {
			//This means that the item is null, so we do nothing as it is nullable
		}
		return Optional.ofNullable(unit);
	}

	

	@Override
	public int create(Unit t) {		
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		String query = "INSERT INTO CCLIB.UNIT (ID, NAME) VALUES (DEFAULT, ?)";
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getName());
			return ps;
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}

	@Override
	public Unit save(Unit t, int id) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public List<Unit> saveAll(List<Unit> t) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
	
	RowMapper<Unit> rowMapper = (rs, rowNum) -> {
		Unit unit = new Unit();
		unit.setId(rs.getInt(1));
		unit.setName(rs.getString(2));
		return unit;
	};
}