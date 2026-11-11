/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.setup;

import com.novalnet.constants.NovalnetcoreConstants;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.setup.SetupImpexService;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetcoreSystemSetupTest {
    private static final String IMPORT_CORE_DATA = "importCoreData";

    private static final String ENABLED = "yes";
    private static final String DISABLED = "no";

    private static final String ADD_PAYMENT_MODE = "/novalnetcore/import/AddNovalnetPaymentMode.impex";
    private static final String RETURN_ORDER = "/novalnetcore/import/projectdata-dynamic-business-return-order.impex";
    private static final String CONSTRAINTS = "/novalnetcore/import/essentialdata-constraints.impex";
    private static final String DYNAMIC_B2C = "/novalnetcore/import/projectdata-dynamic-b2c.impex";

    @Mock
    private SetupImpexService setupImpexService;

    @Mock
    private SystemSetupContext context;

    private NovalnetcoreSystemSetup systemSetup;

    @BeforeEach
    void setUp() {
        systemSetup = new NovalnetcoreSystemSetup();

        ReflectionTestUtils.setField(systemSetup, "setupImpexService", setupImpexService);
    }

    @Test
    void shouldOfferSingleImportCoreDataOption() {
        final List<SystemSetupParameter> options = systemSetup.getInitializationOptions();

        assertThat(options).hasSize(1);
        assertThat(options.get(0).getKey()).isEqualTo(IMPORT_CORE_DATA);
        assertThat(options.get(0).getLabel()).isEqualTo("Import Core Data");
    }

    @Test
    void shouldReturnNewOptionListOnEachCall() {
        assertThat(systemSetup.getInitializationOptions()).isNotSameAs(systemSetup.getInitializationOptions());
    }

    @Test
    void shouldImportAllImpexFilesInOrderWhenEnabled() {
        stubImportCoreData(ENABLED);

        systemSetup.createProjectData(context);

        final InOrder orderedInteractions = inOrder(setupImpexService);

        orderedInteractions.verify(setupImpexService).importImpexFile(ADD_PAYMENT_MODE, false);

        orderedInteractions.verify(setupImpexService).importImpexFile(RETURN_ORDER, false);

        orderedInteractions.verify(setupImpexService).importImpexFile(CONSTRAINTS, false);

        orderedInteractions.verify(setupImpexService).importImpexFile(DYNAMIC_B2C, false);

        orderedInteractions.verifyNoMoreInteractions();
    }

    @Test
    void shouldNotImportAnythingWhenDisabled() {
        stubImportCoreData(DISABLED);

        systemSetup.createProjectData(context);

        verifyNoInteractions(setupImpexService);
    }

    private void stubImportCoreData(final String value) {
        lenient().when(context.getExtensionName()).thenReturn(NovalnetcoreConstants.EXTENSIONNAME);

        lenient().when(context.getParameter(anyString())).thenReturn(value);
    }
}
