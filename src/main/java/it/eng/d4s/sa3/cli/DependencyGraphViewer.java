package it.eng.d4s.sa3.cli;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.model.ModuleBuild;
import it.eng.d4s.sa3.repository.SA3Repository;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class DependencyGraphViewer {
    public static void main(String[] args) throws Exception {
        if(args.length != 3){
            System.out.println("Wrong parameters number. Uasge:");
            System.out.println("DependencyGraphViewer <repositoryPath> <confName> <buildName>");
            System.exit(-1);
        }
        
        String repo = args[0];
        String builtConfiguration = args[1];
        String buildName = args[2];
        

        SA3Repository sa3repo = new SA3Repository(repo, "GeneratePackagesReport-REPO");

        Build b = sa3repo.getBuild(builtConfiguration, buildName);
 
        StringBuffer data = new StringBuffer("var data = [\n");

        Collection<ModuleBuild> mbs = b.getAllModuleBuilds();

        for (Iterator i = mbs.iterator(); i.hasNext();) {
        	ModuleBuild mb = (ModuleBuild) i.next();
        	String mbName = mb.getModuleName();
        	
        	if(!mbName.startsWith("org.gcube") ||
        		mbName.startsWith("org.gcube.ext") ||
        		mbName.endsWith("testsuite") ||
        		mbName.endsWith("serviearchive"))
        			continue;
        	
        	int size = 1000;
        	
        	data.append("{\"name\":\""+mbName+"\",\"size\":"+size+",\"imports\":[");
        	
        	Set<ModuleBuild> deps = mb.getDependencies();
        	for (Iterator i2 = deps.iterator(); i2.hasNext();) {
        		ModuleBuild mb2 = (ModuleBuild) i2.next();
        		String mb2Name = mb2.getModuleName();
        		
        		if(!mb2Name.startsWith("org.gcube") ||
        			mb2Name.startsWith("org.gcube.ext") ||
        			mb2Name.endsWith("testsuite") ||
            		mb2Name.endsWith("serviearchive"))
        					continue;
        		
        		data.append("\""+mb2Name+"\",");
        	}
        	if(data.lastIndexOf(",") == data.length()-1) data.setLength(data.length()-1);
        	data.append("]},\n");
        }
        if(data.lastIndexOf(",") == data.length()-1) data.setLength(data.length()-1);
        data.append("];");
        
        System.out.println(data);
        
    }
}
