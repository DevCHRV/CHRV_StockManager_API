package be.chrverviers.stockmanager.Services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.InterventionTypeEnum;
import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Repositories.InterventionRepository;
import be.chrverviers.stockmanager.Repositories.InterventionTypeRepository;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Repositories.UserRepository;

@Service
@EnableScheduling
public class MaintenanceService {
	
    @Autowired
    Environment env;
    
    @Autowired
    InterventionRepository interventionRepo;
    
    @Autowired
    InterventionTypeRepository interventionTypeRepo;
    
    @Autowired
    ItemRepository itemRepo;
    
    @Autowired
    UserRepository userRepo;
    
    @Autowired
    EmailService emailService;
    
    private Logger logger = LoggerFactory.getLogger(EmailService.class);

    public MaintenanceService() {

    }
    
    //This is a "cron" expression. This one means that it will be done every day at 07 am
    //See https://crontab.cronhub.io/ for easy cron expression generation and explanations
    @Scheduled(cron = "0 0 7 * * *")
    synchronized void check() throws IOException {
        logger.info("Starting to check if some items needs a checkup...");
    	//We start by fetching all the items
    	List<Item> items = itemRepo.findAll();
    	List<Intervention> interventions = interventionRepo.findAllPending();
    	
    	//We iterate through a clone of the list, which will allow us to remove items from the original list without causing problems
    	//We will remove all items that were used, so that we can reduce the number of iteration made over the list
    	for(Item item: new ArrayList<Item>(items)) {
        	//For each item, we check if the checkup has expired
    		if(isCheckupExpired(item)) {
    			//We will get all the items that are in the same room
    			List<Item> roomItems = items.stream().filter(i->i.getRoom().equals(item.getRoom())).collect(Collectors.toList());
    			int expiredCount = 0;
    			int total = roomItems.size();
	            logger.info(String.format("Checking items of Room %s", item.getRoom()));
    			for(Item i:roomItems) {
    	            logger.info(String.format("Item %s needs a checkup", i));
    				if(isCheckupExpired(i))
    					expiredCount++;
    				
    				//We also remove the item from the master list so that it is not iterated over afterwards
    				items.remove(i);
    			}
    			//If more than a third of the items of the room have their checkup expired, we make plan one
    			if(expiredCount > total/3) {
    				for(Item i : roomItems) {
    					//We get the intervention related to the current item
    					List<Intervention> itemInterventions = interventions.stream().filter(inte->inte.getItem().equals(i)).collect(Collectors.toList());
    					//We check that there isn't yet a Maintenance Intervention that is NOT done
    					boolean isCheckupPlanned = false;
    					for(Intervention in: itemInterventions) {
    						if(in.getType().getId()==InterventionTypeEnum.MAINTENANCE.value) {
    							isCheckupPlanned = true;
    	        	            logger.info(String.format("No checkup created for Item %s because one already exists: %s", i, in));
    							//No point in finishing the loop
    							break;
    						}
    					}
    					
    					//if a checkup intervention for this item is already planned and NOT done, then we don't create one for this item
    					if(isCheckupPlanned)
    						continue;
    					
    					//We build a new intervention for the item
    					Intervention intervention = new Intervention();
    					intervention.setType(interventionTypeRepo.findById(InterventionTypeEnum.MAINTENANCE.value).orElse(null));
    					intervention.setDescription("Entretien programmé automatiquement car il a été détecté que plusieurs machines du local n'étaient pas en ordre d'entretien");
    					intervention.setExpectedDate(fromLocalDate(LocalDate.now().plusDays(14)));
    					intervention.setItem(i);
    					intervention.setRoom(i.getRoom());
    					intervention.setUser(userRepo.findByUsername("StockManager").orElse(null));
    					//We create it
    					intervention.setId(interventionRepo.create(intervention));
    					emailService.sendMail(intervention);
        	            logger.info(String.format("Item %s was created a new Intervention %s", i, intervention));
    				}
    			} else {
    	            logger.info(String.format("Canceled Room %s item's checkup because less than 1/3 of the items in the room needs a checkup", item.getRoom()));
    			}
    		}
    		//We remove this item for the collection
    		items.remove(item);
    	}
        logger.info("Finished checkup planning");
    }
    
    private boolean isCheckupExpired(Item i) {
		//We convert the date to the new Java.time LocalDate because they provide nice useful methods the manipulate the dates
		final LocalDate lastCheckupDate = fromDate(i.getLastCheckupAt());
		final LocalDate currentDate = LocalDate.now();
		
		//We check if the lastCheckupDate + the interval is Before the current date
		//If it's before it means that the checkup has expired
    	return lastCheckupDate.plusDays(i.getCheckupInterval()).isBefore(currentDate);
    }
    
    private Date fromLocalDate(LocalDate from) {
    	return Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    private LocalDate fromDate(Date from) {
    	//For some reason, A Java.util.Date can actually be a java.sql.Date
    	//And the java.sql.Date can't use the toInstant() method 
        try {
            return from.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        } catch (UnsupportedOperationException e) {
        	Date date = new Date(from.getTime());
        	return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }
}
