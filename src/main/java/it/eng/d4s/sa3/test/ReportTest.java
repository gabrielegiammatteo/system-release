/**
 *
 */
package it.eng.d4s.sa3.test;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.repository.SA3Repository;

import java.util.Iterator;
import java.util.List;

import org.gcube.tools.report.distribution.DistributionReport;

/**
 * @author Gabriele Giammatteo
 *
 */
public class ReportTest {
    
    
    public static void main(String[] args) throws Exception {
        
 
        SA3Repository sa3repo = new SA3Repository("/home/gabriele/tmp/btrt_test-repo", "test");
   
        Build b = sa3repo.getBuild("org.gcube.2-9-0", "BUILD_29");
        
        DistributionReport dr = DistributionReport.getInstance(b);
     
        System.out.println(dr.getDistributionModuleReport("org.gcube.common.tscharts-datamodel-servicearchive").getStatus());
       
    }

}
