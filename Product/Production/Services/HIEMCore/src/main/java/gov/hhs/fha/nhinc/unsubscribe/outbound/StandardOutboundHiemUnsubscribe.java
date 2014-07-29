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
package gov.hhs.fha.nhinc.unsubscribe.outbound;

import gov.hhs.fha.nhinc.aspect.OutboundProcessingEvent;
import gov.hhs.fha.nhinc.auditrepository.AuditRepositoryLogger;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxy;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxyObjectFactory;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.eventcommon.UnsubscribeEventType;
import gov.hhs.fha.nhinc.common.eventcommon.UnsubscribeMessageType;
import gov.hhs.fha.nhinc.common.hiemauditlog.UnsubscribeResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyResponseType;
import gov.hhs.fha.nhinc.common.nhinccommoninternalorch.UnsubscribeRequestType;
import gov.hhs.fha.nhinc.hiem.consumerreference.ReferenceParametersHelper;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.hiem.processor.common.HiemProcessorConstants;
import gov.hhs.fha.nhinc.hiem.processor.faults.SubscriptionManagerSoapFaultFactory;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.policyengine.PolicyEngineChecker;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxy;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxyObjectFactory;
import gov.hhs.fha.nhinc.subscription.repository.data.HiemSubscriptionItem;
import gov.hhs.fha.nhinc.subscription.repository.service.HiemSubscriptionRepositoryService;
import gov.hhs.fha.nhinc.subscription.repository.service.SubscriptionRepositoryException;
import gov.hhs.fha.nhinc.unsubscribe.aspect.UnsubscribeRequestTransformingBuilder;
import gov.hhs.fha.nhinc.unsubscribe.aspect.UnsubscribeResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.unsubscribe.entity.OutboundUnsubscribeDelegate;
import gov.hhs.fha.nhinc.unsubscribe.entity.OutboundUnsubscribeOrchestratable;
import oasis.names.tc.xacml._2_0.context.schema.os.DecisionType;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;

/**
 * HIEM Unsubscribe outbound standard implementation
 *
 * @author richard.ettema
 */
public class StandardOutboundHiemUnsubscribe implements OutboundHiemUnsubscribe {

    private static final Logger LOG = Logger.getLogger(StandardOutboundHiemUnsubscribe.class);

