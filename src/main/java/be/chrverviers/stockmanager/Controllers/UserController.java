package be.chrverviers.stockmanager.Controllers;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.Role;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.RoleRepository;
import be.chrverviers.stockmanager.Repositories.UserRepository;

@RestController
@RequestMapping(value = "api/user", produces="application/json")
public class UserController {
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	RoleRepository roleRepo;
	/**
	 * Simple GET method
	 * @return all the users
	 */
	@PreAuthorize("hasRole('TEC')")
	@GetMapping
	public @ResponseBody ResponseEntity<List<User>> get() {
		return new ResponseEntity<List<User>>(userRepo.findAll(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method
	 * @param id the id of the user you're looking for
	 * @return the User that has the requested id or an error message
	 */
	@PreAuthorize("hasRole('ADM')")
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Object> getById(@PathVariable("id") int id) {
		//Get the intervention
		User user = userRepo.findById(id).orElse(null);
		if(user==null)
			return new ResponseEntity<Object>("Cet utilisateur n'existe pas !", HttpStatus.BAD_REQUEST);
		//Get and set it's licences
		user.setRoles(roleRepo.findForUser(user));
		return new ResponseEntity<Object>(user, HttpStatus.OK);
	}
	
	/**
	 * Simple PUT method
	 * @param id the id of the user you're looking to update
	 * @return the updated user or an error message
	 */
	@PreAuthorize("hasRole('ADM')")
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody User user, HttpServletRequest request){
		if(id!=user.getId())
			return new ResponseEntity<Object>("Cet utilisateur n'existe pas !", HttpStatus.BAD_REQUEST);
				
		if(roleRepo.findForUser(user).stream().anyMatch(role->role.getName().equals("ROLE_ADM"))) 
			return new ResponseEntity<Object>("Vous ne pouvez pas modifier un compte administrateur !", HttpStatus.BAD_REQUEST);
		
		try {
			//Save the user
			userRepo.save(user, id);
			//Detach all his roles
			userRepo.detachAllRoles(user);
			//Attach the new ones
			userRepo.attachAll(user.getRoles(), user);
			return new ResponseEntity<Object>(userRepo.findById(user.getId()), HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}	
}
