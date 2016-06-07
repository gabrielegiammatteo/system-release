/**
 *
 */
package it.eng.d4s.sa3.util;

import org.w3c.dom.Node;

/**
 * @author Gabriele Giammatteo
 *
 */
public abstract class XMLInitialization {
    

    protected abstract void accept(Node node) throws Exception;
    
    protected String getAttribute(Node node, String name) {
        Node attribute = node.getAttributes().getNamedItem(name);
        if(attribute!=null && attribute.getTextContent().trim().length()>0)
            return attribute.getTextContent();
        return null;
    }
    
    protected String getTextContent(Node node) {
        return node.getTextContent().trim();
    }

}
