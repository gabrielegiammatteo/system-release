/**
 *
 */
package it.eng.d4s.sa3.model;

import it.eng.d4s.sa3.util.Version;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Gabriele Giammatteo
 *
 */

/*
 * TODO: extends XMLInitialization
 */
public class ModuleBuild {
    
    protected static DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    protected Set<String> dependenciesIds;
    protected Set<String> dependantsIds;
    
    protected String projectName;
    protected String projectId;


    protected String moduleName;
    protected String moduleId;

    protected String configurationName;
    protected String configurationId;

    protected ModuleType type;

    protected String checkoutLogFile;
    protected String buildLogFile;
    protected String testReportFile;

    protected Date startTime;
    protected Date endTime;
    protected String duration;

    protected String release;
    protected String result;
    protected Version version;
    protected String currentStatus;
    
    protected Boolean hasFailDeps = null;
    protected String artefact;
    
    protected Build build;

    public ModuleBuild() {
        this.dependenciesIds = new HashSet<String>();
        this.dependantsIds = new HashSet<String>();
        this.hasFailDeps = null;
    }
    
    public ModuleBuild(Node node) throws Exception {
        this();
        String nodeName = node.getNodeName();
        if(!nodeName.equals("module"))
            throw new Exception("Bad node name. 'module' expected");
        
        this.setModuleName(this.getAttribute(node, "name"));
        
        this.setCurrentStatus(this.getAttribute(node, "currentstatus"));
        if(currentStatus.equals("Unresolved")){
            //throw new Exception("Module "+moduleName+" has \"Unresolved\" status");
        }
        
        this.setProjectName(this.getAttribute(node, "project"));
        this.setProjectId(this.getAttribute(node, "projectid"));

        
        this.setModuleId(this.getAttribute(node, "id"));

        this.setConfigurationName(this.getAttribute(node, "config"));
        this.setConfigurationId(this.getAttribute(node, "configid"));

        this.setVersion(new Version(this.getAttribute(node, "version")));
        this.setRelease(this.getAttribute(node, "release"));
        
        /*
         * set module type
         */
        String type = this.getAttribute(node, "type");
        if(type != null){ //if status is Unresolved, type is not given
	        if(type.equals("P"))
	            this.setType(ModuleType.PROJECT);
	        else if(type.equals("S") || type.equals("PS"))
	            this.setType(ModuleType.SUBSYSTEM);
	        else
	            this.setType(ModuleType.COMPONENT);
        }
        else {
        	this.setType(ModuleType.UNDEFINED);
        }
        
        
        try {
            String date = this.getAttribute(node, "starttime");
            if(date != null){
                this.setStartTime(df.parse(date));
            }
            else
                this.setStartTime(new Date());
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
        
        try {
            String date = this.getAttribute(node, "endtime");
            if(date!=null) {
                this.setEndTime(df.parse(date));
            }
            else
                this.setEndTime(new Date());
        }
        catch(ParseException e) {
            e.printStackTrace();
        }
        
        //this.setDuration(this.getAttribute(node, "duration"));
        
        this.setCheckoutLogFile(this.getAttribute(node, "checkoutlogfile"));
        this.setBuildLogFile(this.getAttribute(node, "buildlogfile"));
        this.setTestReportFile(this.getAttribute(node, "testreportfile"));

        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            this.accept(children.item(i));
        }
        
    }
   
    public Set<ModuleBuild> getDependencies() {
        Set<ModuleBuild> out = new HashSet<ModuleBuild>();
        for(String configurationName: this.dependenciesIds) {
            out.add(this.build.getModuleBuild(configurationName));
        }
        return out;
    }

    public Set<ModuleBuild> getDependants() {
        Set<ModuleBuild> out = new HashSet<ModuleBuild>();
        for(String configurationName: this.dependantsIds) {
            out.add(this.build.getModuleBuild(configurationName));
        }
        return out;
    }

