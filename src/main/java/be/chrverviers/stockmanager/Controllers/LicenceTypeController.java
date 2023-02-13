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

import be.chrverviers.stockmanager.Domain.Models.LicenceType;
import be.chrverviers.stockmanager.Domain.Models.Type;
import be.chrverviers.stockmanager.Repositories.LicenceTypeRepository;
import be.chrverviers.stockmanager.Repositories.TypeRepository;

@RestController
@RequestMapping(value = "api/licence/type", produces= "application/json")
public class LicenceTypeController {

	@Autowired
	LicenceTypeRepository typeRepo;
	
	@GetMapping(value = "")
	public @ResponseBody ResponseEntity<List<LicenceType>> get() {
		return new ResponseEntity<List<LicenceType>>(typeRepo.findAll(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<LicenceType> findById(@PathVariable("id") int id) {
		return new ResponseEntity<LicenceType>(typeRepo.findById(id), HttpStatus.OK);
	}
	
	@PostMapping(value = "/save")
	public @ResponseBody ResponseEntity<LicenceType> save() {
		LicenceType t = new LicenceType();
		t.setName("Random");
		return new ResponseEntity<LicenceType>(typeRepo.save(t), HttpStatus.OK);
	}
}
