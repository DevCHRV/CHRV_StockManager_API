package be.chrverviers.stockmanager.Repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import be.chrverviers.stockmanager.Domain.Models.Type;

public interface TypeRepository extends CrudRepository<Type, Integer> {
	
	Type findById(@Param("id") int id);
	
	List<Type> findAll();
	
	Type save(Type entry);

}
