package com.oxygenxml.gim;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.lib.ExtensionFunctionDefinition;

import org.xml.sax.InputSource;

import ro.sync.ecss.extensions.api.access.AuthorXMLUtilAccess;

/**
 * TODO Alex:
 * 1. Expand the sch.
 * 2. Execute XSLT 
 * 3. Map the result into objects
 * 4. Cache the results as associated with the given library. 
 * 5. Add the documentation into tooltips.
 * 
 * Loads a rules library into 
 */
public class BusinessRuleRepository {
  /**
   * A cache with all parsed libraries.
   */
  private Map<String, BusinessRuleLibrary> libraries = new HashMap<String, BusinessRuleLibrary>();
  /**
   * Singleton instance.
   */
  private static BusinessRuleRepository instance = null;
  
  /**
   * Singleton pattern constructor.
   */
  private BusinessRuleRepository() {}
  
  /**
   * @return The singleton instance.
   */
  public static BusinessRuleRepository getInstance() {
    if (instance == null) {
      instance = new BusinessRuleRepository();
    }
    
    return instance;
  }
  
  public BusinessRuleLibrary loadLibrary(AuthorXMLUtilAccess authorXMLAccess, URL library) throws TransformerException, JAXBException {
    // TODO Alex Expand the SCH.
    long timestamp = -1;
    URLConnection openConnection = null;
    try {
      openConnection = library.openConnection();
      timestamp = openConnection.getLastModified();
    } catch (IOException e) {
      if (openConnection instanceof HttpURLConnection) {
        ((HttpURLConnection) openConnection).disconnect();
      }
    }
    
    String libraryLocation = library.toExternalForm();
    BusinessRuleLibrary rulesLibrary = libraries.get(libraryLocation);

    if (rulesLibrary == null || timestamp != rulesLibrary.getTimestamp()) {
      URL extractRulesXSL = getClass().getResource("/extractRules.xsl");

      SAXSource styleSource = new SAXSource(new InputSource(extractRulesXSL.toString()));
      ExtensionFunctionDefinition[] saxonExtensions = new ExtensionFunctionDefinition[0];
      Transformer transformer = authorXMLAccess.createSaxon9HEXSLTTransformerWithExtensions(
          styleSource, saxonExtensions); 


      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      Source xmlSource = new SAXSource(new InputSource(libraryLocation));
      transformer.transform(xmlSource, result);

      rulesLibrary = parseRules(new InputSource(new StringReader(writer.toString())));
      rulesLibrary.setTimestamp(timestamp);
    }
    
    libraries.put(libraryLocation, rulesLibrary);
    
    return rulesLibrary;
  }

  /**
   * Loads the rules from the intermediate XML format.
   * 
   * @param source
   * @return
   * @throws JAXBException
   */
  BusinessRuleLibrary parseRules(InputSource source) throws JAXBException {
    String propname = TransformerFactory.class.getName();
    String propertyVal = System.getProperty(propname);
    try {
      // The JAXP engine tries to set a property (a security mode) not supported by Saxon 6. 
      // So we make sure it will use a transformer that supports that property.
      System.setProperty(propname, "net.sf.saxon.TransformerFactoryImpl");
      JAXBContext jc = JAXBContext.newInstance(BusinessRuleLibrary.class);

      Unmarshaller unmarshaller = jc.createUnmarshaller();
      BusinessRuleLibrary library = (BusinessRuleLibrary) unmarshaller.unmarshal(source);

      return library;
    } finally {
      // Restore the initial property.
      if (propertyVal != null) {
        System.setProperty(propname, propertyVal);
      } else {
        System.getProperties().remove(propname);
      }
    }
  }
}
