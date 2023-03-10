package be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.InterventionTypeEnum;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.ResponsibilityChain;
import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Repositories.InterventionTypeRepository;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Services.EmailService;

@Service
public class MaintenanceInterventionHandler extends ResponsibilityChain<Intervention>{
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	ItemRepository itemRepo;
	
	@Autowired
	InterventionTypeRepository interventionTypeRepo;
	
	//Helpline: 2644@chrverviers.be
	
	@Override
	public void handle(Intervention request) {
        if (request.getType().getId() == InterventionTypeEnum.MAINTENANCE.value) {
        	sendMail(request);
        	Item item = request.getItem();
        	item.setLastCheckupAt(request.getExpectedDate());
			itemRepo.save(item);
        } else if (next != null) {
            next.handle(request);
        }		
	}
	
	private void sendMail(Intervention intervention) {
		intervention.setType(interventionTypeRepo.findById(intervention.getType().getId()).orElse(intervention.getType()));
		emailService.sendMail(intervention);
	}
}
