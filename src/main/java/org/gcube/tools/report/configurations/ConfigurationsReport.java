package org.gcube.tools.report.configurations;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigurationsReport {
	
	Document doc = null;
	
	File file = null;
	
	public static ConfigurationsReport init(File file) throws TransformerException, ParserConfigurationException{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("Configurations");
		doc.appendChild(rootElement);

		ConfigurationsReport d = new ConfigurationsReport();
		d.doc = doc;
		d.file = file;
		
		return d;
	}
	
	public static ConfigurationsReport load(File file) throws ParserConfigurationException, SAXException, IOException{
		
		ConfigurationsReport d = new ConfigurationsReport();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		d.doc = dBuilder.parse(file);
		d.file = file;
		
		return d;
	}

	
	public void addEntry(String configuration, String status){
        Node node = this.doc.createElement("configuration");
        ((Element) node).setAttribute("name", configuration);
        ((Element) node).setAttribute("status", status);
        
        Node configurationNode = this.doc.getFirstChild();
        configurationNode.appendChild(node);
	}
	
	public List<String> getAllConfigurationsName() throws XPathExpressionException{
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();	
        
        List<String> list = new LinkedList<String>();
        
        NodeList nodes = (NodeList) xPath.evaluate("/Configurations/configuration", this.doc, XPathConstants.NODESET);


    	for(int j = 0; j<nodes.getLength(); j++){
            Node n = nodes.item(j);
    		NamedNodeMap attrs = n.getAttributes();
    		list.add(attrs.getNamedItem("name").getNodeValue());
	    }
    	
        return list;
	}
	
	
	public List<String> getUnlockedConfigurationsByNameContains(String nameContains) throws XPathExpressionException{
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();	
        NodeList nodes = (NodeList) xPath.evaluate("/Configurations/configuration[contains(./@name,'"+nameContains+"')]", this.doc, XPathConstants.NODESET);

        List<String> list = new LinkedList<String>();
    	for(int j = 0; j<nodes.getLength(); j++){
            Node n = nodes.item(j);
    		NamedNodeMap attrs = n.getAttributes();
    		if(attrs.getNamedItem("status").getNodeValue().equals("")){
    			list.add(attrs.getNamedItem("name").getNodeValue());
    		}
	    }
    	
        return list;
	}
	
	
	
	public String getConfigurationStatus(String configuration) throws XPathExpressionException{
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();	
        
        Node node = (Node) xPath.evaluate("/Configurations/configuration[./@name='"+configuration+"']", this.doc, XPathConstants.NODE);
        if(node == null)
        	return null;
        NamedNodeMap attrs = node.getAttributes();
        return attrs.getNamedItem("status").getNodeValue();
	}
	
	
	public void save() throws TransformerException{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "8");
		DOMSource source = new DOMSource(this.doc);
		StreamResult result = new StreamResult("file://"+this.file.getAbsolutePath());
		transformer.transform(source, result);		
	}

}
