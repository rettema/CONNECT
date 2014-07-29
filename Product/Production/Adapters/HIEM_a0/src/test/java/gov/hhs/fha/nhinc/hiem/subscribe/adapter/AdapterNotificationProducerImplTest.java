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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.SubscribeRequestType;
import gov.hhs.fha.nhinc.subscribe.adapter.HiemSubscribeAdapterOrchImpl;

import javax.xml.ws.WebServiceContext;

import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;

/**
 * Test HIEM Adapter Subscribe Implementation
 *
 * @author richard.ettema
 */
public class AdapterNotificationProducerImplTest {

    private AssertionType mockAssertion = mock(AssertionType.class);
    private WebServiceContext mockContext = mock(WebServiceContext.class);
    private HiemSubscribeAdapterOrchImpl mockHiemSubscribeAdapterOrchImpl = mock(HiemSubscribeAdapterOrchImpl.class);

	@Test
	public void testSubscribeUnsecured() {
        AdapterNotificationProducerImpl impl = new AdapterNotificationProducerImpl() {

            @Override
            protected HiemSubscribeAdapterOrchImpl getHiemSubscribeAdapterOrchImpl() {
                return mockHiemSubscribeAdapterOrchImpl;
            }
		};

		try {
            SubscribeRequestType subscribeRequestType = new SubscribeRequestType();
            AssertionType assertion = new AssertionType();
            subscribeRequestType.setAssertion(assertion);
            Subscribe subscribe = new Subscribe();
            subscribeRequestType.setSubscribe(subscribe);
            SubscribeResponse subscribeResponse = impl.subscribeUnsecured(subscribeRequestType, mockContext);
		} catch (Exception e) {
            fail("testSubscribeUnsecured test failed with Exception: " + e.getMessage());
		}

	}

    @Test
    public void testSubscribeSecured() {
        AdapterNotificationProducerImpl impl = new AdapterNotificationProducerImpl() {

            @Override
            protected AssertionType getAssertion(WebServiceContext context, AssertionType assertion) {
                return mockAssertion;
            }

            @Override
            protected HiemSubscribeAdapterOrchImpl getHiemSubscribeAdapterOrchImpl() {
                return mockHiemSubscribeAdapterOrchImpl;
            }
        };

        try {
            Subscribe subscribe = new Subscribe();
            SubscribeResponse subscribeResponse = impl.subscribeSecured(subscribe, mockContext);
        } catch (Exception e) {
            fail("testSubscribeSecured test failed with Exception: " + e.getMessage());
        }

    }

}
