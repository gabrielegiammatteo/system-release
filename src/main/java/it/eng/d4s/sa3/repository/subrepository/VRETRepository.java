/**
 *
 */
package it.eng.d4s.sa3.repository.subrepository;

import java.io.InputStream;

import it.eng.d4s.sa3.repository.RepositoryException;
import it.eng.d4s.sa3.repository.SA3Repository;
import it.eng.d4s.sa3.repository.SubRepository;
import it.eng.d4s.sa3.repository.resourcetype.VRETResourceType;

import org.apache.log4j.Logger;

/**
 * @author Gabriele Giammatteo
 *
 */
public class VRETRepository extends SubRepository{
    private static final Logger LOGGER = Logger.getLogger(VRETRepository.class);
    
    
    private String sessionName;
    
    
    public VRETRepository(SA3Repository sa3Repo, String homeDir, String sessionName) {
        super(sa3Repo, homeDir);
        this.sessionName = sessionName;
    }
    
    
    public String getResorucePath(VRETResourceType rt) {
        switch (rt) {
            case SESSION_DESCRIPTOR_XML:    return "dt.xml";
            default:                return null;
        }
    }    

    
    
    public String getSessionName() {
        return sessionName;
    }


    public InputStream getResourceIS(VRETResourceType rt) throws RepositoryException {
        return this.getResourceIS(this.getResorucePath(rt));
    }
    
    
    public String getResourceAbsolutePath(VRETResourceType rt) {
        return this.getAbsoluteResourcePath(getResorucePath(rt));
    }
    
    public boolean resourceExists(VRETResourceType rt) {
        return this.resourceExists(getResorucePath(rt));
    }
}
