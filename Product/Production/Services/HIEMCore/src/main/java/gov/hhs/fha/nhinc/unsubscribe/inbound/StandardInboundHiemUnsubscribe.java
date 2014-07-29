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
package gov.hhs.fha.nhinc.unsubscribe.inbound;

import gov.hhs.fha.nhinc.aspect.InboundProcessingEvent;
import gov.hhs.fha.nhinc.auditrepository.AuditRepositoryLogger;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxy;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxyObjectFactory;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.hiemauditlog.UnsubscribeResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommoninternalorch.UnsubscribeRequestType;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.hiem.dte.Namespaces;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.WsntUnsubscribeResponseMarshaller;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.unsubscribe.adapter.proxy.HiemUnsubscribeAdapterProxy;
import gov.hhs.fha.nhinc.unsubscribe.adapter.proxy.HiemUnsubscribeAdapterProxyObjectFactory;
import gov.hhs.fha.nhinc.unsubscribe.aspect.UnsubscribeRequestTransformingBuilder;
import gov.hhs.fha.nhinc.unsubscribe.aspect.UnsubscribeResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.w3c.dom.Element;

/**
 * HIEM Unsubscribe inbound standard implementation
 *
 * @author richard.ettema
 */
public class StandardInboundHiemUnsubscribe implements InboundHiemUnsubscribe {

    private static final Logger LOG = Logger.getLogger(StandardInboundHiemUnsubscribe.class);

    /**
     * Perform processing for an NHIN unsubscribe message.
     *
     * @param soapMessage
     * @param assertion
     * @param referenceParametersElements
     * @return
     * @throws UnableToDestroySubscriptionFault
     */
    @InboundProcessingEvent(beforeBuilder = UnsubscribeRequestTransformingBuilder.class,
            afterReturningBuilder = UnsubscribeResponseDescriptionBuilder.class, serviceType = "HIEM Unsubscribe",
            version = "2.0")
    public UnsubscribeResponse processNhinUnsubscribe(Unsubscribe unsubscribeRequest, Element soapMessage, SoapMessageElements referenceParametersElements, AssertionType assertion)
            throws UnableToDestroySubscriptionFault {

        LOG.debug("Begin StandardInboundHiemUnsubscribe.processNhinUnsubscribe");

		// Audit the input message
		auditInputMessage(unsubscribeRequest, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
				NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

        UnsubscribeResponse unsubscribeResponse = null;
        Element responseElement = null;

        try {
	        LOG.debug("extract unsubscribe from soapmessage");
	        Element unsubscribe = XmlUtility.getSingleChildElement(soapMessage, Namespaces.WSNT, "Unsubscribe");

	        LOG.info("initialize HiemUnsubscribeAdapterProxyObjectFactory");
	        HiemUnsubscribeAdapterProxyObjectFactory adapterFactory = new HiemUnsubscribeAdapterProxyObjectFactory();
	        LOG.info("initialize HIEM unsubscribe adapter proxy");
	        HiemUnsubscribeAdapterProxy adapterProxy = adapterFactory.getHiemUnsubscribeAdapterProxy();

			// Audit the input message
			auditInputMessage(unsubscribeRequest, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
					NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE);

            LOG.info("begin invoke HIEM unsubscribe adapter proxy");
            responseElement = adapterProxy.unsubscribe(unsubscribe, assertion, referenceParametersElements);
            LOG.info("end invoke HIEM unsubscribe adapter proxy");

            LOG.info("initialize WsntUnsubscribeResponseMarshaller and unmarshal the response Element");
            WsntUnsubscribeResponseMarshaller unsubscribeResponseMarshaller = new WsntUnsubscribeResponseMarshaller();
            unsubscribeResponse = unsubscribeResponseMarshaller.unmarshal(responseElement);

			// Audit the response message
			auditResponseMessage(unsubscribeResponse, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
					NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE);

        } catch (Exception ex) {
            LOG.error("failed to forward unsubscribe to adapter", ex);
            throw new UnableToDestroySubscriptionFault(ex.getMessage());
        }

		// Audit the response message
		auditResponseMessage(unsubscribeResponse, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
				NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

        LOG.debug("End StandardInboundHiemUnsubscribe.processNhinSubscribe");

        return unsubscribeResponse;
    }

	private void auditInputMessage(Unsubscribe unsubscribe, AssertionType assertion, String direction, String logInterface) {

		LOG.debug("In HiemUnsubscribeImpl.auditInputMessage");

		try {
			AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

			UnsubscribeRequestType message = new UnsubscribeRequestType();
			message.setAssertion(assertion);
			message.setUnsubscribe(unsubscribe);

			LogEventRequestType auditLogMsg = auditLogger.logNhinUnsubscribeRequest(message, direction, logInterface);

			if (auditLogMsg != null) {
				AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
				AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
				proxy.auditLog(auditLogMsg, assertion);
			}

		} catch (Throwable t) {
			LOG.error("Error logging unsubscribe message: " + t.getMessage(), t);
		}
	}

	private void auditResponseMessage(UnsubscribeResponse response, AssertionType assertion, String direction,
			String logInterface) {

		LOG.debug("In HiemUnsubscribeImpl.auditResponseMessage");

		try {
			AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

			UnsubscribeResponseMessageType message = new UnsubscribeResponseMessageType();
			message.setAssertion(assertion);
			message.setUnsubscribeResponse(response);

			LogEventRequestType auditLogMsg = auditLogger.logUnsubscribeResponse(message, direction, logInterface);

			if (auditLogMsg != null) {
				AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
				AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
				proxy.auditLog(auditLogMsg, assertion);
			}

		} catch (Throwable t) {
			LOG.error("Error loging subscription response: " + t.getMessage(), t);
		}

	}

}
