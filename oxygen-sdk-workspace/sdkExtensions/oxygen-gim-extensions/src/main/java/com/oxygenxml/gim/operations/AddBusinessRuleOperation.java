package com.oxygenxml.gim.operations;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorDocumentFragment;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AuthorNodeUtil;
import ro.sync.util.URLUtil;

import com.oxygenxml.gim.BusinessRule;
import com.oxygenxml.gim.BusinessRuleLibrary;
import com.oxygenxml.gim.BusinessRuleParam;
import com.oxygenxml.gim.BusinessRuleRepository;
import com.oxygenxml.gim.ui.ec.ECBusinessRuleSelectDialog;
import com.oxygenxml.gim.ui.sa.SABusinessRuleSelectDialog;

/**
 * An operation that adds a new business rule into the document.
 */
public class AddBusinessRuleOperation implements AuthorOperation {
  /**
   * Logger for logging.
   */
  private static final Logger logger = Logger.getLogger(AddBusinessRuleOperation.class.getName());
  /**
   * The location where all the library rules are located.
   */
	private static final String LIBRARY_LOCATION = "libraryLocation";
	
  /**
   * A string representation of an XML fragment. The moved node will be wrapped
   * in this string before moving it in the destination.
   */
  private static final String ARGUMENT_SURROUND_FRAGMENT_LOCATION = "surroundFragment";
  
  /**
   * The insert location argument.
   * The value is <code>insertLocation</code>.
   */
  public static final String ARGUMENT_XPATH_LOCATION = "insertLocation";
  
  /**
   * The insert position argument.
   * The value is <code>insertPosition</code>.
   */
  public static final String ARGUMENT_RELATIVE_LOCATION = "insertPosition";
  
  /**
	 * The arguments for this operation.
	 */
	private ArgumentDescriptor[] arguments;
	
	public AddBusinessRuleOperation() {
	  arguments = new ArgumentDescriptor[] {
	      new ArgumentDescriptor(
	          LIBRARY_LOCATION, 
	          ArgumentDescriptor.TYPE_STRING, 
	          "The location of the abstract rules library."),
	          
	          new ArgumentDescriptor(
	              ARGUMENT_SURROUND_FRAGMENT_LOCATION,
	              ArgumentDescriptor.TYPE_FRAGMENT,
	              "A string representation of an XML fragment. The rule fragment will be wrapped "
	              + "in this string before putting it in the destination."),

        new ArgumentDescriptor(
            ARGUMENT_XPATH_LOCATION, 
            ArgumentDescriptor.TYPE_XPATH_EXPRESSION, 
            "An XPath expression indicating the insert location for the fragment.\n" +
            "Note: If it is not defined then the insert location will be at the caret."),

        // Argument defining the relative position to the node obtained from the XPath location.
        new ArgumentDescriptor(
            ARGUMENT_RELATIVE_LOCATION, 
            ArgumentDescriptor.TYPE_CONSTANT_LIST,
            "The insert position relative to the node determined by the XPath expression.\n" +
                "Can be: " 
                + AuthorConstants.POSITION_BEFORE + ", " +
                AuthorConstants.POSITION_INSIDE_FIRST + ", " +
                AuthorConstants.POSITION_INSIDE_LAST + " or " +
                AuthorConstants.POSITION_AFTER + ".\n" +
                "Note: If the XPath expression is not defined this argument is ignored",
                new String[] {
                AuthorConstants.POSITION_BEFORE,
                AuthorConstants.POSITION_INSIDE_FIRST,
                AuthorConstants.POSITION_INSIDE_LAST,
                AuthorConstants.POSITION_AFTER,
            }, 
            AuthorConstants.POSITION_INSIDE_FIRST)
	  };
	}

