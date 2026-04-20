package uk.gov.justice.probation.courtcaseservice.database.factories.framework

import uk.gov.justice.probation.courtcaseservice.database.data.Faker
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
  private var defendantId: String? = null,
  private var defendantName: String? = null,
  private var personId: String? = null,
  private var type: DefendantType = DefendantType.PERSON,
  private var sex: Sex? = null,
  private var name: NamePropertiesEntity? = null,
  private var address: AddressPropertiesEntity? = null,
  private var phoneNumber: PhoneNumberEntity? = null,
  private var dateOfBirth: LocalDate? = null,
) {
  private val faker = Faker()

  fun count(count: Int = 1): List<DefendantEntity> = Factory(
    newModel = {
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
        // Double-check the format specifications of these example identifiers:
        // CRO = 12345ABCDEF, CRN = X123456, PCN = 2004/0046583U
        .cro(faker.idNumber().valid())
        .crn("X${faker.number().digits(6)}")
        .pnc("${faker.number().numberBetween(1990, 2025)}/${faker.number().digits(7)}U")
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

  private fun randomAddress(): AddressPropertiesEntity = AddressPropertiesEntity.builder()
    .line1(faker.address().streetAddress())
    .line2(faker.address().secondaryAddress())
    .line3(faker.address().city())
    .postcode(faker.address().postcode())
    .build()

  private fun randomPhoneNumber(): PhoneNumberEntity = PhoneNumberEntity.builder()
    .home(faker.phoneNumber().phoneNumber())
    .work(faker.phoneNumber().phoneNumber())
    .mobile(faker.phoneNumber().cellPhone())
    .build()

  private fun randomBirthday(): LocalDate = LocalDate.parse(faker.timeAndDate().birthday(18, 85).toString())

  private fun randomSex(): Sex = faker.options().option(Sex.MALE, Sex.FEMALE, Sex.NOT_SPECIFIED, Sex.NOT_KNOWN)

  private fun randomNationality(): String = faker.options().option("British", "Irish", faker.nation().nationality())

  private fun randomSecondNationality(): String = faker.options().option("", randomNationality())
}
