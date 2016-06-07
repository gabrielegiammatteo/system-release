/**
 *
 */
package it.eng.d4s.sa3.report.deployment;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.eng.d4s.sa3.util.XMLInitialization;

/**
 * @author Gabriele Giammatteo
 *
 */
public class DeploymentModuleReport extends XMLInitialization {
    
    private String status;
    private String moduleName;
    private String reportURL;
    
    public DeploymentModuleReport(Node node) throws Exception {
        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            this.accept(children.item(i));
        }
    }
    
    public int getWarnings() {
        return 0;
    }

    public int getErrors() {
        if("FAILED".equals(this.getStatus()))
            return 1;
        return 0;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatus() {
        return this.status;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getReportURL() {
        return reportURL;
    }

    public void setReportURL(String reportURL) {
        this.reportURL = reportURL;
    }

    @Override
    protected void accept(Node node) throws Exception {
        String nodeName = node.getNodeName();
        if(nodeName.equals("Component"))
            this.setModuleName(node.getTextContent());
        else if(nodeName.equals("Result"))
            this.setStatus(node.getTextContent());
        else if(nodeName.equals("Report"))
            this.setReportURL(node.getTextContent());
    }

}
