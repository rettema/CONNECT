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
package gov.hhs.fha.nhinc.hiem._20.subscribe.entity;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.SubscribeRequestType;
import gov.hhs.fha.nhinc.messaging.server.BaseService;
import gov.hhs.fha.nhinc.subscribe.outbound.OutboundHiemSubscribe;

import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;

/**
 * HIEM Entity Subscribe Service Implementation
 *
 * @author Neil Webb
 * @author richard.ettema
 */
public class EntitySubscribeServiceImpl extends BaseService {

    private static final Logger LOG = Logger.getLogger(EntitySubscribeServiceImpl.class);

    private OutboundHiemSubscribe outboundHiemSubscribe;

    public EntitySubscribeServiceImpl(OutboundHiemSubscribe outboundHiemSubscribe) {
        this.outboundHiemSubscribe = outboundHiemSubscribe;
    }

    /**
     * @param subscribeRequest
     * @param context
     * @return
     * @throws TopicNotSupportedFault
     * @throws InvalidTopicExpressionFault
     * @throws SubscribeCreationFailedFault
     */
    public SubscribeResponse subscribe(SubscribeRequestType subscribeRequest, WebServiceContext context)
            throws TopicNotSupportedFault, InvalidTopicExpressionFault, SubscribeCreationFailedFault {

        LOG.debug("Begin EntitySubscribeServiceImpl.subscribe");

        AssertionType assertion = subscribeRequest.getAssertion();

        Subscribe subscribe = subscribeRequest.getSubscribe();

        NhinTargetCommunitiesType targetCommunitites = subscribeRequest.getNhinTargetCommunities();

        SubscribeResponse response = outboundHiemSubscribe.processSubscribe(subscribe, assertion, targetCommunitites);

        LOG.debug("End EntitySubscribeServiceImpl.subscribe");

        return response;
    }

}