    private String getAttribute(Node node, String name) {
        Node attribute = node.getAttributes().getNamedItem(name);
        if(attribute!=null && attribute.getTextContent().trim().length()>0)
            return attribute.getTextContent();
        return null;
    }

    public void accept(Node node) {
        String nodeName = node.getNodeName();

        if(nodeName.equals("dependency"))
            this.dependenciesIds.add(this.getAttribute(node, "config"));
        if(nodeName.equals("usedby"))
            this.dependantsIds.add(this.getAttribute(node, "config"));
        if(nodeName.equals("artefact")) {
            String type = this.getAttribute(node, "type");
            if(type.equals("tar.gz"))
                this.setArtefact(this.getAttribute(node, "name"));
        }
        
        NodeList children = node.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            this.accept(children.item(i));
        }
    }
    
    public boolean isBuilt() {
        return "Success".equals(this.getCurrentStatus()) ||
        	   "Checked-out (OSD)".equals(this.getCurrentStatus());
    }
    
    public boolean hasFailedDeps() {
        if(hasFailDeps != null) return hasFailDeps;
        boolean hasFD = false;
        for(ModuleBuild dep : this.getDependencies()) {
            if(dep == null) continue;
            if(!dep.isBuilt() || dep.hasFailedDeps()){
                hasFD = true;
                break;
            }
        }
        this.hasFailDeps = hasFD;
        return hasFailDeps;
    }
    
    
    
    public Set<String> getDependenciesIds() {
        return dependenciesIds;
    }

    public Set<String> getDependantsIds() {
        return dependantsIds;
    }
    
    protected void setDependenciesIds(Set<String> dependenciesIds) {
        this.dependenciesIds = dependenciesIds;
    }

    protected void setDependantsIds(Set<String> dependantsIds) {
        this.dependantsIds = dependantsIds;
    }

    public Build getBuild() {
        return this.build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }
    
    public String getBuildLogFile() {
        return buildLogFile;
    }

    protected void setBuildLogFile(String buildLogFile) {
        this.buildLogFile = buildLogFile;
    }


    public String getCheckoutLogFile() {
        return checkoutLogFile;
    }


    protected void setCheckoutLogFile(String checkoutLogFile) {
        this.checkoutLogFile = checkoutLogFile;
    }


    public String getConfigurationId() {
        return configurationId;
    }


    protected void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }


    public String getConfigurationName() {
        return configurationName;
    }


    protected void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }


    public String getCurrentStatus() {
        return currentStatus;
    }


    protected void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }



    public long getDuration() {
    	try{
    		return (this.getEndTime().getTime()-this.getStartTime().getTime())/1000;
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		return -1;
    	}
    }


    public Date getEndTime() {
        return endTime;
    }


    protected void setEndTime(Date endTime) {
        this.endTime = endTime;
    }


    public String getModuleId() {
        return moduleId;
    }


    protected void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }


    public String getModuleName() {
        return moduleName;
    }


    protected void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    
    public String getProjectId() {
        return projectId;
    }


    protected void setProjectId(String projectId) {
        this.projectId = projectId;
    }


    public String getProjectName() {
        return projectName;
    }


    protected void setProjectName(String projectName) {
        this.projectName = projectName;
    }


    public String getRelease() {
        return release;
    }


    protected void setRelease(String release) {
        this.release = release;
    }


    public String getResult() {
        return result;
    }


    public Date getStartTime() {
        return startTime;
    }


    protected void setStartTime(Date startTime) {
        this.startTime = startTime;
    }



    public String getTestReportFile() {
        return testReportFile;
    }


    protected void setTestReportFile(String testReportFile) {
        this.testReportFile = testReportFile;
    }


    public ModuleType getType() {
        return type;
    }


    protected void setType(ModuleType type) {
        this.type = type;
    }



    public Version getVersion() {
        return version;
    }


    protected void setVersion(Version version) {
        this.version = version;
    }

    public String getArtefactFilename() {
        return artefact;
    }

    protected void setArtefact(String artefact) {
        this.artefact = artefact;
    }
}
