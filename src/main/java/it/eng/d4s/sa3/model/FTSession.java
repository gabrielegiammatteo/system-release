/**
 *
 */
package it.eng.d4s.sa3.model;

import it.eng.d4s.sa3.repository.subrepository.FTRepository;
import it.eng.d4s.sa3.util.XMLInitialization;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
public class FTSession extends XMLInitialization {
	private static final Logger LOGGER = Logger.getLogger(FTSession.class);
    
    private static DateFormat dateParser = 
            new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    
    private Date startTime = null;
    private Date endTime = null;  
    
    private String name;
    private int totTCExecutions = 0;
    private int numPassed = 0;
    private int numFailed = 0;
    private int numAmbiguous = 0;
    
    
    private boolean invalid = false;
    
    /*
     * in the format moduleName/testsuite/testcase/executionId
     */
    private Set<String> fqTCExecutions = null;
    
    
    
    public FTSession(FTRepository ftRepo, String ftSessionName) {
        this.fqTCExecutions = new HashSet<String>();
        this.name = ftSessionName;
        
        InputStream ftSessionDescriptor = null;
        
        try {
        	ftSessionDescriptor = ftRepo.getFTSessionDescriptor(ftSessionName);
        	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document document = builder.parse(ftSessionDescriptor);
            
            accept(document);
            ftSessionDescriptor.close();
        	
        }
        catch(Exception e) {
            LOGGER.warn("Error initializing FTSessions "+this+". Error was: "+e.getMessage());
            e.printStackTrace();
            this.invalid = true;
        }
        finally {
            if(ftSessionDescriptor!=null){
                try{ftSessionDescriptor.close();}catch (Exception e) {}
            }
        }
    }
    
    public Date getStartTime() {
        return startTime;
    }


    private void setStartTime(Date startTime) {
        this.startTime = startTime;
    }


    public Date getEndTime() {
        return endTime;
    }


    private void setEndTime(Date endTime) {
        this.endTime = endTime;
    }


    public int getTotTCExecutions() {
        return totTCExecutions;
    }


    private void setTotTCExecutions(int totTCExecutions) {
        this.totTCExecutions = totTCExecutions;
    }


    public int getNumPassed() {
        return numPassed;
    }


    private void setNumPassed(int numPassed) {
        this.numPassed = numPassed;
    }


    public int getNumFailed() {
        return numFailed;
    }


    private void setNumFailed(int numFailed) {
        this.numFailed = numFailed;
    }


    public int getNumAmbiguous() {
        return numAmbiguous;
    }


    private void setNumAmbiguous(int numAmbiguous) {
        this.numAmbiguous = numAmbiguous;
    }


    public String getName() {
        return name;
    }


    private void setName(String name) {
        this.name = name;
    }
    
    public boolean isInvalid() {
    	return this.invalid;
    }

    public Set<String> getFullyQualifiedTCExecutionsNameSet() {
        return fqTCExecutions;
    }
    
    
    public String toString() {
    	StringBuffer sb = new StringBuffer("[FTSESSION:"+this.name);
    	
    	if(this.invalid)
    		sb.append(";invalid!");
    	
    	sb.append("]");
    	return sb.toString();
    }
    
 
    
    
    protected void accept(Node node) {
        String nodeName = node.getNodeName();
        
        if(nodeName.equals("testing_session")){
        	this.setName(getAttribute(node, "id"));
        }
        else if(nodeName.equals("started")){
            try {
            	this.setStartTime(dateParser.parse(node.getTextContent().trim()));
            } catch (Exception e) {}
        }
        else if(nodeName.equals("finished")){
            try {
                this.setEndTime(dateParser.parse(node.getTextContent().trim()));
            } catch (Exception e) {}            
        }
        else if(nodeName.equals("execution")) {
            String path = getAttribute(node, "module") + File.separator
                    + getAttribute(node, "testsuite") + File.separator
                    + getAttribute(node, "testcase") + File.separator
                    + node.getTextContent().trim();
            this.fqTCExecutions.add(path);
        }
        else if(nodeName.equals("result")){
            NodeList children = node.getChildNodes();
            for(int i=0; i<children.getLength(); i++) {
                if(children.item(i).getNodeName().equals("all"))
                	this.setTotTCExecutions(
                            Integer.valueOf(children.item(i).getTextContent().trim()));
                else if(children.item(i).getNodeName().equals("passed"))
                	this.setNumPassed(
                            Integer.valueOf(children.item(i).getTextContent().trim()));
                else if(children.item(i).getNodeName().equals("failed"))
                	this.setNumFailed(
                            Integer.valueOf(children.item(i).getTextContent().trim()));
                else if(children.item(i).getNodeName().equals("ambiguous"))
                	this.setNumAmbiguous(
                            Integer.valueOf(children.item(i).getTextContent().trim()));
            }
        }   
        
        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
        	this.accept(children.item(i));
        }        
    }
}
