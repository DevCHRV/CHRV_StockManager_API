package be.chrverviers.stockmanager.Services;

import java.util.LinkedList;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapOperationsCallback;
import org.springframework.ldap.core.support.SingleContextSource;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.SearchScope;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import be.chrverviers.stockmanager.Domain.Models.Role;
import be.chrverviers.stockmanager.Domain.Models.User;
import be.chrverviers.stockmanager.Repositories.RoleRepository;
import be.chrverviers.stockmanager.Repositories.UserRepository;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service
public class CustomUserDetailsService implements UserDetailsService{
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username or email: "+ username));
        user.setRoles(roleRepository.findForUser(user));
        
        return user;
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
    	        .attributes("sn","givenname","mail")
    	        .where("objectClass").is("person").and("objectCategory").is("User")
    	        .and("sAMAccountName").is(username);
    	//Create a Mapper that will return a new User from the query
		AttributesMapper<User> mapper = new AttributesMapper<User>() {
	           public User mapFromAttributes(Attributes attrs) throws NamingException {
	        	   return new User(username.toUpperCase(), (String) attrs.get("sn").get(),(String) attrs.get("givenname").get(), (String) attrs.get("mail").get());
	           }
	         };
	    //For some reason .search can only return a list so...
    	List<User> list = ldapTemplate.search(query, mapper);
    	//Here we create the new user
    	User user = list.get(0);
    	user.setId(userRepository.create(list.get(0)));
    	return user.getId();
    }
    
    /*
     * 
     */
    
    public List<User> getLDAPUsers(){

    	//Create a Mapper that will return a new User from the query
		AttributesMapper<User> mapper = new AttributesMapper<User>() {
	           public User mapFromAttributes(Attributes attrs) throws NamingException {
	        	   User user = new User();
	        	   user.setUsername(attrs.get("sAMAccountName")!=null ? ((String) attrs.get("sAMAccountName").get()).toUpperCase() : "No Username");
	        	   user.setFirstname(attrs.get("sn")!=null ? (String) attrs.get("sn").get() : "Pas de nom");
	        	   user.setLastname(attrs.get("givenname")!=null ? (String) attrs.get("givenname").get() : "Pas de prénom");
	        	   user.setEmail(attrs.get("mail")!=null ? (String) attrs.get("mail").get() : "Pas d'email");
	        	   return user;
	           }
         };
	         
         PagedResultsDirContextProcessor processor = new PagedResultsDirContextProcessor(1000);
         SearchControls searchControls = new SearchControls();
         searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
         searchControls.setReturningAttributes(new String[] {"sAMAccountName", "sn", "givenname", "mail"});
         
         return SingleContextSource.doWithSingleContext(
                 ldapTemplate.getContextSource(), new LdapOperationsCallback<List<User>>() {
             @Override
             public List<User> doWithLdapOperations(LdapOperations operations) {
                 List<User> result = new LinkedList<User>();
                 do {
                     List<User> oneResult = operations.search("DC=chplt,DC=be",
                    		 "(&(objectCategory=User)(objectClass=person)(employeeid=*))"
                    		 , searchControls, mapper, processor);
                     result.addAll(oneResult);
                 } while (processor.hasMore());
                 return result;
             }
         });
	    
    }
         
   
}
