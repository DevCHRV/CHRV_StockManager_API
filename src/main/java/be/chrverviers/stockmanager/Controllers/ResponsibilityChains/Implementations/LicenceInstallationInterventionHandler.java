package be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.InterventionTypeEnum;
import be.chrverviers.stockmanager.Controllers.ResponsibilityChains.Interface.ResponsibilityChain;
import be.chrverviers.stockmanager.Domain.Models.Intervention;
import be.chrverviers.stockmanager.Repositories.ItemRepository;
import be.chrverviers.stockmanager.Repositories.LicenceRepository;

@Service
public class LicenceInstallationInterventionHandler extends ResponsibilityChain<Intervention>{
	
	@Autowired
	private JavaMailSender emailSender;
	
	@Autowired
	LicenceRepository licenceRepo;
	
	@Autowired
	ItemRepository itemRepo;
	
	@Override
	public void handle(Intervention request) {
        if (request.getType().getId() == InterventionTypeEnum.INSTALLATION_LICENCE.value) {
        	sendMail(request);
        	licenceRepo.detachAll(licenceRepo.findForItem(request.getItem()));
        	licenceRepo.attachAll(request.getLicences(), request.getItem());
			itemRepo.save(request.getItem());
        } else if (next != null) {
            next.handle(request);
        }		
	}
	
	private void sendMail(Intervention intervention) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("stockmanager@chrverviers.be");
		message.setTo("cyril.colin@chrverviers.be");
		message.setSubject("Intervention #"+intervention.getId()+"#");
		message.setText("Une intervention de type "+intervention.getType().getName()+" a été réalisée par:\n"+
		intervention.getUser().getFirstname()+" "+intervention.getUser().getLastname()+"\nEn date du: "+intervention.getExpectedDate()
		+"\nAvec pour description:\n"+intervention.getDescription());
		
		Runnable runnable = () -> { 
			this.emailSender.send(message);
		};
		Thread t = new Thread(runnable);
		t.start();
	}
}
