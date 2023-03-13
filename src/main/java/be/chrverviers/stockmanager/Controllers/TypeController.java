package be.chrverviers.stockmanager.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import be.chrverviers.stockmanager.Domain.Models.Type;
import be.chrverviers.stockmanager.Repositories.TypeRepository;

@RestController
@RequestMapping(value = "api/item/type", produces= "application/json")
@Transactional
public class TypeController {

	@Autowired
	TypeRepository typeRepo;
	
	/**
	 * Simple GET method
	 * @return all the items types
	 */
	@GetMapping(value = "")
	@PreAuthorize("hasRole('TEC')")
	public @ResponseBody ResponseEntity<List<Type>> get() {
		return new ResponseEntity<List<Type>>(typeRepo.findAll(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method
	 * @param id the id of the item type you're looking for
	 * @return the type or an error message
	 */
	@GetMapping(value = "/{id}")
	@PreAuthorize("hasRole('TEC')")
	public @ResponseBody ResponseEntity<Object> findById(@PathVariable("id") int id) {
		Type t = typeRepo.findById(id).orElse(null);
		if(t==null)
			return new ResponseEntity<Object>("Ce type n'existe pas !", HttpStatus.BAD_REQUEST);
		return new ResponseEntity<Object>(t, HttpStatus.OK);
	}
	
	/**
	 * Simple POST method
	 * @return the type with it's generated id or an error message
	 */
	@PostMapping(value = "/save")
	@PreAuthorize("hasRole('PGM')")
	public @ResponseBody ResponseEntity<Integer> save() {
		//TODO
		return null;
//		Type t = new Type();
//		t.setName("Random");
//		t.setDescription("Random desc");
//		return new ResponseEntity<Integer>(typeRepo.create(t), HttpStatus.OK);
	}
}
