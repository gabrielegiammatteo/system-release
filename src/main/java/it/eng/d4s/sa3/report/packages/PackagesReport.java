/**
 *
 */
package it.eng.d4s.sa3.report.packages;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.report.ReportException;
import it.eng.d4s.sa3.repository.resourcetype.BuildResourceType;
import it.eng.d4s.sa3.util.XMLInitialization;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Gabriele Giammatteo
 *
 */
public class PackagesReport extends XMLInitialization{
    private static final Logger LOGGER = Logger.getLogger(PackagesReport.class);
    
    private Map<String, PackagesModuleReport> packagesByModuleName;
    
    
    public static PackagesReport getInstance(Build b) throws ReportException {
        try {
            if(b.getRepo().existsBResource(BuildResourceType.B_PACKAGES_REPORT)) {
                InputStream buildStatusXmlStream = 
                        b.getRepo().getBResourceIS(BuildResourceType.B_PACKAGES_REPORT);    

                    return new PackagesReport(buildStatusXmlStream);
               
            } else {
                throw new ReportException("packages-report.xml not found for build "+b);            
            }
        } catch (Exception e) {
            throw new ReportException("Error loading packagesReport for build "+b);
        }
    }

    private PackagesReport(InputStream buildStatusXmlStream) throws Exception{
        super();
        try {
        
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document document = builder.parse(buildStatusXmlStream); 
            
            this.packagesByModuleName = new HashMap<String, PackagesModuleReport>();
            
            this.accept(document);
        }
        finally {
            if(buildStatusXmlStream!=null){
                try{buildStatusXmlStream.close();}catch (Exception e) {}
            }
        }
    }
    
    public PackagesModuleReport getPackagesModuleReport(String moduleName) {
        return this.packagesByModuleName.get(moduleName);
    }
    
    public Collection<PackagesModuleReport> getAllPackagesModuleReports() {
        return this.packagesByModuleName.values();
    }


    @Override
    protected void accept(Node node) throws Exception {
        String nodeName = node.getNodeName();
        if(nodeName.equals("package")) {
            PackagesModuleReport pmr = null;
            try {
                pmr = new PackagesModuleReport(node);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(pmr != null)
                this.packagesByModuleName.put(pmr.getEticsModuleName(), pmr);
        }
        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            this.accept(children.item(i));
        }   
    }
    
}
