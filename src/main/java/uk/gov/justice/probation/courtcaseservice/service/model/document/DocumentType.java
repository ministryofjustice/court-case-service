package uk.gov.justice.probation.courtcaseservice.service.model.document;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "Types of documents")
public enum DocumentType {
    OFFENDER_DOCUMENT("Offender related"),
    CONVICTION_DOCUMENT("Sentence related"),
    CPSPACK_DOCUMENT("Crown Prosecution Service case pack"),
    PRECONS_DOCUMENT("PNC previous convictions"),
    COURT_REPORT_DOCUMENT("Court report"),
    INSTITUTION_REPORT_DOCUMENT("Institution report"),
    ADDRESS_ASSESSMENT_DOCUMENT("Address assessment related document"),
    APPROVED_PREMISES_REFERRAL_DOCUMENT("Approved premises referral related document"),
    ASSESSMENT_DOCUMENT("Assessment document"),
    CASE_ALLOCATION_DOCUMENT("Case allocation document"),
    PERSONAL_CONTACT_DOCUMENT("Personal contact related document"),
    REFERRAL_DOCUMENT("Referral related document"),
    NSI_DOCUMENT("Non Statutory Intervention related document"),
    PERSONAL_CIRCUMSTANCE_DOCUMENT("Personal circumstance related document"),
    UPW_APPOINTMENT_DOCUMENT("Unpaid work appointment document"),
    CONTACT_DOCUMENT("Contact related document");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
