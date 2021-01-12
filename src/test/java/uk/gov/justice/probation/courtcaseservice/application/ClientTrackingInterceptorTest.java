package uk.gov.justice.probation.courtcaseservice.application;

import com.google.common.net.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientTrackingInterceptorTest {

    private final String TOKEN = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjpudWxsLCJ1c2VyX25hbWUiOiJBbGZyZWQgSGl0Y2hjb2NrIiwic2NvcGUiOlsicmVhZCIsIndyaXRlIiwicHJveHktdXNlciJdLCJhdXRoX3NvdXJjZSI6Im5vbmUiLCJleHAiOjE1ODMyMzMxMjMsImF1dGhvcml0aWVzIjpbIlJPTEVfU1lTVEVNX1VTRVIiLCJST0xFX0NPTU1VTklUWSJdLCJqdGkiOiI0MzI3ZDE2Ny0yOWRiLTRjOTgtODA2ZC1jNmM1NDE2ZjYyYTUiLCJjbGllbnRfaWQiOiJjb21tdW5pdHktYXBpLWNsaWVudCJ9==.OP9TUKRmHfN8uT5QWRdrofoFuSifMZOx7nuLt0Vn_S2i1bBAmTxvPCghn_0njpiu3CS1DnEUOeZ5Oz04aJ871lSWB7wPV4UM-AJnAC99l-iD5M3kL6YXD3Bzr1ydYLkcVJeqqJo0CTbRozOIT_mpmfKpRugIldTRJycWhUxPfPnaWdOAWz6Yfch3ajD6K5EkqS6e5FCrut-bC90Z1edmNYoR5PdJfE-zVQU4fa6ta-VlUsc7ZlmIEzC98A3RB_arxXwGQ_8rNwYmtJb9t1bbTlgsEkul-R5ogwCKPipPWy8hDF-U88dAiv8PfXU_rKzdcFlhP-4C9zK7aUisrko29A";
    private final String TOKEN_WITH_NO_USER_OR_CLIENT = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjpudWxsLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiLCJwcm94eS11c2VyIl0sImF1dGhfc291cmNlIjoibm9uZSIsImV4cCI6MTU4MzIzMzEyMywiYXV0aG9yaXRpZXMiOlsiUk9MRV9TWVNURU1fVVNFUiIsIlJPTEVfQ09NTVVOSVRZIl0sImp0aSI6IjQzMjdkMTY3LTI5ZGItNGM5OC04MDZkLWM2YzU0MTZmNjJhNSJ9==.OP9TUKRmHfN8uT5QWRdrofoFuSifMZOx7nuLt0Vn_S2i1bBAmTxvPCghn_0njpiu3CS1DnEUOeZ5Oz04aJ871lSWB7wPV4UM-AJnAC99l-iD5M3kL6YXD3Bzr1ydYLkcVJeqqJo0CTbRozOIT_mpmfKpRugIldTRJycWhUxPfPnaWdOAWz6Yfch3ajD6K5EkqS6e5FCrut-bC90Z1edmNYoR5PdJfE-zVQU4fa6ta-VlUsc7ZlmIEzC98A3RB_arxXwGQ_8rNwYmtJb9t1bbTlgsEkul-R5ogwCKPipPWy8hDF-U88dAiv8PfXU_rKzdcFlhP-4C9zK7aUisrko29A";

    @Mock
    private ClientDetails clientDetails;
    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    public void preHandle_shouldExtractFieldsFromToken() {
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TOKEN);
        new ClientTrackingInterceptor(clientDetails)
                .preHandle(httpServletRequest, null, null);

        verify(clientDetails).setClientDetails("community-api-client", "Alfred Hitchcock");
        verifyNoMoreInteractions(clientDetails, httpServletRequest);
    }

    @Test
    public void preHandle_shouldNotExtractFieldsFromTokenIfItIsMalformed() {
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("I am bad token");
        new ClientTrackingInterceptor(clientDetails)
                .preHandle(httpServletRequest, null, null);

        verifyNoMoreInteractions(clientDetails, httpServletRequest);
    }


    @Test
    public void preHandle_shouldTolerateMissingValues() {
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(TOKEN_WITH_NO_USER_OR_CLIENT);
        new ClientTrackingInterceptor(clientDetails)
                .preHandle(httpServletRequest, null, null);

        verify(clientDetails).setClientDetails(null, null);
        verifyNoMoreInteractions(clientDetails, httpServletRequest);
    }

}