/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.dao;

import com.novalnet.model.NovalnetPaymentRefInfoModel;
import com.novalnet.model.NovalnetReplPayRefInfoModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.CartModel;


/**
 * Defines data access operations for Novalnet replenishment payments and payment references.
 */
public interface NovalnetReplenishmentDao {
    /**
     * Finds a replenishment payment reference using the replenishment order ID.
     *
     * @param replenishmentOrderId the replenishment order ID
     * @return the replenishment payment reference, or null if not found
     */
    NovalnetReplPayRefInfoModel findReplenishmentPaymentReference(String replenishmentOrderId);


    NovalnetPaymentRefInfoModel findPaymentReference(final CartModel cart, final PK paymentReferencePk);

}
