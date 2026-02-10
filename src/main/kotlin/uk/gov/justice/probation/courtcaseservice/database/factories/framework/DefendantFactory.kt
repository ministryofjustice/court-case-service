package uk.gov.justice.probation.courtcaseservice.database.factories.framework

import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import java.time.LocalDate
import java.util.UUID

class DefendantFactory(
  private val repository: DefendantRepository,
  private var defendantId: String = UUID.randomUUID().toString(),
  private var defendantName: String = "Mr Test Defendant",
  private var personId: String = UUID.randomUUID().toString(),
  private var type: DefendantType = DefendantType.PERSON,
  private var sex: Sex = Sex.NOT_KNOWN,
  private var name: NamePropertiesEntity = NamePropertiesEntity.builder().title("Mr").forename1("Test").surname("Defendant").build(),
  private var address: AddressPropertiesEntity? = null,
  private var phoneNumber: PhoneNumberEntity? = null,
  private var dateOfBirth: LocalDate = LocalDate.parse("1970-01-01"),
) {

  fun count(count: Int = 1): List<DefendantEntity> {
    if (address == null) {
      // todo: set the data dynamically using faker and random
      this.address = AddressPropertiesEntity.builder()
        .line1("1 High Street")
        .line2("Flat 2")
        .line3("Sheffield")
        .postcode("S1 1AA")
        .build()
    }
    if (phoneNumber == null) {
      this.phoneNumber = PhoneNumberEntity.builder()
        .home("01234567890")
        .work("01234567890")
        .mobile("07123456789")
        .build()
    }
    return Factory(
      newModel = {
        DefendantEntity.builder()
          .defendantId(defendantId)
          .defendantName(defendantName)
          .name(name)
          .type(type)
          .sex(sex)
          .personId(personId)
          .dateOfBirth(dateOfBirth)
          .address(address)
          .nationality1("British")
          .nationality2("Irish")
          .phoneNumber(phoneNumber)
          // TODO: Double check the format specifications:
          .cro("12345ABCDEF")
          .crn("X123456")
          .pnc("2004/0046583U")
          .build()
      },
      repository = repository,
      count = count,
    ).create()
  }
}
