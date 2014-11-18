package com.oxygenxml.gim;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains all the rules detected in a specific library.
 */
@XmlRootElement(name="rules") 
public class BusinessRuleLibrary {
  /**
   * The list with the detected business rules.
   */
  @XmlElement(name="rule")
  private final List<BusinessRule> rules = new ArrayList<BusinessRule>();
  /**
   * The modification time of the library when it was parsed.
   */
  private transient long timestamp;
  
  /**
   * @return The rules from this library.
   */
  public List<BusinessRule> getRules() {
    return rules;
  }

  /**
   * @return The modification time of the library when it was parsed.
   */
  public long getTimestamp() {
    return timestamp;
  }
  
  /**
   * @param timestamp The modification time of the library when it was parsed.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
