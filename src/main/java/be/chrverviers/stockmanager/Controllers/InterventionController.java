package be.chrverviers.stockmanager.Controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
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
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations.MaintenanceInterventionHandler;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations.ReturnInterventionHandler;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.ResponsibilityChain;
import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Licence;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.InterventionRepository;
import be.chrverviers.stockmanager.Repositories.InterventionTypeRepository;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Repositories.LicenceRepository;
import be.chrverviers.stockmanager.Repositories.UserRepository;
import be.chrverviers.stockmanager.Services.EmailService;

@RestController
@RequestMapping(value = "api/intervention", produces="application/json")
@Transactional
public class InterventionController {
	
    private Logger logger = LoggerFactory.getLogger(InterventionController.class);
	
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
			@Autowired LicenceDesinstallationInterventionHandler licenceDesinstallationInterventionHandler,
			@Autowired MaintenanceInterventionHandler maintenanceInterventionHandler
			) {
		this.chain = 
				installationInterventionHandler.build(
				desinstallationInterventionHandler,
				loanInterventionHandler, 
				returnInterventionHandler,
				licenceInstallationInterventionHandler,
				licenceDesinstallationInterventionHandler,
				maintenanceInterventionHandler
				);
	}
	
	@Autowired
	InterventionRepository interventionRepo;
	
	@Autowired
	LicenceRepository licenceRepo;
	
	@Autowired
	InterventionTypeRepository typeRepo;
	
	@Autowired
	ItemRepository itemRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	EmailService emailService;
	
	/**
	 * Simple GET method 
	 * @return all the Interventions
	 */
	@PreAuthorize("hasRole('TEC')")
	@GetMapping
	public @ResponseBody ResponseEntity<List<Intervention>> get() {
		return new ResponseEntity<List<Intervention>>(interventionRepo.findAll(), HttpStatus.OK);
	}
	
	/**
	 * Simple GET method
	 * @param id the id of the intervention you're looking for
	 * @return the Intervention that has the requested id or an error message
	 */
	@PreAuthorize("hasRole('TEC')")
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
	@PreAuthorize("hasRole('TEC')")
	@PutMapping(value="/{id}")
	public @ResponseBody ResponseEntity<Object> update(@PathVariable("id") int id, @RequestBody Intervention intervention){
		logger.info(String.format("User '%s' is updating Intervention with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
		if(id!=intervention.getId())
			return new ResponseEntity<Object>("Cette intervention n'existe pas !", HttpStatus.BAD_REQUEST);
		try {
			interventionRepo.save(intervention, id);
			logger.info(String.format("User '%s' has succesfully updated Intervention with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>(interventionRepo.findById(intervention.getId()), HttpStatus.OK);
		} catch(Exception e) {
			logger.error(String.format("User '%s' failed to update Intervention with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>("La modification à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * POST method that saves the intervention passed as parameter
	 * It will also update the related Item according to the responsibility chain
	 * @param intervention the intervention you're looking to create
	 * @return the intervention with it's generated id or an error message
	 */
	@PreAuthorize("hasRole('TEC')")
	@PostMapping(value = "/")
	public @ResponseBody Object save(@RequestBody Intervention intervention) {
		logger.info(String.format("User '%s' is creating a new Intervention", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
		//Simple checks for data validity
		if(intervention.getDescription()==null) {
			logger.info(String.format("User '%s' failed to create a new Intervention due to bad request: bad 'description'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("La description ne peut pas etre vide !", HttpStatus.BAD_REQUEST);
		}
		if(intervention.getType()==null) {
			logger.info(String.format("User '%s' failed to create a new Intervention due to bad request: bad 'type'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("Le type ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		if(intervention.getItem()==null) {
			logger.info(String.format("User '%s' failed to create a new Intervention due to bad request: bad 'item'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("Le matériel ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}

		if(intervention.getRoom()==null || intervention.getRoom().getUnit() == null) {
			logger.info(String.format("User '%s' failed to create a new Intervention due to bad request: bad 'room'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<Object>("Le local ne peut pas être vide !", HttpStatus.BAD_REQUEST);
		}
		//Get the user from Spring's Security context
		User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//Linking the intervention and the user that made it
		intervention.setUser(u);
		try {
			//Fetching the notifier (coming from the LDAP), if it doesn't exists we will need to create it
			//If they do, then we replace the user with the value from the database (so that it has the good id)
			User notifier = intervention.getNotifier();
			if(notifier != null) {
				User tmpNotifier = userRepo.findByUsername(notifier.getUsername()).orElse(null);
				if(tmpNotifier==null) {
					try {
						notifier.setId(userRepo.create(notifier));
					}catch(Exception e) {
						logger.error(String.format("User '%s' failed to create a new Intervention. The selected notifier's creation failed.", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
						return new ResponseEntity<String>("Impossible d'ajouter ce demandeur.", HttpStatus.BAD_REQUEST);
					}
				}
				else {
					intervention.setNotifier(tmpNotifier);
				}
			}
			//As we allow the user to select user's that don't always exist in the database we need to check if they exist or not
			//If they don't, we create them
			//If they do, then we replace the user with the value from the database (so that it has the good id)
			for(Licence l : intervention.getLicences()) {
				User user = l.getUser();
				if(user!=null) {
					User tmp = userRepo.findByUsername(user.getUsername()).orElse(null);
					if(tmp == null) {
						try {
							user.setId(userRepo.create(tmp));
						}catch(Exception e) {
							logger.error(String.format("User '%s' failed to create a new Intervention. The licence's user's creation failed.", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
							return new ResponseEntity<String>("Impossible d'ajouter cet utilisateur.", HttpStatus.BAD_REQUEST);
						}
					} else {
						l.setUser(tmp);
					}
				}
			}
			
			//Try to create the intervention
			intervention.setId(interventionRepo.create(intervention));
			//Updating the related item according to the responsibility chain
			//It means that it's here that we link the intervention and the licences it contains 
			//(by default we unlink everything and re-link the ones that are kept)
			updateItemStatus(intervention);
			//Linking the intervention to the user that requested it
			interventionRepo.attach(intervention.getNotifier(), intervention);
			//TODO: Create and save the automatic report
			logger.info(String.format("User '%s' successfully created a new Intervention with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), intervention.getId()));
			return new ResponseEntity<Intervention>(intervention, HttpStatus.OK);
		} catch(IllegalStateException e) {
			logger.info(String.format("User '%s' failed to create a new Intervention due to bad request", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		catch(Exception e) {
			logger.error(String.format("User '%s' failed to create a new Intervention", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()));
			return new ResponseEntity<String>("La création à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * Simple PUT method, not a REST method but... anyway, this will force send a mail to the helpline to generated a ticket number
	 * @param id the id of the intervention you're looking to update
	 * @param intervention the new value of the intervention you're looking to update
	 * @return the updated Intervention or an error message
	 */
	@PreAuthorize("hasRole('TEC')")
	@PutMapping(value="/{id}/ticket")
	public Object generateTicket(@PathVariable("id") int id, @RequestBody Intervention intervention) {
		logger.info(String.format("User '%s' is manually sending ticket generation mail for Intervention with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
		if(id!=intervention.getId())
			return new ResponseEntity<Object>("Cette intervention n'existe pas !", HttpStatus.BAD_REQUEST);
		try {
			emailService.sendMail(interventionRepo.findById(id).orElse(null), true);
			logger.info(String.format("User '%s' has succesfully sent a ticket generation mail to the helpline for Intervention with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>(HttpStatus.OK);
		} catch(Exception e) {
			logger.error(String.format("User '%s' failed to send a ticket generation mail for Intervention with id:'%s'", (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), id));
			return new ResponseEntity<Object>("L'envoi à échoué !", HttpStatus.BAD_REQUEST);
		}
	}
	
	private void updateItemStatus(Intervention intervention) throws IllegalStateException {
		chain.handle(intervention);
	}
}
