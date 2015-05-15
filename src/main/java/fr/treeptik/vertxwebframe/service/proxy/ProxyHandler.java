package fr.treeptik.vertxwebframe.service.proxy;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import javassist.ClassPool;
import javassist.CtClass;

public class ProxyHandler implements InvocationHandler {

	 private final static Logger logger = LoggerFactory.getLogger(ProxyHandler.class);
	
	private Vertx vertx;

	public ProxyHandler(Vertx vertx) {
		this.vertx = vertx;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		// The service must have JUST one interface 
		Class<?> interfaces = proxy.getClass().getInterfaces()[0];
		String serviceName = interfaces.getSimpleName();
		
		logger.debug("Call proxy method on : " + serviceName);
		
		// Get the name of the method 
		DeliveryOptions options = new DeliveryOptions();
		options.addHeader("method", method.getName());
		
			
		
		// Transform all method parameter to JsonObject
		JsonObject jsonObject = new JsonObject();
		Future<Object> futureParam = Future.<Object>future();
		final Class<?>[] futureTypeObj = new Class<?>[1];
		
		if (args != null && args.length>0){
			for (int i = 0; i < args.length; i++) {
				Object object = args[i];
//
// *TODO : Trouver un moyen correct d'envoyer une réponse du service au controller 
//				if (object.getClass().getName().equals("io.vertx.core.impl.FutureImpl")){
//					logger.debug("Method contain Future param" );					
//					futureParam = (Future<Object>) object;
//					// Get the Future type
//					for (java.lang.reflect.Parameter parameter : method.getParameters()) {
//						// TODO : HACK Get the future Type
//						String typeName = parameter.getParameterizedType().toString();
//						typeName = typeName.substring(typeName.indexOf("<")+1, typeName.lastIndexOf(">"));
//						
//						CtClass ctClass = ClassPool.getDefault().get("java.util.ArrayList");
//						ctClass.setGenericSignature("fr.treeptik.sample.vertxwebframe.model.Pet");
//						//ClassFile classFile = new ClassFile(true, "java.util.List", null);
//						futureTypeObj[0] = Class.forName("java.util.ArrayList");
//					}
//					continue;
//				}
				jsonObject.put(String.valueOf(i), Json.encode(new Parameter(object.getClass().getName(), Json.encode(object))));
			}
		}
		
//		if (futureParam != null){
//			
//			logger.debug("Call service with future " );
//			String uuid = UUID.randomUUID().toString();
//			jsonObject.put("UUID",  uuid);
//
//// TODO : Un consumer pour recevoir la réponse du Serice ??
////			vertx.eventBus().consumer(uuid).handler(m -> {
////				
////			});
//			
//			vertx.eventBus().send(serviceName, jsonObject, options);
//			
//			
//			
//			
//		} else {
			vertx.eventBus().send(serviceName, jsonObject, options);
//		}
		
		
		
		
		return null;
	}
	
	

}
