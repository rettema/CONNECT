/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 * Copyright (c) 2014, AEGIS.net, Inc. All rights reserved.
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
package gov.hhs.fha.nhinc.hiem._20.unsubscribe.nhin;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapHeaderHelper;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.hiem.dte.SoapUtil;
import gov.hhs.fha.nhinc.hiem.processor.faults.SubscriptionManagerSoapFaultFactory;
import gov.hhs.fha.nhinc.messaging.server.BaseService;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants.UDDI_SPEC_VERSION;
import gov.hhs.fha.nhinc.unsubscribe.inbound.InboundHiemUnsubscribe;

import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.w3c.dom.Element;

/**
 * HIEM Nhin Unsubscribe Service Implementation
 *
 * @author jhoppesc
 * @author richard.ettema
 */
public class HiemUnsubscribeImpl extends BaseService {

    private static final Logger LOG = Logger.getLogger(HiemUnsubscribeImpl.class);

    private InboundHiemUnsubscribe inboundHiemUnsubscribe;

    public HiemUnsubscribeImpl(InboundHiemUnsubscribe inboundHiemUnsubscribe) {
        this.inboundHiemUnsubscribe = inboundHiemUnsubscribe;
    }

    /**
     *
     * @param unsubscribeRequest
     * @param context
     * @return
     * @throws UnableToDestroySubscriptionFault
     */
    public UnsubscribeResponse unsubscribe(Unsubscribe unsubscribeRequest, WebServiceContext context)
            throws UnableToDestroySubscriptionFault {

        LOG.debug("Entering HiemUnsubscribeImpl.unsubscribe");

        UnsubscribeResponse response = null;

        try {
            Element soapMessage = extractSoapMessage(context);

            AssertionType assertion = getAssertion(context, null);
            if (assertion != null) {
                assertion.setImplementsSpecVersion(UDDI_SPEC_VERSION.SPEC_2_0.toString());
            }

            SoapMessageElements referenceParametersElements = new SoapHeaderHelper().getSoapHeaderElements(context);

            response = inboundHiemUnsubscribe.processNhinUnsubscribe(unsubscribeRequest, soapMessage, referenceParametersElements, assertion);

        } catch (Exception ex) {
            LOG.error("Exception: " + ex.getMessage(), ex);
            throw new SubscriptionManagerSoapFaultFactory().getGenericProcessingExceptionFault(ex);
        }

        LOG.debug("Exiting HiemUnsubscribeImpl.unsubscribe");

        return response;
    }

    private Element extractSoapMessage(WebServiceContext context) {
        return new SoapUtil().extractSoapMessageElement(context, NhincConstants.HTTP_REQUEST_ATTRIBUTE_SOAPMESSAGE);
    }

}
