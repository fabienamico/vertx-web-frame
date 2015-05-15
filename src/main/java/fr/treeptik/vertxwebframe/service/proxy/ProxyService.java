package fr.treeptik.vertxwebframe.service.proxy;

import io.vertx.core.Vertx;

import java.lang.reflect.Proxy;

public class ProxyService {

	private Vertx vertx;
	private static ProxyService instance;
	
	private ProxyService() {
	}
	
	public static ProxyService getInstance(Vertx vertx) {
		if (instance == null ){
			instance = new ProxyService();
			instance.vertx = vertx;
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T createProxy(Class<T> clazz){
		return (T) Proxy.newProxyInstance(
				clazz.getClassLoader(), clazz.getInterfaces(), new ProxyHandler(vertx));
	}
}
