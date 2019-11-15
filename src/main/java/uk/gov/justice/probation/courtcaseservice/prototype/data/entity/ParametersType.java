
package uk.gov.justice.probation.courtcaseservice.prototype.data.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for parametersType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="parametersType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pi_mcc_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_report_type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_cmu_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_ch_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_date1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_date2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_session_types" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_cr_id_list" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_resequence" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_late_entry" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_means_register" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pi_adjudication_box" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parametersType", propOrder = {
    "piMccId",
    "piReportType",
    "piCmuId",
    "piChId",
    "piDate1",
    "piDate2",
    "piSessionTypes",
    "piCrIdList",
    "piResequence",
    "piLateEntry",
    "piMeansRegister",
    "piAdjudicationBox"
})
public class ParametersType {

    @XmlElement(name = "pi_mcc_id", required = true)
    protected String piMccId;
    @XmlElement(name = "pi_report_type", required = true)
    protected String piReportType;
    @XmlElement(name = "pi_cmu_id", required = true)
    protected String piCmuId;
    @XmlElement(name = "pi_ch_id", required = true)
    protected String piChId;
    @XmlElement(name = "pi_date1", required = true)
    protected String piDate1;
    @XmlElement(name = "pi_date2", required = true)
    protected String piDate2;
    @XmlElement(name = "pi_session_types", required = true)
    protected String piSessionTypes;
    @XmlElement(name = "pi_cr_id_list", required = true)
    protected String piCrIdList;
    @XmlElement(name = "pi_resequence", required = true)
    protected String piResequence;
    @XmlElement(name = "pi_late_entry", required = true)
    protected String piLateEntry;
    @XmlElement(name = "pi_means_register", required = true)
    protected String piMeansRegister;
    @XmlElement(name = "pi_adjudication_box", required = true)
    protected String piAdjudicationBox;

    /**
     * Gets the value of the piMccId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiMccId() {
        return piMccId;
    }

    /**
     * Sets the value of the piMccId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiMccId(String value) {
        this.piMccId = value;
    }

    /**
     * Gets the value of the piReportType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiReportType() {
        return piReportType;
    }

    /**
     * Sets the value of the piReportType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiReportType(String value) {
        this.piReportType = value;
    }

    /**
     * Gets the value of the piCmuId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiCmuId() {
        return piCmuId;
    }

    /**
     * Sets the value of the piCmuId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiCmuId(String value) {
        this.piCmuId = value;
    }

    /**
     * Gets the value of the piChId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiChId() {
        return piChId;
    }

    /**
     * Sets the value of the piChId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiChId(String value) {
        this.piChId = value;
    }

    /**
     * Gets the value of the piDate1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiDate1() {
        return piDate1;
    }

    /**
     * Sets the value of the piDate1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiDate1(String value) {
        this.piDate1 = value;
    }

    /**
     * Gets the value of the piDate2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiDate2() {
        return piDate2;
    }

    /**
     * Sets the value of the piDate2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiDate2(String value) {
        this.piDate2 = value;
    }

    /**
     * Gets the value of the piSessionTypes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiSessionTypes() {
        return piSessionTypes;
    }

    /**
     * Sets the value of the piSessionTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiSessionTypes(String value) {
        this.piSessionTypes = value;
    }

    /**
     * Gets the value of the piCrIdList property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiCrIdList() {
        return piCrIdList;
    }

    /**
     * Sets the value of the piCrIdList property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiCrIdList(String value) {
        this.piCrIdList = value;
    }

    /**
     * Gets the value of the piResequence property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiResequence() {
        return piResequence;
    }

    /**
     * Sets the value of the piResequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiResequence(String value) {
        this.piResequence = value;
    }

    /**
     * Gets the value of the piLateEntry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiLateEntry() {
        return piLateEntry;
    }

    /**
     * Sets the value of the piLateEntry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiLateEntry(String value) {
        this.piLateEntry = value;
    }

    /**
     * Gets the value of the piMeansRegister property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiMeansRegister() {
        return piMeansRegister;
    }

    /**
     * Sets the value of the piMeansRegister property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiMeansRegister(String value) {
        this.piMeansRegister = value;
    }

    /**
     * Gets the value of the piAdjudicationBox property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPiAdjudicationBox() {
        return piAdjudicationBox;
    }

    /**
     * Sets the value of the piAdjudicationBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPiAdjudicationBox(String value) {
        this.piAdjudicationBox = value;
    }

}
