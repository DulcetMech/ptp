//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.13 at 12:25:18 PM EST 
//


package org.eclipse.ptp.rm.jaxb.core.data;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for control-type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="control-type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attribute" type="{org.eclipse.ptp}attribute-type" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="managed-files" type="{org.eclipse.ptp}managed-files-type" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="script" type="{org.eclipse.ptp}script-type" minOccurs="0"/>
 *         &lt;element name="start-up-command" type="{org.eclipse.ptp}command-type" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;sequence>
 *             &lt;element name="submit-interactive" type="{org.eclipse.ptp}command-type"/>
 *             &lt;element name="submit-interactive-debug" type="{org.eclipse.ptp}command-type" minOccurs="0"/>
 *           &lt;/sequence>
 *           &lt;sequence>
 *             &lt;element name="submit-batch" type="{org.eclipse.ptp}command-type"/>
 *             &lt;element name="submit-batch-debug" type="{org.eclipse.ptp}command-type" minOccurs="0"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *         &lt;element name="get-job-status" type="{org.eclipse.ptp}command-type" minOccurs="0"/>
 *         &lt;element name="terminate-job" type="{org.eclipse.ptp}command-type" minOccurs="0"/>
 *         &lt;element name="suspend-job" type="{org.eclipse.ptp}command-type" minOccurs="0"/>
 *         &lt;element name="resume-job" type="{org.eclipse.ptp}command-type" minOccurs="0"/>
 *         &lt;element name="hold-job" type="{org.eclipse.ptp}command-type" minOccurs="0"/>
 *         &lt;element name="release-job" type="{org.eclipse.ptp}command-type" minOccurs="0"/>
 *         &lt;element name="shut-down-command" type="{org.eclipse.ptp}command-type" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="button-action" type="{org.eclipse.ptp}command-type" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="launch-tab" type="{org.eclipse.ptp}launch-tab-type" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "control-type", propOrder = {
    "attribute",
    "managedFiles",
    "script",
    "startUpCommand",
    "submitInteractive",
    "submitInteractiveDebug",
    "submitBatch",
    "submitBatchDebug",
    "getJobStatus",
    "terminateJob",
    "suspendJob",
    "resumeJob",
    "holdJob",
    "releaseJob",
    "shutDownCommand",
    "buttonAction",
    "launchTab"
})
public class ControlType {

    protected List<AttributeType> attribute;
    @XmlElement(name = "managed-files")
    protected List<ManagedFilesType> managedFiles;
    protected ScriptType script;
    @XmlElement(name = "start-up-command")
    protected List<CommandType> startUpCommand;
    @XmlElement(name = "submit-interactive")
    protected CommandType submitInteractive;
    @XmlElement(name = "submit-interactive-debug")
    protected CommandType submitInteractiveDebug;
    @XmlElement(name = "submit-batch")
    protected CommandType submitBatch;
    @XmlElement(name = "submit-batch-debug")
    protected CommandType submitBatchDebug;
    @XmlElement(name = "get-job-status")
    protected CommandType getJobStatus;
    @XmlElement(name = "terminate-job")
    protected CommandType terminateJob;
    @XmlElement(name = "suspend-job")
    protected CommandType suspendJob;
    @XmlElement(name = "resume-job")
    protected CommandType resumeJob;
    @XmlElement(name = "hold-job")
    protected CommandType holdJob;
    @XmlElement(name = "release-job")
    protected CommandType releaseJob;
    @XmlElement(name = "shut-down-command")
    protected List<CommandType> shutDownCommand;
    @XmlElement(name = "button-action")
    protected List<CommandType> buttonAction;
    @XmlElement(name = "launch-tab")
    protected LaunchTabType launchTab;

    /**
     * Gets the value of the attribute property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the attribute property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAttribute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AttributeType }
     * 
     * 
     */
    public List<AttributeType> getAttribute() {
        if (attribute == null) {
            attribute = new ArrayList<AttributeType>();
        }
        return this.attribute;
    }

    /**
     * Gets the value of the managedFiles property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the managedFiles property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getManagedFiles().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ManagedFilesType }
     * 
     * 
     */
    public List<ManagedFilesType> getManagedFiles() {
        if (managedFiles == null) {
            managedFiles = new ArrayList<ManagedFilesType>();
        }
        return this.managedFiles;
    }

    /**
     * Gets the value of the script property.
     * 
     * @return
     *     possible object is
     *     {@link ScriptType }
     *     
     */
    public ScriptType getScript() {
        return script;
    }

    /**
     * Sets the value of the script property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScriptType }
     *     
     */
    public void setScript(ScriptType value) {
        this.script = value;
    }

    /**
     * Gets the value of the startUpCommand property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the startUpCommand property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStartUpCommand().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CommandType }
     * 
     * 
     */
    public List<CommandType> getStartUpCommand() {
        if (startUpCommand == null) {
            startUpCommand = new ArrayList<CommandType>();
        }
        return this.startUpCommand;
    }

