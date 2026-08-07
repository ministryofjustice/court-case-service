package uk.gov.justice.probation.courtcaseservice.database.data.offence

class OffenceProvider(private val archetypes: List<OffenceArchetype> = offenceArchetypes) {
  fun archetype(): OffenceArchetype = archetypes.random()
}