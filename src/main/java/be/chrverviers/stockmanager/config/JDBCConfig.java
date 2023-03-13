package be.chrverviers.stockmanager.config;


import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
public class JDBCConfig {
	
    @Autowired
    Environment env;
    
    @Value("${stockmanager.production}")
    private Boolean isProd;
	
    @Value("${stockmanager.db.drivername}")
    private String drivername;
    
    @Value("${stockmanager.db.connection.string}")
    private String connectionString;
    
    @Value("${stockmanager.db.username}")
    private String username;
    
    @Value("${stockmanager.db.password}")
    private String password;
    
    @Value("${stockmanager.db.debug.drivername}")
    private String debugDriverName;
    
    @Value("${stockmanager.db.debug.connection.string}")
    private String debugConnectionString;
    
    @Value("${stockmanager.db.debug.username}")
    private String debugUsername;
    
    @Value("${stockmanager.db.debug.password}")
    private String debugPassword;
        
    HikariDataSource dataSource;

	/**
	 * Bean version of the database source configuration that can be found in the application.properties
	 * @return
	 */
	@Bean
	public DataSource AS400DataSource() {
		//Under the hood, and the reason we're using an HikariDataSource, it will create a Connection Pool
		//Allowing Spring's Transaction to NOT close the connection to the DB after each request
		//(and thus forcing the next request to fully reopen the connection, which caused about 600ms delay)
		if(isProd) {
			this.dataSource = new HikariDataSource();
			dataSource.setDriverClassName(drivername);
			dataSource.setJdbcUrl(connectionString);
			dataSource.setUsername(username);
			dataSource.setPassword(password);
			//Because we're using a connection pool, we also need to specify a max Lifetime that is shorter or equal to that of the AS400
			//I think I remember that someone told me that the AS400 closed connections after 5 minutes
			dataSource.setMaxLifetime(1000*60*4);
			return dataSource;
		} else {
			this.dataSource = new HikariDataSource();
			dataSource.setDriverClassName(debugDriverName);
			dataSource.setJdbcUrl(debugConnectionString);
			dataSource.setUsername(debugUsername);
			dataSource.setPassword(debugPassword);
			//Because we're using a connection pool, we also need to specify a max Lifetime that is shorter or equal to that of the AS400
			//I think I remember that someone told me that the AS400 closed connections after 5 minutes
			dataSource.setMaxLifetime(1000*60*4);
			return dataSource;
		}
	}
}
