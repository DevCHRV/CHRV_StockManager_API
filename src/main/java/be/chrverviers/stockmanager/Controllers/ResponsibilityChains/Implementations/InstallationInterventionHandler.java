package be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.InterventionTypeEnum;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.ResponsibilityChain;
import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Repositories.ItemRepository;

@Service
public class InstallationInterventionHandler extends ResponsibilityChain<Intervention> {
	
	@Autowired
	ItemRepository itemRepo;
	
	@Override
	public void handle(Intervention request) {
        if (request.getType().getId() == InterventionTypeEnum.INSTALLATION.value) {
        	installItem(request.getItem());
        	updateItemLocation(request);
        	itemRepo.save(request.getItem());
        } else if (next != null) {
            next.handle(request);
        }		
	}
	
	private void installItem(Item i) {
	    if(!i.getIs_available())
	        throw new IllegalStateException("Vous ne pouvez pas installer un objet indisponible.");
	    
        i.setIs_placed(true);
        i.setIs_available(false);
	}
	
	private void updateItemLocation(Intervention intervention) {
		Item i = intervention.getItem();
		if(!intervention.getUnit().equals(i.getUnit()))
			i.setUnit(intervention.getUnit());
		if(!intervention.getRoom().equals(i.getRoom()))
			i.setRoom(intervention.getRoom());
	}
	
}
