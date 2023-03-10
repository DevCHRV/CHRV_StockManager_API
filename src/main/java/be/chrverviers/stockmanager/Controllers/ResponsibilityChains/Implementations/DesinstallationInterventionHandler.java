package be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.InterventionTypeEnum;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.ResponsibilityChain;
import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Repositories.ItemRepository;

@Service
public class DesinstallationInterventionHandler extends ResponsibilityChain<Intervention> {

	@Autowired
	ItemRepository itemRepo;
	
	@Override
	public void handle(Intervention request) {
        if (request.getType().getId() == InterventionTypeEnum.DESINSTALLATION.value) {
        	uninstallItem(request.getItem());
        	updateItemLocation(request);
        	itemRepo.save(request.getItem());
        } else if (next != null) {
            next.handle(request);
        }		
	}
	
	private void uninstallItem(Item i) {
		if(i.getIsAvailable())
	        throw new IllegalStateException("Vous ne pouvez désinstaller un objet disponible");
		
		i.setIsPlaced(false);
		i.setIsAvailable(true);
	}
	
	private void updateItemLocation(Intervention intervention) {
		Item i = intervention.getItem();
		if(!intervention.getRoom().equals(i.getRoom()))
			i.setRoom(intervention.getRoom());
	}
	
}
