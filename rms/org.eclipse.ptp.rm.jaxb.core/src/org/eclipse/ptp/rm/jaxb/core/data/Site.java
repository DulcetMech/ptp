//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.02.26 at 04:30:05 PM CST 
//

package org.eclipse.ptp.rm.jaxb.core.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="control-connection" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="monitor-server-install" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "controlConnection", "monitorServerInstall" })
@XmlRootElement(name = "site")
public class Site {

	@XmlElement(name = "control-connection")
	protected String controlConnection;
	@XmlElement(name = "monitor-server-install")
	protected String monitorServerInstall;

	/**
	 * Gets the value of the controlConnection property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getControlConnection() {
		return controlConnection;
	}

	/**
	 * Gets the value of the monitorServerInstall property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getMonitorServerInstall() {
		return monitorServerInstall;
	}

	/**
	 * Sets the value of the controlConnection property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setControlConnection(String value) {
		this.controlConnection = value;
	}

	/**
	 * Sets the value of the monitorServerInstall property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setMonitorServerInstall(String value) {
		this.monitorServerInstall = value;
	}

}
