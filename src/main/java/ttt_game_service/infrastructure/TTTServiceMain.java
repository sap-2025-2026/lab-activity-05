package ttt_game_service.infrastructure;

import io.vertx.core.Vertx;
import ttt_game_service.application.GameServiceImpl;

public class TTTServiceMain {

	static final int BACKEND_PORT = 8080;

	public static void main(String[] args) {
		
		var service = new GameServiceImpl();
		
		/* injecting adapters for persistency */
		
		service.bindAccountRepository(new InMemoryAccountRepository());
		service.bindGameRepository(new InMemoryGameRepository());
		
		var vertx = Vertx.vertx();
		var server = new VertxGameServiceController(service, BACKEND_PORT);
		vertx.deployVerticle(server);	
	}

}

