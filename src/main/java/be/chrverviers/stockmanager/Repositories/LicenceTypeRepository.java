package be.chrverviers.stockmanager.Repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.LicenceType;

public interface LicenceTypeRepository extends CrudRepository<LicenceType, Integer>{
		
	List<LicenceType> findAll();
	
	LicenceType findById(@Param("id") int id);
	
	LicenceType save(Licence entry);

}
