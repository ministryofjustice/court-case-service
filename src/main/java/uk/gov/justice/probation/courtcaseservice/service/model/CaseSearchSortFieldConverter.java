package uk.gov.justice.probation.courtcaseservice.service.model;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CaseSearchSortFieldConverter implements Converter<String, CaseSearchSortFields> {
    @Override
    public CaseSearchSortFields convert(String source) {
        var sortField = CaseSearchSortFields.bySortFieldIgnoreCase(source);
        if (sortField == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Invalid sort field \"%s\"", source));
        }
        return sortField;
    }
}
