package uk.gov.justice.probation.courtcaseservice.database.data

import net.datafaker.Faker
import java.util.Locale

// Preconfigure Faker with the UK locale so generated data is appropriate (e.g., names, addresses, phone numbers).
class Faker : Faker(Locale.of("en-GB", "GB"))
