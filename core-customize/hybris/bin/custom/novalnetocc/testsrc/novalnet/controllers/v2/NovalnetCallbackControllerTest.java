/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */
package novalnet.controllers.v2;

import com.novalnet.facades.NovalnetCallbackFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.novalnet.beans.NnCallbackRequestData;
import de.novalnet.beans.NnCallbackResponseData;
import jakarta.servlet.http.HttpServletRequest;
import novalnet.dto.payment.NnCallbackRequestWsDTO;
import static org.assertj.core.api.Assertions.assertThat;
import novalnet.dto.payment.NnCallbackResponseWsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NovalnetCallbackController}.
 *
 */
@UnitTest
@ExtendWith(MockitoExtension.class)
public class NovalnetCallbackControllerTest {
    private static final String FIELDS = "DEFAULT";
    private static final String SUCCESS_MESSAGE = "Novalnet webhook script executed. Status updated for initial transaction";
    private static final String ERROR_MESSAGE = "Novalnet callback processing failed";

    @Mock
    private NovalnetCallbackFacade novalnetCallbackFacade;

    @Mock
    private DataMapper dataMapper;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private NnCallbackRequestWsDTO wsRequest;

    @Mock
    private NnCallbackRequestData callbackRequest;

    @Mock
    private NnCallbackResponseWsDTO responseWsDTO;

    @InjectMocks
    private NovalnetCallbackController novalnetCallbackController;


    @Test
    public void shouldHandleCallbackSuccessfully() {
        when(dataMapper.map(wsRequest, NnCallbackRequestData.class, FIELDS)).thenReturn(callbackRequest);
        when(novalnetCallbackFacade.processCallback(callbackRequest, httpRequest)).thenReturn(SUCCESS_MESSAGE);
        when(dataMapper.map(any(NnCallbackResponseData.class), eq(NnCallbackResponseWsDTO.class), eq(FIELDS)))
                .thenReturn(responseWsDTO);

        NnCallbackResponseWsDTO result = novalnetCallbackController.handleCallback(wsRequest, FIELDS, httpRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(responseWsDTO);

        verify(dataMapper).map(wsRequest, NnCallbackRequestData.class, FIELDS);
        verify(novalnetCallbackFacade).processCallback(callbackRequest, httpRequest);
        verify(dataMapper).map(any(NnCallbackResponseData.class), eq(NnCallbackResponseWsDTO.class), eq(FIELDS));
    }


    @Test
    public void shouldHandleCallbackException() {
        when(dataMapper.map(wsRequest, NnCallbackRequestData.class, FIELDS)).thenReturn(callbackRequest);
        when(novalnetCallbackFacade.processCallback(callbackRequest, httpRequest)).thenThrow(new RuntimeException(ERROR_MESSAGE));
        when(dataMapper.map(any(NnCallbackResponseData.class), eq(NnCallbackResponseWsDTO.class), eq(FIELDS)))
                .thenReturn(responseWsDTO);

        NnCallbackResponseWsDTO result = novalnetCallbackController.handleCallback(wsRequest, FIELDS, httpRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(responseWsDTO);

        verify(dataMapper).map(wsRequest, NnCallbackRequestData.class, FIELDS);
        verify(novalnetCallbackFacade).processCallback(callbackRequest, httpRequest);
        verify(dataMapper).map(any(NnCallbackResponseData.class), eq(NnCallbackResponseWsDTO.class), eq(FIELDS));
    }


    @Test
    public void shouldHandleRequestMappingException() {
        when(dataMapper.map(wsRequest, NnCallbackRequestData.class, FIELDS)).thenThrow(new RuntimeException(ERROR_MESSAGE));
        when(dataMapper.map(any(NnCallbackResponseData.class), eq(NnCallbackResponseWsDTO.class), eq(FIELDS)))
                .thenReturn(responseWsDTO);

        NnCallbackResponseWsDTO result = novalnetCallbackController.handleCallback(wsRequest, FIELDS, httpRequest);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(responseWsDTO);

        verify(dataMapper).map(wsRequest, NnCallbackRequestData.class, FIELDS);
        verify(dataMapper).map(any(NnCallbackResponseData.class), eq(NnCallbackResponseWsDTO.class), eq(FIELDS));
    }
}
