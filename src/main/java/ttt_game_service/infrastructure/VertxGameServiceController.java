package ttt_game_service.infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.StaticHandler;
import ttt_game_service.application.AccountAlreadyPresentException;
import ttt_game_service.application.GameAlreadyPresentException;
import ttt_game_service.application.GameService;
import ttt_game_service.application.GameServiceImpl;
import ttt_game_service.application.GameSessionEvent;
import ttt_game_service.application.PlayerSessionEventObserver;
import ttt_game_service.application.LoginFailedException;
import ttt_game_service.domain.InvalidJoinException;
import ttt_game_service.domain.InvalidMoveException;
import ttt_game_service.domain.TTTSymbol;

/**
*
* TicTacToe Game Service controller
* 
* Remark: the API is HTTP, not REST
* 
* @author aricci
*
*/
public class VertxGameServiceController extends VerticleBase  {

	private int port;
	static Logger logger = Logger.getLogger("[TicTacToe Backend]");

	/* Ref. to the application layer */
	private GameService gameService;
	
	public VertxGameServiceController(GameService service, int port) {
		this.port = port;
		logger.setLevel(Level.INFO);
		this.gameService = service;

	}

	public Future<?> start() {
		logger.log(Level.INFO, "TTT Game Service initializing...");
		HttpServer server = vertx.createHttpServer();

		/* API routes */
		
		/* Note: This API is not RESTful */
		
		Router router = Router.router(vertx);
		router.route(HttpMethod.POST, "/api/registerUser").handler(this::registerUser);
		router.route(HttpMethod.POST, "/api/login").handler(this::login);
		router.route(HttpMethod.POST, "/api/createGame").handler(this::createNewGame);
		router.route(HttpMethod.POST, "/api/joinGame").handler(this::joinGame);
		router.route(HttpMethod.POST, "/api/makeAMove").handler(this::makeAMove);
		this.handleEventSubscription(server, "/api/events");

		/* static files */
		
		router.route("/public/*").handler(StaticHandler.create());
		
		/* start the server */
		
		var fut = server
			.requestHandler(router)
			.listen(port);
		
		fut.onSuccess(res -> {
			logger.log(Level.INFO, "TTT Game Service ready - port: " + port);
		});

		return fut;
	}


	/* List of handlers mapping the API */
	
