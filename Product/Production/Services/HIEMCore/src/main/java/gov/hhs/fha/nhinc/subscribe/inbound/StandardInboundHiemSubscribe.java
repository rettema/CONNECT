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
package gov.hhs.fha.nhinc.subscribe.inbound;

import gov.hhs.fha.nhinc.aspect.InboundProcessingEvent;
import gov.hhs.fha.nhinc.auditrepository.AuditRepositoryLogger;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxy;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxyObjectFactory;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.eventcommon.SubscribeEventType;
import gov.hhs.fha.nhinc.common.eventcommon.SubscribeMessageType;
import gov.hhs.fha.nhinc.common.hiemauditlog.SubscribeResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyResponseType;
import gov.hhs.fha.nhinc.common.nhinccommoninternalorch.SubscribeRequestType;
import gov.hhs.fha.nhinc.hiem.dte.Namespaces;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.WsntSubscribeResponseMarshaller;
import gov.hhs.fha.nhinc.hiem.processor.faults.SoapFaultFactory;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.policyengine.PolicyEngineChecker;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxy;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxyObjectFactory;
import gov.hhs.fha.nhinc.subscribe.adapter.proxy.HiemSubscribeAdapterProxy;
import gov.hhs.fha.nhinc.subscribe.adapter.proxy.HiemSubscribeAdapterProxyObjectFactory;
import gov.hhs.fha.nhinc.subscribe.aspect.SubscribeRequestTransformingBuilder;
import gov.hhs.fha.nhinc.subscribe.aspect.SubscribeResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;

import oasis.names.tc.xacml._2_0.context.schema.os.DecisionType;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeCreationFailedFaultType;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.w3c.dom.Element;

/**
 * HIEM Subscribe inbound standard implementation
 *
 * @author richard.ettema
 */
public class StandardInboundHiemSubscribe implements InboundHiemSubscribe {

	private static final Logger LOG = Logger.getLogger(StandardInboundHiemSubscribe.class);

