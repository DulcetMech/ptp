//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.02.26 at 04:30:05 PM CST 
//

package org.eclipse.ptp.rm.jaxb.core.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}site" minOccurs="0"/>
 *         &lt;element ref="{}control"/>
 *         &lt;element ref="{}monitor"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "site", "control", "monitor" })
@XmlRootElement(name = "resource-manager-data")
public class ResourceManagerData {

	protected Site site;
	@XmlElement(required = true)
	protected Control control;
	@XmlElement(required = true)
	protected Object monitor;
	@XmlAttribute
	protected String name;

	/**
	 * Gets the value of the control property.
	 * 
	 * @return possible object is {@link Control }
	 * 
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Gets the value of the monitor property.
	 * 
	 * @return possible object is {@link Object }
	 * 
	 */
	public Object getMonitor() {
		return monitor;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the value of the site property.
	 * 
	 * @return possible object is {@link Site }
	 * 
	 */
	public Site getSite() {
		return site;
	}

	/**
	 * Sets the value of the control property.
	 * 
	 * @param value
	 *            allowed object is {@link Control }
	 * 
	 */
	public void setControl(Control value) {
		this.control = value;
	}

	/**
	 * Sets the value of the monitor property.
	 * 
	 * @param value
	 *            allowed object is {@link Object }
	 * 
	 */
	public void setMonitor(Object value) {
		this.monitor = value;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
		this.name = value;
	}

	/**
	 * Sets the value of the site property.
	 * 
	 * @param value
	 *            allowed object is {@link Site }
	 * 
	 */
	public void setSite(Site value) {
		this.site = value;
	}

}
