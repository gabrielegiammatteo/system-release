/**
 *
 */
package it.eng.d4s.sa3.report.findbugs;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.report.ReportException;
import it.eng.d4s.sa3.report.deployment.DeploymentModuleReport;
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
public class FindbugsReport extends XMLInitialization {
    
    private Map<String, FindbugsModuleReport> modulesReport = null;


    public static FindbugsReport getInstance(Build b) throws ReportException {
        try {
            if(b.getRepo().existsBResource(BuildResourceType.B_BUILD_STATUS)) {
                InputStream is = 
                        b.getRepo().getBResourceIS(BuildResourceType.B_BUILD_STATUS);    

                    return new FindbugsReport(is);
               
            } else {
                throw new ReportException("build-status.xml not found for build "+b);            
            }
        } catch (Exception e) {
            throw new ReportException(e);
        }
    }
    
    public FindbugsReport(InputStream is) throws Exception {
        super();
        try {
        
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document document = builder.parse(is); 
            
            this.modulesReport = new HashMap<String, FindbugsModuleReport>();
            
            this.accept(document);
        }
        finally {
            if(is!=null){
                try{is.close();}catch (Exception e) {}
            }
        }            
    }
    
    
    public FindbugsModuleReport getFindbugsModuleReport(String moduleName) {
        return this.modulesReport.get(moduleName);
    }

    
    
    @Override
    protected void accept(Node node) throws Exception {
        String nodeName = node.getNodeName();
        if(nodeName.equals("module")) {
            FindbugsModuleReport rep = null;
            try {
                rep = new FindbugsModuleReport(node);
            } catch (Exception e) {}
            
            if(rep != null)
                this.modulesReport.put(rep.getModuleName(), rep);
        }
        else {
            NodeList children = node.getChildNodes();
            for(int i=0; i<children.getLength(); i++) {
                this.accept(children.item(i));
            }
        }
    }

}
