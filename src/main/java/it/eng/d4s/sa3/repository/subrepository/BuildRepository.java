/**
 *
 */
package it.eng.d4s.sa3.repository.subrepository;

import it.eng.d4s.sa3.model.ModuleBuild;
import it.eng.d4s.sa3.repository.RepositoryException;
import it.eng.d4s.sa3.repository.SA3Repository;
import it.eng.d4s.sa3.repository.SubRepository;
import it.eng.d4s.sa3.repository.resourcetype.BuildResourceType;
import it.eng.d4s.sa3.util.GZipReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * 
 * in case of archived builds resources' paths are "symbolic" since they does
 * not exists materially on filesystems.
 * 
 * @author Gabriele Giammatteo
 *
 */
public class BuildRepository extends SubRepository {
    private static final Logger LOGGER = Logger.getLogger(BuildRepository.class);
    
    
    public static final String DIST_DIR = "dist";
    public static final String REPORTS_DIR = "reports";
    
    
    String buildName = null;
    boolean isArchived = false;
    
    public BuildRepository(SA3Repository sa3repo, String buildName, String homeDir, boolean isArchived) {
        super(sa3repo, homeDir);
        
        this.buildName = buildName;
        this.isArchived = isArchived;
    }
    
    public boolean isArchived() {
        return isArchived;
    }

    public String getBResorucePath(BuildResourceType rt) {
        switch (rt) {
            case B_BUILD_STATUS:            return "build-status.xml";
            case B_BUILDER_OUTPUT:          return "builder_output";
            case B_BUILDER_STDERR:			return "builder_stderr";
            case B_CO_SIZES_REPORT:         return REPORTS_DIR + "/d4s-co-sizes/co-sizes.xml";
            case B_CHECKSTYLE_REPORT:       return REPORTS_DIR + "/d4s-checkstyle/checkstyleReport.xml";
            case B_CHECKSTYLE_DATA_DIR:     return REPORTS_DIR + "/d4s-checkstyle/";
            case B_DEPLOYMENT_REPORT:       return "dt.xml";
            case B_FINDBUGS_DATA_DIR:       return REPORTS_DIR + "/findbugs/"; //findbugs has not one report, but a number of report in the findbugs/ direcotry
            case B_DISTRIBUTION_REPORT:     return REPORTS_DIR + "/distribution/distribution.xml";
            case B_DISTRIBUTION_LOG_REPORT: return REPORTS_DIR + "/distribution/distribution_log.xml";
            case B_DISTRIBUTION_EXCEPTIONS: return REPORTS_DIR + "/distribution/distribution_exceptions.txt";
            case B_RELEASE_NOTES:			return REPORTS_DIR + "/distribution/releasenotes.xml";
            case B_CONFIGURATIONS_REPORT:	return REPORTS_DIR + "/configurations-report.xml";
            
            default:                return null;
        }
    }
    
    public String getMResorucePath(BuildResourceType rt, ModuleBuild mb) {
        switch (rt) {
            case M_CHECKOUT_LOG:    return REPORTS_DIR + "/" + mb.getCheckoutLogFile(); 
            case M_BUILD_LOG:       return REPORTS_DIR + "/" + mb.getBuildLogFile();
            
            case M_DEPLOYMENT_REPORT:
                                    /*
                                     * deployment report path is specified in dt.xml file
                                     */
                                    return "[not implemented here!. See source code for datalis]";
        
            case M_FINDBUGS_REPORT: return getBResorucePath(BuildResourceType.B_FINDBUGS_DATA_DIR) + "/" +
                                            mb.getModuleName() + "-" + mb.getConfigurationName() + "/" +
                                            "index-bugs.html";
                                    
            case M_TGZ_ARTEFACT:    return DIST_DIR + "/" +
                                            mb.getProjectName() + "/" +
                                            mb.getModuleName() + "/" +
                                            mb.getVersion().getRawRepresentation() + "/" +
                                            mb.getBuild().getPlatform() + "/" +
                                            mb.getArtefactFilename();
            case M_CHECKSTYLE_HTML_REPORT:  
                                    return getBResorucePath(BuildResourceType.B_CHECKSTYLE_DATA_DIR) + "/" +
                                            "CHECKSTYLE-REPORT-"+ mb.getModuleName()+".html";
 
            case M_CHECKSTYLE_XML_REPORT:  
                                    return getBResorucePath(BuildResourceType.B_CHECKSTYLE_DATA_DIR) + "/" +
                                            "CHECKSTYLE-REPORT-"+ mb.getModuleName()+".xml";
                                    
            case M_HTML_REPORT:
            					return REPORTS_DIR + "/" + "reportModuleDetail-" + mb.getModuleName() + ".html";
            
            default:                return null;
        }
    }
    
    public String getBResourceAbsolutePath(BuildResourceType rt) {
        return super.getAbsoluteResourcePath(getBResorucePath(rt));
    }
    
    public String getMResourceAbsolutePath(BuildResourceType rt, ModuleBuild mb) {
        return super.getAbsoluteResourcePath(getMResorucePath(rt, mb));
    }
    
    public InputStream getBResourceIS(BuildResourceType rt) throws RepositoryException {
        return this.getResourceIS(getBResorucePath(rt));
    }
    
    public InputStream getMResourceIS(BuildResourceType rt, ModuleBuild mb) throws RepositoryException {
        return this.getResourceIS(getMResorucePath(rt, mb));
    }    
    
    public boolean existsBResource(BuildResourceType rt) {
        return this.resourceExists(getBResorucePath(rt));
    }
    
    public boolean existsMResource(BuildResourceType rt, ModuleBuild mb) {
        return this.resourceExists(getMResorucePath(rt, mb));
    }

    /*
     * this method is override to handle also archived builds
     */
    @Override
    public InputStream getResourceIS(String resourcePath)
            throws RepositoryException {
        if(this.isArchived) {
            String file = this.homeDir + ".tar.gz";
            String entry = this.buildName + "/" + resourcePath;
            try {
                byte[] res = GZipReader.getInnerEntry(file, entry);
                return new ByteArrayInputStream(res);    
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        } else {            
            return super.getResourceIS(resourcePath);
        }
    }

    /*
     * this method is override to handle also archived builds
     */
    @Override
    public boolean resourceExists(String resourcePath) {
        if(this.isArchived) {
            String file = this.homeDir + ".tar.gz";
            String entry = this.buildName + "/" + resourcePath;
            try {
                return GZipReader.entryExists(file, entry);  
            } catch (IOException e) {
                return false;
            }
        }
        else {
            return super.resourceExists(resourcePath);
        }
    }
}
