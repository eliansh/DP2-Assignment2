//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.01.12 at 12:42:08 AM CET 
//


package it.polito.dp2.WF.sol2.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for processActType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="processActType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="relatedWorkFlow" use="required" type="{http://www.example.org/WFInfo}nameType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processActType")
public class ProcessActType {

    @XmlAttribute(name = "relatedWorkFlow", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String relatedWorkFlow;

    /**
     * Gets the value of the relatedWorkFlow property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelatedWorkFlow() {
        return relatedWorkFlow;
    }

    /**
     * Sets the value of the relatedWorkFlow property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelatedWorkFlow(String value) {
        this.relatedWorkFlow = value;
    }

}