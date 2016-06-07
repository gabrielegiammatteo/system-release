/**
 *
 */
package org.gcube.tools.report.distribution;

import it.eng.d4s.sa3.util.XMLInitialization;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Gabriele Giammatteo
 *
 */
public class DistributionModuleReport extends XMLInitialization {
    
    private String moduleName;
    
    private List<String> errors = new LinkedList<String>();
    private List<String> warnings = new LinkedList<String>();
    private List<String> infos = new LinkedList<String>();
    
    public DistributionModuleReport(Node node) throws Exception {
        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            this.accept(children.item(i));
        }
    }

    public List<String> getErrors() {
		return errors;
	}

	public List<String> getWarnings() {
		return warnings;
	}
	
	
	public List<String> getInfos() {
		return infos;
	}

	
	public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    
    public String getStatus(){
    	if(this.errors.size() > 0) return "FAILED";
    	if(this.warnings.size() > 0) return "WARNING";
    	return "SUCCESS";
    }

    @Override
    protected void accept(Node node) throws Exception {
        String nodeName = node.getNodeName();
        if(nodeName.equals("ETICSRef"))
            this.setModuleName(node.getTextContent());
        else if(nodeName.equals("entry"))
            if(this.getAttribute(node, "level").equals("warn"))
            	this.warnings.add(node.getTextContent());
            else if(this.getAttribute(node, "level").equals("error"))
            	this.errors.add(node.getTextContent());
            else if(this.getAttribute(node, "level").equals("info"))
            	this.infos.add(node.getTextContent());        	
    }

}
