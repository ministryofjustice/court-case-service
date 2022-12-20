package uk.gov.justice.probation.courtcaseservice.service

import hex.genmodel.easy.EasyPredictModelWrapper
import hex.genmodel.easy.RowData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ShortTermCustodyPredictorService(private val model: EasyPredictModelWrapper) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun calculateShortTermCustodyPredictorScore(
        courtName: String,
        offenderAge: Int?,
        offenceCode: String) : Double {

        log.debug("Entered calculateShortTermCustodyPredictorScore($courtName, $offenderAge, $offenceCode)")

        val rowData = RowData()

        rowData["court_name"] = courtName
        offenderAge?.let {
            rowData["offender_age"] = offenderAge.toString()
        }
        rowData["offence_code"] = offenceCode

        val prediction = model.predictBinomial(rowData)
        val score = prediction.classProbabilities[0]
        log.info("Calculated short term custody score of $score for court: $courtName, offender age: $offenderAge, offence code: $offenceCode")

        return score
    }
}