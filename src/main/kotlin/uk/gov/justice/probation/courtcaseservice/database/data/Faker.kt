package uk.gov.justice.probation.courtcaseservice.database.data

import net.datafaker.Faker
import uk.gov.justice.probation.courtcaseservice.database.data.offence.OffenceProvider
import java.util.Locale

// Preconfigure Faker with the UK locale so generated data is appropriate (e.g., names, addresses, phone numbers).
class Faker : net.datafaker.Faker(Locale.of("en", "GB")) {
  // Usage: faker.offence().archetype()
  fun offence() = OffenceProvider()

  // Formats: CRO = 12345ABCDEF, CRN = X123456, PNC = 2004/0046583U
  fun cro(): String = this.idNumber().valid()
  fun crn(): String = "X${this.number().digits(6)}"
  fun pnc(): String = "${this.number().numberBetween(1990, 2025)}/${this.number().digits(7)}U"
}
