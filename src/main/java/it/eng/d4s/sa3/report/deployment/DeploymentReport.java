/**
 *
 */
package it.eng.d4s.sa3.report.deployment;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.report.ReportException;
import it.eng.d4s.sa3.repository.resourcetype.BuildResourceType;
import it.eng.d4s.sa3.util.XMLInitialization;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Gabriele Giammatteo
 *
 */
public class DeploymentReport extends XMLInitialization {
    
    private Map<String, DeploymentModuleReport> modulesReport;
    
    public static DeploymentReport getInstance(Build b) throws ReportException {
        try {
            if(b.getRepo().existsBResource(BuildResourceType.B_DEPLOYMENT_REPORT)) {
                InputStream is = 
                        b.getRepo().getBResourceIS(BuildResourceType.B_DEPLOYMENT_REPORT);    

                    return new DeploymentReport(is);
               
            } else {
                throw new ReportException("dt.xml not found for build "+b);            
            }
        } catch (Exception e) {
            throw new ReportException("Error loading deploymentReport for build "+b);
        }
    }
    
    public DeploymentReport(InputStream is) throws Exception {
        super();
        try {
        
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document document = builder.parse(is); 
            
            this.modulesReport = new HashMap<String, DeploymentModuleReport>();
            
            this.accept(document);
        }
        finally {
            if(is!=null){
                try{is.close();}catch (Exception e) {}
            }
        }       
    }    
    
    
    public DeploymentModuleReport getDeploymentModuleReport(String moduleName) {
        return this.modulesReport.get(moduleName);
    }


    @Override
    protected void accept(Node node) throws Exception {
        String nodeName = node.getNodeName();
        if(nodeName.equals("SoftwareArchive")) {
            DeploymentModuleReport s = new DeploymentModuleReport(node);
            this.modulesReport.put(s.getModuleName(), s);
        }
        else {
            NodeList children = node.getChildNodes();
            for(int i=0; i<children.getLength(); i++) {
                this.accept(children.item(i));
            }
        }
    }

}
