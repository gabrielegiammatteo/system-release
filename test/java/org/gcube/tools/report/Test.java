package org.gcube.tools.report;

import it.eng.d4s.sa3.util.Version;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Test {

	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
		
		File profileFile = new File("testprofile.xml");
        Document docProfile = null;
		
		docProfile = dBuilder.parse(profileFile);
		
		//check serviceVersion = 1.0.0
		String serviceVersion = (String) xPath.evaluate("//Profile/Version/text()", docProfile, XPathConstants.STRING);
		Version v = new Version(serviceVersion);
		String normalizedVersion = v.getNormalizedRepresentation();
		System.out.println("normalized: " + normalizedVersion);
		if(!normalizedVersion.equals("1.0.0-0")){
			System.out.println("serviceVersion not equals to 1.0.0 (was "+serviceVersion+")");
		}
		
	}
	
}
