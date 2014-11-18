package com.oxygenxml.gim;

import java.io.StringWriter;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
 
/**
 * The HTML documentation from a rule looks like this:
 * 
 *  <pre>
 *  <rule name="rule1"> 
 *    <doc> 
 *    <html>
 *      <body>
 *        <title>Task required elements</title>
 *         <p>Although the <codeph>shortdesc</codeph>, <codeph>prolog</codeph>, etc. elements are optional according to the task DTD, we require them to be present in our tasks, so we can use the <codeph>recommendElementInParent</codeph> rule to enforce these additional constraints. Here we add the rules for each optional element that we want to be present.</p>
 *         <p>We want to have short descriptions:</p>
 *       </body>
 *     </html>
 *    </doc>
 *  </pre>
 *  
 *  We want to convert that HTML to a string instead of parsing it into objects.
 */
public class DocHandler implements DomHandler<String, StreamResult> {
 
    private static final String BIO_START_TAG = "<doc";
    private static final String BIO_END_TAG = "</doc>";
 
    private StringWriter xmlWriter = new StringWriter();
 
    public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
        xmlWriter.getBuffer().setLength(0);
        return new StreamResult(xmlWriter);
    }
 
    public String getElement(StreamResult rt) {
        String xml = rt.getWriter().toString();
        int beginIndex1 = xml.indexOf(BIO_START_TAG);
        int beginIndex = xml.indexOf(">", beginIndex1) + 1;
        
        int endIndex = xml.indexOf(BIO_END_TAG);
        StringBuilder doc = new StringBuilder();
        doc.append(xml.substring(beginIndex, endIndex));
        
        return doc.toString();
    }
 
    public Source marshal(String n, ValidationEventHandler errorHandler) {
      throw new IllegalStateException("Not implemented");
    }
 
}