package uk.gov.justice.probation.courtcaseservice.service

import hex.genmodel.easy.exception.PredictUnknownCategoricalLevelException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.probation.courtcaseservice.BaseIntTest


class ShortTermCustodyPredictorServiceTest : BaseIntTest() {

    @Autowired
    lateinit var predictorService: ShortTermCustodyPredictorService

    @Test
    fun `should calculate predictor score for valid parameters`() {
        // Given
        val courtName = "Cardiff"
        val offenderAge = 25
        val offenceCode = "80702"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNotNull
        assertThat(custodyPredictorScore).isEqualTo(0.8756383297739117)
    }

    @Test
    fun `should calculate predictor score where court name contains punctuation characters`() {
        // Given
        val courtName = "Sheffield Magistrate's Court"
        val offenderAge = 25
        val offenceCode = "80702"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNotNull
        assertThat(custodyPredictorScore).isEqualTo(0.9243792950886507)
    }

    @Test
    fun `should throw exception for invalid court name`() {
        // Given
        val courtName = "Nonsense"
        val offenderAge = 25
        val offenceCode = "80702"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val thrown: PredictUnknownCategoricalLevelException = assertThrows(PredictUnknownCategoricalLevelException::class.java) {
            predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)
        }

        // Then
        assertThat(thrown.message).isEqualTo("Unknown categorical level (court_name,Nonsense)")
    }

    @Test
    fun `should throw exception for invalid offence code`() {
        // Given
        val courtName = "Cardiff"
        val offenderAge = 25
        val offenceCode = "9X9X9X9X"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val thrown: PredictUnknownCategoricalLevelException = assertThrows(PredictUnknownCategoricalLevelException::class.java) {
            predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)
        }

        // Then
        assertThat(thrown.message).isEqualTo("Unknown categorical level (offence_code,9X9X9X9X)")
    }

    @Test
    fun `should throw exception for invalid age`() {
        // Given
        val courtName = "Cardiff"
        val offenderAge = 999999
        val offenceCode = "80702"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val thrown: PredictUnknownCategoricalLevelException = assertThrows(PredictUnknownCategoricalLevelException::class.java) {
            predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)
        }

        // Then
        assertThat(thrown.message).isEqualTo("Unknown categorical level (offender_age,999999)")
    }

    @Test
    fun `should throw exception when offender is under 18 years old`() {
        // Given
        val courtName = "Cardiff"
        val offenderAge = 17
        val offenceCode = "80702"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val thrown: PredictUnknownCategoricalLevelException = assertThrows(PredictUnknownCategoricalLevelException::class.java) {
            predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)
        }

        // Then
        assertThat(thrown.message).isEqualTo("Unknown categorical level (offender_age,17)")
    }

    @Test
    fun `should calculate predictor score when offender age is not provided`() {
        // Given
        val courtName = "Cardiff"
        val offenderAge = null
        val offenceCode = "80702"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isEqualTo(0.9081075078820396)
    }

}