package com.solertium.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a serializable simulation of an HTTP Request, capturing the
 * important facets (Method, URI, Headers, Entity) as Strings.
 * 
 * @author robheittman
 *
 */
public class HttpSimulation implements Serializable {

	private String method;
	private String entity;
	private String uri;
	private final Map<String,String> headers = new HashMap<String,String>();
	public String getEntity() {
		return entity;
	}
	public void setEntity(String entity) {
		this.entity = entity;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public void putHeader(String key, String value){
		headers.put(key,value);
	}
	public String getHeader(String key){
		return headers.get(key);
	}
	public void removeHeader(String key){
		headers.remove(key);
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	
	public HttpSimulation(String method, String uri){
		this.method = method;
		this.uri = uri;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 "+method+" "+uri+"\n");
		for(Map.Entry<String,String> entry : headers.entrySet())
			sb.append(entry.getKey()+": "+entry.getValue()+"\n");
		sb.append("\n");
		if(entity!=null) sb.append(entity+"\n");
		return sb.toString();
	}
}
