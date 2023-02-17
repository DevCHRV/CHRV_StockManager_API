package be.chrverviers.stockmanager.Repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
public class UserRepository implements IRepository<User>{
	
	private JdbcTemplate template;
	
	public UserRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}

	RowMapper<User> rowMapper = (rs, rowNum) -> {
		User user = new User();
		user.setId(rs.getInt("id"));
		user.setUsername(rs.getString("username"));
		user.setFirstname(rs.getString("firstname"));
		user.setLastname(rs.getString("lastname"));
		return user;
	};
	
	@Override
	public List<User> findAll() {
		String query = "SELECT * FROM CCLIB.USER";
		return template.query(query, rowMapper);
	}

	public Optional<User> findByUsername(String username){
		String query = "SELECT * FROM CCLIB.USER WHERE USERNAME = ?";
		User user = null;
		try { 
			user = template.queryForObject(query, rowMapper, username);
		} catch(DataAccessException e) {
			//This means that there is no data, but we don't do anything because in this case we return an Optional
		}
		return Optional.ofNullable(user);
	}
	
	@Override
	public Optional<User> findById(int id) {
		String query = "SELECT * FROM CCLIB.USER WHERE ID = ?";
		User user = null;
		try { 
			user = template.queryForObject(query, rowMapper, id);
		} catch(DataAccessException e) {
			//This means that there is no data, but we don't do anything because in this case we return an Optional
		}
		return Optional.ofNullable(user);
	}

	@Override
	public int create(User t) {
		String query = "INSERT INTO CCLIB.USER (ID, USERNAME, FIRSTNAME, LASTNAME) VALUES (DEFAULT, ?, ?, ?)";
		template.update(query, t.getUsername(), t.getFirstname(), t.getLastname());
		return -1;
	}

	@Override
	public User save(User t, int id) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public List<User> saveAll(List<User> list) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}
	
	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
}
