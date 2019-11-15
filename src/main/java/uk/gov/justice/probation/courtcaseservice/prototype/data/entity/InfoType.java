
package uk.gov.justice.probation.courtcaseservice.prototype.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for infoType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="infoType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="general" type="{}generalType"/>
 *         &lt;element name="printers" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="parameters" type="{}parametersType"/>
 *         &lt;element name="start_time" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "infoType", propOrder = {
    "general",
    "printers",
    "parameters",
    "startTime"
})
public class InfoType {

    @XmlElement(required = true)
    protected GeneralType general;
    @XmlElement(required = true)
    protected String printers;
    @XmlElement(required = true)
    protected ParametersType parameters;
    @XmlElement(name = "start_time", required = true)
    protected String startTime;

    /**
     * Gets the value of the general property.
     * 
     * @return
     *     possible object is
     *     {@link GeneralType }
     *     
     */
    public GeneralType getGeneral() {
        return general;
    }

    /**
     * Sets the value of the general property.
     * 
     * @param value
     *     allowed object is
     *     {@link GeneralType }
     *     
     */
    public void setGeneral(GeneralType value) {
        this.general = value;
    }

    /**
     * Gets the value of the printers property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrinters() {
        return printers;
    }

    /**
     * Sets the value of the printers property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrinters(String value) {
        this.printers = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link ParametersType }
     *     
     */
    public ParametersType getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParametersType }
     *     
     */
    public void setParameters(ParametersType value) {
        this.parameters = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonProperty("start_time")
    public void setStartTime(String value) {
        this.startTime = value;
    }

}
