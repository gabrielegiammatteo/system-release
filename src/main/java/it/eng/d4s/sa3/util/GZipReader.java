/**
 *
 */
package it.eng.d4s.sa3.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

/**
 * @author Gabriele Giammatteo
 *
 */
public class GZipReader {
    private static final Logger LOGGER = Logger.getLogger(GZipReader.class);
   
    
    /*
     * this method return an byte array and not as natural an InputStream, because
     * what we do is:
     * * extract the selected entry in a temporary file
     * * load the file in the byte array
     * * delete temporary file
     * 
     * If we return an input stream we could not delete temporary file before
     * method's return
     */
    public static byte[] getInnerEntry(String gZipArchive,
            String entryName)
            throws IOException {
        
        File tmpDir = new File(
                    System.getProperty("java.io.tmpdir") + File.separator 
                    + "GZipReader_" + Long.toString(System.nanoTime()));
        
        tmpDir.mkdir();
        
        String cmdLine = "tar xzf " + gZipArchive + " -C " + tmpDir.getAbsolutePath() + " " + entryName;
        LOGGER.debug("executing: " + cmdLine);
        byte[] res = null;
        
        try {
            Process proc = Runtime.getRuntime().exec(cmdLine);
            
            String line;
            BufferedReader input = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((line = input.readLine()) != null) {
                LOGGER.debug(line);
            }
            input.close();
            
            File extractedFile = new File(tmpDir.getAbsoluteFile() + File.separator + entryName);

            res = file2byteArray(extractedFile);        
        }
        finally {
            deleteDir(tmpDir);
        }
        
        return res;
    }
    
    
    public static boolean entryExists(String gZipArchive, String entryName)
            throws IOException {
        
        String cmdLine = "tar tzf " + gZipArchive + " " + entryName;
        
        LOGGER.debug("executing: " + cmdLine);
        Process proc = Runtime.getRuntime().exec(cmdLine);
        
        //write down standard error
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        while ((line = input.readLine()) != null) {
            LOGGER.debug(line);
        }
        input.close();
        
        //get the first line of standard output (it should be exactly the entry's name)
        input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        line = input.readLine();
        input.close();   
        
        if ((line != null) && (line.equals(entryName)))
            return true;
        else
            return false;
    }

    
    private static byte[] file2byteArray(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = new FileInputStream(file);
        
        byte[] buffer = new byte[1024];
        int len;

        while((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        in.close();
        out.close();
        return out.toByteArray();        
    }
    
    
    /*
     * delete recursively a directory
     * http://www.exampledepot.com/egs/java.io/DeleteDir.html
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
