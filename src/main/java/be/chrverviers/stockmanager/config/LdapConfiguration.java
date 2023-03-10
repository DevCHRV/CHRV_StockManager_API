package be.chrverviers.stockmanager.config;

import javax.naming.directory.SearchControls;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class LdapConfiguration {

    @Autowired
    Environment env;
    
    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.username}")
    private String ldapUserDn;

    @Value("${spring.ldap.password}")
    private String ldapPassword;
    
    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setUserDn(ldapUserDn);
        contextSource.setPassword(ldapPassword);
        contextSource.setPooled(true);
        contextSource.setCacheEnvironmentProperties(false);
        contextSource.setReferral("follow");
        return contextSource;
    }
    
    @Bean
    public LdapTemplate ldapTemplate() {
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource());
        ldapTemplate.setContextSource(contextSource());
        ldapTemplate.setIgnorePartialResultException(true);
        ldapTemplate.setDefaultCountLimit(0); // set page size to 5000
        ldapTemplate.setDefaultSearchScope(SearchControls.SUBTREE_SCOPE);
        return ldapTemplate;
    }

}
