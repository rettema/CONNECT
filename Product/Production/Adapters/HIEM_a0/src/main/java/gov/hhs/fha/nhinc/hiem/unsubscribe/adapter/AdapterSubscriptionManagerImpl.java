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
package gov.hhs.fha.nhinc.hiem.unsubscribe.adapter;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.UnsubscribeRequestType;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapHeaderHelper;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.WsntUnsubscribeMarshaller;
import gov.hhs.fha.nhinc.messaging.server.BaseService;
import gov.hhs.fha.nhinc.unsubscribe.adapter.HiemUnsubscribeAdapterOrchImpl;

import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.w3c.dom.Element;

/**
 * HIEM Adapter Unsubscribe Implementation
 *
 * @author richard.ettema
 */
public class AdapterSubscriptionManagerImpl extends BaseService {

	private static final Logger LOG = Logger.getLogger(AdapterSubscriptionManagerImpl.class);

	/**
	 *
	 * @param unsubscribeRequest
	 * @param context
	 * @return
	 */
	public UnsubscribeResponse unsubscribeUnsecured(UnsubscribeRequestType unsubscribeRequest, WebServiceContext context)
			throws ResourceUnknownFault, UnableToDestroySubscriptionFault {

		LOG.debug("Enter AdapterSubscriptionManagerImpl.unsubscribeUnsecured()");

		UnsubscribeResponse unsubscribeResponse = null;

		try {
			AssertionType assertion = getAssertion(context, unsubscribeRequest.getAssertion());
			SoapMessageElements referenceParametersElements = new SoapHeaderHelper().getSoapHeaderElements(context);

			WsntUnsubscribeMarshaller unsubscribeMarshaller = new WsntUnsubscribeMarshaller();
			Element unsubscribeElement = unsubscribeMarshaller.marshal(unsubscribeRequest.getUnsubscribe());

			// Call HiemUnsubscribeAdapterOrchImpl class
			HiemUnsubscribeAdapterOrchImpl adapterOrchImpl = new HiemUnsubscribeAdapterOrchImpl();
			unsubscribeResponse = adapterOrchImpl
					.adapterUnsubscribe(unsubscribeElement, assertion, referenceParametersElements);

		} catch (Exception e) {
			LOG.error("Failed to process unsubscribe request", e);
			throw new UnableToDestroySubscriptionFault(e.getMessage(), e);
		}

		LOG.debug("End AdapterSubscriptionManagerImpl.unsubscribeUnsecured()");

		return unsubscribeResponse;
	}

	/**
	 *
	 * @param unsubscribe
	 * @param context
	 * @return
	 */
	public UnsubscribeResponse unsubscribeSecured(Unsubscribe unsubscribe, WebServiceContext context)
			throws ResourceUnknownFault, UnableToDestroySubscriptionFault {

		LOG.debug("Enter AdapterSubscriptionManagerImpl.unsubscribeSecured()");

		UnsubscribeResponse unsubscribeResponse = null;

		try {
			AssertionType assertion = getAssertion(context, null);
			SoapMessageElements referenceParametersElements = new SoapHeaderHelper().getSoapHeaderElements(context);

			WsntUnsubscribeMarshaller unsubscribeMarshaller = new WsntUnsubscribeMarshaller();
			Element unsubscribeElement = unsubscribeMarshaller.marshal(unsubscribe);

			// Call HiemUnsubscribeAdapterOrchImpl class
			HiemUnsubscribeAdapterOrchImpl adapterOrchImpl = new HiemUnsubscribeAdapterOrchImpl();
			unsubscribeResponse = adapterOrchImpl
					.adapterUnsubscribe(unsubscribeElement, assertion, referenceParametersElements);

		} catch (Exception e) {
			LOG.error("Failed to process unsubscribe request", e);
			throw new UnableToDestroySubscriptionFault(e.getMessage(), e);
		}

		LOG.debug("End AdapterSubscriptionManagerImpl.unsubscribeSecured()");

		return unsubscribeResponse;
	}

}
