import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPut
import com.github.kittinunf.result.Result
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess

val baseUrl = "https://court-case-service-dev.apps.live-1.cloud-platform.service.justice.gov.uk"
//val baseUrl = "http://localhost:8080"

ping()

putCase("X340906", "Joe JMBBLOGGS", LocalDateTime.now())
putCase("X340753", "Ureet JMBALERNAEU", LocalDateTime.now())

fun putCase(crn: String, defendantName: String, dateTime: LocalDateTime) {

    val dateTimeString =  dateTime.format(DateTimeFormatter.ISO_DATE_TIME)

    println("Creating case for '$defendantName' with crn '$crn' on ${dateTime.format(DateTimeFormatter.ISO_DATE)}")

    val caseId = UUID.randomUUID()
    val caseNo = UUID.randomUUID()
    val putBody = """{
    "caseId": "$caseId",
    "caseNo": "$caseNo",
    "courtCode": "SHF",
    "courtRoom": "1",
    "crn": "$crn",
    "sessionStartTime": "$dateTimeString",
    "probationStatus": "No record",
    "previouslyKnownTerminationDate": "2018-06-24T09:00:00",
    "breach": true,
    "offences": [
    	{
    		"offenceTitle": "Theft from a shop",
    		"offenceSummary": "On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.",
    		"act": "Contrary to section 1(1) and 7 of the Theft Act 1968."
    	},
    	{
    		"offenceTitle": "Theft from a different shop",
    		"offenceSummary": "On 02/01/2015 at own, stole article, to the value of £123.00, belonging to person.",
    		"act": "Contrary to section 1(1) and 7 of the Theft Act 1968."
    	}
    ],
    "data": "{\"inf\": \"POL01\", \"c_id\": 1168460, \"cseq\": 1, \"h_id\": 1246272, \"type\": \"C\", \"valid\": \"Y\", \"caseno\": 1600028912, \"listno\": \"1st\", \"def_age\": 18, \"def_dob\": \"01/01/1998\", \"def_sex\": \"M\", \"def_addr\": {\"line1\": \"a1\", \"line2\": \"a2\", \"line3\": \"a3\"}, \"def_name\": \"JCONE\", \"def_type\": \"P\", \"offences\": {\"offence\": [{\"as\": \"Contrary to section 1(1) and 7 of the Theft Act 1968.\", \"sum\": \"On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.\", \"code\": \"TH68010\", \"oseq\": 1, \"co_id\": 1142407, \"title\": \"Theft from a shop\", \"maxpen\": \"EW: 6M &/or Ultd Fine\"}, {\"as\": \"Contrary to section 1(1) and 7 of the Theft Act 1968.\", \"sum\": \"On 01/01/2015 at own, stole article, to the value of £987.00, belonging to person.\", \"code\": \"TH68010\", \"oseq\": 2, \"co_id\": 1142408, \"title\": \"Theft from a shop\", \"maxpen\": \"EW: 6M &/or Ultd Fine\"}]}}",
    "defendantName": "$defendantName",
    "defendantAddress": {
        "line1": "32 Scotland St",
        "line2": "Sheffield",
        "postcode": "S3 7BS"
    }
}"""
    val putUrl = "$baseUrl/case/$caseId"

    val (_, response, result) = putUrl
            .httpPut()
            .body(putBody)
            .header("Content-Type", "application/json")
            .responseString()

    checkResponse(response, result)
    val getUrl = "$baseUrl/court/SHF/case/$caseNo"
    println("Get from: $getUrl")
}

fun ping() {
    println("Pinging application...")

    val (_, response, result) = ("$baseUrl/ping")
            .httpGet()
            .responseString()

    checkResponse(response, result)
}

fun checkResponse(response: Response, result: Result<String, FuelError>) {
    if (response.statusCode != 200) {
        println(result)
        println("Response body is: ${response.body().asString("text/string")}")
        println("Bad response from ping to court-case-service, aborting")
        exitProcess(1)
    } else {
        println("ok")
    }
}