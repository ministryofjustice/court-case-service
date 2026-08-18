package uk.gov.justice.probation.courtcaseservice.database.factories.framework

interface IFactory<T> {
  fun count(count: Int = 1): IFactory<T>
  fun create(): List<T>
}
