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
package gov.hhs.fha.nhinc.hiem._20.unsubscribe;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import gov.hhs.fha.nhinc.common.nhinccommonentity.UnsubscribeRequestType;
import gov.hhs.fha.nhinc.hiem._20.unsubscribe.entity.EntityUnsubscribeSecuredService;
import gov.hhs.fha.nhinc.hiem._20.unsubscribe.entity.EntityUnsubscribeService;
import gov.hhs.fha.nhinc.hiem._20.unsubscribe.nhin.HiemUnsubscribe;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author richard.ettema
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/hiem/_20/applicationContext.xml" })
public class HiemUnsubscribeSpringContextTest {

    @Autowired
	HiemUnsubscribe inboundHiemUnsubscribe;

    @Autowired
    EntityUnsubscribeService outboundUnsubscribeService;

    @Autowired
    EntityUnsubscribeSecuredService outboundUnsubscribeSecuredService;

    @Test
    public void inbound() {
    	assertNotNull(inboundHiemUnsubscribe);

    	try {
    		Unsubscribe unsubscribeRequest = new Unsubscribe();
	    	UnsubscribeResponse response = inboundHiemUnsubscribe.unsubscribe(unsubscribeRequest);

	    	assertNotNull(response);
    	} catch (Exception e) {
    		fail("inboundHiemUnsubscribe test failed with Exception: " + e.getMessage());
    	}
    }

    @Test
    public void outboundUnsecured() {
    	assertNotNull(outboundUnsubscribeService);

    	try {

    		UnsubscribeRequestType unsubscribeRequest = new UnsubscribeRequestType();
	    	UnsubscribeResponse response = outboundUnsubscribeService.unsubscribe(unsubscribeRequest);

	    	assertNotNull(response);
    	} catch (Exception e) {
    		fail("outboundUnsubscribeService test failed with Exception: " + e.getMessage());
    	}
    }

    @Test
    public void outboundSecured() {
    	assertNotNull(outboundUnsubscribeSecuredService);

    	try {

    		UnsubscribeRequestType unsubscribeRequest = new UnsubscribeRequestType();
	    	UnsubscribeResponse response = outboundUnsubscribeSecuredService.unsubscribe(unsubscribeRequest);

	    	assertNotNull(response);
    	} catch (Exception e) {
    		fail("outboundUnsubscribeSecuredService test failed with Exception: " + e.getMessage());
    	}
    }

}
