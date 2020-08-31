package com.flowable.integration.platform.util;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowable.integration.platform.config.Configuration;
import com.flowable.integration.platform.service.FetchListResultSet;
import com.flowable.integration.platform.service.RestClient;

public class AuthenticationHandler {

	private final Configuration configuration;
	AuthenticationHandler(){
		configuration = Configuration.getInstance();
	}
	
	public static String getSAMLArtifact() {
		AuthenticationHandler handler = new AuthenticationHandler();
		String authType = handler.configuration.getAuthenticationType();

		if ("PLATFORM".equals(authType)) {
			return handler.logInPlatformUsingBasic();
		}
		
		throw new RuntimeException("Un supported Authentication type :" + authType);
	}
	

	
	private String logPlaformUsingOTDS(String otdsTicket){
		String request="<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n\t<SOAP:Header>\r\n\t\t<OTAuthentication xmlns=\"urn:api.bpm.opentext.com\">\r\n\t\t\t<AuthenticationToken>%s</AuthenticationToken>\r\n\t\t</OTAuthentication>\r\n\t</SOAP:Header>\r\n\t<SOAP:Body>\r\n\t\t<samlp:Request xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" MajorVersion=\"1\" MinorVersion=\"1\" IssueInstant=\"2014-05-20T15:29:49.156Z\" RequestID=\"a5470c392e-264e-9537-56ac-4397b1b416d\">\r\n\t\t\t<samlp:AuthenticationQuery>\r\n\t\t\t\t<saml:Subject xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\">\r\n\t\t\t\t\t<saml:NameIdentifier Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\"/>\r\n\t\t\t\t</saml:Subject>\r\n\t\t\t</samlp:AuthenticationQuery>\r\n\t\t</samlp:Request>\r\n\t</SOAP:Body>\r\n</SOAP:Envelope>";
		return getSAMLArtifact(String.format(request,otdsTicket) );
	}
	
	private String logInPlatformUsingBasic(){
		String request = "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n\t<SOAP:Header>\r\n\t\t<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\r\n\t\t\t<wsse:UsernameToken xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\r\n\t\t\t\t<wsse:Username>%s</wsse:Username>\r\n\t\t\t\t<wsse:Password>%s</wsse:Password>\r\n\t\t\t</wsse:UsernameToken>\r\n\t\t</wsse:Security>\t\t\r\n\t</SOAP:Header>\r\n\t<SOAP:Body>\r\n\t\t<samlp:Request xmlns:samlp=\"urn:oasis:names:tc:SAML:1.0:protocol\" MajorVersion=\"1\" MinorVersion=\"1\" IssueInstant=\"2009-04-01T10:23:11Z\" RequestID=\"a997c83a8d-b5d7-b930-edba-02e37ab1765\">\r\n\t\t\t<samlp:AuthenticationQuery>\r\n\t\t\t\t<saml:Subject xmlns:saml=\"urn:oasis:names:tc:SAML:1.0:assertion\">\r\n\t\t\t\t\t<saml:NameIdentifier Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\"></saml:NameIdentifier>\r\n\t\t\t\t</saml:Subject>\r\n\t\t\t</samlp:AuthenticationQuery>\r\n\t\t</samlp:Request>\r\n\t</SOAP:Body>\r\n</SOAP:Envelope>"; 
		return getSAMLArtifact(String.format(request,configuration.getPlatformUser(),configuration.getPlatformPassword()) );
	}
	
	private String getSAMLArtifact(String requestXml){
		try {
			String service_url = "/home/system/com.eibus.web.soap.Gateway.wcp";
			URI uri = new URI(configuration.getPlatformURL() + service_url);
			RestClient client = new RestClient(uri);

			Response response = client.post(requestXml, MediaType.APPLICATION_XML_TYPE);
			
			String jsonString = response.readEntity(String.class);
			String samlArtifact = parseSAMLArtifact(jsonString);

			return samlArtifact;
		} catch (Exception exception) {
			throw new RuntimeException("Exception while generating SAML Artifact: " + exception);
		}
	}
	
	
	
	private String parseSAMLArtifact(String stream) {

		Node node = Utility.convertToXml(stream);
		NamespaceContext context = NamespaceResolver.createNamespaceContext("urn:oasis:names:tc:SAML:1.0:protocol",
				"saml");
		Node result = Utility.findNode(node, "//saml:AssertionArtifact", context);
		if(result == null){
			throw new RuntimeException("Result does not contains SAML Artifact: " + stream);
		}
		return result.getTextContent();
	}
	
	public static void main(String[] args) throws Exception {
		String samlArtifact= getSAMLArtifact();
		
		System.out.println("SAML Artifact : " + samlArtifact);
		
	}

}
