package uk.gov.justice.probation.courtcaseservice.controller;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UpperCaseResolverTest {

    @Mock
    private UpperCasePathVariable crnPathVar;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private NativeWebRequest webRequest;

    private final UpperCaseResolver upperCaseResolver = new UpperCaseResolver();

    @BeforeEach
    void beforeEach() {
        when(methodParameter.getParameterAnnotation(UpperCasePathVariable.class)).thenReturn(crnPathVar);
    }

    @Test
    void whenPassUpperCasePathVariable_thenTrimAndUpperCase() {
        when(crnPathVar.value()).thenReturn("crn");
        when(webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)).thenReturn(Map.of("crn", " x349752 "));

        assertThat(upperCaseResolver.resolveArgument(methodParameter, null, webRequest, null)).isEqualTo("X349752");
    }

    @Test
    void whenRequestReturnsNullAttributes_thenReturnNull() {
        when(webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)).thenReturn(null);

        assertThat(upperCaseResolver.resolveArgument(methodParameter, null, webRequest, null)).isNull();
    }

    @Test
    void whenRequestReturnsWrongAttributes_thenReturnNull() {
        when(crnPathVar.value()).thenReturn("crn");
        when(webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST)).thenReturn(Map.of("courtCode", "x349752"));

        assertThat(upperCaseResolver.resolveArgument(methodParameter, null, webRequest, null)).isNull();
    }
}
