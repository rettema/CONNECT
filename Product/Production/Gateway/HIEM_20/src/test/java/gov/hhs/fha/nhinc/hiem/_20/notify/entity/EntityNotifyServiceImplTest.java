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
package gov.hhs.fha.nhinc.hiem._20.notify.entity;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.NotifyRequestType;
import gov.hhs.fha.nhinc.notify.outbound.OutboundHiemNotify;

import javax.xml.ws.WebServiceContext;

import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.Notify;

/**
 *
 * @author richard.ettema
 */
public class EntityNotifyServiceImplTest {

	private OutboundHiemNotify mockNotify = mock(OutboundHiemNotify.class);
	private WebServiceContext mockContext = mock(WebServiceContext.class);
	private AssertionType mockAssertionType = mock(AssertionType.class);

	@Test
	public void testNotifySecured() {
		EntityNotifyServiceImpl impl = new EntityNotifyServiceImpl(mockNotify) {
			@Override
			protected AssertionType getAssertion(WebServiceContext context, AssertionType oAssertionIn) {
				return mockAssertionType;
			}
		};

		Notify notifyRequest = new Notify();

		NotifyRequestType request = getNotifyRequestType(notifyRequest, mockAssertionType);
		impl.notify(request, mockContext);

		try {
			verify(mockNotify).processNotify(any(Notify.class), any(AssertionType.class), any(String.class));
		} catch (Exception e) {
			fail("testNotifySecured test failed with Exception: " + e.getMessage());
		}

	}

	@Test
	public void testNotifyUnsecured() {
		EntityNotifyServiceImpl impl = new EntityNotifyServiceImpl(mockNotify) {
			@Override
			protected AssertionType getAssertion(WebServiceContext context, AssertionType oAssertionIn) {
				return mockAssertionType;
			}
		};

		Notify notifyRequest = new Notify();

		NotifyRequestType request = getNotifyRequestType(notifyRequest, mockAssertionType);
		impl.notify(request, mockContext);

		try {
			verify(mockNotify).processNotify(any(Notify.class), any(AssertionType.class), any(String.class));
		} catch (Exception e) {
			fail("testNotifySecured test failed with Exception: " + e.getMessage());
		}

	}

	/**
	 * @param target
	 * @param adqRequest
	 * @return
	 */
	private NotifyRequestType getNotifyRequestType(Notify notifyRequest, AssertionType assertion) {
		NotifyRequestType request = new NotifyRequestType();
		request.setNotify(notifyRequest);
		request.setAssertion(assertion);

		return request;
	}

}
