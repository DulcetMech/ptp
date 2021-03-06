//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.13 at 09:16:36 AM CET 
//

package org.eclipse.ptp.rm.lml.core.elements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for splitlayout_type complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="splitlayout_type">
 *   &lt;complexContent>
 *     &lt;extension base="{http://eclipse.org/ptp/lml}layout_type">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element name="left" type="{http://eclipse.org/ptp/lml}pane_type"/>
 *           &lt;element name="right" type="{http://eclipse.org/ptp/lml}pane_type"/>
 *         &lt;/sequence>
 *         &lt;sequence>
 *           &lt;element name="top" type="{http://eclipse.org/ptp/lml}pane_type"/>
 *           &lt;element name="bottom" type="{http://eclipse.org/ptp/lml}pane_type"/>
 *         &lt;/sequence>
 *       &lt;/choice>
 *       &lt;attribute name="divpos" type="{http://eclipse.org/ptp/lml}divpos_type" default="0.5" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "splitlayout_type", propOrder = {
		"left",
		"right",
		"top",
		"bottom"
})
public class SplitlayoutType
		extends LayoutType
{

	protected PaneType left;
	protected PaneType right;
	protected PaneType top;
	protected PaneType bottom;
	@XmlAttribute
	protected Double divpos;

	/**
	 * Gets the value of the left property.
	 * 
	 * @return
	 *         possible object is {@link PaneType }
	 * 
	 */
	public PaneType getLeft() {
		return left;
	}

	/**
	 * Sets the value of the left property.
	 * 
	 * @param value
	 *            allowed object is {@link PaneType }
	 * 
	 */
	public void setLeft(PaneType value) {
		this.left = value;
	}

	/**
	 * Gets the value of the right property.
	 * 
	 * @return
	 *         possible object is {@link PaneType }
	 * 
	 */
	public PaneType getRight() {
		return right;
	}

	/**
	 * Sets the value of the right property.
	 * 
	 * @param value
	 *            allowed object is {@link PaneType }
	 * 
	 */
	public void setRight(PaneType value) {
		this.right = value;
	}

	/**
	 * Gets the value of the top property.
	 * 
	 * @return
	 *         possible object is {@link PaneType }
	 * 
	 */
	public PaneType getTop() {
		return top;
	}

	/**
	 * Sets the value of the top property.
	 * 
	 * @param value
	 *            allowed object is {@link PaneType }
	 * 
	 */
	public void setTop(PaneType value) {
		this.top = value;
	}

	/**
	 * Gets the value of the bottom property.
	 * 
	 * @return
	 *         possible object is {@link PaneType }
	 * 
	 */
	public PaneType getBottom() {
		return bottom;
	}

	/**
	 * Sets the value of the bottom property.
	 * 
	 * @param value
	 *            allowed object is {@link PaneType }
	 * 
	 */
	public void setBottom(PaneType value) {
		this.bottom = value;
	}

	/**
	 * Gets the value of the divpos property.
	 * 
	 * @return
	 *         possible object is {@link Double }
	 * 
	 */
	public double getDivpos() {
		if (divpos == null) {
			return 0.5D;
		} else {
			return divpos;
		}
	}

	/**
	 * Sets the value of the divpos property.
	 * 
	 * @param value
	 *            allowed object is {@link Double }
	 * 
	 */
	public void setDivpos(Double value) {
		this.divpos = value;
	}

}
