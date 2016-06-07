/**
 *
 */
package it.eng.d4s.sa3.report.checkstyle;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.model.ModuleBuild;
import it.eng.d4s.sa3.report.ReportException;
import it.eng.d4s.sa3.report.checkstyle.util.CheckstyleReportGenerator;
import it.eng.d4s.sa3.repository.resourcetype.BuildResourceType;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Gabriele Giammatteo
 *
 */
public class CheckstyleReport {
    private static final Logger LOGGER = Logger.getLogger(CheckstyleReport.class);
    

    private Map<String, ModuleCheckstyleReport> moduleReports;
    
    public static CheckstyleReport getInstance(Build b) throws ReportException {
        try {
            if(b.getRepo().existsBResource(BuildResourceType.B_CHECKSTYLE_REPORT)) {
                InputStream is = 
                        b.getRepo().getBResourceIS(BuildResourceType.B_CHECKSTYLE_REPORT);    

                    return new CheckstyleReport(is);
               
            } else {
                //try to generate it on the fly
                CheckstyleReportGenerator.generate(b, b.getAllModuleBuilds());

                return CheckstyleReport.getInstance(b);          
            }
        } catch (Exception e) {
            throw new ReportException("Error loading checkStyleReport for build "+b);
        }
    }
    
    public CheckstyleReport(InputStream is) throws Exception {
        super();
        try {
        
            Set<ModuleCheckstyleReport> reportData = 
                (Set<ModuleCheckstyleReport>)CheckstyleReportGenerator.getConfiguratedXStream().fromXML(is);
        
        is.close();
        
        this.moduleReports = new HashMap<String, ModuleCheckstyleReport>();
        
        for(ModuleCheckstyleReport r:reportData) {
            moduleReports.put(r.getModuleName(), r);
        }
        }
        finally {
            if(is!=null){
                try{is.close();}catch (Exception e) {}
            }
        }       
    }


    public ModuleCheckstyleReport getReportForModule(ModuleBuild mb) {
        return this.moduleReports.get(mb.getModuleName());
    }
    
}
