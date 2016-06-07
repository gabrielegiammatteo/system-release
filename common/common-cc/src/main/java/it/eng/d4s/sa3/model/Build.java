/**
 *
 */
package it.eng.d4s.sa3.model;

import it.eng.d4s.sa3.repository.resourcetype.BuildResourceType;
import it.eng.d4s.sa3.repository.subrepository.BuildRepository;
import it.eng.d4s.sa3.util.XMLInitialization;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * This object is partially initialised by parsing the build-status.xml file 
 * 
 * @author Gabriele Giammatteo
 *
 */
public class Build extends XMLInitialization {
    private static final Logger LOGGER = Logger.getLogger(Build.class);
    
    protected static DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
   
    // configurationName > moduleReport
    protected Map<String, ModuleBuild> moduleBuildsByConfigName;

    protected String moduleName;
    protected String configurationName;
    protected String configurationVersion;
    protected String projectConfig;
    protected String platform;     
    protected Date startTime;
    protected Date endTime;
    
    protected BuildRepository repo = null;
    protected String buildName = null;
    protected boolean invalid = false;
    
    protected int nFailedModules = 0;
    protected int nHasFailedDepsModules = 0;
    protected int nModules = 0;
    
    
    public Build() {
        super();
    }
    
    public Build(BuildRepository repo, String bcName, String buildName) {
        this.repo = repo;
        //this.configurationName = bcName;
        this.buildName = buildName;
        this.moduleBuildsByConfigName = new HashMap<String, ModuleBuild>();
        
        InputStream buildStatusXmlStream = null;

        try {
            buildStatusXmlStream = 
                    repo.getBResourceIS(BuildResourceType.B_BUILD_STATUS);
        
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document document = builder.parse(buildStatusXmlStream); 
            
            this.accept(document);
        }
        catch(Exception e) {
            LOGGER.warn("Error initializing build "+this+". Error was: "+e.getMessage());
            e.printStackTrace();
            this.invalid = true;
        }
        finally {
            if(buildStatusXmlStream!=null){
                try{buildStatusXmlStream.close();}catch (Exception e) {}
            }
        }
    }
        
    public boolean isInvalid() {
        return invalid;
    }

    public String getBuildName() {
        return buildName;
    }

    public ModuleBuild getModuleBuild(String moduleConfigurationName) {
        return this.moduleBuildsByConfigName.get(moduleConfigurationName);
    }

    public Collection<ModuleBuild> getAllModuleBuilds() {
        return this.moduleBuildsByConfigName.values();
    }
    
    public int getNFailedModules() {
        return nFailedModules;
    }

    public int getNModules() {
        return nModules;
    }

    public int getNHasFailedDepsModules() {
        return nHasFailedDepsModules;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public String getPlatform() {
        return platform;
    }
    
    public String getModuleName() {
        return moduleName;
    }
    
    public String getProjectConfig(){
    	return projectConfig;
    }


    public String getConfigurationName() {
        return configurationName;
    }


    public String getConfigurationVersion() {
        return configurationVersion;
    }
       
    public BuildRepository getRepo() {
        return repo;
    }
    
    public Map<String, ModuleBuild> getModuleBuildsByConfigName() {
        return moduleBuildsByConfigName;
    }
    
    public void setModuleBuildsByConfigName(
            Map<String, ModuleBuild> moduleBuildsByConfigName) {
        this.moduleBuildsByConfigName = moduleBuildsByConfigName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }
    
    public void setProjetConfig(String projectConfig){
    	this.projectConfig = projectConfig;
    }

    public void setConfigurationVersion(String configurationVersion) {
        this.configurationVersion = configurationVersion;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setRepo(BuildRepository repo) {
        this.repo = repo;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public void setNFailedModules(int failedModules) {
        nFailedModules = failedModules;
    }

    public void setNHasFailedDepsModules(int hasFailedDepsModules) {
        nHasFailedDepsModules = hasFailedDepsModules;
    }

    public void setNModules(int modules) {
        nModules = modules;
    }

/*
 * =============================================================================
 * private method used during object initialization to load data from
 * build-status.xml
 */
    protected void accept(Node node) throws Exception {
        String nodeName = node.getNodeName();
        if(nodeName.equals("module")) {
            ModuleBuild mb = null;
            try {
                mb = new ModuleBuild(node);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(mb != null)
                this.trackNewModuleReport(mb);
        }
        else {
            if(nodeName.equals("project")) {
            	try {
                    String date = this.getAttribute(node, "endtime");
                    if(date!=null) {
                    	this.endTime = df.parse(date);
                    }
                    else
                    	this.endTime = new Date();
                }
                catch(ParseException e) {
                    e.printStackTrace();
                }
            	try {
                    String date = this.getAttribute(node, "starttime");
                    if(date!=null) {
                    	this.startTime = df.parse(date);
                    }
                    else
                    	this.startTime = new Date();
                }
                catch(ParseException e) {
                    e.printStackTrace();
                }
            	this.projectConfig = this.getAttribute(node, "config");
                this.moduleName = this.getAttribute(node, "modulename");
                this.configurationVersion = this.getAttribute(node, "moduleversion");
                this.configurationName = this.getAttribute(node, "moduleconfig");
                this.platform = this.getAttribute(node, "platform");
            }
            NodeList children = node.getChildNodes();
            for(int i=0; i<children.getLength(); i++) {
                this.accept(children.item(i));
            }
        }
    }
    
    protected void trackNewModuleReport(ModuleBuild bsmr) {
        this.moduleBuildsByConfigName.put(bsmr.getConfigurationName(), bsmr);
        bsmr.setBuild(this);
        
        this.nModules++;
        
        if(!bsmr.isBuilt() || bsmr.hasFailedDeps())
            this.nFailedModules++;
        
        if(bsmr.hasFailedDeps())
            this.nHasFailedDepsModules++;
    }
    
/*
 * =============================================================================
 */

    @Override
    public String toString() {
        return "[BUILD:"+this.repo.getHomeDir()+"]";
    }
    
    
}
