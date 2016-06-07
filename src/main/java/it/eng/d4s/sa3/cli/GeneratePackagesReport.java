/**
 *
 */
package it.eng.d4s.sa3.cli;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.report.packages.util.PackagesReportGenerator;
import it.eng.d4s.sa3.repository.SA3Repository;

import java.io.File;

/**
 * @author Gabriele Giammatteo
 *
 */
public class GeneratePackagesReport {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if(args.length != 4){
            System.out.println("Wrong parameters number. Uasge:");
            System.out.println("GeneratePackagesReport <repositoryPath> <confName> <buildName> <packageModuleMappingFile>");
            System.exit(-1);
        }
        
        String repo = args[0];
        String builtConfiguration = args[1];
        String buildName = args[2];
        String mappingFile = args[3];
        

        SA3Repository sa3repo = new SA3Repository(repo, "GeneratePackagesReport-REPO");

        Build b = sa3repo.getBuild(builtConfiguration, buildName);

        File p = new File(mappingFile);
        
        PackagesReportGenerator.generateReport(b, p);
        
    }

}
