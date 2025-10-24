package ttt_game_service.application;

import java.util.HashMap;

import ddd.Aggregate;
import ddd.Repository;
import exagonal.OutBoundPort;
import ttt_game_service.domain.Game;

/**
 * 
 * Games Repository
 * 
 */
@OutBoundPort
public interface GameRepository extends Repository {

	void addGame(Game game);
	
	boolean isPresent(String gameId);
	
	Game getGame(String gameId);

}
