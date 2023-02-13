package be.chrverviers.stockmanager.Repositories.Interfaces;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IRepository<T> {
	
	List<T> findAll();
	
	Optional<T> findById(int id);
	
	int create(T t);
	
	T save(T t, int id);
	
	List<T> saveAll(List<T> t);
	
	boolean delete(int id);

}
