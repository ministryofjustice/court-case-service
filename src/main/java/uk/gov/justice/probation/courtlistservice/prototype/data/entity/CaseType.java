
package uk.gov.justice.probation.courtlistservice.prototype.data.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for caseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="caseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="urn" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="asn" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="marker" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="c_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="h_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="valid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="caseno" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="prov" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="def_name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="def_type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="def_sex" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="def_dob" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="def_age" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="def_addr" type="{}def_addrType"/>
 *         &lt;element name="nationality_1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nationality_2" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="inf" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cseq" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="listno" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pg_type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pg_name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pg_addr" type="{}pg_addrType"/>
 *         &lt;element name="estdur" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dq1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="offences" type="{}offencesType"/>
 *         &lt;element name="bailcond" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "caseType", propOrder = {
    "urn",
    "asn",
    "marker",
    "cId",
    "hId",
    "valid",
    "caseno",
    "type",
    "prov",
    "defName",
    "defType",
    "defSex",
    "defDob",
    "defAge",
    "defAddr",
    "nationality1",
    "nationality2",
    "inf",
    "cseq",
    "listno",
    "pgType",
    "pgName",
    "pgAddr",
    "estdur",
    "dq1",
    "offences",
    "bailcond",
    "addinfo",
    "solname"
})
public class CaseType {

    @XmlElement(required = true)
    protected String urn;
    @XmlElement(required = true)
    protected String asn;
    @XmlElement(required = true)
    protected String marker;
    @XmlElement(name = "c_id", required = true)
    protected String cId;
    @XmlElement(name = "h_id", required = true)
    protected String hId;
    @XmlElement(required = true)
    protected String valid;
    @XmlElement(required = true)
    protected String caseno;
    @XmlElement(required = true)
    protected String type;
    @XmlElement(required = true)
    protected String prov;
    @XmlElement(name = "def_name", required = true)
    protected String defName;
    @XmlElement(name = "def_type", required = true)
    protected String defType;
    @XmlElement(name = "def_sex", required = true)
    protected String defSex;
    @XmlElement(name = "def_dob", required = true)
    protected String defDob;
    @XmlElement(name = "def_age", required = true)
    protected String defAge;
    @XmlElement(name = "def_addr", required = true)
    protected DefAddrType defAddr;
    @XmlElement(name = "nationality_1", required = true)
    protected String nationality1;
    @XmlElement(name = "nationality_2", required = true)
    protected String nationality2;
    @XmlElement(required = true)
    protected String inf;
    @XmlElement(required = true)
    protected String cseq;
    @XmlElement(required = true)
    protected String listno;
    @XmlElement(name = "pg_type", required = true)
    protected String pgType;
    @XmlElement(name = "pg_name", required = true)
    protected String pgName;
    @XmlElement(name = "pg_addr", required = true)
    protected PgAddrType pgAddr;
    @XmlElement(required = true)
    protected String estdur;
    @XmlElement(required = true)
    protected String dq1;
    @XmlElement(required = true)
    protected OffencesType offences;
    @XmlElement(required = true)
    protected String bailcond;
    @XmlElement(required = true)
    protected String addinfo;
    @XmlElement(required = true)
    protected String solname;

    public String getAddinfo() {
        return addinfo;
    }

    public void setAddinfo(String addinfo) {
        this.addinfo = addinfo;
    }

    public String getSolname() {
        return solname;
    }

    public void setSolname(String solname) {
        this.solname = solname;
    }

