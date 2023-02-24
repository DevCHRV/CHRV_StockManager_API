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
	
	/**
	 * This is the responsibility chain that is responsible of handling the effect of the different types of operations
	 * on the concerned models
	 * For example: 
	 * Creating a Installation Intervention will set the Item as "installed" and "not available"
	 * @param installationInterventionHandler
	 * @param desinstallationInterventionHandler
	 * @param loanInterventionHandler
	 * @param returnInterventionHandler
	 * @param licenceInstallationInterventionHandler
	 * @param licenceDesinstallationInterventionHandler
	 */
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
	
	/**
	 * Simple GET method 
	 * @return all the Interventions
	 */
	@GetMapping
	public @ResponseBody ResponseEntity<List<Intervention>> get() {
		return new ResponseEntity<List<Intervention>>(interventionRepo.findAll(), HttpStatus.OK);
	}
	/**
	 * Simple GET method
	 * @param id the id of the intervention you're looking for
	 * @return the Intervention that has the requested id or an error message
	 */
	@GetMapping(value = "/{id}")
	public @ResponseBody ResponseEntity<Object> getById(@PathVariable("id") int id) {
		//Get the intervention
		Intervention intervention = interventionRepo.findById(id).orElse(null);
		if(intervention==null)
			return new ResponseEntity<Object>("Cette intervention n'existe pas !", HttpStatus.BAD_REQUEST);
		//Get and set it's licences
		intervention.setLicences(licenceRepo.findForIntervention(intervention));
		return new ResponseEntity<Object>(intervention, HttpStatus.OK);
	}
	/**
	 * Simple PUT method to update the Intervention passed as parameter
	 * @param id the id of the intervention you're looking to update
	 * @param intervention the new value of the intervention you're looking to update
	 * @return the updated Intervention or an error message
	 */
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody Intervention intervention){
		if(id!=intervention.getId())
			return new ResponseEntity<Object>("Cette intervention n'existe pas !", HttpStatus.BAD_REQUEST);
		try {
			interventionRepo.save(intervention, id);
			return new ResponseEntity<Object>(interventionRepo.findById(intervention.getId()), HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * POST method that saves the intervention passed as parameter
	 * It will also update the related Item according to the responsibility chain
	 * @param intervention the intervention you're looking to create
	 * @return the intervention with it's generated id or an error message
	 */
	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody Intervention intervention) {
		//Simple checks for data validity
		if(intervention.getDescription()==null) {
			return new ResponseEntity<Object>("La description ne peut pas etre vide !", HttpStatus.BAD_REQUEST);
		}
		if(intervention.getType()==null) {
			return new ResponseEntity<Object>("Le type ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(intervention.getItem()==null) {
			return new ResponseEntity<Object>("Le matériel ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(intervention.getUnit()==null || intervention.getUnit().equals("")) {
			return new ResponseEntity<Object>("L'unité ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(intervention.getRoom()==null || intervention.getUnit().equals("")) {
			return new ResponseEntity<Object>("Le local ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		//Get the user from Spring's Security context
		User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//Linking the intervention and the user that made it
		intervention.setUser(u);
		try {
			//Try to create the intervention
			intervention.setId(interventionRepo.create(intervention));
			//Updating the related item according to the responsibility chain
			//It means that it's here that we link the intervention and the licences it contains 
			//(by default we unlink everything and re-link the ones that are kept)
			updateItemStatus(intervention);
			//Linking the intervention to the user that requested it
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
