package be.chrverviers.stockmanager.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations.DesinstallationInterventionHandler;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations.InstallationInterventionHandler;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations.LicenceDesinstallationInterventionHandler;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations.LicenceInstallationInterventionHandler;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations.LoanInterventionHandler;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations.ReturnInterventionHandler;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.ResponsibilityChain;
import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.InterventionRepository;
import be.chrverviers.stockmanager.Repositories.InterventionTypeRepository;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Repositories.LicenceRepository;

@RestController
@RequestMapping(value = "api/intervention", produces="application/json")
public class InterventionController {
	
	private ResponsibilityChain<Intervention> chain;
	
	public InterventionController(
			@Autowired InstallationInterventionHandler installationInterventionHandler,
			@Autowired DesinstallationInterventionHandler desinstallationInterventionHandler,
			@Autowired LoanInterventionHandler loanInterventionHandler,
			@Autowired ReturnInterventionHandler returnInterventionHandler,
			@Autowired LicenceInstallationInterventionHandler licenceInstallationInterventionHandler,
			@Autowired LicenceDesinstallationInterventionHandler licenceDesinstallationInterventionHandler
			) {
		this.chain = 
				installationInterventionHandler.build(
				desinstallationInterventionHandler,
				loanInterventionHandler, 
				returnInterventionHandler,
				licenceInstallationInterventionHandler,
				licenceDesinstallationInterventionHandler);
	}
	
	@Autowired
	InterventionRepository interventionRepo;
	
	@Autowired
	LicenceRepository licenceRepo;
	
	@Autowired
	InterventionTypeRepository typeRepo;
	
	@Autowired
	ItemRepository itemRepo;
	
	@GetMapping
	public @ResponseBody ResponseEntity<List<Intervention>> get() {
		return new ResponseEntity<List<Intervention>>(interventionRepo.findAll(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Intervention> getById(@PathVariable("id") int id) {
		Intervention intervention = interventionRepo.findById(id).orElse(null);
		intervention.setLicences(licenceRepo.findForIntervention(intervention));
		return new ResponseEntity<Intervention>(intervention, HttpStatus.OK);
	}
	
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody Intervention intervention){
		try {
			interventionRepo.save(intervention, id);
			return new ResponseEntity<Object>(interventionRepo.findById(intervention.getId()), HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody Intervention intervention) {
		if(intervention.getDescription()==null) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		}
		if(intervention.getType()==null) {
			return new ResponseEntity<Integer>(HttpStatus.BAD_REQUEST);
		}
		User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		intervention.setUser(u);
		try {
			intervention.setId(interventionRepo.create(intervention));
			updateItemStatus(intervention);
			interventionRepo.attachAll(intervention.getLicences(), intervention);
			interventionRepo.attach(intervention.getNotifier(), intervention);
			//TODO: Create and save the automatic report
			return new ResponseEntity<Intervention>(intervention, HttpStatus.OK);
		} catch(IllegalStateException e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		catch(Exception e) {
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	private void updateItemStatus(Intervention intervention) throws IllegalStateException {
		chain.handle(intervention);
	}
}
