package be.chrverviers.stockmanager.config;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import be.chrverviers.stockmanager.Services.JwtService;
import be.chrverviers.stockmanager.Services.Exceptions.UserDisabledException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	@Autowired
	private JwtService jwtService;
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String authHeader = request.getHeader("Authorization");
			
			Cookie cookie = WebUtils.getCookie(request, "auth.jwt_token");

			if(cookie==null) {
				filterChain.doFilter(request, response);
				return;
			}
			
			String jwt = cookie.getValue();
			String username = jwtService.extractUsername(jwt);

			//We check that the username exist in the token, and that there is no ongoing connection
			if(username !=null&& SecurityContextHolder.getContext().getAuthentication()==null) {
				UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
		        if(!userDetails.isEnabled())
		        	throw new UserDisabledException("Vos droits d'accès ont été révoqués. Si vous pensez que c'est une erreur, contactez un administrateur.");
		        
				if(jwtService.isTokenValid(jwt, userDetails)) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							userDetails, userDetails.getPassword(), userDetails.getAuthorities());
					authToken.setDetails(
							new WebAuthenticationDetailsSource().buildDetails(request)
					);
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
			
			filterChain.doFilter(request, response);
		} catch(JwtException e) {
			this.clearCookies(response);
			response.sendError(401, e.getMessage());
		} catch(IllegalArgumentException e) {
			this.clearCookies(response);
			response.sendError(401, e.getMessage());
		} catch(UsernameNotFoundException e) {
			this.clearCookies(response);
			response.sendError(401, e.getMessage());
		} catch (UserDisabledException e) {
			this.clearCookies(response);
			response.sendError(401, e.getMessage());
		}
	}
	
	private void clearCookies(HttpServletResponse  response) {
        Cookie cookie = new Cookie("auth.jwt_token", null);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
	}
}
