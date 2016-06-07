/**
 *
 */
package it.eng.d4s.sa3.report.checkstyle.util;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.model.ModuleBuild;
import it.eng.d4s.sa3.report.ReportException;
import it.eng.d4s.sa3.report.checkstyle.ModuleCheckstyleReport;
import it.eng.d4s.sa3.repository.resourcetype.BuildResourceType;
import it.eng.d4s.sa3.repository.subrepository.BuildRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

/**
 * @author Gabriele Giammatteo
 */
public class CheckstyleReportGenerator {
    private static final Logger LOGGER = Logger.getLogger(CheckstyleReportGenerator.class);
    
    /**
     * Generate the checkstyle report for the given build
     * @param build
     * @param moduleBuildsToProcess
     * @return
     */
    public static void generate(Build build, Collection<ModuleBuild> moduleBuilds) throws ReportException {
        
        /*
         * 1a. check if the build is archived. In this case report cannot be
         *     generated due to performance issues
         */
        if( build.getRepo().isArchived() ) {
            throw new ReportException(
                    "Build is archived. Checkestyle report cannot be created");
        }
        
        /*
         * 1b. check if dir for single-module checkstyle reports exists. If not,
         *     report cannot be generated (we have no data to generate it!) so
         *     an excetpion is thrown
         */
        if( !build.getRepo().existsBResource(BuildResourceType.B_CHECKSTYLE_DATA_DIR)) {
            throw new ReportException(
                        "Checkstyle data directory not found for build" + build);
        }
        
        /*
         * 2. for each moduleBuild in the build, tries to get the checkstyle 
         *    report for that module and process it
         */
        Set<ModuleCheckstyleReport> reportData = 
                new HashSet<ModuleCheckstyleReport>();

        for(ModuleBuild mb:moduleBuilds) {
            ModuleCheckstyleReport rep = processModuleBuild(mb);
            if(rep != null)
                reportData.add(rep);
        }
        
        
        /*
         * 3. store on the filesystem the reportData (this action formally 
         *    create the build checkstyle report
         */        
        try {
            String filepath = build.getRepo().getAbsoluteResourcePath(
                        build.getRepo().getBResorucePath(BuildResourceType.B_CHECKSTYLE_REPORT));
            getConfiguratedXStream().toXML(
                    reportData, 
                    new FileOutputStream(filepath));
        } catch (Exception e) {
            throw new ReportException(e);
        }
        
    }
    
    public static XStream getConfiguratedXStream() {
        XStream xstream = new XStream();
        xstream.alias("ModuleCheckstyleReport", ModuleCheckstyleReport.class);
        return xstream;
    }
    
    
    private static ModuleCheckstyleReport processModuleBuild(ModuleBuild mb) {
        
        BuildRepository repo = mb.getBuild().getRepo();
        
        ModuleCheckstyleReport res = null;
        
        /*
         * 1. add html report info
         */
        if(repo.existsMResource(BuildResourceType.M_CHECKSTYLE_HTML_REPORT, mb)){       
            if(res == null) res = new ModuleCheckstyleReport(mb.getModuleName());
            res.setHasHTMLReport(true);
        }
        
        /*
         * 2. add xml report info
         */
        if(repo.existsMResource(BuildResourceType.M_CHECKSTYLE_XML_REPORT, mb)){
            
            if(res == null) res = new ModuleCheckstyleReport(mb.getModuleName());
            res.setHasXMLReport(true);
            
            try {
                InputStream is = repo.getMResourceIS(BuildResourceType.M_CHECKSTYLE_XML_REPORT, mb);
                res.setErrors(computeNumErrors(is));
                
            } catch (Exception e) {
                LOGGER.warn("Error getting checkstyle report for module "+mb.getModuleName()+": ");
            }
        }
        
        /*
         * 3. return. If neither HTML nor XML reports were found, return null
         */
        return res;
    }
    
    private static int computeNumErrors(InputStream xmlCheckstyleReport) 
            throws ParserConfigurationException, SAXException, IOException {


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlCheckstyleReport);
        xmlCheckstyleReport.close();
        
        Node checkstyle = document.getElementsByTagName("checkstyle").item(0);
        NodeList files = checkstyle.getChildNodes();
        
        int errors = 0;
        
        for (int i=0;i<files.getLength();i++) {
            Node node = files.item(i);
            if (node.getNodeName().equals("file")) {
                NodeList errorList = node.getChildNodes();
                for (int k=0;k<errorList.getLength();k++)
                    if (errorList.item(k).getNodeName().equals("error"))
                        errors += 1;
            }
        }
        
        return errors;
    }
}
