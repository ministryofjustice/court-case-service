package uk.gov.justice.probation.courtcaseservice.controller;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;


/**
 * Custom annotation to provision validation of listNo field.
 */
@Target({TYPE, ANNOTATION_TYPE, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ListNoValidator.class) // 1.
public @interface ValidateListNo {
    String message() default "Invalid lisNo";
    Class<?>[] groups() default {}; // 2.
    Class<? extends Payload>[] payload() default {}; // 3.
}