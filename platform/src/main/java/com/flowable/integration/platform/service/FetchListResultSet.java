package com.flowable.integration.platform.service;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowable.integration.platform.config.Configuration;
import com.flowable.integration.platform.util.AuthenticationHandler;

public class FetchListResultSet {

	private static String ENTITY_ID;
	private static String WORKLIST_ID;

	private static final boolean debug = true;

	private static final Configuration config = Configuration.getInstance();

	public static void main(String[] args) throws Exception {
		
		WORKLIST_ID = "1866DA2F56F0A1E8BB2A437C06B0C87B";
		 getWorklistVersionId();
		String name = "toCustomer$Id";
		//System.out.println("sml art::"+ AuthenticationHandler.getSAMLArtifact());
		
		System.out.println(name.indexOf('$'));

	}

	public static String getListResultsResponse(String workListId, String entityId) throws Exception {
		ENTITY_ID = entityId;
		WORKLIST_ID = workListId;
		System.out.println("ENTITY_ID >>>>>>>>>>>>>>>>> " + ENTITY_ID);
		System.out.println("WORKLIST_ID >>>>>>>>>>>>>>>>> " + WORKLIST_ID);
		String workListVersion = getWorklistVersionId();
		return getListResults(workListVersion);
	}

	static String of(String string) {
		return string.replaceAll("-", "");
	}

	static String getWorklistVersionId() throws Exception {
		URI uri2 = new URI(config.getPlatformURL("system")
				+ String.format("/app/entityRestService/Elements(%s)", of(WORKLIST_ID)));

		RestClient client2 = new RestClient(uri2);
		// client2.addHeader("Cookie", getCookie());
		client2.addHeader("SAMLart", AuthenticationHandler.getSAMLArtifact());
		Response response = client2.buildGet(MediaType.APPLICATION_JSON_TYPE);

		String jsonResponse = response.readEntity(String.class);
		if (debug) {
			System.out.println("Item List Response1: \n" + jsonResponse);
		}

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);
		return rootNode.path("containerVersionId").asText();
	}

	static String getListResults(String workListVersion) throws Exception {
		String jsonString = String.format(
				"{\"viewId\":\"%s\",\"versionId\":\"%s\",\"dataSourceId\":\"%s\",\"dataSourceIds\":[\"%3$s\"],\"parameters\":{},\"distinct\":false,\"select\":[],\"totalCountOption\":\"None\",\"resultOption\":\"Items\",\"queryResultOption\":\"Items\",\"skip\":0,\"top\":200}",
				of(WORKLIST_ID), workListVersion, of(ENTITY_ID));

		URI uri = new URI(config.getPlatformURL("system")
				+ String.format("/app/entityRestService/Elements(%s.%s)/ResultItems?include=PropDescs&",
						workListVersion, of(WORKLIST_ID)));
		RestClient client = new RestClient(uri);
		// client.addHeader("Cookie", getCookie());
		System.out.println("sml art::"+ AuthenticationHandler.getSAMLArtifact());
		client.addHeader("SAMLart", AuthenticationHandler.getSAMLArtifact());
		Response response = client.buildPost(jsonString, MediaType.APPLICATION_JSON_TYPE);
		return response.readEntity(String.class);
	}
}
