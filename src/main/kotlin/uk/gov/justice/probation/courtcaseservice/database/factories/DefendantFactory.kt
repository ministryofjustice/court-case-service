package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.data.Faker
import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository
import java.time.LocalDate
import java.util.UUID

class DefendantFactory(
  private val repository: DefendantRepository,
  private var defendantId: String? = null,
  private var defendantName: String? = null,
  private var personId: String? = null,
  private var type: DefendantType = DefendantType.PERSON,
  private var sex: Sex? = null,
  private var name: NamePropertiesEntity? = null,
  private var address: AddressPropertiesEntity? = null,
  private var phoneNumber: PhoneNumberEntity? = null,
  private var dateOfBirth: LocalDate? = null,
  private val cro: String = "",
  private val crn: String = "",
  private val pnc: String = "",
  private val offender: OffenderEntity? = null,
) {
  private val faker = Faker()

  fun count(count: Int = 1): List<DefendantEntity> = Factory(
    newModel = {
      val cro = cro.ifEmpty { faker.cro() }
      val crn = crn.ifEmpty { faker.crn() }
      val pnc = pnc.ifEmpty { faker.pnc() }

      val generatedName = name ?: randomName()
      val generatedAddress = address ?: randomAddress()
      val generatedPhone = phoneNumber ?: randomPhoneNumber()
      val generatedDob = dateOfBirth ?: randomBirthday()
      val generatedSex = sex ?: randomSex()
      val generatedNationality = randomNationality()
      val generatedNationality2 = randomSecondNationality()

      DefendantEntity.builder()
        .defendantId(defendantId ?: UUID.randomUUID().toString())
        .defendantName(defendantName ?: faker.name().fullName())
        .name(generatedName)
        .type(type)
        .sex(generatedSex)
        .personId(personId ?: UUID.randomUUID().toString())
        .dateOfBirth(generatedDob)
        .address(generatedAddress)
        .nationality1(generatedNationality)
        .nationality2(generatedNationality2)
        .phoneNumber(generatedPhone)
        .cro(cro)
        .crn(crn)
        .pnc(pnc)
        .offender(offender)
        .build()
    },
    repository = repository,
    count = count,
  ).create()

  private fun randomName(): NamePropertiesEntity {
    val fakerName = faker.name()
    return NamePropertiesEntity.builder()
      .title(fakerName.prefix())
      .forename1(fakerName.firstName())
      .surname(fakerName.lastName())
      .build()
  }

  private fun randomAddress(): AddressPropertiesEntity {
    val address = faker.address()

    return AddressPropertiesEntity.builder()
      .line1(address.streetAddress())
      .line2(address.secondaryAddress())
      .line3(address.city())
      .postcode(address.postcode())
      .build()
  }

  private fun randomPhoneNumber(): PhoneNumberEntity = PhoneNumberEntity.builder()
    .home(faker.phoneNumber().phoneNumber())
    .work(faker.phoneNumber().phoneNumber())
    .mobile(faker.phoneNumber().cellPhone())
    .build()

  private fun randomBirthday(): LocalDate = LocalDate.parse(faker.timeAndDate().birthday(18, 85).toString())

  private fun randomSex(): Sex = faker.options().option(Sex.MALE, Sex.FEMALE)

  private fun randomNationality(): String = faker.options().option("British", "Irish", faker.nation().nationality())

  private fun randomSecondNationality(): String = faker.options().option("", randomNationality())
}
