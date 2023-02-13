package be.chrverviers.stockmanager.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import be.chrverviers.stockmanager.Domain.Models.Role;
import be.chrverviers.stockmanager.Domain.Models.User;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
	
    //Optional<List<Role>> findByUsers(User user);
    //Optional<List<Role>> findByUsersId(int id);
    Optional<Role> findByName(String name);
}