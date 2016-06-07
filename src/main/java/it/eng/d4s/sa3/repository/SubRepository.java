/**
 *
 */
package it.eng.d4s.sa3.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * @author Gabriele Giammatteo
 *
 */
public abstract class SubRepository {
    private static final Logger LOGGER = Logger.getLogger(SubRepository.class);
    
    protected SA3Repository sa3Repo;
    protected String homeDir;

    
    public SubRepository(SA3Repository sa3Repo, String homeDir) {
        super();
        this.sa3Repo = sa3Repo;
        this.homeDir = homeDir;
    }
        
    
    public SA3Repository getSA3Repo() {
        return this.sa3Repo;
    }
    
    public String getHomeDir() {
        return homeDir;
    }

    public String getAbsoluteResourcePath(String resourcePath) {
        return this.homeDir + File.separator + resourcePath;
    }

    public boolean resourceExists(String resourcePath) {
        String absPath = this.homeDir + "/" + resourcePath;
        File res = new File(absPath);
        return res.exists();
    }
    
    
    
    public InputStream getResourceIS(String resourcePath) throws RepositoryException {          
        String absPath = this.getAbsoluteResourcePath(resourcePath);
        try{
            InputStream is = new FileInputStream(absPath);
            LOGGER.debug("Resource "+absPath+" loaded.");
            return is;
        }
        catch(Exception e){
            LOGGER.error("Error loading resource "+absPath+". Error was: " + e.getMessage());
            throw new RepositoryException(e);
        }
    }


    @Override
    public String toString() {
        return "[REPO:"+ this.homeDir + "]";
    }
}
