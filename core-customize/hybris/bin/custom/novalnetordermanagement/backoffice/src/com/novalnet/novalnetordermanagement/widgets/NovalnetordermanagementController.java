/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved
 */
package com.novalnet.novalnetordermanagement.widgets;

import com.hybris.cockpitng.util.DefaultWidgetController;
import com.novalnet.novalnetordermanagement.services.NovalnetordermanagementService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Label;


public class NovalnetordermanagementController extends DefaultWidgetController {
    private static final long serialVersionUID = 1L;
    private Label label;

    @WireVariable
    private transient NovalnetordermanagementService novalnetordermanagementService;

    @Override
    public void initialize(final Component comp) {
        super.initialize(comp);
        label.setValue(novalnetordermanagementService.getHello() + " NovalnetordermanagementController");
    }
}
