/**
 *
 */
package it.eng.d4s.sa3.report.packages;

import it.eng.d4s.sa3.util.Version;
import it.eng.d4s.sa3.util.XMLInitialization;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Gabriele Giammatteo
 *
 */
public class PackagesModuleReport extends XMLInitialization {
    private static final Logger LOGGER = Logger.getLogger(PackagesModuleReport.class);
    
    String serviceName = null;
    String packageName = null;
    Version version = null;
    
    String artefact = null;
    URL wikidocURL = null;
    
    String eticsModuleName = null;
    
    
    public PackagesModuleReport(Node node) throws Exception {
        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            this.accept(children.item(i));
        }        
    }
    

    /**
     * @param serviceName
     * @param packageName
     * @param artefact
     * @param wikidoc
     * @param eticsModule
     */
    public PackagesModuleReport(String serviceName, String packageName,
            String version, String artefact, String wikidoc, String moduleName) {
        super();
        this.setServiceName(serviceName);
        this.setPackageName(packageName);
        this.setVersion(version);
        this.setArtefact(artefact);
        if(wikidoc!=null) this.setWikidocURL(wikidoc);
        this.setEticsModuleName(moduleName);
    }
    
    
    private void setVersion(String version) {
        this.version = new Version(version);
    }

    public Version getVersion() {
        return version;
    }

 
    


    public String getServiceName() {
        return serviceName;
    }


    public String getPackageName() {
        return packageName;
    }


    public String getArtefact() {
        return artefact;
    }


    public URL getWikidocURL() {
        return wikidocURL;
    }


    public String getEticsModuleName() {
        return eticsModuleName;
    }


    private void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    private void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    private void setVersion(Version version) {
        this.version = version;
    }


    private void setArtefact(String artefact) {
        this.artefact = artefact;
    }


    private void setWikidocURL(String wikidocURL) {
        try{
            this.wikidocURL = new URL(wikidocURL);
        }
        catch(MalformedURLException e) {
            LOGGER.warn("Error setting wikidocURL at value "+wikidocURL + " Error was: "+e.getMessage());
            e.printStackTrace();
        }
    }


    private void setEticsModuleName(String eticsModuleName) {
        this.eticsModuleName = eticsModuleName;
    }


    /* (non-Javadoc)
     * @see it.eng.d4science.sa3.util.XMLInitialization#accept(org.w3c.dom.Node)
     */
    @Override
    protected void accept(Node node) throws Exception {
        String nodeName = node.getNodeName();
        if(nodeName.equals("service-name")) {
            this.setServiceName(getTextContent(node));
        }
        else if(nodeName.equals("package-name")) {
            this.setPackageName(getTextContent(node));
        }
        else if(nodeName.equals("package-version")) {
            this.setVersion(getTextContent(node));
        }
        else if(nodeName.equals("artefact")) {
            this.setArtefact(getTextContent(node));
        }
        else if(nodeName.equals("wikidoc")) {
            this.setWikidocURL(getTextContent(node));
        }
        else if(nodeName.equals("package-name")) {
            this.setPackageName(getTextContent(node));
        }        
        else if(nodeName.equals("etics-module")) {
            this.setEticsModuleName(getTextContent(node));
        }             
        
        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            this.accept(children.item(i));
        }        
    }
}
