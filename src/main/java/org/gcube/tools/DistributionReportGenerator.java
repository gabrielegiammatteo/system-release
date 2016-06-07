package org.gcube.tools;
import it.eng.d4s.sa3.util.Version;

import java.io.File;
import java.util.Scanner;
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
import org.gcube.tools.report.distribution.DistributionLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DistributionReportGenerator {
	
    public static final String WIKIDOC_MATCHING_PATTERN = "(https://gcube.wiki.gcube-system.org/gcube/index.php.*)|(https://technical.wiki.d4science.research-infrastructures.eu/documentation.*)";
	private static boolean acceptSnapshots = false;

	

	
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(DistributionReportGenerator.class + " <init|add> [OPTIONS]", options);		
	}
	
	
	private static void initializeDistributionReport(File dReport, File logReport) throws ParserConfigurationException, TransformerException{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("Packages");
		doc.appendChild(rootElement);
	
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "8");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult("file://"+dReport.getAbsolutePath());
		transformer.transform(source, result);
		
		DistributionLog dl = DistributionLog.init(logReport);
		dl.save();
	}
	
	
	private static void validatePackage(Node packageNode, Node profile, DistributionLog	dl, File SADir, String ETICSRef, String packageName) throws XPathExpressionException{
		
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

    	String groupId = (String) xPath.evaluate("groupID", packageNode, XPathConstants.STRING);
    	String artifactId = (String) xPath.evaluate("artifactID", packageNode, XPathConstants.STRING);
    	String version = (String) xPath.evaluate("version", packageNode, XPathConstants.STRING);
    	String javadoc = (String) xPath.evaluate("javadoc", packageNode, XPathConstants.STRING);
    	String URL = (String) xPath.evaluate("URL", packageNode, XPathConstants.STRING);
    	
    	
    	//checks maven coordinates
    	if(URL.equals("null")){
    		dl.addError("URL null for artifact "+groupId+":"+artifactId, ETICSRef);
    	}	

    	if(javadoc.equals("null")){
    		dl.addWarning("javadoc URL null for artifact "+groupId+":"+artifactId, ETICSRef);
    	}
    	
    	if(groupId.equals("null")){
    		dl.addError("groupId null for artifact "+groupId+":"+artifactId, ETICSRef);
    	}

    	
    	if(artifactId.equals("null")){
    		dl.addError("artifactId null for artifact "+groupId+":"+artifactId, ETICSRef);
    	}
    	
    	if(version.equals("null")){
    		dl.addError("version is null for artifact "+groupId+":"+artifactId, ETICSRef);
    	}
    	
    	if(version.endsWith("-SNAPSHOT")){
    		if(!acceptSnapshots)	
    			dl.addError("SNAPSHOT version found for artifact "+groupId+":"+artifactId, ETICSRef);
    	}
    	else {
    		if(acceptSnapshots)		
    			dl.addError("RELEASE version found for artifact "+groupId+":"+artifactId, ETICSRef);
    	}		
    	
    	if(profile != null){
    		if(packageName == null || packageName.equals("")){
	        	dl.addError("Impossible to find any package with artifactId \""+artifactId+"\" in profile.xml", ETICSRef);
    		}
    		else {
	    	
	    		//check files
	    		File packageFolder = new File(SADir + File.separator + packageName);
	    		if(!packageFolder.exists()){
	    			dl.addError("folder for package "+packageName+" does not exist", ETICSRef);
	    		}
	        	NodeList files = 
	        			(NodeList) xPath.evaluate("//Files[../MavenCoordinates/artifactId/text()=\""+artifactId+"\"]/File | //GARArchive[../MavenCoordinates/artifactId/text()=\""+artifactId+"\"]", profile, XPathConstants.NODESET);
	
	        	for(int j = 0; j<files.getLength(); j++){
	    	    	String f = files.item(j).getTextContent();
	    	    	File t = new File(SADir + File.separator + packageName + File.separator + f);
	    	    	if(!t.exists()){
	    	    		dl.addError("file "+f+" does not exist in package "+packageName, ETICSRef);
	    	    	}
	    	    }

    		}
    	}
	}
	
	
	
	
	private static void processSA(File distributionReport, DistributionLog dl, File SGReport, File artifactDir, String ETICSRef) throws Exception{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        
		if(!artifactDir.isDirectory()){
			System.err.println("Ther artifact directory "+artifactDir+" does not exist. Cannot continue");
			dl.addError("servicearchive package not valid or not existent", ETICSRef);
			return;
		}

        
        
        String wikidocURL = null;
        File readmeFile = new File(artifactDir + File.separator + "README");
        //check README existence
        if(!readmeFile.exists()){
        	dl.addWarning("README file not found", ETICSRef);
        }
        else {
        	//check existence of wikidoc url in README
			String readmeText = new Scanner(readmeFile).useDelimiter("\\Z").next();
			Pattern p = Pattern.compile(WIKIDOC_MATCHING_PATTERN);
	        Matcher m = p.matcher(new String(readmeText));
	        if(m.find()){
	        	wikidocURL= m.group();
	        }
	        else {
	        	dl.addWarning("wikidoc URL not found in README", ETICSRef);
	        }
        }
        
        
        //load global svnpath, if present
        String globalSVN = null;
        File globalSvnpath = new File(artifactDir + File.separator + "svnpath.txt");
        if(globalSvnpath.exists()){
        	globalSVN = new Scanner(globalSvnpath).useDelimiter("\\Z").next();			
        }
        
        //check profile.xml
        File profileFile = new File(artifactDir + File.separator + "profile.xml");
        Document docProfile = null;
        if(!profileFile.exists()){
        	dl.addError("profile.xml not found", ETICSRef);
        }	
        else {
			docProfile = dBuilder.parse(profileFile);
			
			//check serviceVersion = 1.0.0
    		String serviceVersion = (String) xPath.evaluate("//Profile/Version/text()", docProfile, XPathConstants.STRING);
    		Version v = new Version(serviceVersion);
			String normalizedVersion = v.getNormalizedRepresentation();
			if(!normalizedVersion.equals("1.0.0-0")){
				dl.addError("serviceVersion not equals to 1.0.0 (was "+serviceVersion+")", ETICSRef);
			}
        }
        
        //check SGReport existence
		if(!SGReport.exists()){
			dl.addError("SG Report not found", ETICSRef);
			//BLOCKING ERROR! Return
			return;
		}
        Document SGReportDoc = dBuilder.parse(SGReport);
		Document distributionReportDoc = dBuilder.parse(distributionReport);
		Node dreportPackages = (Node) xPath.evaluate("/Packages", distributionReportDoc, XPathConstants.NODE);

        
        
        //5. iterate over <Package> in SGReport
        NodeList SGpackageNodes = 
                (NodeList) xPath.evaluate("/Packages/Package", SGReportDoc, XPathConstants.NODESET);
        
        for (int i = 0; i < SGpackageNodes.getLength(); i++) {
        	Node packageNode = SGpackageNodes.item(i);
        	
        	String artifactId = (String) xPath.evaluate("artifactID", packageNode, XPathConstants.STRING);
        	
        	String sgStatus = (String) xPath.evaluate("Status", packageNode, XPathConstants.STRING);
        	if(sgStatus.equals("ERROR")){
        		dl.addError("SG Report Status is ERROR for artifactId "+artifactId, ETICSRef);      		
        	} else if(sgStatus.equals("WARN")){
        		dl.addWarning("SG Report Status is WARN for artifactId "+artifactId, ETICSRef);      
        	} else if(sgStatus.equals("SUCCESS")){
        		dl.addInfo("SG Report Status is SUCCESS for artifactId "+artifactId, ETICSRef);      
        	}  else {
        		dl.addError("SG Report Status is unknown for artifactId "+artifactId, ETICSRef);      
        	}  
        	
    		
    		String packageName = (String) xPath.evaluate("//Name[../MavenCoordinates/artifactId/text()=\""+artifactId+"\"]/text()", docProfile, XPathConstants.STRING);

        	validatePackage(packageNode, docProfile, dl, artifactDir, ETICSRef, packageName);
        	
			//check svnpath
        	String svnpath = globalSVN;
    		if(svnpath == null){
				try {
					svnpath = new Scanner(new File(artifactDir + File.separator + packageName + File.separator + "svnpath.txt")).useDelimiter("\\Z").next();
				}
				catch (Exception e) {
					dl.addWarning("svnpath.txt not found for packate " + packageName, ETICSRef);
				}  
    		}
    		
    		
        	
    		
    		//4.3 add wikidoc, svnpath, ETICSRef nodes to package
    		Element el = packageNode.getOwnerDocument().createElement("wikidoc");
    		el.setTextContent(wikidocURL);
    		packageNode.appendChild(el);
    		
    		Element el2 = packageNode.getOwnerDocument().createElement("svnpath");
    		el2.setTextContent(svnpath);
    		packageNode.appendChild(el2);

    		Element el3 = packageNode.getOwnerDocument().createElement("ETICSRef");
    		el3.setTextContent(ETICSRef);
    		packageNode.appendChild(el3);
    		
    		
    		//4.4 add package to distribution.xml
    		Node packageImported = distributionReportDoc.importNode(packageNode, true);
    		dreportPackages.appendChild(packageImported);
        }
        
        
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "8");
		DOMSource source = new DOMSource(distributionReportDoc);
		StreamResult result = new StreamResult(distributionReport);
		transformer.transform(source, result);       
	}

	
	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws Exception {
		
		Option helpOpt = OptionBuilder.withLongOpt("help").hasArg(false).withDescription("print this message").create("h");

		Option dreportOpt = OptionBuilder.withLongOpt("dreport").withArgName("DISTRIBUTION.XML").hasArg(true).withDescription("the distribution.xml file to use as output. By default it is distribution.xml").create("d");
		Option packagesOpt = OptionBuilder.withLongOpt("packages").withArgName("PACKAGES.XML").hasArg(true).withDescription("packages report. It is returned by the software gatewasy client. By default it is report.xml").create("p");		
		Option artifactNameOpt = OptionBuilder.withLongOpt("artifactname").withArgName("FOLDER").hasArg(true).withDescription("the artifact name").create("a");		
		Option artifactDirOpt = OptionBuilder.withLongOpt("artifactdir").withArgName("FOLDER").hasArg(true).withDescription("the artifact directory").create("r");		
		Option logReportOpt = OptionBuilder.withLongOpt("logreport").withArgName("DISTRIBUTION_LOG.XML").hasArg(true).withDescription("the default is distribution_log.xml").create("l");		
		Option acceptSnapshotsOpt = OptionBuilder.withLongOpt("snapshots").withDescription("whether postifx verion with -SNAPSHOT or not.").create("s");

		
		// create the Options
		Options options = new Options();
		options.addOption(helpOpt);
		options.addOption(dreportOpt);
		options.addOption(packagesOpt);
		options.addOption(artifactNameOpt);
		options.addOption(artifactDirOpt);
		options.addOption(logReportOpt);
		options.addOption(acceptSnapshotsOpt);
		
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args);

		if (cmd.hasOption(helpOpt.getOpt())) {
			printHelp(options);
			return;
		}
		
		
		if(cmd.hasOption("s")){
			acceptSnapshots = true;
		}
		
		File dReport = null;
		if(cmd.hasOption("d")){
			dReport = new File(cmd.getOptionValue("d"));
		}
		else {
			dReport = new File("distribution.xml");
		}
		
		File logReport = null;
		if(cmd.hasOption("l")){
			logReport = new File(cmd.getOptionValue("l"));
		}
		else {
			logReport = new File("distribution_log.xml");
		}	
		
		File packagesReport = null;
		if(cmd.hasOption("p")){
			packagesReport = new File(cmd.getOptionValue("p"));
		}
		else {
			packagesReport = new File("report.xml");
		}	
		
		File artifactDir = null;
		if(cmd.hasOption("r")){
			artifactDir = new File(cmd.getOptionValue("r"));
		}
		else {
			artifactDir = new File("artifacttmp");
		}	
		
		String artifactName = cmd.getOptionValue("a");

		
		String command = args[0];
		if(command.equals("init")){
			initializeDistributionReport(dReport, logReport);
		}
		else if(command.equals("add")){
			DistributionLog distributionLog = DistributionLog.load(logReport);
			try{
			processSA(dReport, distributionLog, packagesReport, artifactDir, artifactName);
			} catch (Exception e) {
				e.printStackTrace();
				distributionLog.addError("Error processing servicearchive", artifactName);
			}
			distributionLog.save();
		}
		else {
			System.err.println("command (first parameter) must be one of 'init' or 'add'. It was: "+command);
		}
	}

}
