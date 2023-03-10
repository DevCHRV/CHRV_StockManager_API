package be.chrverviers.stockmanager.Repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import be.chrverviers.stockmanager.Domain.Models.LicenceType;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
public class LicenceTypeRepository implements IRepository<LicenceType>{


	private JdbcTemplate template;

	public LicenceTypeRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}
	
	@Override
	public List<LicenceType> findAll() {
		String query = "SELECT * FROM CCLIB.LICENCE_TYPE";
		return template.query(query, rowMapper);
	}
	@Override
	public Optional<LicenceType> findById(int id) {
		String query = "SELECT * FROM CCLIB.LICENCE_TYPE t WHERE t.ID = ?";
		LicenceType type = null;
		try { 
			type = template.queryForObject(query, rowMapper, id);
		} catch(DataAccessException e) {
			//This means that the item is null, so we do nothing as it is nullable
		}
		return Optional.ofNullable(type);
	}

	@Override
	public int create(LicenceType t) {
		// TODO Module de remplacement de méthode auto-généré
		return 0;
	}

	@Override
	public LicenceType save(LicenceType t, int id) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public List<LicenceType> saveAll(List<LicenceType> t) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
	
	RowMapper<LicenceType> rowMapper = (rs, rowNum) -> {
		LicenceType type = new LicenceType();
		type.setId(rs.getInt(1));
		type.setName(rs.getString(2));
		type.setAlias(rs.getString(3));
		return type;
	};

}
