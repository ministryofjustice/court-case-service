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
    // Omitting the schema prefix, so this will run against the current database (dev/test).
    entityManager.createNativeQuery(
        "TRUNCATE offender CASCADE;\n" +
        "TRUNCATE offender_match_group CASCADE;\n" +
        "TRUNCATE offender_match CASCADE;\n" +
        "TRUNCATE hearing_day CASCADE;\n" +
        "TRUNCATE hearing CASCADE;\n" +
        "TRUNCATE hearing_defendant CASCADE;\n" +
        "TRUNCATE case_comments CASCADE;\n" +
        "TRUNCATE hearing_notes CASCADE;\n" +
        "TRUNCATE verdict CASCADE;\n" +
        "TRUNCATE court_case CASCADE;\n" +
        "TRUNCATE judicial_result CASCADE;\n" +
        "TRUNCATE offence CASCADE;\n" +
        "TRUNCATE plea CASCADE;\n" +
        "TRUNCATE defendant CASCADE;\n" +
        "TRUNCATE hearing_outcome CASCADE;\n" +
        "TRUNCATE revinfo CASCADE;\n" +
        "TRUNCATE case_defendant CASCADE;\n" +
        "TRUNCATE case_defendant_documents CASCADE;\n" +
        "TRUNCATE case_defendant_documents_aud CASCADE;\n",
    ).executeUpdate()
  }
}
