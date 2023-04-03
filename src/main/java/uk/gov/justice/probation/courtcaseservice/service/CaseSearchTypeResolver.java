package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

enum CaseSearchType {
    CRN, NAME
}

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseSearchTypeResolver {
    private CaseSearchType type;
    private String searchTerm;
    private String extendedSearchTerm;

    private static final Pattern CRN_PATTERN = Pattern.compile("^[A-Z][0-9]{6}$");

    public static CaseSearchTypeResolver get(String searchTerm) {

        if (CRN_PATTERN.matcher(searchTerm).matches()) {
            return new CaseSearchTypeResolver(CaseSearchType.CRN, searchTerm, searchTerm.trim().toUpperCase());
        }

        var postgresTsQueryString = Arrays.stream(searchTerm.split(" ")).map(s -> s.trim()).collect(Collectors.joining(" & "));
        return new CaseSearchTypeResolver(CaseSearchType.NAME, searchTerm, postgresTsQueryString);
    }
}
