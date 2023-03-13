package be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.InterventionTypeEnum;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.ResponsibilityChain;
import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Repositories.RoomRepository;

@Service
public class LoanInterventionHandler extends ResponsibilityChain<Intervention> {

	@Autowired
	ItemRepository itemRepo;
	
	@Autowired
	RoomRepository roomRepo;
	
	@Override
	public void handle(Intervention request) {
        if (request.getType().getId() == InterventionTypeEnum.LOAN.value) {
        	loanItem(request.getItem());
        	updateItemLocation(request);
        	itemRepo.save(request.getItem());
        } else if (next != null) {
            next.handle(request);
        }		
	}
	
	private void loanItem(Item i) {
	    if(!i.getIsAvailable())
	        throw new IllegalStateException("Vous ne pouvez pas prêter un objet indisponible.");
	    i.setIsAvailable(false);
	}
	
	private void updateItemLocation(Intervention intervention) {
		Item i = intervention.getItem();
		if(!intervention.getRoom().equals(i.getRoom()))
			i.setRoom(intervention.getRoom());
	}
	
}
