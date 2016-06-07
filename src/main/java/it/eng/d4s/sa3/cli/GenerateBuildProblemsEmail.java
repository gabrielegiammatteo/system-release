package it.eng.d4s.sa3.cli;

import it.eng.d4s.sa3.model.Build;
import it.eng.d4s.sa3.model.ModuleBuild;
import it.eng.d4s.sa3.repository.SA3Repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

public class GenerateBuildProblemsEmail {
	
	private static String csvToTableScript = null;
	
	
	private static String executeScript(String input) {
		String out = "";
		
        try
        {
            Runtime r = Runtime.getRuntime();
            Process p = r.exec("python " + csvToTableScript);
            
            OutputStream os = p.getOutputStream();
            os.write(input.getBytes());
            os.close();

            p.waitFor();
            
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            
            String line;
            while ((line = br.readLine()) != null)
            	out += "\n" + line;

            p.waitFor();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return out;
	}
	

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
        if(args.length != 4){
            System.out.println("Wrong parameters number. Uasge:");
            System.out.println("GenerateBuildProblemsEmail <repositoryPath> <confName> <buildName> <csv2tableScript>");
            System.exit(-1);
        }
        
        String repo = args[0];
        String builtConfiguration = args[1];
        String buildName = args[2];
        String cvs2tableScript = args[3];
        
        GenerateBuildProblemsEmail.csvToTableScript = cvs2tableScript;
        

        SA3Repository sa3repo = new SA3Repository(repo, "GeneratePackagesReport-REPO");

        Build b = sa3repo.getBuild(builtConfiguration, buildName);
        
        
        
        
        String input = "component";
        Collection<ModuleBuild> mbs = b.getAllModuleBuilds();
        for (Iterator iterator = mbs.iterator(); iterator.hasNext();) {
			ModuleBuild mb = (ModuleBuild) iterator.next();
			input += mb.getModuleName() + "\n";
		}
        
        
        System.out.println(executeScript(input));

	}

}
