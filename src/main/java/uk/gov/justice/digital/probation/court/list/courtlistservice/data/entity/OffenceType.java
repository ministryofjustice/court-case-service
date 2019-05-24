
package uk.gov.justice.digital.probation.court.list.courtlistservice.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for offenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="offenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oseq" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="co_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="maxpen" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="alm" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ala" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sum" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="as" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sof" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="plea" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pleadate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="convdate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="adjdate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="adjreason" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "offenceType", propOrder = {
    "oseq",
    "coId",
    "code",
    "maxpen",
    "title",
    "alm",
    "ala",
    "sum",
    "as",
    "sof",
    "plea",
    "pleadate",
    "convdate",
    "adjdate",
    "adjreason"
})
public class OffenceType {

    @XmlElement(required = true)
    protected String oseq;
    @XmlElement(name = "co_id", required = true)
    protected String coId;
    @XmlElement(required = true)
    protected String code;
    @XmlElement(required = true)
    protected String maxpen;
    @XmlElement(required = true)
    protected String title;
    @XmlElement(required = true)
    protected String alm;
    @XmlElement(required = true)
    protected String ala;
    @XmlElement(required = true)
    protected String sum;
    @XmlElement(required = true)
    protected String as;
    @XmlElement(required = true)
    protected String sof;
    @XmlElement(required = true)
    protected String plea;
    @XmlElement(required = true)
    protected String pleadate;
    @XmlElement(required = true)
    protected String convdate;
    @XmlElement(required = true)
    protected String adjdate;
    @XmlElement(required = true)
    protected String adjreason;

    /**
     * Gets the value of the oseq property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOseq() {
        return oseq;
    }

    /**
     * Sets the value of the oseq property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOseq(String value) {
        this.oseq = value;
    }

    /**
     * Gets the value of the coId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("co_id")
    public String getCoId() {
        return coId;
    }

    /**
     * Sets the value of the coId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCoId(String value) {
        this.coId = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the maxpen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxpen() {
        return maxpen;
    }

    /**
     * Sets the value of the maxpen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxpen(String value) {
        this.maxpen = value;
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

    /**
     * Gets the value of the alm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlm() {
        return alm;
    }

    /**
     * Sets the value of the alm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlm(String value) {
        this.alm = value;
    }

    /**
     * Gets the value of the ala property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAla() {
        return ala;
    }

    /**
     * Sets the value of the ala property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAla(String value) {
        this.ala = value;
    }

    /**
     * Gets the value of the sum property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSum() {
        return sum;
    }

    /**
     * Sets the value of the sum property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSum(String value) {
        this.sum = value;
    }

    /**
     * Gets the value of the as property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAs() {
        return as;
    }

    /**
     * Sets the value of the as property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAs(String value) {
        this.as = value;
    }

    /**
     * Gets the value of the sof property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSof() {
        return sof;
    }

    /**
     * Sets the value of the sof property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSof(String value) {
        this.sof = value;
    }

    /**
     * Gets the value of the plea property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlea() {
        return plea;
    }

    /**
     * Sets the value of the plea property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlea(String value) {
        this.plea = value;
    }

    /**
     * Gets the value of the pleadate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPleadate() {
        return pleadate;
    }

    /**
     * Sets the value of the pleadate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPleadate(String value) {
        this.pleadate = value;
    }

    /**
     * Gets the value of the convdate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConvdate() {
        return convdate;
    }

    /**
     * Sets the value of the convdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConvdate(String value) {
        this.convdate = value;
    }

    /**
     * Gets the value of the adjdate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdjdate() {
        return adjdate;
    }

    /**
     * Sets the value of the adjdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdjdate(String value) {
        this.adjdate = value;
    }

    /**
     * Gets the value of the adjreason property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdjreason() {
        return adjreason;
    }

    /**
     * Sets the value of the adjreason property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdjreason(String value) {
        this.adjreason = value;
    }

}
