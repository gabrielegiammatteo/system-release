package org.gcube.tools.report.distribution;

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

public class DistributionReport extends XMLInitialization {
    
    private Map<String, DistributionModuleReport> modulesReport;
    
    public static DistributionReport getInstance(Build b) throws ReportException {
        try {
            if(b.getRepo().existsBResource(BuildResourceType.B_DISTRIBUTION_LOG_REPORT)) {
                InputStream is = 
                        b.getRepo().getBResourceIS(BuildResourceType.B_DISTRIBUTION_LOG_REPORT);    

                    return new DistributionReport(is);
               
            } else {
                throw new ReportException("distribution_log.xml not found for build "+b);            
            }
        } catch (Exception e) {
            throw new ReportException("Error loading distributionReport for build "+b);
        }
    }
    
    public DistributionReport(InputStream is) throws Exception {
        super();
        try {
        
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document document = builder.parse(is); 
            
            this.modulesReport = new HashMap<String, DistributionModuleReport>();
            
            this.accept(document);
        }
        finally {
            if(is!=null){
                try{is.close();}catch (Exception e) {}
            }
        }       
    }    
    
    
    public DistributionModuleReport getDistributionModuleReport(String moduleName) {
        return this.modulesReport.get(moduleName);
    }


    @Override
    protected void accept(Node node) throws Exception {
        String nodeName = node.getNodeName();
        if(nodeName.equals("package")) {
        	DistributionModuleReport s = new DistributionModuleReport(node);
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
