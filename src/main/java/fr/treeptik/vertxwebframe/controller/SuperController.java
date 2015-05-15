package fr.treeptik.vertxwebframe.controller;

import fr.treeptik.vertxwebframe.annotation.RequestMapping;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.Router;

import java.lang.reflect.Method;
import java.util.HashMap;

public class SuperController extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(SuperController.class);
	
	public HttpServer server;
	public Router router;

	@Override
	public void start() throws Exception {

		Method[] methods = this.getClass().getMethods();
		for (Method method : methods) {

			if (method.isAnnotationPresent(RequestMapping.class)) {
				RequestMapping annotation = method
						.getAnnotation(RequestMapping.class);
				String url = annotation.url();

				router.route(url).handler(context -> {

					try {

						HashMap<String, Object> map = new HashMap<>();
						method.invoke(this, map);
						logger.debug("Controller values pass to view : " + map);
						map.forEach((k, v) -> context.put(k, v));

					} catch (Exception e) {
						e.printStackTrace();
					}

					context.next();
				});
			}

		}

	}
}
