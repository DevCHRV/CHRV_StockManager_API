package be.chrverviers.stockmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//URL Websphere: protocol://server:port/artifactId/
//URL Websphere: https://localhost:9443/stockmanager/api/Home/Hello
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class StockmanagerApplication {
	
	public static void main(String[] args) {
		
		SpringApplication.run(StockmanagerApplication.class, args);
	}
}

//ADD => Créer EAR => Mettre sur le serveur => Onglet serveur drag-and-drop l'EAR sur le serveur