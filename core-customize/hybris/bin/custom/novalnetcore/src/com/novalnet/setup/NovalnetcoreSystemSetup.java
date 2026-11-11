/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.setup;

import com.novalnet.constants.NovalnetcoreConstants;
import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.SetupImpexService;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides system setup configuration and project data initialization for the Novalnet extension.
 */
@SystemSetup(extension = NovalnetcoreConstants.EXTENSIONNAME)
public class NovalnetcoreSystemSetup extends AbstractSystemSetup {
    private static final String IMPORT_CORE_DATA = "importCoreData";

    @Resource
    private SetupImpexService setupImpexService;

    /**
     * Provides the initialization options available for the Novalnet extension.
     *
     * @return a list of system setup parameters used during initialization
     */
    @Override
    @SystemSetupParameterMethod
    public List<SystemSetupParameter> getInitializationOptions() {
        final List<SystemSetupParameter> params = new ArrayList<>();
        params.add(createBooleanSystemSetupParameter(IMPORT_CORE_DATA, "Import Core Data", true));
        return params;
    }

    /**
     * Creates the project data required by the Novalnet extension.
     *
     * @param context the system setup context containing the initialization configuration
     */
    @SystemSetup(type = SystemSetup.Type.PROJECT, process = SystemSetup.Process.ALL)
    public void createProjectData(final SystemSetupContext context) {
        if (this.getBooleanSystemSetupParameter(context, IMPORT_CORE_DATA)) {
            setupImpexService.importImpexFile("/novalnetcore/import/AddNovalnetPaymentMode.impex", false);
            setupImpexService.importImpexFile("/novalnetcore/import/projectdata-dynamic-business-return-order.impex", false);
            setupImpexService.importImpexFile("/novalnetcore/import/essentialdata-constraints.impex", false);
            setupImpexService.importImpexFile("/novalnetcore/import/projectdata-dynamic-b2c.impex", false);
        }
    }
}
