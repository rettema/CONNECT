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
package gov.hhs.fha.nhinc.hiem._20.notify;

import gov.hhs.fha.nhinc.common.nhinccommonentity.NotifyRequestType;
import gov.hhs.fha.nhinc.hiem._20.notify.entity.EntityNotifySecured;
import gov.hhs.fha.nhinc.hiem._20.notify.entity.EntityNotifyService;
import gov.hhs.fha.nhinc.hiem._20.notify.nhin.HiemNotify;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author richard.ettema
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/hiem/_20/applicationContext.xml" })
public class HiemNotifySpringContextTest {

    @Autowired
	HiemNotify inboundHiemNotify;

    @Autowired
    EntityNotifyService outboundNotifyService;

    @Autowired
    EntityNotifySecured outboundNotifySecured;

    @Test
    public void inbound() {
    	assertNotNull(inboundHiemNotify);

    	try {
	    	Notify notify = new Notify();
	    	inboundHiemNotify.notify(notify);
    	} catch (Exception e) {
    		fail("inbound test failed with Exception: " + e.getMessage());
    	}
    }

    @Test
    public void outboundUnsecured() {
    	assertNotNull(outboundNotifyService);

    	try {
    		NotifyRequestType notifyRequest = new NotifyRequestType();
    		outboundNotifyService.notify(notifyRequest);
    	} catch (Exception e) {
    		fail("outboundUnsecured test failed with Exception: " + e.getMessage());
    	}
    }

    @Test
    public void outboundSecured() {
    	assertNotNull(outboundNotifySecured);

    	try {
    		NotifyRequestType notifyRequest = new NotifyRequestType();
	    	outboundNotifySecured.notify(notifyRequest);
    	} catch (Exception e) {
    		fail("outboundSecured test failed with Exception: " + e.getMessage());
    	}
    }

}
