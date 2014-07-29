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
package gov.hhs.fha.nhinc.notify.inbound;

import gov.hhs.fha.nhinc.aspect.InboundProcessingEvent;
import gov.hhs.fha.nhinc.auditrepository.AuditRepositoryLogger;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxy;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxyObjectFactory;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.eventcommon.NotifyEventType;
import gov.hhs.fha.nhinc.common.hiemauditlog.EntityNotifyResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyResponseType;
import gov.hhs.fha.nhinc.common.nhinccommoninternalorch.NotifyRequestType;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.notify.adapter.proxy.HiemNotifyAdapterProxy;
import gov.hhs.fha.nhinc.notify.adapter.proxy.HiemNotifyAdapterProxyObjectFactory;
import gov.hhs.fha.nhinc.notify.aspect.NotifyRequestTransformingBuilder;
import gov.hhs.fha.nhinc.notify.aspect.NotifyResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.policyengine.PolicyEngineChecker;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxy;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxyObjectFactory;
import oasis.names.tc.xacml._2_0.context.schema.os.DecisionType;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3c.dom.Element;

/**
 * HIEM Notify inbound standard implementation
 *
 * @author richard.ettema
 */
public class StandardInboundHiemNotify implements InboundHiemNotify {

    private static final Logger LOG = Logger.getLogger(StandardInboundHiemNotify.class);

	/**
	 * Perform processing for an NHIN notify message.
	 *
	 * @param soapMessage NHIN notify SOAP message
	 * @param assertion Assertion information extracted from the SOAP header
	 * @return void
	 * @throws java.lang.Exception
	 */
	@InboundProcessingEvent(beforeBuilder = NotifyRequestTransformingBuilder.class, afterReturningBuilder = NotifyResponseDescriptionBuilder.class, serviceType = "HIEM Notify", version = "2.0")
	public void processNhinNotify(Notify notifyRequest, Element soapMessage, SoapMessageElements referenceParametersElements,
			AssertionType assertion) {

		LOG.debug("Begin StandardInboundHiemNotify.processNhinNotify");

		auditInputMessage(notifyRequest, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
				NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

		AcknowledgementType response = null;

		try {
			if (checkPolicy(notifyRequest, assertion)) {

				LOG.info("initialize HiemNotifyAdapterProxyObjectFactory");
				HiemNotifyAdapterProxyObjectFactory adapterFactory = new HiemNotifyAdapterProxyObjectFactory();

				LOG.info("initialize HIEM notify adapter proxy");
				HiemNotifyAdapterProxy adapterProxy = adapterFactory.getHiemNotifyAdapterProxy();

				auditInputMessage(notifyRequest, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
						NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE);

				LOG.info("begin invoke HIEM notify adapter proxy");
				response = adapterProxy.notify(notifyRequest, referenceParametersElements, assertion);
				LOG.info("end invoke HIEM notify adapter proxy");

				// Audit Adapter Notify Response
				auditResponseMessage(response, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
						NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE);
			} else {
				LOG.error("Failed policy check on notify message");
				response = null;
			}

		} catch (Exception ex) {
			LOG.error("failed to forward notify to adapter", ex);
			response = null;
		}

		// Audit NHIN Notify Response
		auditResponseMessage(response, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
				NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

		LOG.debug("End StandardInboundHiemNotify.processNhinNotify");

	}

	/**
	 * Audit the request from the adapter.
	 *
	 * @param notifyRequest The request to be audited
	 * @param assertion The assertion to be audited
	 * @param direction The direction of the audited message
	 * @param logInterface The interface of the audited message
	 */
	private void auditInputMessage(Notify notifyRequest, AssertionType assertion, String direction, String logInterface) {

		LOG.debug("Begin StandardInboundHiemNotify.auditInputMessage");

		try {
			AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

			NotifyRequestType message = new NotifyRequestType();
			message.setAssertion(assertion);
			message.setNotify(notifyRequest);

			LogEventRequestType auditLogMsg = auditLogger.logNhinNotifyRequest(message, direction, logInterface);

			if (auditLogMsg != null) {
				AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
				AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
				proxy.auditLog(auditLogMsg, assertion);
			}

		} catch (Throwable t) {
			LOG.error("Failed to log notify message: " + t.getMessage(), t);
		}

		LOG.debug("End StandardInboundHiemNotify.auditInputMessage");

	}

	/**
	 * Audit the response from the adapter.
	 *
	 * @param ack The ack to be audited
	 * @param assertion The assertion to be audited
	 * @param direction The direction of the audited message
	 * @param logInterface The interface of the audited message
	 */
	private void auditResponseMessage(AcknowledgementType ack, AssertionType assertion, String direction, String logInterface) {

		LOG.debug("Begin StandardInboundHiemNotify.auditResponseMessage");

		try {
			AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

			EntityNotifyResponseMessageType message = new EntityNotifyResponseMessageType();
			message.setAssertion(assertion);
			message.setAck(ack);

			LogEventRequestType auditLogMsg = auditLogger.logEntityNotifyResponse(message, direction, logInterface);

			if (auditLogMsg != null) {
				AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
				AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
				proxy.auditLog(auditLogMsg, assertion);
			}
		} catch (Throwable t) {
			LOG.error("Error logging notify acknowledgement message: " + t.getMessage(), t);
		}

		LOG.debug("End StandardInboundHiemNotify.auditResponseMessage");
	}

	private boolean checkPolicy(Notify notifyRequest, AssertionType assertion) {

		LOG.debug("Begin StandardInboundHiemNotify.checkPolicy");

		boolean policyIsValid = false;

		NotifyEventType policyCheckReq = new NotifyEventType();
		policyCheckReq.setDirection(NhincConstants.POLICYENGINE_INBOUND_DIRECTION);
		gov.hhs.fha.nhinc.common.eventcommon.NotifyMessageType request = new gov.hhs.fha.nhinc.common.eventcommon.NotifyMessageType();
		request.setAssertion(assertion);
		request.setNotify(notifyRequest);
		policyCheckReq.setMessage(request);

		PolicyEngineChecker policyChecker = new PolicyEngineChecker();
		CheckPolicyRequestType policyReq = policyChecker.checkPolicyNotify(policyCheckReq);
		PolicyEngineProxyObjectFactory policyEngFactory = new PolicyEngineProxyObjectFactory();
		PolicyEngineProxy policyProxy = policyEngFactory.getPolicyEngineProxy();
		CheckPolicyResponseType policyResp = policyProxy.checkPolicy(policyReq, assertion);

		if (policyResp.getResponse() != null && NullChecker.isNotNullish(policyResp.getResponse().getResult())
				&& policyResp.getResponse().getResult().get(0).getDecision() == DecisionType.PERMIT) {
			policyIsValid = true;
		}

		LOG.debug("End StandardInboundHiemNotify.checkPolicy - valid: " + policyIsValid);

		return policyIsValid;
	}

}
