package uk.gov.justice.probation.courtcaseservice.service.exceptions;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String fileType, List<String> allowedExtensions) {
        super(String.format("Unsupported or missing file type {%s}. Supported file types %s", fileType, StringUtils.join(allowedExtensions)));
    }
}
