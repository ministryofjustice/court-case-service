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
    fun `should return a null score for an invalid court name`() {
        // Given
        val courtName = "Nonsense"
        val offenderAge = 25
        val offenceCode = "80702"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNull()
    }

    @Test
    fun `should return a null score for an invalid offence code`() {
        // Given
        val courtName = "Cardiff"
        val offenderAge = 25
        val offenceCode = "9X9X9X9X"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNull()
    }

    @Test
    fun `should calculate score when home office offence code contains non-numeric characters`() {
        // Given
        val courtName = "Cardiff"
        val offenderAge = 25
        val offenceCode = "046/00"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNotNull
        assertThat(custodyPredictorScore).isEqualTo(0.5668145445162719)
    }

    @Test
    fun `should throw exception for invalid age`() {
        // Given
        val courtName = "Cardiff"
        val offenderAge = 999999
        val offenceCode = "80702"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNull()
    }

    @Test
    fun `should throw exception when offender is under 18 years old`() {
        // Given
        val courtName = "Cardiff"
        val offenderAge = 17
        val offenceCode = "80702"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(courtName, offenderAge, offenceCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNull()
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