package com.oxygenxml.gim;

import java.io.StringReader;
import java.net.URL;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import junit.framework.TestCase;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.ExtensionFunctionDefinition;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.InputSource;

import ro.sync.ecss.extensions.api.access.AuthorXMLUtilAccess;

/**
 * Tests the loading of the intermediate XML format into Java objects.
 */
public class JAXBLadingTest extends TestCase {

  /**
   * Tests the loading of the intermediate XML format into Java objects.
   * 
   * @throws Exception If it fails.
   */
  public void testLoadJAXB() throws Exception {
    String xml = "<rules>\n" + 
        "    <rule name=\"rule1\">\n"
        + "    <doc>\n"
        + "     <title>Task required elements</title><p>Although the\n" + 
        "          <codeph>shortdesc</codeph>, <codeph>prolog</codeph>, etc. elements are optional according\n" + 
        "        to the task DTD, we require them to be present in our tasks, so we can use the\n" + 
        "          <codeph>recommendElementInParent</codeph> rule to enforce these additional constraints.\n" + 
        "        Here we add the rules for each optional element that we want to be present.</p><p>We want to\n" + 
        "        have short descriptions:</p>\n"
        + "</doc>" +
        "<parameters xmlns=\"http://oxygenxml.com/ns/schematron/params\">"
        + "      <parameter>\n" + 
        "        <name>p1</name>\n" + 
        "        <desc>Specifies the element who's word number should be counted.</desc>\n" + 
        "      </parameter>\n" + 
        "      <parameter>\n" + 
        "        <name>p2</name>\n" + 
        "        <desc>Specifies the minimum number of words that is accepted.</desc>\n" + 
        "      </parameter>\n" +
        "</parameters>" +
        "    </rule>\n" + 
        "    <rule name=\"rule2\">\n"
        + "<doc><title>Second</title></doc>" +
        "<parameters xmlns=\"http://oxygenxml.com/ns/schematron/params\">" +
        "      <parameter>\n" + 
        "        <name>p21</name>\n" + 
        "        <desc>Specifies the element who's word number should be counted.</desc>\n" + 
        "      </parameter>\n" + 
        "      <parameter>\n" + 
        "        <name>p22</name>\n" + 
        "        <desc>Specifies the minimum number of words that is accepted.</desc>\n" + 
        "      </parameter>\n" + 
        "      <parameter>\n" + 
        "        <name>p23</name>\n" + 
        "        <desc>Specifies the maximum number of words that is accepted.</desc>\n" + 
        "      </parameter>\n" + 
        "</parameters>" +
        "    </rule>\n" + 
        "</rules>";
    
    InputSource source = new InputSource(new StringReader(xml));
    List<BusinessRule> rules = BusinessRuleRepository.getInstance().parseRules(source).getRules();
    
    assertNotNull(rules);
    
    assertEquals(2, rules.size());
    
    BusinessRule businessRule = rules.get(0);
    assertEquals("rule1", businessRule.getRuleName());
    List<BusinessRuleParam> params = businessRule.getParams();
    assertEquals(2, params.size());
    assertEquals("[p1, p2]", params.toString());
    assertEquals("<title>Task required elements</title><p>Although the\n" + 
        "          <codeph>shortdesc</codeph>, <codeph>prolog</codeph>, etc. elements are optional according\n" + 
        "        to the task DTD, we require them to be present in our tasks, so we can use the\n" + 
        "          <codeph>recommendElementInParent</codeph> rule to enforce these additional constraints.\n" + 
        "        Here we add the rules for each optional element that we want to be present.</p><p>We want to\n" + 
        "        have short descriptions:</p>\n" + 
        "", businessRule.getDescription());
    
    businessRule = rules.get(1);
    assertEquals("rule2", businessRule.getRuleName());
    
    params = businessRule.getParams();
    assertEquals(3, params.size());
    assertEquals("[p21, p22, p23]", params.toString());
    assertEquals("<title>Second</title>", businessRule.getDescription());
  }
  
  /**
   * We start with a library of rules written in Schematron. The first step is to apply the 
   * StyleSheet that transforms it into the XML format that represents the rules. This XML
   * format is then loaded inside the Java objects. 
   * 
   * @throws Exception If it fails.
   */
  public void testParseRules() throws Exception {
    AuthorXMLUtilAccess authorAccessMock = Mockito.mock(AuthorXMLUtilAccess.class);
    final TransformerFactory newInstance = new TransformerFactoryImpl();
    Answer<Transformer> answer = new Answer<Transformer>() {
      @Override
      public Transformer answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        
        Configuration configuration = ((TransformerFactoryImpl) newInstance).getConfiguration();
        ExtensionFunctionDefinition[] extensions = (ExtensionFunctionDefinition[]) args[1];
        for (ExtensionFunctionDefinition extensionFunctionDefinition : extensions) {
          configuration.registerExtensionFunction(extensionFunctionDefinition);
        }
        
        Controller newTransformer = (Controller) newInstance.newTransformer((Source) args[0]);
        
        return newTransformer;
      }
    };
    Mockito.when(authorAccessMock.createSaxon9HEXSLTTransformerWithExtensions(
        Mockito.<Source>any(), Mockito.<ExtensionFunctionDefinition[]>any())).then(answer);
    
    URL library = getClass().getResource("/library.sch");
    
    BusinessRuleLibrary loadLibrary = BusinessRuleRepository.getInstance().loadLibrary(authorAccessMock, library);
    List<BusinessRule> rules = loadLibrary.getRules();
    
    assertEquals(5, rules.size());
    
    assertRule(rules.get(0), "avoidWordInElement", new String[] {"element", "word", "message"});
  }

  private void assertRule(BusinessRule businessRule, String ruleName, String[] expectedParams) {
    assertEquals(ruleName, businessRule.getRuleName());
    
    List<BusinessRuleParam> params = businessRule.getParams();
    assertEquals(expectedParams.length, params.size());
    for (int j = 0; j < expectedParams.length; j++) {
      assertEquals(expectedParams[j], params.get(j).getName());
    }
  }
}
