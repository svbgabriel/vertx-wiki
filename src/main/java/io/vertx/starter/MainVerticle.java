package io.vertx.starter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;

public class MainVerticle extends AbstractVerticle {

	private JDBCClient dbClient;
	private FreeMarkerTemplateEngine templateEngine;

	private static final String SQL_CREATE_PAGES_TABLE = "create table if not exists Pages (Id integer identity primary key, Name varchar(255) unique, Content clob)";
	private static final String SQL_GET_PAGE = "select Id, Content from Pages where Name = ?";
	private static final String SQL_CREATE_PAGE = "insert into Pages values (NULL, ?, ?)";
	private static final String SQL_SAVE_PAGE = "update Pages set Content = ? where Id = ?";
	private static final String SQL_ALL_PAGES = "select Name from Pages";
	private static final String SQL_DELETE_PAGE = "delete from Pages where Id = ?";

	private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

	private Future<Void> prepareDatabase() {
		Promise<Void> promise = Promise.promise();

		dbClient = JDBCClient.createShared(vertx, new JsonObject().put("url", "jdbc:hsqldb:file:db/wiki")
				.put("driver_class", "org.hsqldb.jdbcDriver").put("max_pool_size", 30));

		dbClient.getConnection(ar -> {
			if (ar.failed()) {
				LOGGER.error("Could not open a database connection", ar.cause());
				promise.fail(ar.cause());
			} else {
				SQLConnection connection = ar.result();
				connection.execute(SQL_CREATE_PAGES_TABLE, create -> {
					connection.close();
					if (create.failed()) {
						LOGGER.error("Database preparation error", create.cause());
						promise.fail(create.cause());
					} else {
						promise.complete();
					}
				});
			}
		});

		return promise.future();
	}

	private Future<Void> startHttpServer() {
		Promise<Void> promise = Promise.promise();
		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		router.get("/").handler(this::indexHandler);
		router.get("/wiki/:page").handler(this::pageRenderingHandler);
		router.post().handler(BodyHandler.create());
		router.post("/save").handler(this::pageUpdateHandler);
		router.post("/create").handler(this::pageCreateHandler);
		router.post("/delete").handler(this::pageDeletionHandler);

		templateEngine = FreeMarkerTemplateEngine.create(vertx);

		server.requestHandler(router).listen(8080, ar -> {
			if (ar.succeeded()) {
				LOGGER.info("HTTP server running on port 8080");
				promise.complete();
			} else {
				LOGGER.error("Could not start a HTTP server", ar.cause());
				promise.fail(ar.cause());
			}
		});

		return promise.future();
	}

	@Override
	public void start(Promise<Void> promise) {
		Future<Void> steps = prepareDatabase().compose(v -> startHttpServer());
		steps.setHandler(ar -> {
			if (ar.succeeded()) {
				promise.complete();
			} else {
				promise.fail(ar.cause());
			}
		});
	}

}