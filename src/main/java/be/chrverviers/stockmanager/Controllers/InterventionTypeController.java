package be.chrverviers.stockmanager.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import be.chrverviers.stockmanager.Domain.Models.InterventionType;
import be.chrverviers.stockmanager.Repositories.InterventionTypeRepository;

@RestController
@RequestMapping(value = "api/intervention/type", produces="application/json")
@Transactional
public class InterventionTypeController {

	@Autowired
	InterventionTypeRepository typeRepo;
	
	/**
	 * Simple GET method
	 * @return all the InterventionTypes
	 */
	@PreAuthorize("hasRole('TEC')")
	@GetMapping
	public @ResponseBody ResponseEntity<List<InterventionType>> get() {
		return new ResponseEntity<List<InterventionType>>(typeRepo.findAll(), HttpStatus.OK);
	}
	
}
