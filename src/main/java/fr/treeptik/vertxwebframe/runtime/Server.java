package fr.treeptik.vertxwebframe.runtime;

import fr.treeptik.vertxwebframe.controller.SuperController;
import fr.treeptik.vertxwebframe.service.proxy.ProxyService;
import fr.treeptik.vertxwebframe.service.verticle.ServiceVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Router;
import io.vertx.ext.apex.handler.TemplateHandler;
import io.vertx.ext.apex.templ.ThymeleafTemplateEngine;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Server  {

	private final static Logger logger = LoggerFactory.getLogger(Server.class);

	public static Map<String, Object> serviceRepo = new HashMap<String, Object>();
	public static void main(String[] args) throws Exception {

		logger.info("Welcome Vertx Web Frame - Start main server ");
		
		Vertx vertx = Vertx.vertx();
		
		// Init WEB
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create();
		
		// Load conf
		Path path;
		try{
			path = Paths.get(ClassLoader.getSystemResource("application.conf").toURI());
		}catch(Exception e){
			throw new IllegalAccessError("Can't find classpath:/application.conf");
		}

		// 
		//Deploy Service
		//
		Consumer<String> loadService = s-> {
			
			String[] split = s.split(" ");
			String clazz = split[1];

			try {
				ServiceVerticle serviceVerticle = new ServiceVerticle(clazz);
				vertx.deployVerticle(serviceVerticle);
				logger.debug("Deploy new Service : " + serviceVerticle.getClass().getName());
			
				// TODO : HACK : Create interface name
				String interfaceName = serviceVerticle.getRealService().getClass().getInterfaces()[0].getName();
				serviceRepo.put(interfaceName, ProxyService.getInstance(vertx).createProxy(serviceVerticle.getRealService().getClass()));
				
			} catch (Exception e) {
				throw new IllegalArgumentException("Can't load service ", e);
			}
		};
		Files.readAllLines(path).stream().filter(p -> p.startsWith("service")).forEach(loadService);		
		
		// 
		//Deploy Controller
		//
		Consumer<String> loadController = s-> {
			
			String[] split = s.split(" ");
			String clazz = split[1];
			try {
				SuperController newInstance = (SuperController) Server.class.getClassLoader().loadClass(clazz).newInstance();
				newInstance.server = server;
				newInstance.router = router;
				
				// Inject Service
				Field[] declaredFields = newInstance.getClass().getDeclaredFields();
				List<Field> fileds = Arrays.asList(declaredFields);
				fileds.stream().filter( f -> serviceRepo.containsKey(f.getType().getName())).forEach(f->{
					f.setAccessible(true);
					try {
						logger.debug("Inject service into controller");
						f.set(newInstance, serviceRepo.get(f.getType().getName()));
					} catch (Exception e) {
						throw new IllegalArgumentException("Wrong Service type  ", e);
					}
					
				});
				logger.debug("Deploy new Controller : " + newInstance.getClass().getName());
				vertx.deployVerticle(newInstance);
			} catch (Exception e) {
				throw new IllegalArgumentException("Can't load controller ", e);
			}
			
			
		};
		Files.readAllLines(path).stream().filter(p -> p.startsWith("controller")).forEach(loadController);
		
		

		server.requestHandler(router::accept).listen(8080);
		router.route().handler(
				TemplateHandler.create(engine, "page", "text/html"));
	}
	

}
