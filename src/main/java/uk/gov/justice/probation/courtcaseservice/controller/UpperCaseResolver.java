package uk.gov.justice.probation.courtcaseservice.controller;

import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

public class UpperCaseResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!parameter.hasParameterAnnotation(UpperCasePathVariable.class)) {
            return false;
        }
        if (Map.class.isAssignableFrom(parameter.nestedIfOptional().getNestedParameterType())) {
            val pathVariable = parameter.getParameterAnnotation(PathVariable.class);
            return pathVariable != null && StringUtils.hasText(pathVariable.value());
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory) {
        val attr = parameter.getParameterAnnotation(UpperCasePathVariable.class);
        val templateVars = webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);

        if (templateVars instanceof Map) {
            return Optional.ofNullable(((Map<String, String>) templateVars).get(attr.value()))
                .map(String::trim)
                .map(String::toUpperCase)
                .orElse(null);
        }

        return null;
    }
}
