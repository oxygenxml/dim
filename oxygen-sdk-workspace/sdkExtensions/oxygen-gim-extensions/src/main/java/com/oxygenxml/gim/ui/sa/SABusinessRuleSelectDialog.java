package com.oxygenxml.gim.ui.sa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import ro.sync.ecss.extensions.commons.ui.OKCancelDialog;
import ro.sync.ui.application.OkCancelAndOtherDialogConstants;

import com.oxygenxml.gim.BusinessRule;
import com.oxygenxml.gim.BusinessRuleLibrary;
import com.oxygenxml.gim.operations.BusinessRuleSelector;

/**
 * A dialog for selecting a business rule.
 */
public class SABusinessRuleSelectDialog extends OKCancelDialog implements BusinessRuleSelector {
  /**
   * Displays the description of the rule.
   */
  private JEditorPane descriptionPanel;
  /**
   * The combo box for selecting a rule.
   */
  private JList rulesList;
  /**
   * All the available business rules.
   */
  private BusinessRuleLibrary businessRuleLibrary;
  /**
   * Text field in which to add a filter.
   */
  private JTextField searchField;
  /**
   * The last location where the dialog was presented.
   */
  private static Point lastLocation;

  public SABusinessRuleSelectDialog(JFrame parentFrame) {
    super(parentFrame, "Business Rule", true);
    
    if (lastLocation != null) {
      setLocation(lastLocation);
    }
    
    initControls();
    
    pack();
    
    setLocationRelativeTo(parentFrame);
  }

  
  private void initControls() {
    searchField = new JTextField() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (getText().length() == 0) {
          drawHint(this, g, "Search for business rule");
        }
      }
    };
    searchField.setToolTipText("Use \",\" to specify more patterns");
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      Timer timer = new Timer(400, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          initBusinessRulesModel();
        }
      });
      {
        timer.setRepeats(false);
      }
      @Override
      public void removeUpdate(DocumentEvent e) {
        insertUpdate(e);
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
        timer.restart();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        insertUpdate(e);
      }
    });
    
    rulesList = new JList();
    rulesList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value,
          int index, boolean isSelected, boolean cellHasFocus) {
        BusinessRule rule = (BusinessRule) value;
        if (rule != null) {
          value = rule.getRuleName();
        }
        
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });
    
    descriptionPanel = new JEditorPane("text/html", "<html/>");
    
    rulesList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    rulesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        syncDescription();
      }
    });
    
    
    initDescriptionArea(true);
    
    getContentPane().setLayout(new BorderLayout(3, 3));
    getContentPane().add(searchField, BorderLayout.NORTH);
    getContentPane().add(new JScrollPane(rulesList), BorderLayout.WEST);
    getContentPane().add(new JScrollPane(descriptionPanel), BorderLayout.CENTER);
    
    descriptionPanel.setPreferredSize(new Dimension(300, 300));
    pack();
  }
  
  private void initDescriptionArea(boolean useStandardTextFont) {
    if(useStandardTextFont){
      // Forces the JEditorPane to take the font from the UI, rather than the HTML document.
      descriptionPanel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
      Font font = UIManager.getDefaults().getFont("TextArea.font");
      if(font != null){
        setFont(font);
      }
    }
    descriptionPanel.setEditable(false);
    descriptionPanel.setBorder(BorderFactory.createEmptyBorder());
  }

  private void initBusinessRulesModel() {
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    List<BusinessRule> rules = businessRuleLibrary.getRules();
    String filterText = searchField.getText().toLowerCase();
    String[] patternsAsStrings = filterText.split(",");
    List<String[]> patterns = new ArrayList<String[]>(patternsAsStrings.length);
    for (String pattern : patternsAsStrings) {
      if (pattern.trim().length() > 0) {
        String[] split = pattern.trim().split(" ");
        patterns.add(split);
      }
    }
    for (BusinessRule businessRule : rules) {
      if ("".equals(filterText) || matches(businessRule, patterns)) {
        model.addElement(businessRule);
      }
    }

    rulesList.setModel(model);
    if (rules.size() > 0) {
      rulesList.getSelectionModel().setSelectionInterval(0, 0);
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
    BusinessRule selectedItem = (BusinessRule) rulesList.getSelectedValue();
    String text = "<html/>";
    if (selectedItem != null) {
      text = selectedItem.getDescription();
    }
    descriptionPanel.setText(text);
  }

  
  public BusinessRule selectRule(BusinessRuleLibrary businessRuleLibrary) {
    this.businessRuleLibrary = businessRuleLibrary;
    initBusinessRulesModel();
    setVisible(true);
    
    BusinessRule selectedItem = null;
    if (getResult() == OkCancelAndOtherDialogConstants.OK_BUTTON) {
      selectedItem = (BusinessRule) rulesList.getSelectedValue();
    }
    
    return selectedItem;
  }
  
  /**
   * Draws a hint text inside a text component.
   *  
   * @param component Text component.
   * @param g Graphics used to draw.
   * @param noFocusHint The text to draw.
   */
  public static void drawHint(JTextComponent component, Graphics g, String noFocusHint) {
    FontMetrics fm = component.getFontMetrics(g.getFont());
    int x = 0;
    int y = 0;
    int availableHeight = component.getHeight();

    // Adjust for insets
    Insets insets = component.getInsets();
    if (insets != null) {
      x += insets.left;
      y += insets.top;
      availableHeight -= insets.top;
      availableHeight -= insets.bottom;
    }

    // Adjust the y if the font height is different than the available height
    // @see BasicTextFieldUI#getBaseline(javax.swing.JComponent, int, int)
    int fontHeight = fm.getHeight();
    if (availableHeight != fontHeight) {
      y += (availableHeight - fontHeight) / 2;
    }

    y += fm.getAscent();

    g.setColor(Color.gray);
    
    g.drawString(noFocusHint, x, y);
  }
  
  @Override
  public void setVisible(boolean visible) {
    if (!visible) {
      lastLocation = getLocation();
    }
    super.setVisible(visible);
  }
}
