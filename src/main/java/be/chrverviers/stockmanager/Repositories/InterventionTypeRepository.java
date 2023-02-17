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

import be.chrverviers.stockmanager.Domain.Models.InterventionType;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.LicenceType;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
public class InterventionTypeRepository implements IRepository<InterventionType> {

	private JdbcTemplate template;
	
	public InterventionTypeRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}
	
	
	@Override
	public List<InterventionType> findAll() {
		String query = "SELECT * FROM CCLIB.INTERVENTION_TYPE";
		return template.query(query, rowMapper);
	}

	@Override
	public Optional<InterventionType> findById(int id) {
		String query = "SELECT * FROM CCLIB.INTERVENTION_TYPE it WHERE it.ID = ?";
		InterventionType interventionType = new InterventionType();
		try { 
			interventionType = template.queryForObject(query, rowMapper, id);
		} catch(DataAccessException e) {
			//This means that there is no data, but we don't do anything because in this case we return an Optional
		}
		return Optional.ofNullable(interventionType);
	}

	@Override
	public int create(InterventionType t) {	
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

		String query = "INSERT INTO CCLIB.INTERVENTION_TYPE (ID, NAME) VALUES (DEFAULT, ?)";
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getName());
			return ps;
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}

	@Override
	public InterventionType save(InterventionType t, int id) {
		String query = "UPDATE CCLIB.INTERVENTION_TYPE t SET t.NAME = ? WHERE t.ID = ?";
		//Note that we are forced to convert boolean to char(1) by hand because the OS/400 doesn't like booleans and JDBC doesn't make the conversion alone		
		template.update(query, t.getName(), id);
		return null;
	}

	@Override
	public List<InterventionType> saveAll(List<InterventionType> t) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
	
	RowMapper<InterventionType> rowMapper = (rs, rowNum) -> {
		InterventionType type = new InterventionType();
		type.setId(rs.getInt(1));
		type.setName(rs.getString(2));
		return type;
	};
	

}
