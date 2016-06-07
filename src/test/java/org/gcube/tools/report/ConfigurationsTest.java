package org.gcube.tools.report;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.gcube.tools.report.configurations.ConfigurationsReport;
import org.xml.sax.SAXException;

public class ConfigurationsTest {

	/**
	 * @param args
	 * @throws TransformerException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws TransformerException, ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		File f = new File("/home/gabriele/tmp/test/test.xml");
		
		ConfigurationsReport rd = ConfigurationsReport.load(f);
		
		List<String> res= rd.getUnlockedConfigurationsByNameContains("maven");
		
		for (Iterator iterator = res.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
		}
	}

}
