package be.chrverviers.stockmanager.config;


import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableTransactionManagement
public class JDBCConfig {

	/**
	 * Bean version of the database source configuration that can be found in the application.properties
	 * @return
	 */
	@Bean
	public DataSource AS400DataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.ibm.as400.access.AS400JDBCDriver");
		dataSource.setUrl("jdbc:as400://hades/CCLIB");
		dataSource.setUsername("COLINCYR");
		dataSource.setPassword("walaxv32");
		return dataSource;
	}
}
