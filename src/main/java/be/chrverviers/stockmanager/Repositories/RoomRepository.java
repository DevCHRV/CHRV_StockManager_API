package be.chrverviers.stockmanager.Repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import com.ibm.db2.cmx.PushDownError.SQLException;

import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.Room;
import be.chrverviers.stockmanager.Domain.Models.Unit;
import be.chrverviers.stockmanager.Repositories.Interfaces.IRepository;

@Repository
public class RoomRepository implements IRepository<Room> {
	
	private JdbcTemplate template;

	public RoomRepository(JdbcTemplate template) {
		super();
		this.template = template;
	}
	
	@Override
	public List<Room> findAll() {
		String query = "SELECT * FROM CCLIB.ROOM r JOIN UNIT u ON r.UNIT_ID = u.ID";
		return template.query(query, withUnitRowMapper);
	}
	@Override
	public Optional<Room> findById(int id) {
		String query = "SELECT * FROM CCLIB.ROOM t JOIN UNIT u ON t.UNIT_ID = u.ID WHERE t.ID = ?";
		Room room = null;
		try { 
			room = template.queryForObject(query, withUnitRowMapper, id);
		} catch(DataAccessException e) {
			//This means that the item is null, so we do nothing as it is nullable
		}
		return Optional.ofNullable(room);
	}
	
	public Optional<Room> findByName(String name) {
		String query = "SELECT * FROM CCLIB.ROOM t JOIN UNIT u ON t.UNIT_ID = u.ID WHERE t.NAME = ?";
		Room room = null;
		try { 
			room = template.queryForObject(query, withUnitRowMapper, name);
		} catch(DataAccessException e) {
			//This means that the item is null, so we do nothing as it is nullable
		}
		return Optional.ofNullable(room);
	}

	public List<Room> findForUnit(Unit unit){
		String query = "SELECT * FROM CCLIB.ROOM r WHERE r.UNIT_ID = ?";
		List<Room> rooms = new ArrayList<Room>();
		try { 
			rooms = template.query(query, rowMapper, unit.getId());
		} catch(DataAccessException e) {
			e.printStackTrace();
		}
		return rooms;
	}

	@Override
	public int create(Room t) {
		Integer unitId = t.getUnit()!=null ? t.getUnit().getId() : null;
		
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

		String query = "INSERT INTO CCLIB.ROOM (ID, NAME, UNIT_ID) VALUES (DEFAULT, ?, ?)";
		template.update(connection -> {
			PreparedStatement ps =  connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, t.getName());
			ps.setInt(2, unitId);
			return ps;
		}, keyHolder);
		return keyHolder.getKey().intValue();
	}

	@Override
	public Room save(Room t, int id) {
		Integer unitId = t.getUnit()!=null ? t.getUnit().getId() : null;
		
		String query = "UPDATE CCLIB.ROOM r SET r.NAME = ?, r.UNIT_ID = ? WHERE r.ID = ?";
		
		template.update(query, t.getName(), unitId, id);
		return null;
	}

	@Override
	public List<Room> saveAll(List<Room> t) {
		for(Room r : t) {
			this.save(r, r.getId());
		}
		return null;
	}

	@Override
	public boolean delete(int id) {
		try {
			String query = "DELETE FROM CCLIB.ROOM WHERE ID = ?";
			template.update(query, id);
			
			return true;
		} catch(DataAccessException e) {
			return false;
		}
	}
	
	RowMapper<Room> withUnitRowMapper = (rs, rowNum) -> {
		Room room = new Room();
		room.setId(rs.getInt(1));
		room.setName(rs.getString(2));
		Unit unit = new Unit();
		unit.setId(rs.getInt(4));
		unit.setName(rs.getString(5));
		room.setUnit(unit);
		return room;
	};
	
	RowMapper<Room> rowMapper = (rs, rowNum) -> {
		Room room = new Room();
		room.setId(rs.getInt(1));
		room.setName(rs.getString(2));
		room.setUnit(new Unit(rs.getInt(3)));
		return room;
	};
}