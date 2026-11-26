package uk.gov.justice.probation.courtcaseservice.service.mapper

import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedAddress
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedDefendantDetails
import uk.gov.justice.probation.courtcaseservice.controller.model.EnrichedName
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex

object EnrichedDefendantMapper {
  fun mergeDefendantDetails(
    defendantEntity: DefendantEntity,
    enrichedDefendantDetails: EnrichedDefendantDetails,
  ): DefendantEntity {
    val name = enrichedDefendantDetails.name?.let { mergeName(defendantEntity.name, it) } ?: defendantEntity.name
    val address = enrichedDefendantDetails.address?.let { mergeAddress(defendantEntity.address, it) } ?: defendantEntity.address
    return defendantEntity
      .withCprUUID(enrichedDefendantDetails.cprUUID ?: defendantEntity.cprUUID)
      .withName(name)
      .withDefendantName(name?.fullName ?: defendantEntity.defendantName)
      .withAddress(address)
      .withDateOfBirth(enrichedDefendantDetails.dateOfBirth ?: defendantEntity.dateOfBirth)
      .withSex(enrichedDefendantDetails.sex?.let(Sex::fromString) ?: defendantEntity.sex)
  }

  private fun mergeName(existingName: NamePropertiesEntity?, enrichedName: EnrichedName): NamePropertiesEntity = NamePropertiesEntity.builder()
    .title(enrichedName.title ?: existingName?.title)
    .forename1(enrichedName.forename1 ?: existingName?.forename1)
    .forename2(enrichedName.forename2 ?: existingName?.forename2)
    .forename3(existingName?.forename3)
    .surname(enrichedName.surname ?: existingName?.surname)
    .build()

  private fun mergeAddress(existingAddress: AddressPropertiesEntity?, enrichedAddress: EnrichedAddress): AddressPropertiesEntity = AddressPropertiesEntity.builder()
    .line1(enrichedAddress.line1 ?: existingAddress?.line1)
    .line2(enrichedAddress.line2 ?: existingAddress?.line2)
    .line3(enrichedAddress.line3 ?: existingAddress?.line3)
    .line4(enrichedAddress.line4 ?: existingAddress?.line4)
    .line5(enrichedAddress.line5 ?: existingAddress?.line5)
    .postcode(enrichedAddress.postcode ?: existingAddress?.postcode)
    .build()
}
