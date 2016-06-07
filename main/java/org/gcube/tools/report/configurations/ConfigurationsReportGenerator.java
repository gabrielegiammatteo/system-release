package org.gcube.tools.report.configurations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.etics.buildsystem.exception.DataAccessException;
import org.etics.buildsystem.exception.ElementNotFoundException;
import org.etics.buildsystem.exception.EticsSecurityException;
import org.etics.buildsystem.stub.BuildSystemService;
import org.etics.buildsystem.stub.BuildSystemServiceService;
import org.etics.buildsystem.stub.BuildSystemServiceServiceLocator;
import org.etics.buildsystem.stub.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ConfigurationsReportGenerator {

	
	private String eticsWSEndpoint;
	private BuildSystemService eticsService;
	
	public ConfigurationsReportGenerator(String eticsWSEndpoint){
		this.eticsWSEndpoint = eticsWSEndpoint;
	}

	
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(ConfigurationsReportGenerator.class + " <init|add> [OPTIONS]", options);		
	}
	
	
	public BuildSystemService getETICSWs() throws ServiceException {
		if(eticsService == null) {
			BuildSystemServiceService locator  =  new BuildSystemServiceServiceLocator();
			((BuildSystemServiceServiceLocator)locator).setEndpointAddress("BuildSystemService", eticsWSEndpoint);
			eticsService = locator.getBuildSystemService();
		}
		return eticsService;
	}
	
	public List<String> getBuiltConfigs(File buildStatus) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        
        Document buildStatusDoc = dBuilder.parse(buildStatus);
        
        List<String> output = new LinkedList<String>();
        
        NodeList modules = 
            (NodeList) xPath.evaluate("/project/modules/module", buildStatusDoc, XPathConstants.NODESET);
        for(int i=0; i< modules.getLength(); i++){
        	Node module = modules.item(i);
            String is = (String) xPath.evaluate("@configid", module, XPathConstants.STRING);
            output.add(is);
        }
                
		return output;
		
	}

	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 * @throws TransformerException 
	 */
	public static void main(String[] args) throws ParseException, XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerException {
		

		Option helpOpt = OptionBuilder.withLongOpt("help").hasArg(false).withDescription("print this message").create("h");
		Option eticsWS = OptionBuilder.withLongOpt("eticsws").withArgName("eticsEndpoint").hasArg(true).withDescription("etics web service endpoint").create("e");

		Option buildStatusOpt = OptionBuilder.withLongOpt("buildstatus").withArgName("build-status.xml").hasArg(true).withDescription("path to build-status.xml file").create("b");
		Option outputFileOpt = OptionBuilder.withLongOpt("output").withArgName("file").hasArg(true).withDescription("output txt file. If not provided prints on stdout").create("o");	

		
		// create the Options
		Options options = new Options();
		options.addOption(helpOpt);
		options.addOption(buildStatusOpt);
		options.addOption(outputFileOpt);
		options.addOption(eticsWS);
		
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args);

		if (cmd.hasOption(helpOpt.getOpt())) {
			printHelp(options);
			return;
		}
		

		File outputFile = null;
		if(cmd.hasOption("o")){
			outputFile = new File(cmd.getOptionValue("o"));
		}
		else {
			System.err.println("output file not specified. Cannot continue. Exiting...");
			System.exit(1);
		}
		
		
		String eticsWSEndpoint = "";
		if(cmd.hasOption("e")){
			eticsWSEndpoint = cmd.getOptionValue("e");
		}
		else {
			System.err.println("ETICS Web Service Endpoint not specified. Please use option --eticsws");
			System.exit(1);			
		}
		
		
		File buildStatus = null;
		if(cmd.hasOption("b")){
			buildStatus = new File(cmd.getOptionValue("b"));
		}
		else {
			buildStatus = new File("build-status.xml");
		}
		
		if(!buildStatus.exists()){
			System.err.println("build-status.xml file " + buildStatus +" does not exist. Cannot continue. Exiting...");
			System.exit(1);
		}
		
		
		ConfigurationsReportGenerator instance = new ConfigurationsReportGenerator(eticsWSEndpoint);
		
		List<String> builtConfigs = instance.getBuiltConfigs(buildStatus);
		
		ConfigurationsReport rc = ConfigurationsReport.init(outputFile);
		
		
		for(String confId: builtConfigs){
			Configuration c;
			try {
				c = instance.getETICSWs().getConfigurationById(confId);
				System.out.println("Adding "+ c.getName());
				rc.addEntry(c.getName(), c.getStatus());

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		rc.save();
	}

}
