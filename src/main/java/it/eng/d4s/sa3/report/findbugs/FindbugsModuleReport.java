/**
 *
 */
package it.eng.d4s.sa3.report.findbugs;

import it.eng.d4s.sa3.report.ReportException;
import it.eng.d4s.sa3.util.XMLInitialization;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Gabriele Giammatteo
 *
 */
public class FindbugsModuleReport extends XMLInitialization{


    
    private String moduleName = null; 
    private int highPriorityWarnings;
    private int mediumPriorityWarnings;
    private int lowPriorityWarnings;
    private boolean processed;
    
    

    public FindbugsModuleReport(Node node) throws ReportException{
        super();
        this.moduleName = getAttribute(node, "name");
        
        try {
            this.accept(node);
        } catch (Exception e) {
            throw new ReportException(e);
        }
        
        if(this.processed == false) {
            throw new ReportException(
                    "Findbugs information not found in " + moduleName + "build-status node.");
        }
    }


    public int getHighPriorityWarnings() {
        return highPriorityWarnings;
    }

    public void setHighPriorityWarnings(int highPriorityWarnings) {
        this.highPriorityWarnings = highPriorityWarnings;
    }
    
    public int getMediumPriorityWarnings() {
        return mediumPriorityWarnings;
    }

    public void setMediumPriorityWarnings(int mediumPriorityWarnings) {
        this.mediumPriorityWarnings = mediumPriorityWarnings;
    }

    public int getLowPriorityWarnings() {
        return lowPriorityWarnings;
    }


    public void setLowPriorityWarnings(int lowPriorityWarnings) {
        this.lowPriorityWarnings = lowPriorityWarnings;
    }
    

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }


    public void parseWarningTag(Node node) {
        String name = this.getAttribute(node, "name");
        int value = Integer.parseInt(this.getAttribute(node, "value"));
        if(name.equals("low") || name.equals("low priority warnings"))
            this.setLowPriorityWarnings(value);
        else if(name.equals("medium") || name.equals("medium priority warnings"))
            this.setMediumPriorityWarnings(value);
        else if(name.equals("high") || name.equals("high priority warnings"))
            this.setHighPriorityWarnings(value);
    }
    
    @Override
    protected void accept(Node node) throws Exception {
        String nodeName = node.getNodeName();
        if(nodeName.equals("metrics") && "Findbugs".equals(this.getAttribute(node, "name"))) {
            this.processed=true;
        }
        if(nodeName.equals("value")) {
            this.parseWarningTag(node);
        }
        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            this.accept(children.item(i));
        } 
    }

}
