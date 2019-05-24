
package uk.gov.justice.digital.probation.court.list.courtlistservice.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sessionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sessionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="s_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="doh" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="lja" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cmu" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="panel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="court" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="room" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sstart" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="send" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="blocks" type="{}blocksType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sessionType", propOrder = {
    "sId",
    "doh",
    "lja",
    "cmu",
    "panel",
    "court",
    "room",
    "sstart",
    "send",
    "blocks"
})
public class SessionType {

    @XmlElement(name = "s_id", required = true)
    protected String sId;
    @XmlElement(required = true)
    protected String doh;
    @XmlElement(required = true)
    protected String lja;
    @XmlElement(required = true)
    protected String cmu;
    @XmlElement(required = true)
    protected String panel;
    @XmlElement(required = true)
    protected String court;
    @XmlElement(required = true)
    protected String room;
    @XmlElement(required = true)
    protected String sstart;
    @XmlElement(required = true)
    protected String send;
    @XmlElement(required = true)
    protected BlocksType blocks;

    /**
     * Gets the value of the sId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("s_id")
    public String getSId() {
        return sId;
    }

    /**
     * Sets the value of the sId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSId(String value) {
        this.sId = value;
    }

    /**
     * Gets the value of the doh property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDoh() {
        return doh;
    }

    /**
     * Sets the value of the doh property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDoh(String value) {
        this.doh = value;
    }

    /**
     * Gets the value of the lja property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLja() {
        return lja;
    }

    /**
     * Sets the value of the lja property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLja(String value) {
        this.lja = value;
    }

    /**
     * Gets the value of the cmu property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCmu() {
        return cmu;
    }

    /**
     * Sets the value of the cmu property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCmu(String value) {
        this.cmu = value;
    }

    /**
     * Gets the value of the panel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPanel() {
        return panel;
    }

    /**
     * Sets the value of the panel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPanel(String value) {
        this.panel = value;
    }

    /**
     * Gets the value of the court property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCourt() {
        return court;
    }

    /**
     * Sets the value of the court property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCourt(String value) {
        this.court = value;
    }

    /**
     * Gets the value of the room property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoom() {
        return room;
    }

    /**
     * Sets the value of the room property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoom(String value) {
        this.room = value;
    }

    /**
     * Gets the value of the sstart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSstart() {
        return sstart;
    }

    /**
     * Sets the value of the sstart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSstart(String value) {
        this.sstart = value;
    }

    /**
     * Gets the value of the send property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSend() {
        return send;
    }

    /**
     * Sets the value of the send property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSend(String value) {
        this.send = value;
    }

    /**
     * Gets the value of the blocks property.
     * 
     * @return
     *     possible object is
     *     {@link BlocksType }
     *     
     */
    public BlocksType getBlocks() {
        return blocks;
    }

    /**
     * Sets the value of the blocks property.
     * 
     * @param value
     *     allowed object is
     *     {@link BlocksType }
     *     
     */
    public void setBlocks(BlocksType value) {
        this.blocks = value;
    }

}
