package com.mycompany.myproject;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

public class MainVerticle extends Verticle {

	@Override
	public void start() {
		
		// register handlers
		vertx.eventBus().registerHandler("sum", new SumHandler());
		vertx.eventBus().registerHandler("square", new SquareHandler());
		vertx.eventBus().registerHandler("inc", new IncHandler());
		vertx.eventBus().registerHandler("range", new RangeHandler());
		
		
		
		// register web handlers
		RouteMatcher rm = new RouteMatcher();
				
		rm.post("/sum", new RequestBodyHandoffHandler(vertx, "sum"));
		rm.post("/square", new RequestBodyHandoffHandler(vertx, "square"));
		rm.post("/inc", new RequestBodyHandoffHandler(vertx, "inc"));
		rm.post("/range", new RequestBodyHandoffHandler(vertx, "range"));
		
		vertx.createHttpServer().requestHandler(rm).listen(8080);
	}
	
	/**
	 * Get the body of the current (post) request and hand it off to
	 * some buss-address. register for reply and then pass it back as http response.
	 * @author Eckart
	 *
	 */
	public static class RequestBodyHandoffHandler implements Handler<HttpServerRequest> {

		private static final Logger LOG = Logger.getLogger(RequestBodyHandoffHandler.class.getName());
		private Vertx vertxInstance;
		private String targetAddress;
		
		public RequestBodyHandoffHandler(Vertx vertx, String address) {
			this.vertxInstance = vertx;
			this.targetAddress = address;
		}
		
		@Override
		public void handle(final HttpServerRequest r) {
			r.bodyHandler(new Handler<Buffer>(){
				public void handle(Buffer body) {
					vertxInstance.eventBus().send(targetAddress, body, new Handler<Message<Buffer>>(){
						public void handle(Message<Buffer> event) {
							LOG.log(Level.FINE, "replying with: " + event.body());
							r.response().end(event.body());								
						}						
					});
					
				}
				
			});
			
		}
		
	}
	
}
