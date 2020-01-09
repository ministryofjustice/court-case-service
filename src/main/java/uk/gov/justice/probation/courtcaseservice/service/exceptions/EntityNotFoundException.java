package uk.gov.justice.probation.courtcaseservice.service.exceptions;

public class EntityNotFoundException extends RuntimeException {

        public EntityNotFoundException(String msg, Object... args) {
            super(String.format(msg, args));
        }

    }
