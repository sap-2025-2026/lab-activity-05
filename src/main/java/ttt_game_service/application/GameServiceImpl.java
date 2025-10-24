package ttt_game_service.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import ttt_game_service.domain.Account;
import ttt_game_service.domain.Game;
import ttt_game_service.domain.InvalidJoinException;
import ttt_game_service.domain.TTTSymbol;
import ttt_game_service.domain.UserId;

/**
 * 
 * Implementation of the Game Service entry point at the application layer
 * 
 * Designed as a modular monolith
 * 
 */
public class GameServiceImpl implements GameService {
	static Logger logger = Logger.getLogger("[Game Service]");

	private AccountRepository accountRepository;
    private GameRepository gameRepository;    
    
    private UserSessions userSessionRepository;
    private PlayerSessions playerSessionRepository;
    private int sessionCount;
    private int playerSessionCount;
    
    public GameServiceImpl(){
    	userSessionRepository = new UserSessions();
    	playerSessionRepository = new PlayerSessions();
    	sessionCount = 0;
    	playerSessionCount = 0;
    }
    
    /**
     * 
     * Register a new user.
     * 
     * @param userName
     * @param password
     * @return
     * @throws AccountAlreadyPresentException
     */
	public Account registerUser(String userName, String password) throws AccountAlreadyPresentException {
		logger.log(Level.INFO, "Register User: " + userName + " " + password);		
		if (accountRepository.isPresent(userName)) {
			throw new AccountAlreadyPresentException();
		}
		var account = new Account(userName, password);
		accountRepository.addAccount(account);
		return account;
	}

	/**
	 * 
	 * Login an existing user.
	 * 
	 * @param userName
	 * @param password
	 * @return
	 * @throws LoginFailedException
	 */
	public UserSession login(String userName, String password) throws LoginFailedException {
		logger.log(Level.INFO, "Login: " + userName + " " + password);
		if (!accountRepository.isValid(userName, password)) {
			throw new LoginFailedException();
		}		
		var id = new UserId(userName);
		sessionCount++;
		var sessionId = "user-session-" + sessionCount;
		var us = new UserSession(sessionId, id, this);
		userSessionRepository.addSession(us);
		return us;
	}

	/**
	 * 
	 * Retrieve an existing user session.
	 * 
	 * @param id
	 * @return
	 */
	public UserSession getUserSession(String sessionId) {
		return userSessionRepository.getSession(sessionId);
	}

	/**
	 * 
	 * Retrieve an existing player session.
	 * 
	 * @param id
	 * @return
	 */
	public PlayerSession getPlayerSession(String sessionId) {
		return playerSessionRepository.getSession(sessionId);
	}

	
	/* 
	 * 
	 * Create a game -- called by a UserSession  
	 * 
	 */
	public void createNewGame(String gameId) throws GameAlreadyPresentException {
		logger.log(Level.INFO, "create New Game " + gameId);
		var game = new Game(gameId);
		if (gameRepository.isPresent(gameId)) {
			throw new GameAlreadyPresentException();
		}
		gameRepository.addGame(game);
	}
	
	/*
	 * 
	 * Join a game -- called by a UserSession (logged in user), creates a new PlayerSession
	 * 
	 */
	public PlayerSession joinGame(UserId userId, String gameId, TTTSymbol symbol, PlayerSessionEventObserver notifier) throws InvalidJoinException  {
		logger.log(Level.INFO, "JoinGame - user: " + userId + " game: " + gameId + " symbol " + symbol);
		var game = gameRepository.getGame(gameId);
		game.joinGame(userId, symbol);	
		playerSessionCount++;
		var playerSessionId = "player-session-" + playerSessionCount;
		var ps = new PlayerSession(playerSessionId, userId, game, symbol);		
		ps.bindPlayerSessionEventNotifier(notifier);
		playerSessionRepository.addSession(ps);
		game.addGameObserver(ps);
		
		/* 
		 * Once both players (sessions) are ready to observe
		 * events, then we can start the game 
		 */
		if (game.isReadyToStart()) {
			game.startGame();
		}
		return ps;
	}
	
    public void bindAccountRepository(AccountRepository repo) {
    	this.accountRepository = repo;
    }

    public void bindGameRepository(GameRepository repo) {
    	this.gameRepository = repo;
    }
    
	


}