    /**
     * This method performs the entity orchestration for an unsubscribe at the entity.
     *
     * @param unsubscribe - This request
     * @param assertion - The assertion of the message
     * @param targetCommunitites - The target of the request
     * @return a subscription response of success or fail
     * @throws Exception
     * @throws UnableToDestroySubscriptionFault
     */
    @OutboundProcessingEvent(beforeBuilder = UnsubscribeRequestTransformingBuilder.class,
            afterReturningBuilder = UnsubscribeResponseDescriptionBuilder.class, serviceType = "HIEM Unsubscribe",
            version = "2.0")
    public UnsubscribeResponse processUnsubscribe(Unsubscribe unsubscribe, String subscriptionId, AssertionType assertion)
            throws UnableToDestroySubscriptionFault {

        LOG.debug("Begin StandardOutboundHiemUnsubscribe.processUnsubscribe");

        UnsubscribeResponse response = null;

        auditEntityRequest(unsubscribe, assertion);

        // retrieve by consumer reference - subscriptionId
        HiemSubscriptionRepositoryService repo = new HiemSubscriptionRepositoryService();
        HiemSubscriptionItem subscriptionItem = null;

        try {
            LOG.debug("lookup subscription by subscriptionId");

            subscriptionItem = repo.retrieveBySubscriptionId(subscriptionId);

            LOG.debug("subscriptionItem isnull? = " + (subscriptionItem == null));
        } catch (SubscriptionRepositoryException ex) {
            LOG.error(ex);
            throw new SubscriptionManagerSoapFaultFactory().getGenericProcessingExceptionFault(ex);
        }

        if (subscriptionItem == null) {
            throw new SubscriptionManagerSoapFaultFactory().getGenericProcessingExceptionFault("Subscription not found.");
        }

        // Local subscription found; now send nhin unsubscribe request

        try {
	        // Build target reference from remote producer HCID
	        NhinTargetSystemType nhinTargetSystem = new NhinTargetSystemType();
	        HomeCommunityType homeCommunityType = new HomeCommunityType();
	        homeCommunityType.setHomeCommunityId(subscriptionItem.getProducer());
	        nhinTargetSystem.setHomeCommunity(homeCommunityType);

	        ReferenceParametersHelper referenceParametersHelper = new ReferenceParametersHelper();
	        SoapMessageElements referenceParametersElements = referenceParametersHelper
	                .createReferenceParameterElementsFromSubscriptionReference(subscriptionItem.getSubscriptionReferenceXML());
	        LOG.debug("extracted " + referenceParametersElements.getElements().size() + " element(s)");

	        response = getResponseFromTarget(unsubscribe, referenceParametersElements, assertion, nhinTargetSystem,
	                subscriptionItem.getStorageObject().getSubscriptionId());
        } catch (Exception ex) {
            LOG.error("Exception thrown extracting SoapMessageElements.  This should result in a unable to destroy subscription fault", ex);
            throw new SubscriptionManagerSoapFaultFactory().getFailedToRemoveSubscriptionFault(ex);
        }

        // Check response; if valid, then update local subscription
        if (response != null) {
            LOG.debug("invoking subscription storage service to unsubscribe subscription");

            try {
                repo.updateSubscriptionItemStatus(subscriptionItem.getStorageObject().getSubscriptionId(),
                        HiemProcessorConstants.SUBSCRIPTION_ROLE_CONSUMER,
                        HiemProcessorConstants.SUBSCRIPTION_STATUS_UNSUBSCRIBED);
            } catch (SubscriptionRepositoryException ex) {
                LOG.error("SubscriptionRepositoryException thrown deleting subscription.  This should result in a unable to destroy subscription fault", ex);
                throw new SubscriptionManagerSoapFaultFactory().getFailedToRemoveSubscriptionFault(ex);
            } catch (Exception ex) {
                LOG.error("Exception thrown deleting subscription.  This should result in a unable to destroy subscription fault", ex);
                throw new SubscriptionManagerSoapFaultFactory().getFailedToRemoveSubscriptionFault(ex);
            }

        } else {
			throw new SubscriptionManagerSoapFaultFactory().getGenericProcessingExceptionFault("Target community "
					+ subscriptionItem.getProducer() + " returned null unsubscribe response");
        }

        auditEntityResponse(response, assertion);

        LOG.debug("End StandardOutboundHiemUnsubscribe.processUnsubscribe");

        return response;
    }

    /**
     * Audit the entity (initiating) request.
     *
     * @param request The request to be audited
     * @param assertion The assertion to be audited
     */
    private void auditEntityRequest(Unsubscribe unsubscribe, AssertionType assertion) {

        LOG.debug("Begin StandardOutboundHiemUnsubscribe.auditInputMessage");

        try {
            AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

            UnsubscribeRequestType message = new UnsubscribeRequestType();
            message.setAssertion(assertion);
            message.setUnsubscribe(unsubscribe);

            LogEventRequestType auditLogMsg = auditLogger.logNhinUnsubscribeRequest(message,
                    NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ENTITY_INTERFACE);

            if (auditLogMsg != null) {
                AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
                AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
                proxy.auditLog(auditLogMsg, assertion);
            }

        } catch (Throwable t) {
            LOG.error("Error logging subscribe message: " + t.getMessage(), t);
        }
    }

    /**
     * Audit the entity (initiating) response.
     *
     * @param response The response to be audited
     * @param assertion The assertion to be audited
     */
    private void auditEntityResponse(UnsubscribeResponse response, AssertionType assertion) {

        LOG.debug("Begin StandardOutboundHiemUnsubscribe.auditResponseMessage");

        try {
            AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

            UnsubscribeResponseMessageType message = new UnsubscribeResponseMessageType();
            message.setAssertion(assertion);
            message.setUnsubscribeResponse(response);

            LogEventRequestType auditLogMsg = auditLogger.logUnsubscribeResponse(message,
                    NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ENTITY_INTERFACE);

            if (auditLogMsg != null) {
                AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
                AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
                proxy.auditLog(auditLogMsg, assertion);
            }

        } catch (Throwable t) {
            LOG.error("Error logging subscribe response message: " + t.getMessage(), t);
        }
    }

