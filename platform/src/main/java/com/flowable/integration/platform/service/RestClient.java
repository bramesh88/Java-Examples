package com.flowable.integration.platform.service;

import java.net.URI;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowable.integration.platform.config.Configuration;

public class RestClient {
	private final URI loginURI;
	private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<String, Object>();
	private final MultivaluedMap<String, String> parameters = new MultivaluedHashMap<String, String>();
	private final boolean debug = true;

	public RestClient(final URI loginURI) {
		this.loginURI = loginURI;
		enableFiddlerCapture();
	}

	public void addHeader(final String name, final String value) {
		headers.add(name, value);
	}

	public void addParameter(final String name, final String value) {
		parameters.add(name, value);
	}

	/**
	 * This method will invoke the rest service and returns the service
	 * response.
	 * 
	 * @param responseType
	 * @return Response
	 */
	public Response post(final String jsonString, final MediaType responseType) {
		Response response = prepareBuilder(responseType).post(Entity.json(jsonString));
		logResponse(response);
		return response;

	}

	/**
	 * This method will invoke the rest service and returns the service
	 * response.
	 * 
	 * @param responseType
	 * @return Response
	 */
	public Response buildPost(final String jsonString, final MediaType responseType) {
		Response response = prepareBuilder(responseType).headers(headers).buildPost(Entity.json(jsonString)).invoke();
		logResponse(response);
		return response;

	}

	public Response buildGet(final MediaType responseType) {

		return prepareInvokation(responseType).headers(headers).buildGet().invoke();
	}

	/**
	 * 
	 * @param responseType
	 * @return
	 */
	public Response get(final MediaType responseType) {

		return prepareBuilder(responseType).headers(headers).buildGet().invoke();
	}

	private Builder prepareInvokation(final MediaType responseType) {
		WebTarget target = prepareTarget();
		for (String key : parameters.keySet()) {
			target = target.queryParam(key, parameters.get(key).get(0));
		}
		return target.request(responseType);
	}

	/**
	 * It Prepares the builder
	 * 
	 * @param responseType
	 * @return
	 */
	private Builder prepareBuilder(final MediaType responseType) {
		return prepareTarget().request(responseType);
	}

	/**
	 * It prepares the Target obj
	 * 
	 * @return WebTarget
	 */
	private WebTarget prepareTarget() {

		ClientBuilder clientBuilder = ClientBuilder.newBuilder();
		return clientBuilder.build().target(loginURI);
	}

	private void logResponse(Response response) {
		if (debug) {
			System.out.println("Response  :\n"+response.readEntity(String.class));
		}
	}

	static String prettyPrintJsonString(String string) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(string, Object.class);
			String jsonSting = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
			System.out.println(jsonSting);

			return jsonSting;
		} catch (Exception e) {
			return "Sorry, pretty print didn't work";
		}
	}

	public void enableFiddlerCapture() {

		if (Configuration.getInstance().enableProxy()) {
			System.setProperty("http.proxyHost", "127.0.0.1");
			System.setProperty("https.proxyHost", "127.0.0.1");
			System.setProperty("http.proxyPort", "8888");
			System.setProperty("https.proxyPort", "8888");
		}
	}
}
