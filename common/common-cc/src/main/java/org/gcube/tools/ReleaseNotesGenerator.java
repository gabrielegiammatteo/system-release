package org.gcube.tools;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.gcube.tools.report.configurations.ConfigurationsReport;
import org.gcube.tools.report.distribution.DistributionLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ReleaseNotesGenerator {
	
	
	
	
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(ReleaseNotesGenerator.class + " <init|add> [OPTIONS]", options);		
	}
	
	
	private static void initializeChangelogReport(File report) throws ParserConfigurationException, TransformerException{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("Results");
		doc.appendChild(rootElement);
	
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "8");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult("file://"+report.getAbsolutePath());
		transformer.transform(source, result);
		
		StreamResult logResult = new StreamResult("file://"+report.getAbsolutePath());
		transformer.transform(source, logResult);
	}
	
	
	public static void addReleaseNotes(File saDir, File releasenotesReport, ConfigurationsReport releasedConfigs, String artifactName, DistributionLog dl) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        
        Document releaseNotesDoc = dBuilder.parse(releasenotesReport);
        

		
		File changelogFile = new File(saDir + File.separator + "changelog.xml");
        Document changelogDoc = null;
        if(!changelogFile.exists()){
        	dl.addWarning("changelog.xml file not present for " + artifactName, artifactName);
        	return;
        }	
        else {
        	changelogDoc = dBuilder.parse(changelogFile);    
        }
        
    	NodeList changesets = 
    			(NodeList) xPath.evaluate("ReleaseNotes/Changeset", changelogDoc, XPathConstants.NODESET);

    	boolean changeSetFound = false;
    	for(int i = 0; i< changesets.getLength(); i++){
	    	Node changeset = changesets.item(i);
	        String configurationName = (String) xPath.evaluate("@component", changeset, XPathConstants.STRING);
	        																												
	       
	        //if exists, remove system version from configuration name
			Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+");
			Matcher matcher = pattern.matcher(configurationName);
			int count = 0;
			while (matcher.find()){count++;}
			if(count==2)
				configurationName = configurationName.replaceAll("-\\d+\\.\\d+\\.\\d+$", "");
	        
	        if(releasedConfigs.getConfigurationStatus(configurationName) == null ||
	        		releasedConfigs.getConfigurationStatus(configurationName).equals("locked"))
	        	continue;
	        
	        
	        String date = (String) xPath.evaluate("@date", changeset, XPathConstants.STRING);
	        
	        Element releasenoteNode = releaseNotesDoc.createElement("ReleaseNote");
	        Element cn = releaseNotesDoc.createElement("componentName");
	        cn.setTextContent(configurationName);
	        releasenoteNode.appendChild(cn);
	        Element daten = releaseNotesDoc.createElement("date");
	        daten.setTextContent(date);
	        releasenoteNode.appendChild(daten);
	        
	        Element messagen = releaseNotesDoc.createElement("message");
	        releasenoteNode.appendChild(messagen);
	        
	    	NodeList changes = 
	    			(NodeList) xPath.evaluate("Change", changeset, XPathConstants.NODESET);
	        for(int j = 0;j < changes.getLength(); j++){
	        	String change = (String) xPath.evaluate("text()", changes.item(j), XPathConstants.STRING);
	        	
	        	Element el = releaseNotesDoc.createElement("string");
	        	el.setTextContent(change);
	        	messagen.appendChild(el);
	        	
	        }
	        
	        Node releaseNotesRoot = releaseNotesDoc.getFirstChild();
	        releaseNotesRoot.appendChild(releasenoteNode);
	        
	        changeSetFound = true;
	    }
    
    	

    	if(!changeSetFound && releasedConfigs.getUnlockedConfigurationsByNameContains(artifactName).size() > 0){ //unlocked configurations have status = ""
    		dl.addWarning("no entries for released configuration in changelog.xml", artifactName);
    	}
        

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "8");
		DOMSource source = new DOMSource(releaseNotesDoc);
		StreamResult result = new StreamResult(releasenotesReport);
		transformer.transform(source, result);
				
	}
	
	

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws Exception {
		
		Option helpOpt = OptionBuilder.withLongOpt("help").hasArg(false).withDescription("print this message").create("h");

		Option releaseNotesOpt = OptionBuilder.withLongOpt("releasenotes").withArgName("RELEASENOTES.XML").hasArg(true).withDescription("the releasenotes.xml file to use as output. By default it is distribution.xml").create("r");
		Option saDirOpt = OptionBuilder.withLongOpt("sadir").withArgName("FOLDER").hasArg(true).withDescription("the decompressed SA directory").create("s");		
		Option logReportOpt = OptionBuilder.withLongOpt("logreport").withArgName("DISTRIBUTION_LOG.XML").hasArg(true).withDescription("the default is distribution_log.xml").create("l");		
		Option artifactNameOpt = OptionBuilder.withLongOpt("artifactname").withArgName("FOLDER").hasArg(true).withDescription("the artifact name").create("a");		
		Option releasedConfigsReportOpt = OptionBuilder.withLongOpt("releasedreport").withArgName("xml file").hasArg(true).withDescription("ReleasedConfigurationReport file").create("u");		

		
		// create the Options
		Options options = new Options();
		options.addOption(helpOpt);
		options.addOption(releaseNotesOpt);
		options.addOption(saDirOpt);
		options.addOption(logReportOpt);
		options.addOption(artifactNameOpt);
		options.addOption(releasedConfigsReportOpt);
		
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args);

		if (cmd.hasOption(helpOpt.getOpt())) {
			printHelp(options);
			return;
		}
		
		
		File releasenotesReport = null;
		if(cmd.hasOption("r")){
			releasenotesReport = new File(cmd.getOptionValue("r"));
		}
		else {
			releasenotesReport = new File("releasenotes.xml");
		}
		
		
		File logReport = null;
		if(cmd.hasOption("l")){
			logReport = new File(cmd.getOptionValue("l"));
		}
		else {
			logReport = new File("distribution_log.xml");
		}	
		
		DistributionLog dl = DistributionLog.load(logReport);	
		
		File saDir = null;
		if(cmd.hasOption("s")){
			saDir = new File(cmd.getOptionValue("s"));
		}
		else {
			saDir = new File("artifacttmp");
		}	
		

		ConfigurationsReport releasedReport = null;
		if(cmd.hasOption("u")){
			File releasedConfigsReportFile = new File(cmd.getOptionValue("u"));
			releasedReport = ConfigurationsReport.load(releasedConfigsReportFile);
		}
		
		
		String artifactName = cmd.getOptionValue("a");
		

		
		String command = args[0];
		if(command.equals("init")){
			initializeChangelogReport(releasenotesReport);
		}
		else if(command.equals("add")){
			if(!saDir.isDirectory()){
				System.err.println("Ther sa directory "+saDir+" does not exist. Cannot continue");
				System.exit(1);
			}
			
			addReleaseNotes(saDir, releasenotesReport, releasedReport, artifactName, dl);
			
		}
		else {
			System.err.println("command (first parameter) must be one of 'init' or 'add'. It was: "+command);
		}
		
		dl.save();
	}

}
