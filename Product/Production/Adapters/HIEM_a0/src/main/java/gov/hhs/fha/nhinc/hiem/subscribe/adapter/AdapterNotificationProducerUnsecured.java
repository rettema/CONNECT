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

import gov.hhs.fha.nhinc.common.nhinccommonadapter.SubscribeRequestType;

import javax.annotation.Resource;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault;
import org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault;

/**
 * HIEM Adapter Subscribe Unsecured Service Interface
 *
 * @author richard.ettema
 */
@BindingType(value = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
@Addressing(enabled = true)
public class AdapterNotificationProducerUnsecured implements
		gov.hhs.fha.nhinc.adaptersubscription.AdapterNotificationProducerUnsecured {

	private static final Logger LOG = Logger.getLogger(AdapterNotificationProducerUnsecured.class);

	@Resource
	private WebServiceContext context;

	/* (non-Javadoc)
	 * @see gov.hhs.fha.nhinc.adaptersubscription.AdapterNotificationProducerUnsecured#subscribe(gov.hhs.fha.nhinc.common.nhinccommonadapter.SubscribeRequestType)
	 */
	public SubscribeResponse subscribe(SubscribeRequestType subscribeRequest) throws UnrecognizedPolicyRequestFault,
			InvalidProducerPropertiesExpressionFault, NotifyMessageNotSupportedFault, TopicNotSupportedFault,
			InvalidTopicExpressionFault, SubscribeCreationFailedFault, UnsupportedPolicyRequestFault,
			UnacceptableInitialTerminationTimeFault, InvalidMessageContentExpressionFault, InvalidFilterFault,
			TopicExpressionDialectUnknownFault {

		LOG.info("Begin AdapterNotificationProducerUnsecured.subscribe");

		SubscribeResponse subscribeResponse = null;

		AdapterNotificationProducerImpl notificationProducerImpl = new AdapterNotificationProducerImpl();

		subscribeResponse = notificationProducerImpl.subscribeUnsecured(subscribeRequest, context);

		LOG.info("End AdapterNotificationProducerUnsecured.subscribe");

		return subscribeResponse;
	}

}
