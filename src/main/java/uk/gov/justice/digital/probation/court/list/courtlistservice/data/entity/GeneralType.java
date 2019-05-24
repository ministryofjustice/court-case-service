
package uk.gov.justice.digital.probation.court.list.courtlistservice.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for generalType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="generalType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="alerts_address" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="erm_flag" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="doc_type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="outputtype" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="docref" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="system" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="preview_text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "generalType", propOrder = {
    "alertsAddress",
    "ermFlag",
    "docType",
    "outputtype",
    "version",
    "docref",
    "system",
    "previewText"
})
public class GeneralType {

    @XmlElement(name = "alerts_address", required = true)
    protected String alertsAddress;
    @XmlElement(name = "erm_flag", required = true)
    protected String ermFlag;
    @XmlElement(name = "doc_type", required = true)
    protected String docType;
    @XmlElement(required = true)
    protected String outputtype;
    @XmlElement(required = true)
    protected String version;
    @XmlElement(required = true)
    protected String docref;
    @XmlElement(required = true)
    protected String system;
    @XmlElement(name = "preview_text", required = true)
    protected String previewText;

    /**
     * Gets the value of the alertsAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("alerts_address")
    public String getAlertsAddress() {
        return alertsAddress;
    }

    /**
     * Sets the value of the alertsAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlertsAddress(String value) {
        this.alertsAddress = value;
    }

    /**
     * Gets the value of the ermFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("erm_flag")
    public String getErmFlag() {
        return ermFlag;
    }

    /**
     * Sets the value of the ermFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErmFlag(String value) {
        this.ermFlag = value;
    }

    /**
     * Gets the value of the docType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("doc_type")
    public String getDocType() {
        return docType;
    }

    /**
     * Sets the value of the docType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocType(String value) {
        this.docType = value;
    }

    /**
     * Gets the value of the outputtype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOutputtype() {
        return outputtype;
    }

    /**
     * Sets the value of the outputtype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOutputtype(String value) {
        this.outputtype = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the docref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocref() {
        return docref;
    }

    /**
     * Sets the value of the docref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocref(String value) {
        this.docref = value;
    }

    /**
     * Gets the value of the system property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystem() {
        return system;
    }

    /**
     * Sets the value of the system property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystem(String value) {
        this.system = value;
    }

    /**
     * Gets the value of the previewText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviewText() {
        return previewText;
    }

    /**
     * Sets the value of the previewText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonProperty("preview_text")
    public void setPreviewText(String value) {
        this.previewText = value;
    }

}
