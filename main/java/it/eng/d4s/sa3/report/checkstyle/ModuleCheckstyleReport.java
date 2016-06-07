/**
 *
 */
package it.eng.d4s.sa3.report.checkstyle;


/**
 * @author Gabriele Giammatteo
 *
 */
public class ModuleCheckstyleReport {
        
    private String moduleName = null; 
    
    private int errors = 0;
    private boolean hasHTMLReport = false;
    private boolean hasXMLReport = false;
    
    
    /**
     * @param moduleName
     * @param errors
     * @param hasHTMLReport
     * @param hasXMLReport
     */
    public ModuleCheckstyleReport(String moduleName) {
        super();
        this.moduleName = moduleName;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public boolean hasHTMLReport() {
        return hasHTMLReport;
    }

    public void setHasHTMLReport(boolean hasHTMLReport) {
        this.hasHTMLReport = hasHTMLReport;
    }

    public boolean hasXMLReport() {
        return hasXMLReport;
    }

    public void setHasXMLReport(boolean hasXMLReport) {
        this.hasXMLReport = hasXMLReport;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    
}
