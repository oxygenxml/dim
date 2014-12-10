package com.oxygenxml.gim.operations;

import com.oxygenxml.gim.BusinessRule;
import com.oxygenxml.gim.BusinessRuleLibrary;

/**
 * Common interface for selecting a rule from a library. 
 */
public interface BusinessRuleSelector {

  /**
   * Selects a rule from the given library.
   * 
   * @param businessRuleLibrary The rule library.
   * 
   * @return The selected rule.
   */
  public BusinessRule selectRule(BusinessRuleLibrary businessRuleLibrary);
}
