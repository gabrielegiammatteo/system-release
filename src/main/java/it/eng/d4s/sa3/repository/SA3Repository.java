/**
 *
 */
package it.eng.d4s.sa3.repository;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.repository.subrepository.BuildRepository;
import it.eng.d4s.sa3.repository.subrepository.FTRepository;
import it.eng.d4s.sa3.repository.subrepository.VRETRepository;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Gabriele Giammatteo
 *
 */
public class SA3Repository {
    
	public static final String BUILD_NAME_PREFIX = "BUILD_";
    public static final String BUILD_NAME_PATTERN = "^"+BUILD_NAME_PREFIX+"[0-9]+$";
    
    public static final String ARCHIVED_BUILD_FILE_EXTENSION = ".tar.gz";
    
    String homeDir = null;
    String name = null;

    /**
     * @param homeDir
     * @param name
     */
    public SA3Repository(String homeDir, String name) throws RepositoryException{
        super();
        
        File homeDirF = new File(homeDir);
        if(!homeDirF.exists()) throw new RepositoryException("Repository's homeDir  "
                    + homeDir + " does not exist. Impossible to initialize repository.");
        
        this.homeDir = homeDir;
        this.name = name;
    }
    
    public VRETRepository getVRETRepository(String bcName, String sessionName) {
        return new VRETRepository(this, this.homeDir + "/" + bcName + "/VRE_REPORTS/" + sessionName, sessionName);
    }
    
    public FTRepository getFTRepository(String bcName) throws Exception {
    	String ftHomeDir = this.homeDir + "/" + bcName + "/FT_REPORTS";
    	return new FTRepository(this, ftHomeDir);
    }
    
    public BuildRepository getBuildRepository(String bcName, String buildName) throws Exception{
       String buildHomeDir = this.homeDir + "/" + bcName + "/" + buildName;
       if((new File(buildHomeDir)).isDirectory()) {
           //regular
           return new BuildRepository(this, buildName, buildHomeDir, false);
       }
       else if((new File(buildHomeDir + ".tar.gz")).exists()) {
           //archived build
           return new BuildRepository(this, buildName, buildHomeDir, true);
       }
       else {
           //invalid parameters
           throw new RepositoryException("Build "+buildName+"(conf:"+bcName+") not found in repository: "+this);
       }
    }
    
    public Build getBuild(String bcName, String buildName) throws Exception {
        BuildRepository brepo = this.getBuildRepository(bcName, buildName);
        return new Build(brepo, bcName, buildName);
    }
    
    public List<String> getBuildsList(String bcName, int from, int to) {
    	if((to - from) < 1) return null;
    	
    	List<String> outcome = new LinkedList<String>();
    	
    	for(int i = to; i >= from; i--) {
    		outcome.add(BUILD_NAME_PREFIX + Integer.toString(i));
    	}
    	
    	return outcome;
    }
    
    public Build getLatestBuild(String bcName) throws Exception {
        List<Build> builds = this.getLatestNBuilds(bcName, 1);
        if(builds.size()<1) return null;
        return builds.get(0);
    }
    
    /**
     * if num is -1 all builds will be returned
     * 
     */
    public List<Build> getLatestNBuilds(String bcName, int num) throws Exception {
        List<String> buildsName = this.getLatestNBuildsName(bcName, num);
        List<Build> builds = new LinkedList<Build>();
        
        for (Iterator i = buildsName.iterator(); i.hasNext();) {
            String buildName = (String) i.next();
            builds.add(this.getBuild(bcName, buildName));
        }
        
        return builds;
    }
    
    public Set<String> listAllBuiltConfigurations() {
        return listSubDirs("", "^.*$");
    }
    
    
    public Set<String> listAllVRETSessions(String bcName) {
        return listSubDirs(bcName + "/VRE_REPORTS", "^.*$");       
    }    
    

    public String getHomeDir() {
        return homeDir;
    }

    public String getName() {
        return name;
    }
    
    
    private Set<String> listSubDirs(String path, final String pattern) {
        String baseDir = this.homeDir + File.separator + path;
        
        File[] items = (new File(baseDir)).listFiles(new FileFilter() {
            public boolean accept(File arg0) {
                return arg0.getName().matches(pattern) && (!arg0.isHidden()) && arg0.isDirectory();
            }});
        
        Set<String> out = new HashSet<String>();
        if(items == null) return out;
        
        for (int i = 0; i < items.length; i++) {
            out.add(items[i].getName());
        }
        return out;
    }
    
    
    
    /**
     * returns the name of the latest build accordingly with build names.
     * In this method we assume that build names are in the form 
     * <code>BUILD_&lt;sequence_number&gt;</code>. The build with higher 
     * sequence number is selected.
     * 
     * The algorithm used in this method is inappropriate, though very
     * fast; the best way of selecting latest build would be to get all build's
     * endDate and select the one with the most recent date.
     * 
     * if num is -1 all builds will be returned
     * 
     * @param configuration
     * @return
     */
    public List<String> getLatestNBuildsName(String configurationName, int num) {
        
        Set<String> allBuilds = getAllBuildsName(configurationName);
        if(allBuilds.size()<1) return new LinkedList<String>();
        
        int[] sequence = new int[allBuilds.size()];
        int c = 0;
        for(String b:allBuilds) {
            try {
                sequence[c] = Integer.parseInt(b.substring(6));
            } catch (Exception e){
                continue;
            }
            c++;
        }
        
        Arrays.sort(sequence);
        
        if(num == -1) num = sequence.length;
        
        List<String> res = new LinkedList<String>();
        for(int i = sequence.length - 1; i >= sequence.length - num; i--){
            if(i<0) break;
            res.add(BUILD_NAME_PREFIX + sequence[i]);
        }
        
        return res;
    }    
    
    
    public Set<String> getAllBuildsName(String builtConfiguration) {
        
        /*
         * iterate over configurationHomeDir subdirectories and select all directories
         * and ".tar.gz" files
         */
        File confHomeDir = new File(this.homeDir + File.separator + builtConfiguration);         
        File[] items = confHomeDir.listFiles();        
        Set<String> out = new HashSet<String>();       
        if(items == null) return out;
        
        for (int i = 0; i < items.length; i++) {            
            String name = null;           
            if(items[i].isFile() && items[i].getName().endsWith(ARCHIVED_BUILD_FILE_EXTENSION) ){
                name = items[i].getName().substring(0, 
                        items[i].getName().length() - ARCHIVED_BUILD_FILE_EXTENSION.length());
            } else if(items[i].isDirectory()){
                name = items[i].getName();
            } else {
                continue;
            }
            
            if(name.matches(BUILD_NAME_PATTERN)){
                out.add(name);
                continue;
            }
        }   
        return out;
    }    
}
