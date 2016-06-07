/**
 *
 */
package it.eng.d4s.sa3.report.vret;

import it.eng.d4s.sa3.report.ReportException;
import it.eng.d4s.sa3.repository.resourcetype.VRETResourceType;
import it.eng.d4s.sa3.repository.subrepository.VRETRepository;
import it.eng.d4s.sa3.util.XMLInitialization;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
public class VRETSession  extends XMLInitialization {
    private static final Logger LOGGER = Logger.getLogger(VRETSession.class);
    
    private static DateFormat dateParser = 
        new SimpleDateFormat("EEE MMM dd hh:mm:ss 'CET' yyyy");

    private String name;

    private String reportFilepath = "";
    private boolean isPassed = false;
    private Date date = null;
    
    
    public static VRETSession getInstance(VRETRepository vretRepo) throws ReportException {
        try {
            InputStream is = vretRepo.getResourceIS(VRETResourceType.SESSION_DESCRIPTOR_XML);
            return new VRETSession(vretRepo.getSessionName(), is);
        }
        catch (Exception e) {
            throw new ReportException("Error loading vretSession for session "+vretRepo);
        }
    }
    
    
    public VRETSession(String name, InputStream descriptorXml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document document = builder.parse(descriptorXml);

        accept(document);

        descriptorXml.close();

        this.name = name;
    }

    protected void accept(Node node) {
        String nodeName = node.getNodeName();
        
        if (nodeName.equals("Report")) {
            this.setReportFilepath(node.getTextContent().trim());
            
        } else if (nodeName.equals("Result")) {
            this.setPassed(node.getTextContent().trim().equals("SUCCESS"));
            
        } else if (nodeName.equals("date")) {
            try{
                this.setDate(dateParser.parse(node.getTextContent().trim()));
            } catch(Exception e) {
                e.printStackTrace();
                LOGGER.error("Error parsing date <"+ node.getTextContent().trim() + "> initializing object: " + this);
            }
        }
        
        //iterate over children nodes
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            this.accept(children.item(i));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReportFilepath() {
        return reportFilepath;
    }

    private void setReportFilepath(String reportFilepath) {
        this.reportFilepath = reportFilepath;
    }

    public boolean isPassed() {
        return isPassed;
    }

    private void setPassed(boolean isPassed) {
        this.isPassed = isPassed;
    }

    public Date getDate() {
        return date;
    }

    private void setDate(Date date) {
        this.date = date;
    }
}
