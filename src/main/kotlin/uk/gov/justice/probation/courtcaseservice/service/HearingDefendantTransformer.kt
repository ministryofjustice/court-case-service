package uk.gov.justice.probation.courtcaseservice.service

import org.hibernate.query.TupleTransformer
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantDTO
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDefendantDTOKotlin


class HearingDefendantTransformer : TupleTransformer<HearingDefendantDTO> {
    private val hearingDefendantDTOKotlinMap: LinkedHashMap<Long, HearingDefendantDTO> = LinkedHashMap<Long, HearingDefendantDTO>();


    override fun transformTuple(
        tuple: Array<Any?>,
        aliases: Array<String?>?
    ): HearingDefendantDTO {

        println("hello")
//        val aliasToIndexMap: Map<String, Int> = aliasToIndexMap(aliases)
//
//        val postId: Long = longValue(tuple[aliasToIndexMap[HearingDefendantDTO.ID_ALIAS]!!])
//
//        val hearingDefendantDTO: HearingDefendantDTO = hearingDefendantDTOMap.computeIfAbsent(
//            postId
//        ) { id -> HearingDefendantDTO(tuple, aliasToIndexMap) }
//
//        hearingDefendantDTO.getComments().add(
//            HearingDefendantDTO(tuple, aliasToIndexMap)
//        )

//        return hearingDefendantDTO;
        return HearingDefendantDTO();
    }
}