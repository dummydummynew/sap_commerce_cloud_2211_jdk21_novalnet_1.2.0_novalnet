/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved
 */
package com.novalnet.backoffice.widgets;

import com.hybris.cockpitng.util.DefaultWidgetController;
import com.novalnet.backoffice.editors.NovalnetbackofficeService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Label;


public class NovalnetbackofficeController extends DefaultWidgetController {
    private static final long serialVersionUID = 1L;
    private Label label;

    @WireVariable
    private transient NovalnetbackofficeService novalnetbackofficeService;

    @Override
    public void initialize(final Component comp) {
        super.initialize(comp);
        label.setValue(novalnetbackofficeService.getHello() + " NovalnetbackofficeController");
    }
}
