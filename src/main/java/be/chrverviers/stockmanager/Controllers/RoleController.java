package be.chrverviers.stockmanager.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import be.chrverviers.stockmanager.Domain.Models.Role;
import be.chrverviers.stockmanager.Repositories.RoleRepository;

@RestController
@RequestMapping(value = "api/role", produces="application/json")
public class RoleController {
	
	@Autowired
	RoleRepository roleRepo;
	/**
	 * Simple GET method
	 * @return all the users
	 */
	@PreAuthorize("hasRole('ADM')")
	@GetMapping
	public @ResponseBody ResponseEntity<List<Role>> get() {
		return new ResponseEntity<List<Role>>(roleRepo.findAll(), HttpStatus.OK);
	}
}
