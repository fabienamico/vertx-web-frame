package fr.treeptik.vertxwebframe.service.verticle;

import fr.treeptik.vertxwebframe.service.proxy.Parameter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class ServiceVerticle extends AbstractVerticle{

	 private final static Logger logger = LoggerFactory.getLogger(ServiceVerticle.class);
			 
	private Object realService;
	
	public ServiceVerticle(String clazz) {
		try {
			setRealService(this.getClass().getClassLoader().loadClass(clazz).newInstance());
			
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			throw new IllegalArgumentException("Service verticle Class not foud");
		}
	}

	@Override
	public void start() throws Exception {

		Class<?> interfaces =getRealService().getClass().getInterfaces()[0];
		String serviceName = interfaces.getSimpleName();

		logger.debug("Init eventbus consumer on " + serviceName);
		
		vertx.eventBus().consumer(serviceName, res -> {
			try{
			logger.debug("Service verticle receive message on bus:  " + serviceName);

			String methodName = res.headers().get("method");
			JsonObject receiveObject = (JsonObject) res.body();
//			
//			String uuid = (String)receiveObject.remove("UUID");
//			logger.debug("Get the uuid  " + uuid);
			
			
			Object[] values = receiveObject.getMap().values().toArray();
			List<Object> paramsMethod = new LinkedList<>();
			/*
			 * Get All Parameter and convert it 
			 */
			for (int i = 0; i < values.length; i++) {
				Parameter parameter = Json.decodeValue((String) values[i], Parameter.class) ;
				try {
					Class<?> classLoad = this.getClass().getClassLoader().loadClass(parameter.clazzType);
					paramsMethod.add(Json.decodeValue(parameter.jsonValue, classLoad));
					
				} catch (Exception e) {
					throw new IllegalArgumentException("Service Verticle cant find parameter class", e);
				}
			}
			
			/*
			 * Find the method and call it
			 */
			List<Method> methods = Arrays.asList(getRealService().getClass().getMethods());
			methods.stream().filter(m -> m.getName().equalsIgnoreCase(methodName)).forEach(m -> {
				try {
					// Check if there is a UUID for sending result					
					logger.debug("Service Verticle invoke method on real service");
					m.invoke(getRealService(), paramsMethod.toArray());
					
				} catch (Exception e) {
					throw new IllegalArgumentException("Method " + methodName +" not found", e);
				}	
			}); 
			}catch (Exception e){
				e.printStackTrace();
			}	
		});
		
	}

	public Object getRealService() {
		return realService;
	}

	public void setRealService(Object realService) {
		this.realService = realService;
	}

}