    /**
     * Gets the value of the urn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrn() {
        return urn;
    }

    /**
     * Sets the value of the urn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrn(String value) {
        this.urn = value;
    }

    /**
     * Gets the value of the asn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAsn() {
        return asn;
    }

    /**
     * Sets the value of the asn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAsn(String value) {
        this.asn = value;
    }

    /**
     * Gets the value of the marker property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMarker() {
        return marker;
    }

    /**
     * Sets the value of the marker property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMarker(String value) {
        this.marker = value;
    }

    /**
     * Gets the value of the cId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("c_id")
    public String getCId() {
        return cId;
    }

    /**
     * Sets the value of the cId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCId(String value) {
        this.cId = value;
    }

    /**
     * Gets the value of the hId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("h_id")
    public String getHId() {
        return hId;
    }

    /**
     * Sets the value of the hId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHId(String value) {
        this.hId = value;
    }

    /**
     * Gets the value of the valid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValid() {
        return valid;
    }

    /**
     * Sets the value of the valid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValid(String value) {
        this.valid = value;
    }

    /**
     * Gets the value of the caseno property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCaseno() {
        return caseno;
    }

    /**
     * Sets the value of the caseno property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCaseno(String value) {
        this.caseno = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the prov property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProv() {
        return prov;
    }

    /**
     * Sets the value of the prov property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProv(String value) {
        this.prov = value;
    }

    /**
     * Gets the value of the defName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("def_name")
    public String getDefName() {
        return defName;
    }

    /**
     * Sets the value of the defName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefName(String value) {
        this.defName = value;
    }

    /**
     * Gets the value of the defType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("def_type")
    public String getDefType() {
        return defType;
    }

    /**
     * Sets the value of the defType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefType(String value) {
        this.defType = value;
    }

    /**
     * Gets the value of the defSex property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("def_sex")
    public String getDefSex() {
        return defSex;
    }

    /**
     * Sets the value of the defSex property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefSex(String value) {
        this.defSex = value;
    }

    /**
     * Gets the value of the defDob property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("def_dob")
    public String getDefDob() {
        return defDob;
    }

    /**
     * Sets the value of the defDob property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefDob(String value) {
        this.defDob = value;
    }

    /**
     * Gets the value of the defAge property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("def_age")
    public String getDefAge() {
        return defAge;
    }

    /**
     * Sets the value of the defAge property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefAge(String value) {
        this.defAge = value;
    }

    /**
     * Gets the value of the defAddr property.
     * 
     * @return
     *     possible object is
     *     {@link DefAddrType }
     *     
     */
    @JsonProperty("def_addr")
    public DefAddrType getDefAddr() {
        return defAddr;
    }

    /**
     * Sets the value of the defAddr property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefAddrType }
     *     
     */
    public void setDefAddr(DefAddrType value) {
        this.defAddr = value;
    }

    /**
     * Gets the value of the nationality1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("nationality_1")
    public String getNationality1() {
        return nationality1;
    }

    /**
     * Sets the value of the nationality1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNationality1(String value) {
        this.nationality1 = value;
    }

    /**
     * Gets the value of the nationality2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("nationality_2")
    public String getNationality2() {
        return nationality2;
    }

    /**
     * Sets the value of the nationality2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNationality2(String value) {
        this.nationality2 = value;
    }

    /**
     * Gets the value of the inf property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInf() {
        return inf;
    }

    /**
     * Sets the value of the inf property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInf(String value) {
        this.inf = value;
    }

    /**
     * Gets the value of the cseq property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCseq() {
        return cseq;
    }

    /**
     * Sets the value of the cseq property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCseq(String value) {
        this.cseq = value;
    }

    /**
     * Gets the value of the listno property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getListno() {
        return listno;
    }

    /**
     * Sets the value of the listno property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setListno(String value) {
        this.listno = value;
    }

    /**
     * Gets the value of the pgType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("pg_type")
    public String getPgType() {
        return pgType;
    }

    /**
     * Sets the value of the pgType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPgType(String value) {
        this.pgType = value;
    }

    /**
     * Gets the value of the pgName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @JsonProperty("pg_name")
    public String getPgName() {
        return pgName;
    }

    /**
     * Sets the value of the pgName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPgName(String value) {
        this.pgName = value;
    }

    /**
     * Gets the value of the pgAddr property.
     * 
     * @return
     *     possible object is
     *     {@link PgAddrType }
     *     
     */
    @JsonProperty("pg_addr")
    public PgAddrType getPgAddr() {
        return pgAddr;
    }

    /**
     * Sets the value of the pgAddr property.
     * 
     * @param value
     *     allowed object is
     *     {@link PgAddrType }
     *     
     */
    public void setPgAddr(PgAddrType value) {
        this.pgAddr = value;
    }

    /**
     * Gets the value of the estdur property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEstdur() {
        return estdur;
    }

    /**
     * Sets the value of the estdur property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEstdur(String value) {
        this.estdur = value;
    }

    /**
     * Gets the value of the dq1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDq1() {
        return dq1;
    }

    /**
     * Sets the value of the dq1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDq1(String value) {
        this.dq1 = value;
    }

    /**
     * Gets the value of the offences property.
     * 
     * @return
     *     possible object is
     *     {@link OffencesType }
     *     
     */
    public OffencesType getOffences() {
        return offences;
    }

    /**
     * Sets the value of the offences property.
     * 
     * @param value
     *     allowed object is
     *     {@link OffencesType }
     *     
     */
    public void setOffences(OffencesType value) {
        this.offences = value;
    }

    /**
     * Gets the value of the bailcond property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBailcond() {
        return bailcond;
    }

    /**
     * Sets the value of the bailcond property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBailcond(String value) {
        this.bailcond = value;
    }

}
