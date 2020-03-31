package uk.gov.justice.probation.courtcaseservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Ping")
@RestController
public class PingEndpoint {

    @ApiOperation(value = "Simple endpoint to test status of server")
    @CrossOrigin
    @RequestMapping(value = "/ping")
    public String ping() {
        return "pong";
    }
}
