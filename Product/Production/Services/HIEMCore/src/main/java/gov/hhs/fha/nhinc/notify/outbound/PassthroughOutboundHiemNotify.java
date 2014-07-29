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
package gov.hhs.fha.nhinc.notify.outbound;

import gov.hhs.fha.nhinc.aspect.OutboundProcessingEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.notify.aspect.NotifyRequestTransformingBuilder;
import gov.hhs.fha.nhinc.notify.aspect.NotifyResponseDescriptionBuilder;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Notify;

/**
 * HIEM Notify outbound passthru implementation
 *
 * @author richard.ettema
 */
public class PassthroughOutboundHiemNotify implements OutboundHiemNotify {

    private static final Logger LOG = Logger.getLogger(PassthroughOutboundHiemNotify.class);

    /**
     * This method is a stub to be used by those who wish to call their own implementation.
     *
     * @param notify - This request
     * @param assertion - The assertion of the message
     * @param rawNotifyXml - The target of the request
     * @throws Exception
     */
    @OutboundProcessingEvent(beforeBuilder = NotifyRequestTransformingBuilder.class,
            afterReturningBuilder = NotifyResponseDescriptionBuilder.class, serviceType = "HIEM Notify",
            version = "2.0")
	@Override
	public void processNotify(Notify notify, AssertionType assertion, String rawNotifyXml) throws Exception {

        LOG.info("Begin PassthroughOutboundHiemNotify.processNotify");

		/*
		 * CALL CUSTOM IMPLEMENTATION HERE
		 */

        LOG.info("End PassthroughOutboundHiemNotify.processNotify");

	}

}
