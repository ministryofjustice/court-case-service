package uk.gov.justice.probation.courtcaseservice.database.factories.framework

import org.springframework.data.repository.CrudRepository

open class Factory<T : Any>(
  private val newModel: () -> T,
  private val repository: CrudRepository<T, *>,
  private var count: Int = 1,
) : IFactory<T> {

  override fun count(count: Int): IFactory<T> {
    this.count = count
    return this
  }

  override fun create(): List<T> {
    val items = (1..count).map { newModel() }
    return repository.saveAll(items).toList()
  }
}
