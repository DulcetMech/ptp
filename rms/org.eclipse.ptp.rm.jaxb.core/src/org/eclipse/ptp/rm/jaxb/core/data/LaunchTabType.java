//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:56:07 AM EDT 
//


package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for launch-tab-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="launch-tab-type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dynamic" type="{http://org.eclipse.ptp/rm}tab-controller-type" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="import" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="export-for-override" type="{http://org.eclipse.ptp/rm}attribute-viewer-type" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="title" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "launch-tab-type", propOrder = {
    "dynamic",
    "_import"
})
public class LaunchTabType {

    protected List<TabControllerType> dynamic;
    @XmlElement(name = "import")
    protected LaunchTabType.Import _import;

    /**
     * Gets the value of the dynamic property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dynamic property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDynamic().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TabControllerType }
     * 
     * 
     */
    public List<TabControllerType> getDynamic() {
        if (dynamic == null) {
            dynamic = new ArrayList<TabControllerType>();
        }
        return this.dynamic;
    }

    /**
     * Gets the value of the import property.
     * 
     * @return
     *     possible object is
     *     {@link LaunchTabType.Import }
     *     
     */
    public LaunchTabType.Import getImport() {
        return _import;
    }

    /**
     * Sets the value of the import property.
     * 
     * @param value
     *     allowed object is
     *     {@link LaunchTabType.Import }
     *     
     */
    public void setImport(LaunchTabType.Import value) {
        this._import = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="export-for-override" type="{http://org.eclipse.ptp/rm}attribute-viewer-type" minOccurs="0"/>
     *       &lt;/sequence>
     *       &lt;attribute name="title" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "exportForOverride"
    })
    public static class Import {

        @XmlElement(name = "export-for-override")
        protected AttributeViewerType exportForOverride;
        @XmlAttribute(required = true)
        protected String title;

        /**
         * Gets the value of the exportForOverride property.
         * 
         * @return
         *     possible object is
         *     {@link AttributeViewerType }
         *     
         */
        public AttributeViewerType getExportForOverride() {
            return exportForOverride;
        }

        /**
         * Sets the value of the exportForOverride property.
         * 
         * @param value
         *     allowed object is
         *     {@link AttributeViewerType }
         *     
         */
        public void setExportForOverride(AttributeViewerType value) {
            this.exportForOverride = value;
        }

        /**
         * Gets the value of the title property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTitle() {
            return title;
        }

        /**
         * Sets the value of the title property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTitle(String value) {
            this.title = value;
        }

    }

}