    /**
     * Perform processing for an NHIN subscribe message.
     *
     * @param subscribe NHIN subscribe message
     * @param assertion Assertion information extracted from the SOAP header
     * @return SubscribeResponse response message
     * @throws java.lang.Exception
     */
    @InboundProcessingEvent(beforeBuilder = SubscribeRequestTransformingBuilder.class,
            afterReturningBuilder = SubscribeResponseDescriptionBuilder.class, serviceType = "HIEM Subscribe",
            version = "2.0")
    public SubscribeResponse processNhinSubscribe(Subscribe subscribeRequest, Element soapMessage, AssertionType assertion)
            throws NotifyMessageNotSupportedFault,
            SubscribeCreationFailedFault,
            TopicNotSupportedFault,
            InvalidTopicExpressionFault {

        LOG.debug("Begin StandardInboundHiemSubscribe.processNhinSubscribe");

        // Audit the input message
        auditInputMessage(subscribeRequest, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
                NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

        SubscribeResponse subscribeResponse = null;

        try {
            if (checkPolicy(subscribeRequest, assertion)) {
		        LOG.debug("extract subscribe from soapmessage");
		        Element subscribe = XmlUtility.getSingleChildElement(soapMessage, Namespaces.WSNT, "Subscribe");
		        Element responseElement = null;

		        LOG.info("initialize HiemSubscribeAdapterProxyObjectFactory");
		        HiemSubscribeAdapterProxyObjectFactory adapterFactory = new HiemSubscribeAdapterProxyObjectFactory();
		        LOG.info("initialize HIEM subscribe adapter proxy");
		        HiemSubscribeAdapterProxy adapterProxy = adapterFactory.getHiemSubscribeAdapterProxy();

	            // Audit the input message
	            auditInputMessage(subscribeRequest, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
	                    NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE);

	            LOG.info("begin invoke HIEM subscribe adapter proxy");
	            responseElement = adapterProxy.subscribe(subscribe, assertion);
	            LOG.info("end invoke HIEM subscribe adapter proxy");

	            LOG.info("initialize WsntSubscribeResponseMarshaller and unmarshal the response Element");
	            WsntSubscribeResponseMarshaller subscribeResponseMarshaller = new WsntSubscribeResponseMarshaller();
	            subscribeResponse = subscribeResponseMarshaller.unmarshal(responseElement);

	            // Audit the response message
	            auditResponseMessage(subscribeResponse, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
	                    NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE);
            } else {
                SubscribeCreationFailedFaultType faultInfo = null;
                throw new SubscribeCreationFailedFault("Policy check failed", faultInfo);
            }

        } catch (SubscribeCreationFailedFault ex) {
        	throw ex;

        } catch (Exception ex) {
            LOG.error("failed to forward subscribe to adapter", ex);
            throw new SoapFaultFactory().getFailedToForwardSubscribeToAgencyFault(ex);
        }

        // Audit the response message
        auditResponseMessage(subscribeResponse, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
                NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

        LOG.debug("End StandardInboundHiemSubscribe.processNhinSubscribe");

        return subscribeResponse;
    }

    private void auditInputMessage(Subscribe subscribe, AssertionType assertion, String direction, String logInterface) {

        LOG.debug("Begin HiemSubscriptionImpl.auditInputMessage");

        try {
            AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

            SubscribeRequestType message = new SubscribeRequestType();
            message.setAssertion(assertion);
            message.setSubscribe(subscribe);

            LogEventRequestType auditLogMsg = auditLogger.logNhinSubscribeRequest(message, direction, logInterface);

            if (auditLogMsg != null) {
                AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
                AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
                proxy.auditLog(auditLogMsg, assertion);
            }
        } catch (Throwable t) {
            LOG.error("Error logging subscribe message: " + t.getMessage(), t);
        }

        LOG.debug("End HiemSubscriptionImpl.auditInputMessage");

    }

    private void auditResponseMessage(SubscribeResponse response, AssertionType assertion, String direction, String logInterface) {

        LOG.debug("Begin HiemSubscriptionImpl.auditResponseMessage");

        try {
            AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

            SubscribeResponseMessageType message = new SubscribeResponseMessageType();
            message.setAssertion(assertion);
            message.setSubscribeResponse(response);

            LogEventRequestType auditLogMsg = auditLogger.logSubscribeResponse(message, direction, logInterface);

            if (auditLogMsg != null) {
                AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
                AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
                proxy.auditLog(auditLogMsg, assertion);
            }
        } catch (Throwable t) {
            LOG.error("Error loging subscription response: " + t.getMessage(), t);
        }

        LOG.debug("End HiemSubscriptionImpl.auditResponseMessage");

    }

    private boolean checkPolicy(Subscribe subscribe, AssertionType assertion) {

        LOG.debug("In HiemSubscriptionImpl.checkPolicy");

        boolean policyIsValid = false;

        SubscribeEventType policyCheckReq = new SubscribeEventType();
        policyCheckReq.setDirection(NhincConstants.POLICYENGINE_INBOUND_DIRECTION);
        SubscribeMessageType request = new SubscribeMessageType();
        request.setAssertion(assertion);
        request.setSubscribe(subscribe);
        policyCheckReq.setMessage(request);

        PolicyEngineChecker policyChecker = new PolicyEngineChecker();
        CheckPolicyRequestType policyReq = policyChecker.checkPolicySubscribe(policyCheckReq);
        PolicyEngineProxyObjectFactory policyEngFactory = new PolicyEngineProxyObjectFactory();
        PolicyEngineProxy policyProxy = policyEngFactory.getPolicyEngineProxy();
        CheckPolicyResponseType policyResp = policyProxy.checkPolicy(policyReq, assertion);

        if (policyResp.getResponse() != null && NullChecker.isNotNullish(policyResp.getResponse().getResult())
                && policyResp.getResponse().getResult().get(0).getDecision() == DecisionType.PERMIT) {
            policyIsValid = true;
        }

        LOG.debug("Finished HiemSubscriptionImpl.checkPolicy - valid: " + policyIsValid);

        return policyIsValid;
    }

}
