package be.chrverviers.stockmanager.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;

import io.jsonwebtoken.ExpiredJwtException;

@ControllerAdvice
public class JwtExceptionHandler extends HandlerExceptionResolverComposite{
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(ExpiredJwtException e, HttpServletRequest request){
        return new ResponseEntity<>("Votre session a expiré, veuillez vous reconnecter.", HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request){
        return new ResponseEntity<>("Vous n'avez pas l'autorisation d'accéder à ceci.", HttpStatus.FORBIDDEN);
    }
}
