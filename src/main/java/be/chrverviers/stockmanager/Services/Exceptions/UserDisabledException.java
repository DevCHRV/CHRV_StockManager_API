package be.chrverviers.stockmanager.Services.Exceptions;

import javax.naming.AuthenticationException;

public class UserDisabledException extends AuthenticationException {


	public UserDisabledException() {
		super();
	}
	
	/**
	 * Constructs a <code>UsernameNotFoundException</code> with the specified message.
	 * @param msg the detail message.
	 */
	public UserDisabledException(String msg) {
		super(msg);
	}

}
