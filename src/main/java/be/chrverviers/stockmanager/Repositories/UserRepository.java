package be.chrverviers.stockmanager.Repositories;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import be.chrverviers.stockmanager.Domain.Models.Role;
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
		user.setUsername(rs.getString("USERNAME"));
		user.setFirstname(rs.getString("FIRSTNAME"));
		user.setLastname(rs.getString("LASTNAME"));
		user.setIsActive(rs.getBoolean("IS_ACTIVE"));
		user.setEmail(rs.getString("EMAIL"));
		return user;
	};
	
	@Override
	public List<User> findAll() {
		String query = "SELECT * FROM CCLIB.USER";
		return template.query(query, rowMapper);
	}

	//		String query = "SELECT * FROM CCLIB.USER u JOIN CCLIB.USER_ROLE ur ON ur.USER_ID = u.ID JOIN CCLIB.ROLE r ON ur.ROLE_ID = r.ID  WHERE u.USERNAME = ?";
	
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
	
	public void attach(Role role, int id) {
		String query = "INSERT INTO CCLIB.USER_ROLE (ROLE_ID, USER_ID) VALUES (?,?)";
		template.update(query, role.getId(), id);
	}
	
	public void attach(Role role, User user) {
		this.attach(role, user.getId());
	}
	
	public void attachAll(List<Role> roles, User user){
		for(Role role : roles) {
			this.attach(role, user);
		}
	}

	@Override
	public int create(User t) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		
		String query = "INSERT INTO CCLIB.USER (ID, USERNAME, FIRSTNAME, LASTNAME, IS_ACTIVE, EMAIL) VALUES (DEFAULT, ?, ?, ?, DEFAULT, ?)";
		
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getUsername());
			ps.setString(2, t.getFirstname());
			ps.setString(3, t.getLastname());
			ps.setString(4, t.getEmail());
			return ps;
		}, keyHolder);		
		
		return keyHolder.getKey().intValue();
	}

	@Override
	public User save(User t, int id) {
		String query = "UPDATE CCLIB.USER u SET u.FIRSTNAME = ?, u.LASTNAME = ?, u.IS_ACTIVE = ? WHERE u.ID = ?";
		
		template.update(query, t.getFirstname(), t.getLastname(), t.getIsActive()?'1':'0', id);
		return null;
	}

	@Override
	public List<User> saveAll(List<User> list) {
		for(User user : list) {
			this.save(user,  user.getId());
		}
		return null;
	}
	
	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
	
	public void detachAllRoles(User user) {
		this.detachAllRoles(user.getId());
	}
	
	public void detachAllRoles(int userId) {
		String query = "DELETE FROM USER_ROLE ur WHERE ur.USER_ID = ?";
		template.update(query, userId);
	}

	public void detachAll(List<Role> roles, User user) {
		for(Role role: roles) {
			this.detach(role, user);
		}
	}
	
	public void detach(Role role, User user) {
		this.detach(role.getId(), user.getId());
	}
	
	public void detach(int roleId, int userId) {
		String query = "DELETE FROM USER_ROLE ur WHERE ur.ROLE_ID = ? AND ur.USER_ID = ?";
		
		template.update(query, roleId, userId);
	}
}
