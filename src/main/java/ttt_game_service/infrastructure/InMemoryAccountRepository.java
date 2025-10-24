package ttt_game_service.infrastructure;

import java.util.HashMap;
import java.util.logging.Logger;
import exagonal.Adapter;
import ttt_game_service.application.AccountRepository;
import ttt_game_service.domain.Account;

/**
 * 
 * A simple in-memory implementation of the AccountRepository - no persistence.
 * 
 */
@Adapter
public class InMemoryAccountRepository implements AccountRepository {
	static Logger logger = Logger.getLogger("[AccountRepo]");

	private HashMap<String, Account> userAccounts;
	
	public InMemoryAccountRepository() {
		userAccounts = new HashMap<>();
	}
	
	public void addAccount(Account account) {
		userAccounts.put(account.getId(), account);
	}
	
	
	public boolean isPresent(String userName) {
		return userAccounts.containsKey(userName);
	}
	
	/**
	 * 
	 * Authenticate
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	public boolean isValid(String userName, String password) {
		return (userAccounts.containsKey(userName) && userAccounts.get(userName).getPassword().equals(password));
	}
	
}
