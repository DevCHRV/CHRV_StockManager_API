package be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.InterventionTypeEnum;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.ResponsibilityChain;
import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Domain.Models.Item;
import be.chrverviers.stockmanager.Repositories.ItemRepository;

@Service
public class LoanInterventionHandler extends ResponsibilityChain<Intervention> {

	@Autowired
	ItemRepository itemRepo;
	
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
	    if(!i.getIs_available())
	        throw new IllegalStateException("Vous ne pouvez pas prêter un objet indisponible.");
	    i.setIs_available(false);
	}
	
	private void updateItemLocation(Intervention intervention) {
		Item i = intervention.getItem();
		i.setUnit("Prêt");
		i.setRoom("");
	}
	
}
