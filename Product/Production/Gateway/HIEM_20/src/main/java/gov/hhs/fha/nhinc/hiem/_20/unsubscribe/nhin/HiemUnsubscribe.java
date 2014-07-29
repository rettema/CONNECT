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
package gov.hhs.fha.nhinc.hiem._20.unsubscribe.nhin;

import gov.hhs.fha.nhinc.aspect.InboundMessageEvent;
import gov.hhs.fha.nhinc.unsubscribe.aspect.UnsubscribeRequestTransformingBuilder;
import gov.hhs.fha.nhinc.unsubscribe.aspect.UnsubscribeResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.unsubscribe.inbound.InboundHiemUnsubscribe;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;

/**
 * HIEM Nhin Unsubscribe Secured Interface
 *
 * @author Neil Webb
 * @author richard.ettema
 */
@WebService(endpointInterface = "org.oasis_open.docs.wsn.bw_2.SubscriptionManager")
@BindingType(value = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
@Addressing(enabled = true)
public class HiemUnsubscribe {

    private InboundHiemUnsubscribe inboundHiemUnsubscribe;

    private WebServiceContext context;

    /**
     *
     * @param unsubscribeRequest
     * @return
     * @throws UnableToDestroySubscriptionFault
     */
    @InboundMessageEvent(beforeBuilder = UnsubscribeRequestTransformingBuilder.class,
            afterReturningBuilder = UnsubscribeResponseDescriptionBuilder.class, serviceType = "HIEM Unsubscribe",
            version = "2.0")
    public UnsubscribeResponse unsubscribe(Unsubscribe unsubscribeRequest) throws UnableToDestroySubscriptionFault {

        HiemUnsubscribeImpl hiemUnsubscribeImpl = new HiemUnsubscribeImpl(inboundHiemUnsubscribe);

        return hiemUnsubscribeImpl.unsubscribe(unsubscribeRequest, context);
    }

    public void setInboundHiemUnsubscribe(InboundHiemUnsubscribe inboundHiemUnsubscribe) {
        this.inboundHiemUnsubscribe = inboundHiemUnsubscribe;
    }

    @Resource
    public void setContext(WebServiceContext context) {
        this.context = context;
    }

}
