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
package gov.hhs.fha.nhinc.hiem.subscribe.adapter;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.SubscribeRequestType;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.WsntSubscribeMarshaller;
import gov.hhs.fha.nhinc.messaging.server.BaseService;
import gov.hhs.fha.nhinc.subscribe.adapter.HiemSubscribeAdapterOrchImpl;

import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.w3c.dom.Element;

/**
 * HIEM Adapter Subscribe Implementation
 *
 * @author richard.ettema
 */
public class AdapterNotificationProducerImpl extends BaseService {

	private static final Logger LOG = Logger.getLogger(AdapterNotificationProducerImpl.class);

	/**
	 *
	 * @param subscribeRequest
	 * @param context
	 * @return
	 * @throws SubscribeCreationFailedFault
	 * @throws InvalidTopicExpressionFault
	 * @throws TopicNotSupportedFault
	 */
	public SubscribeResponse subscribeUnsecured(SubscribeRequestType subscribeRequest, WebServiceContext context)
			throws TopicNotSupportedFault, InvalidTopicExpressionFault, SubscribeCreationFailedFault {

		LOG.debug("Enter AdapterNotificationProducerImpl.subscribeUnsecured()");

		AssertionType assertion = getAssertion(context, subscribeRequest.getAssertion());

		WsntSubscribeMarshaller subscribeMarshaller = new WsntSubscribeMarshaller();
		Element subscribeElement = subscribeMarshaller.marshalSubscribeRequest(subscribeRequest.getSubscribe());

		// Call HiemSubscribeAdapterOrchImpl class
		HiemSubscribeAdapterOrchImpl adapterOrchImpl = getHiemSubscribeAdapterOrchImpl();

		SubscribeResponse subscribeResponse = adapterOrchImpl.adapterSubscribe(subscribeElement, assertion);

		LOG.debug("End AdapterNotificationProducerImpl.subscribeUnsecured()");

		return subscribeResponse;
	}

	/**
	 *
	 * @param subscribe
	 * @param context
	 * @return
	 * @throws SubscribeCreationFailedFault
	 * @throws InvalidTopicExpressionFault
	 * @throws TopicNotSupportedFault
	 */
	public SubscribeResponse subscribeSecured(Subscribe subscribe, WebServiceContext context) throws TopicNotSupportedFault,
			InvalidTopicExpressionFault, SubscribeCreationFailedFault {

		LOG.debug("Enter AdapterNotificationProducerImpl.subscribeSecured()");

		AssertionType assertion = getAssertion(context, null);

		WsntSubscribeMarshaller subscribeMarshaller = new WsntSubscribeMarshaller();
		Element subscribeElement = subscribeMarshaller.marshalSubscribeRequest(subscribe);

		// Call HiemSubscribeAdapterOrchImpl class
		HiemSubscribeAdapterOrchImpl adapterOrchImpl = getHiemSubscribeAdapterOrchImpl();

		SubscribeResponse subscribeResponse = adapterOrchImpl.adapterSubscribe(subscribeElement, assertion);

		LOG.debug("End AdapterNotificationProducerImpl.subscribeSecured()");

		return subscribeResponse;
	}

	/**
	 *
	 * @return
	 */
	protected HiemSubscribeAdapterOrchImpl getHiemSubscribeAdapterOrchImpl() {
		return new HiemSubscribeAdapterOrchImpl();
	}

}
