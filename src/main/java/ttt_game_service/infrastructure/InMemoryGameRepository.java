package ttt_game_service.infrastructure;

import java.util.HashMap;

import ddd.Aggregate;
import ddd.Repository;
import exagonal.OutBoundPort;
import ttt_game_service.application.GameRepository;
import ttt_game_service.domain.Game;

/**
 * 
 * Games Repository
 * 
 */
@OutBoundPort
public class InMemoryGameRepository implements GameRepository {

	private HashMap<String, Game> games;

	public InMemoryGameRepository() {
		games = new HashMap<>();
	}
	
	public void addGame(Game game) {
		games.put(game.getId(), game);
		
	}
	
	public boolean isPresent(String gameId) {
		return games.containsKey(gameId);
	}
	
	public Game getGame(String gameId) {
		return games.get(gameId);
	}


}
