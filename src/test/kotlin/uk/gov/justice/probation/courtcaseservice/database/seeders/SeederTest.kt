package uk.gov.justice.probation.courtcaseservice.database.seeders

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import uk.gov.justice.probation.courtcaseservice.database.seeders.framework.ISeeder
import uk.gov.justice.probation.courtcaseservice.database.seeders.framework.Seeder
import jakarta.persistence.EntityManager

class SeederTest {
  private class TestSeeder(
    private val cleanFlag: Boolean = false,
    entityManager: EntityManager,
  ) : Seeder(entityManager) {
    val calls = mutableListOf<String>()

    override fun shouldClean(): Boolean = cleanFlag
    override fun clean() { calls.add("clean") }
    override fun beforeSeed() { calls.add("beforeSeed") }
    override fun seed() { calls.add("seed") }
  }

  @Test
  fun `TestSeeder implements ISeeder`() {
    val seeder = TestSeeder(entityManager = mock(EntityManager::class.java))
    assertThat(seeder).isInstanceOf(ISeeder::class.java)
  }

  @Test
  fun `run only executes beforeSeed then seed when clean is false`() {
    val seeder = TestSeeder(cleanFlag = false, entityManager = mock(EntityManager::class.java))
    seeder.run()
    assertThat(seeder.calls).containsExactly("beforeSeed", "seed")
  }

  @Test
  fun `run executes clean then beforeSeed then seed when clean is true`() {
    val seeder = TestSeeder(cleanFlag = true, entityManager = mock(EntityManager::class.java))
    seeder.run()
    assertThat(seeder.calls).containsExactly("clean", "beforeSeed", "seed")
  }
}