    /**
     * Gets the value of the submitInteractive property.
     * 
     * @return
     *     possible object is
     *     {@link CommandType }
     *     
     */
    public CommandType getSubmitInteractive() {
        return submitInteractive;
    }

    /**
     * Sets the value of the submitInteractive property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandType }
     *     
     */
    public void setSubmitInteractive(CommandType value) {
        this.submitInteractive = value;
    }

    /**
     * Gets the value of the submitInteractiveDebug property.
     * 
     * @return
     *     possible object is
     *     {@link CommandType }
     *     
     */
    public CommandType getSubmitInteractiveDebug() {
        return submitInteractiveDebug;
    }

    /**
     * Sets the value of the submitInteractiveDebug property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandType }
     *     
     */
    public void setSubmitInteractiveDebug(CommandType value) {
        this.submitInteractiveDebug = value;
    }

    /**
     * Gets the value of the submitBatch property.
     * 
     * @return
     *     possible object is
     *     {@link CommandType }
     *     
     */
    public CommandType getSubmitBatch() {
        return submitBatch;
    }

    /**
     * Sets the value of the submitBatch property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandType }
     *     
     */
    public void setSubmitBatch(CommandType value) {
        this.submitBatch = value;
    }

    /**
     * Gets the value of the submitBatchDebug property.
     * 
     * @return
     *     possible object is
     *     {@link CommandType }
     *     
     */
    public CommandType getSubmitBatchDebug() {
        return submitBatchDebug;
    }

    /**
     * Sets the value of the submitBatchDebug property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandType }
     *     
     */
    public void setSubmitBatchDebug(CommandType value) {
        this.submitBatchDebug = value;
    }

    /**
     * Gets the value of the getJobStatus property.
     * 
     * @return
     *     possible object is
     *     {@link CommandType }
     *     
     */
    public CommandType getGetJobStatus() {
        return getJobStatus;
    }

    /**
     * Sets the value of the getJobStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandType }
     *     
     */
    public void setGetJobStatus(CommandType value) {
        this.getJobStatus = value;
    }

    /**
     * Gets the value of the terminateJob property.
     * 
     * @return
     *     possible object is
     *     {@link CommandType }
     *     
     */
    public CommandType getTerminateJob() {
        return terminateJob;
    }

    /**
     * Sets the value of the terminateJob property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandType }
     *     
     */
    public void setTerminateJob(CommandType value) {
        this.terminateJob = value;
    }

    /**
     * Gets the value of the suspendJob property.
     * 
     * @return
     *     possible object is
     *     {@link CommandType }
     *     
     */
    public CommandType getSuspendJob() {
        return suspendJob;
    }

    /**
     * Sets the value of the suspendJob property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandType }
     *     
     */
    public void setSuspendJob(CommandType value) {
        this.suspendJob = value;
    }

    /**
     * Gets the value of the resumeJob property.
     * 
     * @return
     *     possible object is
     *     {@link CommandType }
     *     
     */
    public CommandType getResumeJob() {
        return resumeJob;
    }

    /**
     * Sets the value of the resumeJob property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandType }
     *     
     */
    public void setResumeJob(CommandType value) {
        this.resumeJob = value;
    }

    /**
     * Gets the value of the holdJob property.
     * 
     * @return
     *     possible object is
     *     {@link CommandType }
     *     
     */
    public CommandType getHoldJob() {
        return holdJob;
    }

    /**
     * Sets the value of the holdJob property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandType }
     *     
     */
    public void setHoldJob(CommandType value) {
        this.holdJob = value;
    }

    /**
     * Gets the value of the releaseJob property.
     * 
     * @return
     *     possible object is
     *     {@link CommandType }
     *     
     */
    public CommandType getReleaseJob() {
        return releaseJob;
    }

    /**
     * Sets the value of the releaseJob property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommandType }
     *     
     */
    public void setReleaseJob(CommandType value) {
        this.releaseJob = value;
    }

    /**
     * Gets the value of the shutDownCommand property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shutDownCommand property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getShutDownCommand().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CommandType }
     * 
     * 
     */
    public List<CommandType> getShutDownCommand() {
        if (shutDownCommand == null) {
            shutDownCommand = new ArrayList<CommandType>();
        }
        return this.shutDownCommand;
    }

    /**
     * Gets the value of the buttonAction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the buttonAction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getButtonAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CommandType }
     * 
     * 
     */
    public List<CommandType> getButtonAction() {
        if (buttonAction == null) {
            buttonAction = new ArrayList<CommandType>();
        }
        return this.buttonAction;
    }

    /**
     * Gets the value of the launchTab property.
     * 
     * @return
     *     possible object is
     *     {@link LaunchTabType }
     *     
     */
    public LaunchTabType getLaunchTab() {
        return launchTab;
    }

    /**
     * Sets the value of the launchTab property.
     * 
     * @param value
     *     allowed object is
     *     {@link LaunchTabType }
     *     
     */
    public void setLaunchTab(LaunchTabType value) {
        this.launchTab = value;
    }

}
