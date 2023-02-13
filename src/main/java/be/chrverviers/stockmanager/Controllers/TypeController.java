package be.chrverviers.stockmanager.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class TypeController {

	@Autowired
	TypeRepository typeRepo;
	
	@GetMapping(value = "")
	public @ResponseBody ResponseEntity<List<Type>> get() {
		return new ResponseEntity<List<Type>>(typeRepo.findAll(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Type> findById(@PathVariable("id") int id) {
		return new ResponseEntity<Type>(typeRepo.findById(id), HttpStatus.OK);
	}
	
	@PostMapping(value = "/save")
	public @ResponseBody ResponseEntity<Type> save() {
		Type t = new Type();
		t.setName("Random");
		t.setDescription("Random desc");
		return new ResponseEntity<Type>(typeRepo.save(t), HttpStatus.OK);
	}
}
