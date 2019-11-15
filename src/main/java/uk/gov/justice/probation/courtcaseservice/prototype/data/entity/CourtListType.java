
package uk.gov.justice.probation.courtcaseservice.prototype.data.entity;

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "jobType", propOrder = {
    "courtName",
    "date",
    "sessions"
})
@XmlRootElement(name = "CourtList")
public class CourtListType {

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    protected String courtName;
    protected String date;
    protected SessionsType sessions;

    /**
     * Gets the value of the sessions property.
     * 
     * @return
     *     possible object is
     *     {@link SessionsType }
     *     
     */
    public SessionsType getSessions() {
        return sessions;
    }

    /**
     * Sets the value of the sessions property.
     * 
     * @param value
     *     allowed object is
     *     {@link SessionsType }
     *     
     */
    public void setSessions(SessionsType value) {
        this.sessions = value;
    }

}
