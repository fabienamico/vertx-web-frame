package fr.treeptik.vertxwebframe.service.proxy;

import java.io.Serializable;

public class Parameter implements Serializable{
	
	public static final long serialVersionUID = 1L;

	public String clazzType;
	public String jsonValue;

	public Parameter() {
	}
	
	public Parameter(String clazzType, String value) {
		this.clazzType = clazzType;
		this.jsonValue = value;
	}

	@Override
	public String toString() {
		return "Parameter [clazzType=" + clazzType + ", jsonValue=" + jsonValue
				+ "]";
	}
	
	
	
}