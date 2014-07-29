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
package gov.hhs.fha.nhinc.hiem._20.unsubscribe.entity;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.UnsubscribeRequestType;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapHeaderHelper;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.messaging.server.BaseService;
import gov.hhs.fha.nhinc.unsubscribe.outbound.OutboundHiemUnsubscribe;
import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;

import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.w3c.dom.Element;

/**
 * HIEM Entity Unsubscribe Service Implementation
 *
 * @author rayj
 * @author richard.ettema
 */
public class EntityUnsubscribeServiceImpl extends BaseService {

    private static final Logger LOG = Logger.getLogger(EntityUnsubscribeServiceImpl.class);

    private OutboundHiemUnsubscribe outboundHiemUnsubscribe;

    public EntityUnsubscribeServiceImpl(OutboundHiemUnsubscribe outboundHiemUnsubscribe) {
        this.outboundHiemUnsubscribe = outboundHiemUnsubscribe;
    }

    /**
     *
     * @param unsubscribeRequest
     * @param context
     * @return
     * @throws UnableToDestroySubscriptionFault
     */
    public UnsubscribeResponse unsubscribe(UnsubscribeRequestType unsubscribeRequest, WebServiceContext context)
            throws UnableToDestroySubscriptionFault {

        LOG.debug("Begin EntityUnsubscribeServiceImpl.unsubscribe");

        AssertionType assertion = unsubscribeRequest.getAssertion();
        String subscriptionId = getSubscriptionId(context);

        UnsubscribeResponse response = outboundHiemUnsubscribe.processUnsubscribe(unsubscribeRequest.getUnsubscribe(),
                subscriptionId, assertion);

        LOG.debug("Begin EntityUnsubscribeServiceImpl.unsubscribe");

        return response;
    }

    /**
     *
     * @param context
     * @return
     */
    private String getSubscriptionId(WebServiceContext context) {

        LOG.debug("Begin EntityUnsubscribeServiceImpl.getSubscriptionId");

        SoapMessageElements soapHeaderElements = new SoapHeaderHelper().getSoapHeaderElements(context);

        String subscriptionId = null;

        if (soapHeaderElements != null) {

            for (Element soapHeaderElement : soapHeaderElements.getElements()) {

                String nodeName = soapHeaderElement.getLocalName();

                if (nodeName.equals("SubscriptionId")) {
                    String nodeValue = XmlUtility.getNodeValue(soapHeaderElement);

                    return nodeValue;
                }
            }
        }

        LOG.debug("End EntityUnsubscribeServiceImpl.getSubscriptionId");

        return subscriptionId;
    }

}
