//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.13 at 12:25:18 PM EST 
//


package org.eclipse.ptp.rm.jaxb.core.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for form-layout-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="form-layout-type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="marginHeight" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="marginWidth" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="marginTop" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="marginBottom" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="marginLeft" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="marginRight" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="spacing" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "form-layout-type")
public class FormLayoutType {

    @XmlAttribute(name = "marginHeight")
    protected Integer marginHeight;
    @XmlAttribute(name = "marginWidth")
    protected Integer marginWidth;
    @XmlAttribute(name = "marginTop")
    protected Integer marginTop;
    @XmlAttribute(name = "marginBottom")
    protected Integer marginBottom;
    @XmlAttribute(name = "marginLeft")
    protected Integer marginLeft;
    @XmlAttribute(name = "marginRight")
    protected Integer marginRight;
    @XmlAttribute(name = "spacing")
    protected Integer spacing;

    /**
     * Gets the value of the marginHeight property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMarginHeight() {
        return marginHeight;
    }

    /**
     * Sets the value of the marginHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarginHeight(Integer value) {
        this.marginHeight = value;
    }

    /**
     * Gets the value of the marginWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMarginWidth() {
        return marginWidth;
    }

    /**
     * Sets the value of the marginWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarginWidth(Integer value) {
        this.marginWidth = value;
    }

    /**
     * Gets the value of the marginTop property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMarginTop() {
        return marginTop;
    }

    /**
     * Sets the value of the marginTop property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarginTop(Integer value) {
        this.marginTop = value;
    }

    /**
     * Gets the value of the marginBottom property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMarginBottom() {
        return marginBottom;
    }

    /**
     * Sets the value of the marginBottom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarginBottom(Integer value) {
        this.marginBottom = value;
    }

    /**
     * Gets the value of the marginLeft property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMarginLeft() {
        return marginLeft;
    }

    /**
     * Sets the value of the marginLeft property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarginLeft(Integer value) {
        this.marginLeft = value;
    }

    /**
     * Gets the value of the marginRight property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMarginRight() {
        return marginRight;
    }

    /**
     * Sets the value of the marginRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMarginRight(Integer value) {
        this.marginRight = value;
    }

    /**
     * Gets the value of the spacing property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSpacing() {
        return spacing;
    }

    /**
     * Sets the value of the spacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSpacing(Integer value) {
        this.spacing = value;
    }

}
