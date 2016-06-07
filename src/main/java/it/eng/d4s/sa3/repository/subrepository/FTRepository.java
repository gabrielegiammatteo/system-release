/**
 *
 */
package it.eng.d4s.sa3.repository.subrepository;

import it.eng.d4s.sa3.model.FTSession;
import it.eng.d4s.sa3.repository.RepositoryException;
import it.eng.d4s.sa3.repository.SA3Repository;
import it.eng.d4s.sa3.repository.SubRepository;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Gabriele Giammatteo
 *
 */
public class FTRepository extends SubRepository {
	private static final Logger LOGGER = Logger.getLogger(FTRepository.class);
	
	private static final String DESCRIPTORS_PATH = "__FT_SESSIONS";
	private static final String DESCRIPTORS_PATTERN = "^FT_[0-9]{3}\\.xml$";

	public FTRepository(SA3Repository sa3Repo, String homeDir) {
		super(sa3Repo, homeDir);
	}
	
	
	public InputStream getFTSessionDescriptor(String FTSessionName) throws RepositoryException {
		String resourcePath = DESCRIPTORS_PATH + "/" + FTSessionName + ".xml";
		return getResourceIS(resourcePath);
	}
	
	
	public FTSession getFTSession(String name) {
		return new FTSession(this, name);
	}
	
	
	public Set<FTSession> getAllFTSessions() {
        String baseDir = homeDir + "/" + DESCRIPTORS_PATH;
        
        File[] items = (new File(baseDir)).listFiles(new FileFilter() {
            public boolean accept(File arg0) {
                return arg0.isFile() && arg0.getName().matches(DESCRIPTORS_PATTERN);
            }});
        
        Set<FTSession> out = new HashSet<FTSession>();
        if(items == null) return out;
        
        for (int i = 0; i < items.length; i++) {
            out.add(getFTSession(items[i].getName().substring(0, items[i].getName().length() - 4)));
        }
        return out;
	}

}
