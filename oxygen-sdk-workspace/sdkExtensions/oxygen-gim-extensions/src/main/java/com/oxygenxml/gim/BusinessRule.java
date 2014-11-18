package com.oxygenxml.gim;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="rule") 
public class BusinessRule {
  /**
   * The name of the rule.
   */
  @XmlAttribute(name="name", required=true)
  private String ruleName;

  /**
   * The parameters of this rule.
   */
  @XmlElementWrapper(name="parameters", namespace="http://oxygenxml.com/ns/schematron/params")
  @XmlElements(
      @XmlElement(name="parameter", namespace="http://oxygenxml.com/ns/schematron/params")
  )
  private List<BusinessRuleParam> params;
  /**
   * The documentation.
   */
  @XmlAnyElement(DocHandler.class)
  private String description;
  
  /**
   * @return The name of this rule.
   */
  public String getRuleName() {
    return ruleName;
  }

  /**
   * @param patternId Sets the rule name.
   */
  public void setRuleName(String patternId) {
    this.ruleName = patternId;
  }

  /**
   * @return The parameters for this rule.
   */
  public List<BusinessRuleParam> getParams() {
    return params;
  }

  /**
   * @param params The parameters for this rule.
   */
  public void setParams(List<BusinessRuleParam> params) {
    this.params = params;
  }

  /**
   * @return Gets the description for this parameter.
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * @param description Sets the description for this parameter.
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
