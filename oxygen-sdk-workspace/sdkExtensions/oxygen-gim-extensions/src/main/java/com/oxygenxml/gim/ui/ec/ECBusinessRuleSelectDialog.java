package com.oxygenxml.gim.ui.ec;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ro.sync.ecss.extensions.commons.table.operations.ListContentProvider;

import com.oxygenxml.gim.BusinessRule;
import com.oxygenxml.gim.BusinessRuleLibrary;
import com.oxygenxml.gim.operations.BusinessRuleSelector;

/**
 * A dialog for selecting a business rule.
 */
public class ECBusinessRuleSelectDialog extends Dialog implements BusinessRuleSelector {
  /**
   * Displays the description of the rule.
   */
  private Browser descriptionPanel;
  /**
   * The combo box for selecting a rule.
   */
  private ListViewer rulesList;
  /**
   * All the available business rules.
   */
  private BusinessRuleLibrary businessRuleLibrary;
  /**
   * Text field in which to add a filter.
   */
  private ECFilterText searchField;
  /**
   * The selected rule.
   */
  private BusinessRule selectedRule;

  public ECBusinessRuleSelectDialog(Shell parentFrame) {
    super(parentFrame);
    setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
  }
  
  protected void configureShell(Shell newShell) {
    newShell.setText("Business Rule");
    super.configureShell(newShell);
  }

  /**
   * Create Dialog area.
   * 
   * @param parent The parent composite.
   * @return The dialog control.
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite) super.createDialogArea(parent);
    composite.setLayout(new GridLayout(2, false));
    
    searchField = new ECFilterText(composite, "Search for business rule") {
      Timer timer = new Timer(false);
      TimerTask previous = null;
      
      @Override
      protected void filterWasChanged(final String text) {
        if (previous != null) {
          previous.cancel();
        }
        
        previous = new TimerTask() {
          @Override
          public void run() {
            Display.getDefault().syncExec(new Runnable() {
              @Override
              public void run() {
                initBusinessRulesModel(text);
              }
            });
          }
        };
        
        timer.schedule(previous, 400);
      }
    };
    
    searchField.getTextWidget().setToolTipText("Use \",\" to specify more patterns");
    GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
    data.horizontalSpan = 2;
    searchField.getTextWidget().setLayoutData(data);
    
    rulesList = new ListViewer(composite);
    data = new GridData(SWT.NONE, SWT.FILL, false, true);
    rulesList.getList().setLayoutData(data);
    rulesList.setContentProvider(new ListContentProvider());
    rulesList.setLabelProvider(new LabelProvider() {
      @Override
      public String getText(Object element) {
        BusinessRule rule = (BusinessRule) element;
        return rule.getRuleName();
      }
    });
    
    // TODO Use a BrowserViewer?
    descriptionPanel = new Browser(composite, SWT.BORDER);
    
    rulesList.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent arg0) {
        syncDescription();
      }
    });
    
    data = new GridData(SWT.FILL, SWT.FILL, true, true);
    data.widthHint = 300;
    data.heightHint = 300;
    descriptionPanel.setLayoutData(data);
    
    initBusinessRulesModel("");
    
    rulesList.getList().setFocus();
    
    return composite;
  }
  
  private void initBusinessRulesModel(String filterText) {
    List<BusinessRule> rules = businessRuleLibrary.getRules();
    String[] patternsAsStrings = filterText.split(",");
    List<String[]> patterns = new ArrayList<String[]>(patternsAsStrings.length);
    for (String pattern : patternsAsStrings) {
      if (pattern.trim().length() > 0) {
        String[] split = pattern.trim().split(" ");
        patterns.add(split);
      }
    }
    
    List<BusinessRule> input = new ArrayList<BusinessRule>();
    for (BusinessRule businessRule : rules) {
      if ("".equals(filterText) || matches(businessRule, patterns)) {
        input.add(businessRule);
      }
    }
    
    rulesList.setInput(input);
    
    if (rules.size() > 0) {
      rulesList.setSelection(new StructuredSelection(rules.get(0)));
      syncDescription();
    }
  }
  
  private boolean matches(BusinessRule businessRule, List<String[]> patterns) {
    boolean matches = false;
    String ruleName = businessRule.getRuleName().toLowerCase();
    String description = businessRule.getDescription().toLowerCase();
    
    for (String[] particles : patterns) {
      // Counts if the particles form the current pattern match.
      boolean tempMatches = true;
      for (String particle : particles) {
        particle = particle.toLowerCase();
        tempMatches = tempMatches && (ruleName.contains(particle) || description.contains(particle));
        if (!tempMatches) {
          break;
        }
      }
      if (tempMatches) {
        matches = true;
        break;
      }
    }
    
    return matches;
  }


  private void syncDescription() {
    selectedRule = (BusinessRule) ((StructuredSelection) rulesList.getSelection()).getFirstElement();
    String text = "<html/>";
    if (selectedRule != null) {
      text = selectedRule.getDescription();
    }
    descriptionPanel.setText(text);
  }

  
  public BusinessRule selectRule(BusinessRuleLibrary businessRuleLibrary) {
    this.businessRuleLibrary = businessRuleLibrary;
    int result = open();
    
    BusinessRule selectedItem = null;
    if (result == Dialog.OK) {
      selectedItem = selectedRule;
    }
    
    System.out.println("result " + result + " sel " + selectedItem);
    
    return selectedItem;
  }
}
