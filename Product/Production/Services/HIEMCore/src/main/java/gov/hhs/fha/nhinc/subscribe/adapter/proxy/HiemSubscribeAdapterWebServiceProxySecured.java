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
package gov.hhs.fha.nhinc.subscribe.adapter.proxy;

import gov.hhs.fha.nhinc.aspect.AdapterDelegationEvent;
import gov.hhs.fha.nhinc.adaptersubscription.AdapterNotificationProducerSecured;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.WsntSubscribeResponseMarshaller;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.WsntSubscribeMarshaller;
import gov.hhs.fha.nhinc.messaging.client.CONNECTCXFClientFactory;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClient;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.subscribe.adapter.proxy.service.HiemSubscribeAdapterSecuredServicePortDescriptor;
import gov.hhs.fha.nhinc.subscribe.aspect.SubscribeRequestTransformingBuilder;
import gov.hhs.fha.nhinc.subscribe.aspect.SubscribeResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.w3c.dom.Element;

/**
 *
 * @author Jon Hoppesch
 * @author richard.ettema
 */
public class HiemSubscribeAdapterWebServiceProxySecured implements HiemSubscribeAdapterProxy {

    private static final Logger LOG = Logger.getLogger(HiemSubscribeAdapterWebServiceProxySecured.class);

    private static WebServiceProxyHelper oProxyHelper = null;

    /* (non-Javadoc)
     * @see gov.hhs.fha.nhinc.subscribe.adapter.proxy.HiemSubscribeAdapterProxy#subscribe(org.w3c.dom.Element, gov.hhs.fha.nhinc.common.nhinccommon.AssertionType)
     */
    @Override
    @AdapterDelegationEvent(beforeBuilder = SubscribeRequestTransformingBuilder.class,
        afterReturningBuilder = SubscribeResponseDescriptionBuilder.class, serviceType = "HIEM Subscribe",
        version = "2.0")
    public Element subscribe(Element subscribeElement, AssertionType assertion) throws Exception {
        Element responseElement = null;

        String url = getWebServiceProxyHelper().getAdapterEndPointFromConnectionManager(
                NhincConstants.HIEM_SUBSCRIBE_ADAPTER_SECURED_SERVICE_NAME);
        if (NullChecker.isNotNullish(url)) {
            WsntSubscribeMarshaller subscribeMarshaller = new WsntSubscribeMarshaller();
            Subscribe subscribe = subscribeMarshaller.unmarshalUnsubscribeRequest(subscribeElement);

            ServicePortDescriptor<AdapterNotificationProducerSecured> portDescriptor = new HiemSubscribeAdapterSecuredServicePortDescriptor();

            CONNECTClient<AdapterNotificationProducerSecured> client = getCONNECTClientSecured(portDescriptor, url,
                    assertion);

            SubscribeResponse response = (SubscribeResponse) client.invokePort(
                    AdapterNotificationProducerSecured.class, "subscribe", subscribe);

            WsntSubscribeResponseMarshaller subscribeResponseMarshaller = new WsntSubscribeResponseMarshaller();
            responseElement = subscribeResponseMarshaller.marshal(response);
        } else {
            LOG.error("Failed to call the web service (" + NhincConstants.HIEM_SUBSCRIBE_ADAPTER_SECURED_SERVICE_NAME
                    + ").  The URL is null.");
        }

        return responseElement;
    }

    /**
     *
     * @param portDescriptor
     * @param url
     * @param assertion
     * @return
     */
    protected CONNECTClient<AdapterNotificationProducerSecured> getCONNECTClientSecured(
            ServicePortDescriptor<AdapterNotificationProducerSecured> portDescriptor, String url,
            AssertionType assertion) {

        return CONNECTCXFClientFactory.getInstance().getCONNECTClientSecured(portDescriptor, url, assertion);
    }

    /**
     *
     * @return <code>WebServiceProxyHelper</code> instance
     */
    protected WebServiceProxyHelper getWebServiceProxyHelper() {
        if (oProxyHelper == null) {
            oProxyHelper = new WebServiceProxyHelper();
        }
        return oProxyHelper;
    }

}
