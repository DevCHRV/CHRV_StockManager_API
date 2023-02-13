package be.chrverviers.stockmanager.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.config.ldap.LdapPasswordComparisonAuthenticationManagerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.server.UnboundIdContainer;

import be.chrverviers.stockmanager.Repositories.UserRepository;


@Configuration
public class ApplicationConfig {
	
	@Autowired
	private UserRepository userRepository;
	
	public ApplicationConfig(UserRepository repo) {
		this.userRepository = repo;
	}
		  
	ContextSource contextSource(UnboundIdContainer container) {
		return new DefaultSpringSecurityContextSource("ldap://chplt.be");
	}
	
	@Bean
	AuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource) {
		LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(
				contextSource);
		//DON'T TOUCH THIS MAGIC LINE
		factory.setUserSearchFilter("(&(objectCategory=User)(objectClass=person)(sAMAccountName={0}))");
		//HERE YOU MAP THE AUTHORIZED FOLDER FROM THE ROOT OF THE ACTIVE DIRECTORY
		//DON'T REMOVE THE DOMAINE (DC)
		factory.setUserSearchBase("OU=Informatique,OU=Administratifs,OU=Chrv_Users,DC=chplt,DC=be");
		return factory.createAuthenticationManager();
	}
	
    @Bean
    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
    	return new ActiveDirectoryLdapAuthenticationProvider("chplt.be", "ldap://chplt.be", "dc=chplt,dc=be");
    }
    
    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsService() {
			
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				return userRepository.findByUsername(username)
						.orElseThrow(()->new UsernameNotFoundException("User with these credentials not found"));
			}
		};
	}
}
