package be.chrverviers.stockmanager.Controllers;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.UserRepository;

@RestController
@RequestMapping(value = "api/user", produces="application/json")
public class UserController {
	
	@Autowired
	UserRepository userRepo;
	
	/**
	 * Simple GET method
	 * @return all the users
	 */
	@GetMapping
	public @ResponseBody ResponseEntity<List<User>> get() {
		return new ResponseEntity<List<User>>(userRepo.findAll(), HttpStatus.OK);
	}
	
	@GetMapping("current") ResponseEntity<Object> current() {
		User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = userRepo.findById(u.getId()).orElse(null);
		if(u==null || user == null) {
			return new ResponseEntity<Object>("Aucun utilisateur n'est connecté !", HttpStatus.UNAUTHORIZED);
		}
		return new ResponseEntity<Object>(user, HttpStatus.OK);
	}
}
