package ttt_game_service.domain;

import ddd.Entity;

/**
 * 
 * Modelling a user account. 
 * 
 */
public class Account implements Entity<String> {
	
	private String userName; /* this is the id */
	private String password;
	
	public Account(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	@Override
	public String getId() {
		return userName;
	}
	
}
