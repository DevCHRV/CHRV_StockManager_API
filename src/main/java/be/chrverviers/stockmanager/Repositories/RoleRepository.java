package be.chrverviers.stockmanager.Repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import be.chrverviers.stockmanager.Domain.Models.Role;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;
import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepository implements IRepository<Role> {
	private JdbcTemplate template;
	
	public RoleRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}

	RowMapper<Role> rowMapper = (rs, rowNum) -> {
		Role role = new Role();
		role.setId(rs.getInt(1));
		role.setName(rs.getString(2));
		return role;
	};

	public List<Role> findForUser(User user){
		return this.findForUserId(user.getId());
	}
	
	public List<Role> findForUserId(int id){
		String query = "SELECT r.* FROM CCLIB.ROLE r LEFT JOIN CCLIB.USER_ROLE ur ON ur.ROLE_ID = r.ID LEFT JOIN CCLIB.USER u ON ur.USER_ID = u.ID WHERE u.id = ?";
		return template.query(query, rowMapper, id);
	}
	
	@Override
	public List<Role> findAll() {
		String query = "SELECT * FROM CCLIB.ROLE";
		return template.query(query, rowMapper);
	}

	@Override
	public Optional<Role> findById(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public int create(Role t) {
		// TODO Module de remplacement de méthode auto-généré
		return 0;
	}

	@Override
	public Role save(Role t, int id) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public List<Role> saveAll(List<Role> t) {
		// TODO Module de remplacement de méthode auto-généré
		return null;
	}

	@Override
	public boolean delete(int id) {
		// TODO Module de remplacement de méthode auto-généré
		return false;
	}
}