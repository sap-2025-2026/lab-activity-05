package ttt_game_service.application;

import exagonal.InBoundPort;
import ttt_game_service.domain.Account;
import ttt_game_service.domain.InvalidJoinException;
import ttt_game_service.domain.TTTSymbol;
import ttt_game_service.domain.UserId;

/**
 * 
 * Interface of the Game Service at the application layer
 * 
 */
@InBoundPort
public interface GameService  {

	/**
     * 
     * Register a new user.
     * 
     * @param userName
     * @param password
     * @return
     * @throws AccountAlreadyPresentException
     */
	Account registerUser(String userName, String password) throws AccountAlreadyPresentException;

	/**
	 * 
	 * Login an existing user.
	 * 
	 * @param userName
	 * @param password
	 * @return
	 * @throws LoginFailedException
	 */
	UserSession login(String userName, String password) throws LoginFailedException;
	
	/**
	 * 
	 * Retrieve an existing user session.
	 * 
	 * @param id
	 * @return
	 */
	UserSession getUserSession(String sessionId);
	
	/**
	 * 
	 * Retrieve an existing player session.
	 * 
	 * @param id
	 * @return
	 */
	public PlayerSession getPlayerSession(String sessionId);
	
	/**
	 * 
	 * Create a game -- called by a UserSession (logged in user) 
     *
	 * @param gameId -- name of the game, to be created
	 *  
	 * @throws GameAlreadyPresentException
	 */
	void createNewGame(String gameId) throws GameAlreadyPresentException;
	
	/**
	 * 
	 * Join a game -- called by a UserSession (logged in user), creates a new PlayerSession
	 * 
	 * @param userId -- id of the user (player)
	 * @param gameId -- id of the game to be joined
	 * @param symbol -- symbol to be used (X, O)
	 * @param notifier -- observer of the events 
	 * @return
	 * @throws InvalidJoinException
	 */
	PlayerSession joinGame(UserId userId, String gameId, TTTSymbol symbol, PlayerSessionEventObserver observer) throws InvalidJoinException;


	/**
	 * 
	 * Bind the specific account repository implementation
	 * 
	 * @param repo
	 */
    void bindAccountRepository(AccountRepository repo);

    
	/**
	 * 
	 * Bind the specific game repository implementation
	 * 
	 * @param repo
	 */
    void bindGameRepository(GameRepository repo);
    
}
