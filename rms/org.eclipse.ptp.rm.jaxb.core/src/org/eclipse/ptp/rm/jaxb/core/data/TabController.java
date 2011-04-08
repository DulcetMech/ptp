//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.04.08 at 01:27:12 AM CDT 
//

package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for tab-controller complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="tab-controller">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element name="tab-folder" type="{http://org.eclipse.ptp/rm}tab-folder-descriptor"/>
 *           &lt;element name="composite" type="{http://org.eclipse.ptp/rm}composite-descriptor"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="sharedEnvironment" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tab-controller", propOrder = { "title", "tabFolderOrComposite" })
public class TabController {

	@XmlElement(required = true)
	protected String title;
	@XmlElements({ @XmlElement(name = "tab-folder", type = TabFolderDescriptor.class),
			@XmlElement(name = "composite", type = CompositeDescriptor.class) })
	protected List<Object> tabFolderOrComposite;
	@XmlAttribute
	protected Boolean sharedEnvironment;

	/**
	 * Gets the value of the tabFolderOrComposite property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the tabFolderOrComposite property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getTabFolderOrComposite().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link TabFolderDescriptor } {@link CompositeDescriptor }
	 * 
	 * 
	 */
	public List<Object> getTabFolderOrComposite() {
		if (tabFolderOrComposite == null) {
			tabFolderOrComposite = new ArrayList<Object>();
		}
		return this.tabFolderOrComposite;
	}

	/**
	 * Gets the value of the title property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the value of the sharedEnvironment property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isSharedEnvironment() {
		if (sharedEnvironment == null) {
			return false;
		} else {
			return sharedEnvironment;
		}
	}

	/**
	 * Sets the value of the sharedEnvironment property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setSharedEnvironment(Boolean value) {
		this.sharedEnvironment = value;
	}

	/**
	 * Sets the value of the title property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setTitle(String value) {
		this.title = value;
	}

}