    /**
     * Send subscription response to target.
     *
     * @param request The subscribe to send.
     * @param assertion The assertion to send
     * @param targetCommunitites The targets to be sent to
     * @return the response from the foreign entity
     */
    private UnsubscribeResponse getResponseFromTarget(Unsubscribe request, SoapMessageElements referenceParameters,
            AssertionType assertion, NhinTargetSystemType targetSystem, String subscriptionId) {

        UnsubscribeResponse nhinResponse = null;

        if (isPolicyValid(request, assertion)) {
            LOG.info("Policy check successful");

            try {
                // send request to nhin proxy
                nhinResponse = sendToNhinProxy(request, referenceParameters, assertion, targetSystem, subscriptionId);
            } catch (Exception e) {
                String hcid = targetSystem.getHomeCommunity().getHomeCommunityId();
                LOG.error("Fault encountered while trying to send message to the nhin " + hcid, e);
                nhinResponse = createFailedPolicyCheckResponse();
            }

        } else {
            LOG.error("Failed policy check.  Sending error response.");
            nhinResponse = createFailedPolicyCheckResponse();
        }

        return nhinResponse;
    }

    private UnsubscribeResponse sendToNhinProxy(Unsubscribe request, SoapMessageElements referenceParameters,
            AssertionType assertion, NhinTargetSystemType nhinTargetSystem, String subscriptionId) {

        OutboundUnsubscribeDelegate dsDelegate = new OutboundUnsubscribeDelegate();
        OutboundUnsubscribeOrchestratable dsOrchestratable = createOrchestratable(dsDelegate, request, referenceParameters,
                assertion, nhinTargetSystem, subscriptionId);
        UnsubscribeResponse response = ((OutboundUnsubscribeOrchestratable) dsDelegate.process(dsOrchestratable)).getResponse();

        return response;
    }

    private OutboundUnsubscribeOrchestratable createOrchestratable(OutboundUnsubscribeDelegate delegate, Unsubscribe request,
            SoapMessageElements referenceParameters, AssertionType assertion, NhinTargetSystemType nhinTargetSystem,
            String subscriptionId) {

        OutboundUnsubscribeOrchestratable dsOrchestratable = new OutboundUnsubscribeOrchestratable(delegate);
        dsOrchestratable.setAssertion(assertion);
        dsOrchestratable.setRequest(request);
        dsOrchestratable.setReferenceParameters(referenceParameters);
        dsOrchestratable.setTarget(nhinTargetSystem);
        dsOrchestratable.setSubscriptionId(subscriptionId);

        return dsOrchestratable;
    }

    /**
     * Check if policy for message is valid.
     *
     * @param subscribe The message to be checked.
     * @param assertion The assertion to be checked.
     * @return
     */
    private boolean isPolicyValid(Unsubscribe unsubscribe, AssertionType assertion) {

        LOG.debug("Begin StandardOutboundHiemUnsubscribe.isPolicyValid");

        boolean policyIsValid = false;

        UnsubscribeEventType policyCheckReq = new UnsubscribeEventType();
        policyCheckReq.setDirection(NhincConstants.POLICYENGINE_OUTBOUND_DIRECTION);
        UnsubscribeMessageType request = new UnsubscribeMessageType();
        request.setAssertion(assertion);
        request.setUnsubscribe(unsubscribe);
        policyCheckReq.setMessage(request);

        PolicyEngineChecker policyChecker = new PolicyEngineChecker();
        CheckPolicyRequestType policyReq = policyChecker.checkPolicyUnsubscribe(policyCheckReq);
        policyReq.setAssertion(assertion);
        PolicyEngineProxyObjectFactory policyEngFactory = new PolicyEngineProxyObjectFactory();
        PolicyEngineProxy policyProxy = policyEngFactory.getPolicyEngineProxy();
        CheckPolicyResponseType policyResp = policyProxy.checkPolicy(policyReq, assertion);

        if (policyResp.getResponse() != null && NullChecker.isNotNullish(policyResp.getResponse().getResult())
                && policyResp.getResponse().getResult().get(0).getDecision() == DecisionType.PERMIT) {
            policyIsValid = true;
        }

        LOG.debug("End StandardOutboundHiemUnsubscribe.isPolicyValid - valid: " + policyIsValid);

        return policyIsValid;
    }

    private UnsubscribeResponse createFailedPolicyCheckResponse() {
        // NULL UnsubscribeResponse represents failure
        return null;
    }

}
