package com.oxygenxml.gim;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="parameter") 
public class BusinessRuleParam {
  /**
   * The parameter name.
   */
  @XmlElement(name="name", namespace="http://oxygenxml.com/ns/schematron/params")
  private String name;
  /**
   * The documentation.
   */
  @XmlElement(name="desc", namespace="http://oxygenxml.com/ns/schematron/params")
  private String description;
  /**
   * @return The name of this parameter.
   */
  public String getName() {
    return name;
  }
  /**
   * @param name The name of this parameter.
   */
  public void setName(String name) {
    this.name = name;
  }
  /**
   * @return The documentation for this presentation.
   */
  public String getDescription() {
    return description;
  }
  /**
   * @param description The documentation for this parameter.
   */
  public void setDescription(String description) {
    this.description = description;
  }
  
  @Override
  public String toString() {
    return name;
  }
}