	@Override
	public String getDescription() {
		return "Operation that parses a given library of rules and offser an UI in which the user can select a rule and give values for each parameter";
	}

	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args)
			throws IllegalArgumentException, AuthorOperationException {
	  // TODO How this operation works
	  // If we are inside a section with rules, just add it at the caret location.
	  // If we are somewhere else in the document, but we have a rules section, add it at the end of that.
	  // Otherwise, create a rules section and add it there.
	  // TODO Could this cases be expressed in more operations at the action level? And just give some
	  // arguments to the operation? like insertLocation and wrapper string?
	  
	  String libraryLocationArg = (String) args.getArgumentValue(LIBRARY_LOCATION);
	  System.out.println("libraryLocationArg " + libraryLocationArg);
	  if (libraryLocationArg != null) {
	    String expandedLibraryLocation = authorAccess.getUtilAccess().expandEditorVariables(libraryLocationArg, null);
	    URL library = null;
      try {
        library = new URL(expandedLibraryLocation);
      } catch (MalformedURLException e1) {
        // Maybe is not an URL. Try to parse it as a File.
        try {
          library = URLUtil.correct(new File(expandedLibraryLocation));
        } catch (MalformedURLException e) {
          throw new AuthorOperationException("Unable to build an URL from the given library: " + libraryLocationArg);
        }
      }
	    
      try {
        BusinessRuleLibrary rulesLibrary = BusinessRuleRepository.getInstance().loadLibrary(
            authorAccess.getXMLUtilAccess(), 
            library);
        
        BusinessRuleSelector businessRuleSelector = null;
        // TODO make an interface BusinessRuleSelector, selectRule(rulesLibrary)
        if (authorAccess.getWorkspaceAccess().isStandalone()) {
          businessRuleSelector = new SABusinessRuleSelectDialog(
              (JFrame) authorAccess.getWorkspaceAccess().getParentFrame());
        } else {
        	//
          businessRuleSelector = new ECBusinessRuleSelectDialog(
              (Shell) authorAccess.getWorkspaceAccess().getParentFrame());
        }

        
        BusinessRule selectedRule = businessRuleSelector.selectRule(rulesLibrary);
        if (selectedRule != null) {
          Object xpathLocation = args.getArgumentValue(ARGUMENT_XPATH_LOCATION);
          Object relativeLocation = args.getArgumentValue(ARGUMENT_RELATIVE_LOCATION);

          int insertionOffset = authorAccess.getEditorAccess().getCaretOffset();
          if (xpathLocation != null && ((String)xpathLocation).trim().length() > 0) {
            // Evaluate the expression and obtain the offset of the first node from the result
            insertionOffset =
                authorAccess.getDocumentController().getXPathLocationOffset(
                    (String) xpathLocation, (String) relativeLocation);
          }

          String fragment = (String) args.getArgumentValue(ARGUMENT_SURROUND_FRAGMENT_LOCATION);
          AuthorDocumentController ctrl = authorAccess.getDocumentController();
          if (fragment != null) {
            // 1. The fragment is optional. Insert the fragment, if any.
            AuthorDocumentFragment xmlFragment = ctrl.createNewDocumentFragmentInContext(fragment, insertionOffset);
            ctrl.insertFragment(insertionOffset, xmlFragment);

            // 1.2 Relocated the insertion offset inside the first leaf of the fragment.
            AuthorNode firstLeaf = AuthorNodeUtil.getFirstLeaf(xmlFragment);
            if (firstLeaf != null) {
              insertionOffset += firstLeaf.getStartOffset() + 1;
            }
          }

          StringBuilder xmlFragment = new StringBuilder();
          xmlFragment.append(
              "<dl>\n" + 
              "        <dlhead>\n" + 
              "          <dthd>Rule</dthd>\n" + 
              "          <ddhd>");
          xmlFragment.append(selectedRule.getRuleName());
          
          
          xmlFragment.append("</ddhd>" + 
              "        </dlhead>\n");
          List<BusinessRuleParam> params = selectedRule.getParams();
          for (BusinessRuleParam param : params) {
            xmlFragment.append(
                "        <dlentry>\n" + 
                "          <dt>");
            xmlFragment.append(param.getName());
            xmlFragment.append("</dt>\n" + 
                "          <dd></dd>\n" + 
                "        </dlentry>\n");
          }
          
          xmlFragment.append("</dl>");
          
          ctrl.insertXMLFragment(xmlFragment.toString(), insertionOffset);
          
          // Move the caret in the first value that the user have to insert.
          try {
            AuthorNode dlNode = ctrl.getNodeAtOffset(insertionOffset + 1);
            AuthorNode[] findNodes = ctrl.findNodesByXPath("dlentry[1]/dd", dlNode, true, true, true, false);
            if (findNodes != null && findNodes.length > 0) {
              authorAccess.getEditorAccess().setCaretPosition(findNodes[0].getStartOffset() + 1);
            }
          } catch (BadLocationException e) {
            logger.error(e, e);
          }
        }
      } catch (TransformerException e) {
        e.printStackTrace();
        throw new AuthorOperationException("Loading the library failed.", e);
      } catch (JAXBException e) {
        e.printStackTrace();
        throw new AuthorOperationException("Loading the library failed.", e);
      }
	  } else {
	    throw new AuthorOperationException("The library location was not specified.");
	  }
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return arguments;
	}
}
