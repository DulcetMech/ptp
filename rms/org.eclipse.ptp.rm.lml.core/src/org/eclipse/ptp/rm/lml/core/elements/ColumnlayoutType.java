//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.13 at 09:16:36 AM CET 
//

package org.eclipse.ptp.rm.lml.core.elements;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * Layout-definitions for exactly one column of a table.
 * Defines where to position the column, width and if this
 * column is sorted
 * 
 * 
 * <p>
 * Java class for columnlayout_type complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="columnlayout_type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pattern" type="{http://eclipse.org/ptp/lml}pattern_type" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="cid" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="pos" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="width">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}double">
 *             &lt;minInclusive value="0"/>
 *             &lt;maxInclusive value="1"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="sorted" type="{http://eclipse.org/ptp/lml}columnsortedtype" default="false" />
 *       &lt;attribute name="active" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="key" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "columnlayout_type", propOrder = {
		"pattern"
})
public class ColumnlayoutType {

	protected PatternType pattern;
	@XmlAttribute(required = true)
	@XmlSchemaType(name = "positiveInteger")
	protected BigInteger cid;
	@XmlAttribute
	@XmlSchemaType(name = "nonNegativeInteger")
	protected BigInteger pos;
	@XmlAttribute
	protected Double width;
	@XmlAttribute
	protected Columnsortedtype sorted;
	@XmlAttribute
	protected Boolean active;
	@XmlAttribute
	protected String key;

	/**
	 * Gets the value of the pattern property.
	 * 
	 * @return
	 *         possible object is {@link PatternType }
	 * 
	 */
	public PatternType getPattern() {
		return pattern;
	}

	/**
	 * Sets the value of the pattern property.
	 * 
	 * @param value
	 *            allowed object is {@link PatternType }
	 * 
	 */
	public void setPattern(PatternType value) {
		this.pattern = value;
	}

	/**
	 * Gets the value of the cid property.
	 * 
	 * @return
	 *         possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getCid() {
		return cid;
	}

	/**
	 * Sets the value of the cid property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setCid(BigInteger value) {
		this.cid = value;
	}

	/**
	 * Gets the value of the pos property.
	 * 
	 * @return
	 *         possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getPos() {
		return pos;
	}

	/**
	 * Sets the value of the pos property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setPos(BigInteger value) {
		this.pos = value;
	}

	/**
	 * Gets the value of the width property.
	 * 
	 * @return
	 *         possible object is {@link Double }
	 * 
	 */
	public Double getWidth() {
		return width;
	}

	/**
	 * Sets the value of the width property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public void setWidth(Double value) {
		this.width = value;
	}

	/**
	 * Gets the value of the sorted property.
	 * 
	 * @return
	 *         possible object is {@link Columnsortedtype }
	 * 
	 */
	public Columnsortedtype getSorted() {
		if (sorted == null) {
			return Columnsortedtype.FALSE;
		} else {
			return sorted;
		}
	}

	/**
	 * Sets the value of the sorted property.
	 * 
	 * @param value
	 *            allowed object is {@link Columnsortedtype }
	 * 
	 */
	public void setSorted(Columnsortedtype value) {
		this.sorted = value;
	}

	/**
	 * Gets the value of the active property.
	 * 
	 * @return
	 *         possible object is {@link Boolean }
	 * 
	 */
	public boolean isActive() {
		if (active == null) {
			return true;
		} else {
			return active;
		}
	}

	/**
	 * Sets the value of the active property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setActive(Boolean value) {
		this.active = value;
	}

	/**
	 * Gets the value of the key property.
	 * 
	 * @return
	 *         possible object is {@link String }
	 * 
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the value of the key property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setKey(String value) {
		this.key = value;
	}

}
