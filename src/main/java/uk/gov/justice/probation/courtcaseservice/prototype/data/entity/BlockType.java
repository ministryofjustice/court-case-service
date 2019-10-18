
package uk.gov.justice.probation.courtlistservice.prototype.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for blockType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="blockType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sb_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bstart" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bend" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cases" type="{}casesType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "blockType", propOrder = {
    "sbId",
    "bstart",
    "bend",
    "desc",
    "cases"
})
public class BlockType {

    @XmlElement(name = "sb_id", required = true)
    protected String sbId;
    @XmlElement(required = true)
    protected String bstart;
    @XmlElement(required = true)
    protected String bend;
    @XmlElement(required = true)
    protected String desc;
    @XmlElement(required = true)
    protected CasesType cases;

    /**
     * Gets the value of the sbId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("sb_id")
    public String getSbId() {
        return sbId;
    }

    /**
     * Sets the value of the sbId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSbId(String value) {
        this.sbId = value;
    }

    /**
     * Gets the value of the bstart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBstart() {
        return bstart;
    }

    /**
     * Sets the value of the bstart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBstart(String value) {
        this.bstart = value;
    }

    /**
     * Gets the value of the bend property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBend() {
        return bend;
    }

    /**
     * Sets the value of the bend property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBend(String value) {
        this.bend = value;
    }

    /**
     * Gets the value of the desc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Sets the value of the desc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesc(String value) {
        this.desc = value;
    }

    /**
     * Gets the value of the cases property.
     * 
     * @return
     *     possible object is
     *     {@link CasesType }
     *     
     */
    public CasesType getCases() {
        return cases;
    }

    /**
     * Sets the value of the cases property.
     * 
     * @param value
     *     allowed object is
     *     {@link CasesType }
     *     
     */
    public void setCases(CasesType value) {
        this.cases = value;
    }

}
