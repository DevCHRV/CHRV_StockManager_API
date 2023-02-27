package be.chrverviers.stockmanager.Services;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import be.chrverviers.stockmanager.Domain.Models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final static String SECRET_KEY = "79244226452948404D6251655468576D5A7134743777217A25432A462D4A614E";
	
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}
	
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	public String generateToken(User user) {
		return generateToken(new HashMap<>(), user);
	}
	
	public String generateToken(Map<String, Object> extraClaims, User user) {
		return Jwts
				.builder()
				.setClaims(extraClaims)
				.addClaims(Collections.singletonMap("id", user.getId()))
				.addClaims(Collections.singletonMap("firstname", user.getFirstname()))
				.addClaims(Collections.singletonMap("lastname", user.getLastname()))
				.addClaims(Collections.singletonMap("roles", user.getAuthorities()))
				.setSubject(user.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24)))
				.signWith(getSigninKey(), SignatureAlgorithm.HS256)
				.compact();
	}
	
	public boolean isTokenValid(String token, UserDetails user) {
		String username = extractUsername(token);
		return (username.equals(user.getUsername())&&!isTokenExpired(token));
	}
	
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigninKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private Key getSigninKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		return Keys.hmacShaKeyFor(keyBytes);
	}

}
