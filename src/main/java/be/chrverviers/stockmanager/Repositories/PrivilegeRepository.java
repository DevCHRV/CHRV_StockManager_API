package be.chrverviers.stockmanager.Repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import be.chrverviers.stockmanager.Domain.Models.Privilege;

public interface PrivilegeRepository extends JpaRepository<Privilege, Integer>{
	
	Optional<Privilege> findById(@Param("id") int id);
	
	Optional<Privilege> findByName(@Param("name") String name);
	
	List<Privilege> findAll();
	
	Privilege save(Privilege entry);

}