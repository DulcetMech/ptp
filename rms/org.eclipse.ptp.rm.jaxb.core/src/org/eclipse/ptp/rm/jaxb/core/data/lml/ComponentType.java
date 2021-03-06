//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.14 at 08:46:16 AM CET 
//

package org.eclipse.ptp.rm.jaxb.core.data.lml;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Describes the absolute position of one graphical
 * object
 * 
 * <p>
 * Java class for component_type complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="component_type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="gid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="x" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="y" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="w" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" default="100" />
 *       &lt;attribute name="h" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" default="100" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "component_type")
public class ComponentType {

	@XmlAttribute(name = "gid", required = true)
	protected String gid;
	@XmlAttribute(name = "x", required = true)
	protected BigInteger x;
	@XmlAttribute(name = "y", required = true)
	protected BigInteger y;
	@XmlAttribute(name = "w")
	@XmlSchemaType(name = "nonNegativeInteger")
	protected BigInteger w;
	@XmlAttribute(name = "h")
	@XmlSchemaType(name = "nonNegativeInteger")
	protected BigInteger h;

	/**
	 * Gets the value of the gid property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getGid() {
		return gid;
	}

	/**
	 * Sets the value of the gid property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setGid(String value) {
		this.gid = value;
	}

	/**
	 * Gets the value of the x property.
	 * 
	 * @return
	 *         possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getX() {
		return x;
	}

	/**
	 * Sets the value of the x property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setX(BigInteger value) {
		this.x = value;
	}

	/**
	 * Gets the value of the y property.
	 * 
	 * @return
	 *         possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getY() {
		return y;
	}

	/**
	 * Sets the value of the y property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setY(BigInteger value) {
		this.y = value;
	}

	/**
	 * Gets the value of the w property.
	 * 
	 * @return
	 *         possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getW() {
		if (w == null) {
			return new BigInteger("100");
		} else {
			return w;
		}
	}

	/**
	 * Sets the value of the w property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setW(BigInteger value) {
		this.w = value;
	}

	/**
	 * Gets the value of the h property.
	 * 
	 * @return
	 *         possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getH() {
		if (h == null) {
			return new BigInteger("100");
		} else {
			return h;
		}
	}

	/**
	 * Sets the value of the h property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setH(BigInteger value) {
		this.h = value;
	}

}
