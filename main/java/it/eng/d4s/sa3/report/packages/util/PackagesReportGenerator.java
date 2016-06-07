/**
 *
 */
package it.eng.d4s.sa3.report.packages.util;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.model.ModuleBuild;
import it.eng.d4s.sa3.report.ReportException;
import it.eng.d4s.sa3.report.packages.PackagesModuleReport;
import it.eng.d4s.sa3.repository.resourcetype.BuildResourceType;
import it.eng.d4s.sa3.util.GZipReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Gabriele Giammatteo
 *
 */
public class PackagesReportGenerator {
    private static final Logger LOGGER = Logger.getLogger(PackagesReportGenerator.class);
    
    
    //either new or old wiki
    public static final String WIKIDOC_MATCHING_PATTERN = "(https://gcube.wiki.gcube-system.org/gcube/index.php.*)|(https://technical.wiki.d4science.research-infrastructures.eu/documentation.*)";
    //new wiki
    //public static final String WIKIDOC_MATCHING_PATTERN = "https://gcube.wiki.gcube-system.org/gcube/index.php.*";
    // old wiki
    //public static final String WIKIDOC_MATCHING_PATTERN = "https://technical.wiki.d4science.research-infrastructures.eu/documentation.*";
        
    
    /**
     * generate the pacages-report.xml file for the given build
     */
    public static void generateReport(Build b, File packageModuleMappingFile) throws ReportException {
        
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Element rootElement = document.createElement("packages");
            document.appendChild(rootElement);        

            
            Set<PackagesModuleReport> packages = generatePackagesModuleReports(b, packageModuleMappingFile);

            for (Iterator i = packages.iterator(); i.hasNext();) {
                    PackagesModuleReport pmr = (PackagesModuleReport) i.next();
                    
                    Element em = document.createElement("package");
                    rootElement.appendChild(em);
                    
                    Element serviceNameNode = document.createElement("service-name");
                    serviceNameNode.appendChild(document.createTextNode(pmr.getServiceName()));
                    em.appendChild(serviceNameNode);
                    
                    Element packageNameNode = document.createElement("package-name");
                    packageNameNode.appendChild(document.createTextNode(pmr.getPackageName()));
                    em.appendChild(packageNameNode);
                    
                    Element packageVersionNode = document.createElement("package-version");
                    packageVersionNode.appendChild(document.createTextNode(pmr.getVersion().getRawRepresentation()));
                    em.appendChild(packageVersionNode);
                    
                    Element artefactNode = document.createElement("artefact");
                    artefactNode.appendChild(document.createTextNode(pmr.getArtefact()));            
                    em.appendChild(artefactNode);
                    
                    if(pmr.getWikidocURL()!=null) {
                        Element wikidocNode = document.createElement("wikidoc");
                        wikidocNode.appendChild(document.createTextNode(pmr.getWikidocURL().toString())); 
                        em.appendChild(wikidocNode);
                    }
                    
                    Element moduleNode = document.createElement("etics-module");
                    moduleNode.appendChild(document.createTextNode(pmr.getEticsModuleName()));               
                    em.appendChild(moduleNode);
            }
        
            File outputFile = new File(b.getRepo().getBResourceAbsolutePath(BuildResourceType.B_PACKAGES_REPORT));
            
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();
            
            LOGGER.info("writing down packagesReport to "+outputFile.getAbsolutePath());
            
            //write down generated xml in the report's file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            OutputStream out = new FileOutputStream(outputFile);
            StreamResult result =  new StreamResult(out);
            transformer.transform(source, result);
        }
        catch(Exception e){
            throw new ReportException(e);
        }
    }
    
    
    
    private static Set<PackagesModuleReport> generatePackagesModuleReports(Build b, File packageModuleMappingFile) throws FileNotFoundException, IOException {
        
        /*
         * 0. tries to load module-package mapping
         */
        Map<String,String> modulePackageMapping = 
            loadModuleMappings(packageModuleMappingFile);
        
        
        
        /*
         * 1. generate list saSet containing all moduleBuild that are
         *    servicearchive and are built (for failed modules tgz does not exists)
         */
        Set<ModuleBuild> saSet = new HashSet<ModuleBuild>();
        
        Collection<ModuleBuild> mbs = b.getAllModuleBuilds();
        
        for (Iterator iterator = mbs.iterator(); iterator.hasNext();) {
            ModuleBuild mb = (ModuleBuild) iterator.next();
            if(mb.getModuleName().matches("^.*servicearchive.*$") && mb.isBuilt()){
                saSet.add(mb);
            }
        }
        
        /*
         * 2. for each servicearchive...
         */
        Set<PackagesModuleReport> packages =  new HashSet<PackagesModuleReport>();
        
        for (Iterator iterator = saSet.iterator(); iterator.hasNext();) {
            ModuleBuild mb = (ModuleBuild) iterator.next();
            
            String tgzPath = b.getRepo().getMResourceAbsolutePath(BuildResourceType.M_TGZ_ARTEFACT, mb);
            
            // 2.1 extract wikidoc from README (if it exist)
            String wikidoc = null;
            try {
              byte[] readmeText = GZipReader.getInnerEntry(tgzPath, "README");
              wikidoc = extractWikidocLink(readmeText);
            }
            catch(Exception e) {
                LOGGER.warn("README not found in servicearchive "+mb.getArtefactFilename());
            }
            
            //2.2 extract packages definition from profile.xml
            try {
                byte[] profileText = GZipReader.getInnerEntry(tgzPath, "profile.xml");
                acceptProfile(profileText, mb.getArtefactFilename(), wikidoc, modulePackageMapping, packages);
              }
              catch(Exception e) {
                  LOGGER.warn("profile.xml not found in servicearchive "+mb.getArtefactFilename());
              }
            
        }
        return packages;
    }

    
    private static String extractWikidocLink(byte[] readmeText) {
        Pattern p = Pattern.compile(WIKIDOC_MATCHING_PATTERN);
        Matcher m = p.matcher(new String(readmeText));
        
        /*
         * we look for the first occurence of wikidoc-pattern. In case README 
         * contains more wikidoc links, only the first is considerated.
         */
        if(m.find()) return m.group();
        
        return null;
    }
    
    private static Map<String, String> loadModuleMappings(File moduleMappingsFilepath) 
        throws FileNotFoundException, IOException {
    	
    	LOGGER.info("Load mappings from "+ moduleMappingsFilepath);

        Map<String, String> res = new HashMap<String, String>();

        Properties p = new Properties();
        p.load(new FileInputStream(moduleMappingsFilepath));
        //TODO: try to build the map from the entrySet in a more "elegant" way
        res = new HashMap<String, String>();
        Set<Entry<Object, Object>> entries = p.entrySet();
        for (Iterator<Entry<Object, Object>> i = entries.iterator(); i.hasNext();) {
            Entry<Object, Object> entry = i.next();
            LOGGER.info("loading "+(String)entry.getKey() + " - " +(String)entry.getValue());
            res.put(
                ((String) entry.getKey()).trim(),
                ((String) entry.getValue()).trim());
        }
        
        return res;
    }
    
    /*
     * this method process the profile.xml file, generate a PackageModuleReport
     * object for each package defined in profile.xml and put them in packagesMap
     */
    private static void acceptProfile(final byte[] profile, String artefactName, 
                String wikidoc, Map<String,String> modulePackageMapping,
                Set<PackagesModuleReport> packages) throws XPathExpressionException, ReportException {
        
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();

        InputSource inputSource = null;

        /*
         * extract nedded information from profile.xml using XPATH
         * impl.note:
         *   inputSource object is not reusable, so we have to create a new object
         *   every time (source: http://www.coderanch.com/t/129821/
         *   XML-Related-Technologies/XPath-evaluate-exception)
         */

        /* service name */
        inputSource = new InputSource(new ByteArrayInputStream(profile));
        String serviceName = (String) xPath.evaluate("/Resource/Profile/Name",
                inputSource, XPathConstants.STRING);


        if ((serviceName == null) || (serviceName.equals(""))) {
            throw new ReportException("Service Name not defined in profile.xml in servicearchive " + artefactName);
        }
        
        
        /* main package (cardinality: ?) */
        inputSource = new InputSource(new ByteArrayInputStream(profile));
        String mainPackageName = 
                (String) xPath.evaluate("/Resource/Profile/Packages/Main/Name", inputSource, XPathConstants.STRING);

        inputSource = new InputSource(new ByteArrayInputStream(profile));
        String mainPackageVersion = 
                (String) xPath.evaluate("/Resource/Profile/Packages/Main/Version", inputSource, XPathConstants.STRING);

        if ((mainPackageName != null) && (!mainPackageName.equals(""))) {           
            String moduleName = modulePackageMapping.get(serviceName + "." + mainPackageName);
            if(moduleName!=null){
                PackagesModuleReport pmr = new PackagesModuleReport(serviceName,
                            mainPackageName,
                            mainPackageVersion,
                            artefactName,
                            wikidoc,
                            moduleName);
                packages.add(pmr);
            }
        }      
        
        
        /* softwares packages (cardinality: *) */
        inputSource = new InputSource(new ByteArrayInputStream(profile));
        NodeList softwareNodes = 
                (NodeList) xPath.evaluate("/Resource/Profile/Packages/*[name()='Software' or name()='Plugin']", inputSource, XPathConstants.NODESET);
        
        for (int i = 0; i < softwareNodes.getLength(); i++) {
            Node softwareNode = softwareNodes.item(i);
            /*
             * look for <Name> and <Version> tags
             */
            String packageName = null;
            String packageVersion = null;
            NodeList childsNodes = softwareNode.getChildNodes();
            for (int j = 0; j < childsNodes.getLength(); j++) {
                String tagname = childsNodes.item(j).getNodeName();
                if (tagname.equals("Name")) {
                    packageName = childsNodes.item(j).getTextContent();
                } else if (tagname.equals("Version")) {
                    packageVersion = childsNodes.item(j).getTextContent();
                }
            }
            
            if ((packageName != null) && (!packageVersion.equals(""))) {           
                String moduleName = modulePackageMapping.get(serviceName + "." + packageName);
                if(moduleName!=null){
                    PackagesModuleReport pmr = new PackagesModuleReport(serviceName,
                                packageName,
                                packageVersion,
                                artefactName,
                                wikidoc,
                                moduleName);
                    packages.add(pmr);
                }
            }
        }
    }
}
