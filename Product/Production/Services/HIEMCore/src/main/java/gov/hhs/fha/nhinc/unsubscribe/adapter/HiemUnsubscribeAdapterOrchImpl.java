/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.unsubscribe.adapter;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.hiem.consumerreference.ReferenceParametersHelper;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.hiem.processor.common.HiemProcessorConstants;
import gov.hhs.fha.nhinc.subscription.repository.service.HiemSubscriptionRepositoryService;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.w3c.dom.Element;

/**
 * HIEM Unsubscribe Adapter Orchestration implementation
 *
 * @author richard.ettema
 */
public class HiemUnsubscribeAdapterOrchImpl {

    private static final Logger LOG = Logger.getLogger(HiemUnsubscribeAdapterOrchImpl.class);

    public UnsubscribeResponse adapterUnsubscribe(Element unsubscribe, AssertionType assertion, SoapMessageElements referenceParametersElements) throws Exception {

        LOG.debug("Begin HiemUnsubscribeAdapterOrchImpl.adapterUnsubscribe");

        UnsubscribeResponse response = null;

        try {
            // Get subscription id
            String subscriptionId = ReferenceParametersHelper.getSubscriptionId(referenceParametersElements);

            // Update subscription record status to 'UNSUBSCRIBED'
            HiemSubscriptionRepositoryService repo = new HiemSubscriptionRepositoryService();
            repo.updateSubscriptionItemStatus(subscriptionId, HiemProcessorConstants.SUBSCRIPTION_ROLE_PRODUCER, HiemProcessorConstants.SUBSCRIPTION_STATUS_UNSUBSCRIBED);

            // If we got this far, success - instantiate empty unsubscribe response
            response = new UnsubscribeResponse();
        } catch (Exception e) {
            LOG.error("Failed to process unsubscribe request", e);
            throw new UnableToDestroySubscriptionFault(e.getMessage());
        }

        LOG.debug("End HiemUnsubscribeAdapterOrchImpl.adapterUnsubscribe");

        return response;

    }

}
