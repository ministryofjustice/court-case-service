package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.probation.courtcaseservice.BaseIntTest


class ShortTermCustodyPredictorServiceTest : BaseIntTest() {

    @Autowired
    lateinit var predictorService: ShortTermCustodyPredictorService

    @ParameterizedTest
    @MethodSource("provideMagistrateCourtCodes")
    fun `should calculate predictor score for valid court codes`(courtCode : String) {
        // Given
        val offenderAge = 25
        val offenceCode = "80702"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(offenderAge, offenceCode, courtCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNotNull
        assertThat(custodyPredictorScore).isGreaterThan(0.0)
        assertThat(custodyPredictorScore).isLessThan(1.0)
    }



    @Test
    fun `should return a null score for an invalid court code`() {
        // Given
        val offenderAge = 25
        val offenceCode = "80702"
        val courtCode = "XXXXX"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(offenderAge, offenceCode, courtCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNull()
    }

    @Test
    fun `should return a null score for an invalid offence code`() {
        // Given
        val offenderAge = 25
        val offenceCode = "9X9X9X9X"
        val courtCode = "B04DS"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(offenderAge, offenceCode, courtCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNull()
    }

    @Test
    fun `should calculate score when home office offence code contains non-numeric characters`() {
        // Given
        val offenderAge = 25
        val offenceCode = "046/00"
        val courtCode = "B04DS"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(offenderAge, offenceCode, courtCode)


        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNotNull
        assertThat(custodyPredictorScore).isEqualTo(0.9311253572283462)
    }

    @Test
    fun `should throw exception for invalid age`() {
        // Given
        val offenderAge = 999999
        val offenceCode = "80702"
        val courtCode = "B04DS"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(offenderAge, offenceCode, courtCode)

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
        val courtCode = "C62CR"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(offenderAge, offenceCode, courtCode)

        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isNull()
    }

    @Test
    fun `should calculate predictor score when offender age is not provided`() {
        // Given
        val offenderAge = null
        val offenceCode = "80702"
        val courtCode = "B04DS"
        val shortTermCustodyPredictorParameters = ShortTermCustodyPredictorParameters(offenderAge, offenceCode, courtCode)


        // When
        val custodyPredictorScore = predictorService.calculateShortTermCustodyPredictorScore(shortTermCustodyPredictorParameters)

        // Then
        assertThat(custodyPredictorScore).isEqualTo(0.9870862213669677)
    }

    companion object {
        @JvmStatic
        fun provideMagistrateCourtCodes() : List<String> {
            return listOf(
                "B01BH",
                "B01CE",
                "B01CN",
                "B01CX",
                "B01DU",
                "B01EF",
                "B01FA",
                "B01GQ",
                "B01GU",
                "B01IX",
                "B01LA",
                "B01LY",
                "B01MN",
                "B01ND",
                "B01NM",
                "B01OK",
                "B03AX",
                "B03DE",
                "B04BP",
                "B04BQ",
                "B04CO",
                "B04DS",
                "B04HQ",
                "B04KO",
                "B05BK",
                "B05PK",
                "B06AN",
                "B06BV",
                "B06IS",
                "B06MK",
                "B06OJ",
                "B07DM",
                "B07ED",
                "B07FQ",
                "B10BD",
                "B10BF",
                "B10FR",
                "B10JQ",
                "B10LX",
                "B10MR",
                "B11EI",
                "B11JP",
                "B11KF",
                "B12GH",
                "B12JR",
                "B12LK",
                "B12LT",
                "B12PA",
                "B13CC",
                "B13HD",
                "B13HT",
                "B14AV",
                "B14ET",
                "B14LO",
                "B16BG",
                "B16CJ",
                "B16GB",
                "B16HE",
                "B17JA",
                "B20BL",
                "B20EB",
                "B20EY",
                "B20NQ",
                "B20OQ",
                "B21DA",
                "B22GR",
                "B22HM",
                "B22KS",
                "B22MZ",
                "B22OS",
                "B23HS",
                "B23PP",
                "B30PG",
                "B30PI",
                "B31IT",
                "B31JV",
                "B32BX",
                "B32HX",
                "B33HU",
                "B33II",
                "B34JS",
                "B34NX",
                "B35CZ",
                "B35HF",
                "B35KE",
                "B36FZ",
                "B36HN",
                "B36JU",
                "B37HI",
                "B40BC",
                "B40IM",
                "B41ME",
                "B41MJ",
                "B41US",
                "B42AZ",
                "B42CM",
                "B42CO",
                "B42MB",
                "B43AQ",
                "B43JC",
                "B43KB",
                "B43KQ",
                "B43LV",
                "B43OX",
                "B44AG",
                "B44BA",
                "B44JK",
                "B44KM",
                "B44MA",
                "B45GC",
                "B45MH",
                "B46DB",
                "B46DH",
                "B46FO",
                "B46IR",
                "B46IU",
                "B46LN",
                "B47CL",
                "B47EC",
                "B47FB",
                "B47GL",
                "B47HB",
                "B47OV",
                "B50AW",
                "B50BU",
                "B50JO",
                "B50KH",
                "B50NL",
                "B52BB",
                "B52CM",
                "B52MY",
                "B52OC",
                "B52OZ",
                "B53DJ",
                "B54MW",
                "B54WV",
                "B55KL",
                "B55OE",
                "B60ID",
                "B60JE",
                "B60OW",
                "B60WU",
                "B61EH",
                "B61NP",
                "B62DC",
                "B62IZ",
                "B62MV",
                "B63AD",
                "B63GN",
                "B63IC",
                "B63IE",
                "B63NZ"
            )
        }
    }
}