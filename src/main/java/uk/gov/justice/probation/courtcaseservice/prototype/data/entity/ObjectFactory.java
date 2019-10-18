
package uk.gov.justice.probation.courtlistservice.prototype.data.entity;

import org.w3c.dom.DocumentType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the uk.gov.justice.digital.court.crimeportal.data.entity package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Document_QNAME = new QName("", "document");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: uk.gov.justice.digital.court.crimeportal.data.entity
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GeneralType }
     * 
     */
    public GeneralType createGeneralType() {
        return new GeneralType();
    }

    /**
     * Create an instance of {@link BlockType }
     * 
     */
    public BlockType createBlockType() {
        return new BlockType();
    }

    /**
     * Create an instance of {@link PgAddrType }
     * 
     */
    public PgAddrType createPgAddrType() {
        return new PgAddrType();
    }

    /**
     * Create an instance of {@link OffenceType }
     * 
     */
    public OffenceType createOffenceType() {
        return new OffenceType();
    }

    /**
     * Create an instance of {@link CaseType }
     * 
     */
    public CaseType createCaseType() {
        return new CaseType();
    }

    /**
     * Create an instance of {@link InfoType }
     * 
     */
    public InfoType createInfoType() {
        return new InfoType();
    }

    /**
     * Create an instance of {@link SessionsType }
     * 
     */
    public SessionsType createSessionsType() {
        return new SessionsType();
    }

    /**
     * Create an instance of {@link CasesType }
     * 
     */
    public CasesType createCasesType() {
        return new CasesType();
    }

    /**
     * Create an instance of {@link SessionType }
     * 
     */
    public SessionType createSessionType() {
        return new SessionType();
    }

    /**
     * Create an instance of {@link OffencesType }
     * 
     */
    public OffencesType createOffencesType() {
        return new OffencesType();
    }

    /**
     * Create an instance of {@link BlocksType }
     * 
     */
    public BlocksType createBlocksType() {
        return new BlocksType();
    }

    /**
     * Create an instance of {@link ParametersType }
     * 
     */
    public ParametersType createParametersType() {
        return new ParametersType();
    }

    /**
     * Create an instance of {@link CourtListType }
     * 
     */
    public CourtListType createJobType() {
        return new CourtListType();
    }

    /**
     * Create an instance of {@link DefAddrType }
     * 
     */
    public DefAddrType createDefAddrType() {
        return new DefAddrType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DocumentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "document")
    public JAXBElement<DocumentType> createDocument(DocumentType value) {
        return new JAXBElement<DocumentType>(_Document_QNAME, DocumentType.class, null, value);
    }

}
