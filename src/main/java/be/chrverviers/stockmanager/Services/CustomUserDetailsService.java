package be.chrverviers.stockmanager.Services;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.UserRepository;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class CustomUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	// 
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email: "+ username));
        Set<GrantedAuthority> authorities = user
                .getRoles()
                .stream()
                .map((role) -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
        		user.getUsername(),
        		null,
                //user.getPassword(),
                true,
                true,
                true,
                true,
                authorities
                );
    }
    
    /**
     * Method that is called when the user is successfully authenticated by the LDAP server but doesn't exist in our
     * database yet.
     * This method will query the LDAP
     * @param username
     * @return
     */
    public int buildUser(String username) {
    	//Create a query to the LDAP Server
    	LdapQuery query = query()
    			.base("OU=Informatique,OU=Administratifs,OU=Chrv_Users,DC=chplt,DC=be")
    	        .attributes("sn","givenname")
    	        .where("objectClass").is("person").and("objectCategory").is("User")
    	        .and("sAMAccountName").is(username);
    	//Create a Mapper that will return a new User from the query
		AttributesMapper<User> mapper = new AttributesMapper<User>() {
	           public User mapFromAttributes(Attributes attrs) throws NamingException {
	        	   return new User(username.toUpperCase(), (String) attrs.get("sn").get(),(String) attrs.get("givenname").get());
	           }
	         };
	    //For some reason .search can only return a list so...
    	List<User> list = ldapTemplate.search(query, mapper);
    	//Here we save an return the new user
    	return userRepository.create(list.get(0));
    }
}