	/**
	 * 
	 * Register a new user
	 * 
	 * @param context
	 */
	protected void registerUser(RoutingContext context) {
		logger.log(Level.INFO, "RegisterUser request");
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			logger.log(Level.INFO, "Payload: " + userInfo);
			var userName = userInfo.getString("userName");
			var password = userInfo.getString("password");
			var reply = new JsonObject();
			try {
				gameService.registerUser(userName, password);
				reply.put("result", "ok");
				sendReply(context.response(), reply);
			} catch (AccountAlreadyPresentException ex) {
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}
		});
	}

	/**
	 * 
	 * Login a user
	 * 
	 * It creates a User Session
	 * 
	 * @param context
	 */
	protected void login(RoutingContext context) {
		logger.log(Level.INFO, "Login request");
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			logger.log(Level.INFO, "Payload: " + userInfo);
			var userName = userInfo.getString("userName");
			var password = userInfo.getString("password");
			var reply = new JsonObject();
			try {
				var session = gameService.login(userName, password);
				reply.put("result", "ok");
				reply.put("sessionId", session.getSessionId());
				sendReply(context.response(), reply);
			} catch (LoginFailedException ex) {
				reply.put("result", "login-failed");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}			
		});
	}
	
	
	/**
	 * 
	 * Create a New Game - by users logged in (with a UserSession)
	 * 
	 * @param context
	 */
	protected void createNewGame(RoutingContext context) {
		logger.log(Level.INFO, "CreateNewGame request - " + context.currentRoute().getPath());
		context.request().handler(buf -> {
			JsonObject userInfo = buf.toJsonObject();
			logger.log(Level.INFO, "Payload: " + userInfo);
			var sessionId = userInfo.getString("sessionId");
			var reply = new JsonObject();
			try {
				var session = gameService.getUserSession(sessionId);
				var gameId = userInfo.getString("gameId");
				session.createNewGame(gameId);
				reply.put("result", "ok");
				sendReply(context.response(), reply);
			} catch (GameAlreadyPresentException ex) {
				reply.put("result", "error");
				reply.put("error", "game-already-present");
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}			
		});		
	}

	/**
	 * 
	 * Join a Game - by user logged in (with a UserSession)
	 * 
	 * It creates a PlayerSession
	 * 
	 * @param context
	 */
	protected void joinGame(RoutingContext context) {
		logger.log(Level.INFO, "JoinGame request - " + context.currentRoute().getPath());
		context.request().handler(buf -> {
			JsonObject joinInfo = buf.toJsonObject();
			logger.log(Level.INFO, "Join info: " + joinInfo);
			
			String sessionId = joinInfo.getString("sessionId");
			String gameId = joinInfo.getString("gameId");
			String symbol = joinInfo.getString("symbol");

			var reply = new JsonObject();
			try {
				var session = gameService.getUserSession(sessionId);
				var playerSession = session.joinGame(gameId, symbol.equals("X") ? TTTSymbol.X : TTTSymbol.O, new VertxPlayerSessionEventObserver(vertx.eventBus()));
				reply.put("playerSessionId", playerSession.getId());
				reply.put("result", "ok");
				sendReply(context.response(), reply);
			} catch (InvalidJoinException  ex) {
				reply.put("result", "error");
				reply.put("error", ex.getMessage());
				sendReply(context.response(), reply);
			} catch (Exception ex1) {
				sendError(context.response());
			}			
		});
	}
	
	/**
	 * 
	 * Make a move in a game - by players playing a game (with a PlayerSession)
	 * 
	 * @param context
	 */
	protected void makeAMove(RoutingContext context) {
		logger.log(Level.INFO, "MakeAMove request - " + context.currentRoute().getPath());
		context.request().handler(buf -> {
			var  reply = new JsonObject();
			try {
				JsonObject moveInfo = buf.toJsonObject();
				logger.log(Level.INFO, "move info: " + moveInfo);
				
				String playerSessionId = moveInfo.getString("playerSessionId");
				int x = Integer.parseInt(moveInfo.getString("x"));
				int y = Integer.parseInt(moveInfo.getString("y"));
				var ps = gameService.getPlayerSession(playerSessionId);
				ps.makeMove(x, y);				
				reply.put("result", "accepted");
				sendReply(context.response(), reply);
			} catch (InvalidMoveException ex) {
				reply.put("result", "invalid-move");
				sendReply(context.response(), reply);				
			} catch (Exception ex1) {
				reply.put("result", ex1.getMessage());
				try {
					sendReply(context.response(), reply);
				} catch (Exception ex2) {
					sendError(context.response());
				}				
			}
		});
	}


	/* Handling subscribers using web sockets */
	
	protected void handleEventSubscription(HttpServer server, String path) {
		server.webSocketHandler(webSocket -> {
			logger.log(Level.INFO, "New TTT subscription accepted.");

			/* 
			 * 
			 * Receiving a first message including the id of the game
			 * to observe 
			 * 
			 */
			webSocket.textMessageHandler(openMsg -> {
				logger.log(Level.INFO, "For game: " + openMsg);
				JsonObject obj = new JsonObject(openMsg);
				String playerSessionId = obj.getString("playerSessionId");
				
				
				/* 
				 * Subscribing events on the event bus to receive
				 * events concerning the game, to be notified 
				 * to the frontend using the websocket
				 * 
				 */
				EventBus eb = vertx.eventBus();
				
				eb.consumer(playerSessionId, msg -> {
					JsonObject ev = (JsonObject) msg.body();
					logger.log(Level.INFO, "Event: " + ev.encodePrettily());
					webSocket.writeTextMessage(ev.encodePrettily());
				});
				
				var ps = gameService.getPlayerSession(playerSessionId);
				var en = ps.getPlayerSessionEventNotifier();
				en.enableEventNotification(playerSessionId);
								
			});
		});
	}
	
	/* Aux methods */
	

	private void sendReply(HttpServerResponse response, JsonObject reply) {
		response.putHeader("content-type", "application/json");
		response.end(reply.toString());
	}
	
	private void sendError(HttpServerResponse response) {
		response.setStatusCode(500);
		response.putHeader("content-type", "application/json");
		response.end();
	}


}
