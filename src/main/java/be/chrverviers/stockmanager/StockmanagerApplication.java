package be.chrverviers.stockmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ldap.query.LdapQuery;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
//URL Websphere: protocol://server:port/artifactId/
//URL Websphere: https://localhost:9443/stockmanager/api/Home/Hello

@SpringBootApplication
public class StockmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockmanagerApplication.class, args);
	}
}

//ADD => Créer EAR => Mettre sur le serveur => Onglet serveur drag-and-drop l'EAR sur le serveur