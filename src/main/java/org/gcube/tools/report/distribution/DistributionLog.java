package org.gcube.tools.report.distribution;

import java.io.File;
import java.io.IOException;

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
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class DistributionLog {
	
	Document doc = null;
	
	File file = null;
	
	public static DistributionLog init(File file) throws TransformerException, ParserConfigurationException{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("Packages");
		doc.appendChild(rootElement);

		DistributionLog d = new DistributionLog();
		d.doc = doc;
		d.file = file;
		
		return d;
	}
	
	public static DistributionLog load(File file) throws ParserConfigurationException, SAXException, IOException{
		
		DistributionLog d = new DistributionLog();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		d.doc = dBuilder.parse(file);
		d.file = file;
		
		return d;
	}
	
	
	public void addWarning(String message, String ETICSRef) throws XPathExpressionException{
		addEntry("warn", message, ETICSRef);
	}
	
	public void addError(String message, String ETICSRef) throws XPathExpressionException{
		addEntry("error", message, ETICSRef);
	}
	
	public void addInfo(String message, String ETICSRef) throws XPathExpressionException{
		addEntry("info", message, ETICSRef);
	}
	
	private void addEntry(String level, String message, String ETICSRef) throws XPathExpressionException{
		Node node = this.getPackageNode(ETICSRef);
		
    	Element el = this.doc.createElement("entry");
    	el.setAttribute("level", level);
    	el.setTextContent(message);
    	node.appendChild(el);     	
	}
	
	private Node getPackageNode(String ETICSRef) throws XPathExpressionException{
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();	
        
        Node node = (Node) xPath.evaluate("/Packages/package[./ETICSRef/text()=\""+ETICSRef+"\"]", this.doc, XPathConstants.NODE);
	
        if(node == null){
            node = this.doc.createElement("package");
            Node logPackages = this.doc.getFirstChild();
            logPackages.appendChild(node);
    		Element el4 = this.doc.createElement("ETICSRef");
    		el4.setTextContent(ETICSRef);
    		node.appendChild(el4);
        }
        
        return node;
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
