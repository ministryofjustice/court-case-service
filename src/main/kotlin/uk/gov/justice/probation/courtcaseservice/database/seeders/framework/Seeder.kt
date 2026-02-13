package uk.gov.justice.probation.courtcaseservice.database.seeders.framework

import jakarta.persistence.EntityManager

abstract class Seeder(
  protected val entityManager: EntityManager,
) : ISeeder {
  open fun beforeSeed() {}
  open fun seed() {}
  open fun shouldClean(): Boolean = false
  open fun afterSeed() {}

  override fun run() {
    if (shouldClean()) {
      clean()
    }
    beforeSeed()
    seed()
    afterSeed()
  }

  open fun clean() {
    // truncate DB tables in the correct order to avoid FK issues
    entityManager.createNativeQuery(
      "TRUNCATE courtcaseservicetest.offender_match_group CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.offender_match CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.hearing_day CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.hearing CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.hearing_defendant CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.defendant CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.offender CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.court_case CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.court CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.case_comments CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.hearing_outcome CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.hearing_notes CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.hearing_day_aud CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.offence_aud CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.hearing_defendant_aud CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.defendant_aud CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.hearing_aud CASCADE;\n" +
        "TRUNCATE courtcaseservicetest.court_case_aud CASCADE;",
    ).executeUpdate()
  }
}
