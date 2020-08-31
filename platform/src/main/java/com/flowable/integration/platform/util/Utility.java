package com.flowable.integration.platform.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utility {

	
	static Node convertToXml(String xmlString) {
		return convertToXml(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
	}
	
	static Node convertToXml(InputStream xmlStream) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(xmlStream);
		} catch (Exception e) {
			throw new RuntimeException("Unable to convert string to xml document " + e);
		}
	}

	static Node findNode(Node node, String xpathString, NamespaceContext namespace) {
		try {
			// Get XPath expression
			XPathFactory xpathfactory = XPathFactory.newInstance();
			XPath xpathObject = xpathfactory.newXPath();
			xpathObject.setNamespaceContext(namespace);
			XPathExpression expr = xpathObject.compile(xpathString);
			// Search XPath expression
			return (Node) expr.evaluate(node, XPathConstants.NODE);
		} catch (Exception e) {
			throw new RuntimeException("Unable to perform xpath evaluation " + e);
		}
	};
	
    static String prettyPrintJsonString(JsonNode jsonNode) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(jsonNode.toString(), Object.class);
           return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
           
        } catch (Exception e) {
            return "Sorry, pretty print didn't work";
        }
    }
}

class NamespaceResolver implements NamespaceContext {

	private final Map<String, String> namespaces = new HashMap<String, String>();

	private NamespaceResolver(String namespace, String prefix) {
		namespaces.put(namespace, prefix);
	}

	// The lookup for the namespace uris is delegated to the stored document.
	public String getNamespaceURI(String prefix) {
		String ns = null;

		for (Map.Entry<String, String> entry : namespaces.entrySet()) {
			if (prefix.equals(entry.getValue())) {
				ns = entry.getKey();
				break;
			}
		}
		return ns;
	}

	public String getPrefix(String namespaceURI) {
		return namespaces.get(namespaceURI);
	}

	public void addNamespace(String namespace, String prefix) {
		namespaces.put(namespace, prefix);
	}
	
	public static NamespaceContext createNamespaceContext(String namespace, String prefix) {
		return new NamespaceResolver(namespace,prefix);
	}

	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String namespaceURI) {
		return null;
	}
}
