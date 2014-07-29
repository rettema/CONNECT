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
package gov.hhs.fha.nhinc.unsubscribe.adapter.proxy;

import gov.hhs.fha.nhinc.aspect.AdapterDelegationEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.WsntUnsubscribeResponseMarshaller;
import gov.hhs.fha.nhinc.unsubscribe.adapter.HiemUnsubscribeAdapterOrchImpl;
import gov.hhs.fha.nhinc.unsubscribe.aspect.UnsubscribeRequestTransformingBuilder;
import gov.hhs.fha.nhinc.unsubscribe.aspect.UnsubscribeResponseDescriptionBuilder;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.w3c.dom.Element;

/**
 * HIEM Unsubscribe Adapter Java implementation
 *
 * @author richard.ettema
 */
public class HiemUnsubscribeAdapterJavaImpl implements HiemUnsubscribeAdapterProxy {

    private static final Logger LOG = Logger.getLogger(HiemUnsubscribeAdapterJavaImpl.class);

    /* (non-Javadoc)
     * @see gov.hhs.fha.nhinc.unsubscribe.adapter.proxy.HiemUnsubscribeAdapterProxy#unsubscribe(org.w3c.dom.Element, gov.hhs.fha.nhinc.common.nhinccommon.AssertionType, gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements)
     */
    @Override
    @AdapterDelegationEvent(beforeBuilder = UnsubscribeRequestTransformingBuilder.class,
    afterReturningBuilder = UnsubscribeResponseDescriptionBuilder.class, serviceType = "HIEM Unsubscribe",
    version = "2.0")
    public Element unsubscribe(Element unsubscribeElement, AssertionType assertion,
            SoapMessageElements referenceParametersElements) throws Exception {

        LOG.debug("Enter HiemUnsubscribeAdapterJavaImpl.unsubscribe()");

        Element responseElement = null;

        // Call HiemUnsubscribeAdapterOrchImpl class
        HiemUnsubscribeAdapterOrchImpl adapterOrchImpl = new HiemUnsubscribeAdapterOrchImpl();

        UnsubscribeResponse unsubscribeResponse = adapterOrchImpl.adapterUnsubscribe(unsubscribeElement, assertion,
                referenceParametersElements);

        WsntUnsubscribeResponseMarshaller unsubscribeMarshaller = new WsntUnsubscribeResponseMarshaller();
        responseElement = unsubscribeMarshaller.marshal(unsubscribeResponse);

        LOG.debug("End HiemUnsubscribeAdapterJavaImpl.unsubscribe()");

        return responseElement;
    }

}
