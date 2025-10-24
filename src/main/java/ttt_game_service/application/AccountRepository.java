package ttt_game_service.application;

import ddd.Repository;
import exagonal.OutBoundPort;
import ttt_game_service.domain.Account;

/**
 * 
 * Interface of account repository
 * 
 */
@OutBoundPort
public interface AccountRepository extends Repository {

	void addAccount(Account account);
	
	boolean isPresent(String userName);
	
	boolean isValid(String userName, String password);
}
