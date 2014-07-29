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
package gov.hhs.fha.nhinc.hiem.orchestration;

import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.connectmgr.NhinEndpointManager;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.notify.entity.OutboundNotifyFactory;
import gov.hhs.fha.nhinc.orchestration.AbstractOrchestrationContextFactory;
import gov.hhs.fha.nhinc.orchestration.OrchestrationContextBuilder;
import gov.hhs.fha.nhinc.subscribe.entity.OutboundSubscribeFactory;
import gov.hhs.fha.nhinc.unsubscribe.entity.OutboundUnsubscribeFactory;

/**
 * @author akong
 * @author richard.ettema
 */
public class OrchestrationContextFactory extends AbstractOrchestrationContextFactory {

    private static OrchestrationContextFactory INSTANCE = new OrchestrationContextFactory();

    private OrchestrationContextFactory() {
    }

    public static OrchestrationContextFactory getInstance() {
        return INSTANCE;
    }

    /* (non-Javadoc)
     * @see gov.hhs.fha.nhinc.orchestration.AbstractOrchestrationContextFactory#getBuilder(gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType, gov.hhs.fha.nhinc.nhinclib.NhincConstants.NHIN_SERVICE_NAMES)
     */
    @Override
    public OrchestrationContextBuilder getBuilder(HomeCommunityType homeCommunityType,
            NhincConstants.NHIN_SERVICE_NAMES serviceName) {

        NhinEndpointManager nem = new NhinEndpointManager();
        NhincConstants.GATEWAY_API_LEVEL apiLevel = nem.getApiVersion(homeCommunityType.getHomeCommunityId(), serviceName);

        return getBuilder(apiLevel, serviceName);
    }

    /**
     * Return the corresponding OrchestrationContextBuilder implementation class based on the Gateway API Level and HIEM service
     * name
     *
     * @param apiLevel
     * @param serviceName
     * @return
     */
    private OrchestrationContextBuilder getBuilder(NhincConstants.GATEWAY_API_LEVEL apiLevel,
            NhincConstants.NHIN_SERVICE_NAMES serviceName) {

        switch (serviceName) {
        case HIEM_SUBSCRIBE:
            return OutboundSubscribeFactory.getInstance().createOrchestrationContextBuilder(apiLevel);
        case HIEM_NOTIFY:
            return OutboundNotifyFactory.getInstance().createOrchestrationContextBuilder(apiLevel);
        case HIEM_UNSUBSCRIBE:
            return OutboundUnsubscribeFactory.getInstance().createOrchestrationContextBuilder(apiLevel);
        default:
            break;
        }

        return null;
    }

}
