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

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import gov.hhs.fha.nhinc.aspect.OutboundProcessingEvent;
import gov.hhs.fha.nhinc.auditrepository.AuditRepositoryLogger;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxy;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxyObjectFactory;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.eventcommon.NotifyEventType;
import gov.hhs.fha.nhinc.common.hiemauditlog.EntityNotifyResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyResponseType;
import gov.hhs.fha.nhinc.common.nhinccommoninternalorch.NotifyRequestType;
import gov.hhs.fha.nhinc.hiem.consumerreference.ReferenceParametersHelper;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.hiem.dte.NotifyBuilder;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.NotificationMessageMarshaller;
import gov.hhs.fha.nhinc.hiem.processor.common.HiemProcessorConstants;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.notify.aspect.NotifyRequestTransformingBuilder;
import gov.hhs.fha.nhinc.notify.aspect.NotifyResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.notify.entity.OutboundNotifyDelegate;
import gov.hhs.fha.nhinc.notify.entity.OutboundNotifyOrchestratable;
import gov.hhs.fha.nhinc.orchestration.OutboundOrchestratable;
import gov.hhs.fha.nhinc.policyengine.PolicyEngineChecker;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxy;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxyObjectFactory;
import gov.hhs.fha.nhinc.subscription.repository.data.HiemSubscriptionItem;
import gov.hhs.fha.nhinc.subscription.repository.service.HiemSubscriptionRepositoryService;
import gov.hhs.fha.nhinc.subscription.repository.service.NotificationStorageItemService;
import gov.hhs.fha.nhinc.subscription.repository.service.SubscriptionRepositoryException;
import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;
import gov.hhs.fha.nhinc.xmlCommon.XpathHelper;

import oasis.names.tc.xacml._2_0.context.schema.os.DecisionType;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * HIEM Notify outbound standard implementation
 *
 * @author richard.ettema
 */
public class StandardOutboundHiemNotify implements OutboundHiemNotify {

    private static final Logger LOG = Logger.getLogger(StandardOutboundHiemNotify.class);

    /**
     * This method performs the entity orchestration for an notify at the entity.
     *
     * @param notify - This request
     * @param assertion - The assertion of the message
     * @param rawNotifyXml - The target of the request
     * @throws Exception
     */
    @OutboundProcessingEvent(beforeBuilder = NotifyRequestTransformingBuilder.class,
            afterReturningBuilder = NotifyResponseDescriptionBuilder.class, serviceType = "HIEM Notify",
            version = "2.0")
    public void processNotify(Notify notify, AssertionType assertion, String rawNotifyXml) throws Exception {

        LOG.info("Begin StandardOutboundHiemNotify.processNotify");

        LOG.debug("Received Notify: " + rawNotifyXml);

        auditEntityRequest(notify, assertion);

        NodeList notificationMessageNodes = getNotificationMessageNodes(rawNotifyXml);
        if (notificationMessageNodes != null) {
        	LOG.debug("Processing [" + notificationMessageNodes.getLength() + "] Notify message(s)");

        	for (int i = 0; i < notificationMessageNodes.getLength(); i++) {
                try {
                    Node notificationMessageNode = notificationMessageNodes.item(i);
                    processSingleNotify(notificationMessageNode, assertion);
                } catch (XPathExpressionException ex) {
                    LOG.error("failed to process notify", ex);
                }
            }
        }

        // No explicit notify response message to audit; use generic acknowledgment
        AcknowledgementType ack = new AcknowledgementType();
        ack.setMessage("Notification Message Sent");
        auditEntityResponse(ack, assertion);

        LOG.info("End StandardOutboundHiemNotify.processNotify");
    }

    private void processSingleNotify(Node notificationMessageNode, AssertionType assertion) throws XPathExpressionException {

        if (notificationMessageNode != null) {

            String nodeName = notificationMessageNode.getLocalName();

            LOG.debug("Node name: " + nodeName);

            if (notificationMessageNode instanceof Element) {

                Element notificationMessageElement = (Element) notificationMessageNode;
                HiemSubscriptionRepositoryService serviceDAO = new HiemSubscriptionRepositoryService();

                try {
                    // Get list of subscriptions for this notification
                    List<HiemSubscriptionItem> subscriptions = serviceDAO.RetrieveByNotificationMessage(
                            notificationMessageElement, HiemProcessorConstants.SUBSCRIPTION_ROLE_PRODUCER);

                    if (subscriptions != null) {
                        LOG.debug("found " + subscriptions.size() + " matching subscriptions");

                        for (HiemSubscriptionItem subscription : subscriptions) {
                            String subscriptionRef = subscription.getSubscriptionReferenceXML();

                            LOG.debug("processing subscription.  SubscriptionReference=[" + subscriptionRef + "]");
                            String endpoint = findNotifyEndpoint(subscription);
                            LOG.debug("endpoint=" + endpoint);

                            LOG.debug("extracting reference parameters from consumer reference");
                            ReferenceParametersHelper referenceParametersHelper = new ReferenceParametersHelper();
                            SoapMessageElements referenceParametersElements = referenceParametersHelper
                                    .createReferenceParameterElementsFromConsumerReference(subscription.getSubscribeXML());
                            LOG.debug("extracted reference parameters from consumer reference");

                            NhinTargetSystemType targetSystem = new NhinTargetSystemType();
                            LOG.debug("building targetSystem.  Endpoint URL=[" + endpoint + "]");
                            targetSystem.setUrl(endpoint);
                            LOG.debug("building targetSystem.  Consumer Home Community=[" + subscription.getConsumer() + "]");
                            HomeCommunityType homeCommunityType = new HomeCommunityType();
                            homeCommunityType.setHomeCommunityId(subscription.getConsumer());
                            targetSystem.setHomeCommunity(homeCommunityType);

                            LOG.debug("building notify");
                            Element subscriptionReferenceElement = null;
                            try {
                                subscriptionReferenceElement = XmlUtility.convertXmlToElement(subscription
                                        .getSubscriptionReferenceXML());
                            } catch (Exception ex) {
                                LOG.error("Error getting subscription reference element:" + ex.getMessage(), ex);
                            }
                            NotifyBuilder builder = new NotifyBuilder();
                            Notify notifyElement = builder.buildNotifyFromSubscribe(notificationMessageElement,
                                    subscriptionReferenceElement);

                            AcknowledgementType ack = sendRequestToTarget(notifyElement, referenceParametersElements, assertion, targetSystem);

                            // Write notification record
                            writeNotificationItem(notificationMessageElement, subscription, ack);
                        }
                    }

                } catch (SubscriptionRepositoryException ex) {
                    LOG.error("Error collecting subscription records: " + ex.getMessage(), ex);
                }
            }
        }
    }

    private String findNotifyEndpoint(HiemSubscriptionItem subscription) {

        LOG.debug("Begin StandardOutboundHiemNotify.findNotifyEndpoint");

        String endpoint = "";

        if (subscription != null) {

            String rawSubscribeXml = subscription.getSubscribeXML();

            if (rawSubscribeXml != null) {
                try {
                    String xpathQuery = "//*[local-name()='Subscribe']/*[local-name()='ConsumerReference']"
                            + "/*[local-name()='Address']";

                    Node addressNode = XpathHelper.performXpathQuery(rawSubscribeXml, xpathQuery);

                    if (addressNode != null) {
                        endpoint = XmlUtility.getNodeValue(addressNode);
                        LOG.debug("Endpoint extracted from subscribe message: " + endpoint);
                    }

                } catch (XPathExpressionException ex) {
                    LOG.error("Error extracting the endpoint from a subscribe message: " + ex.getMessage(), ex);
                }
            }
        }

        LOG.debug("End StandardOutboundHiemNotify.findNotifyEndpoint");

        return endpoint;
    }

    private NodeList getNotificationMessageNodes(String rawNotifyXml) {

        NodeList msgNodes = null;

        try {
            javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
            javax.xml.xpath.XPath xpath = factory.newXPath();
            InputSource inputSource = new InputSource(new ByteArrayInputStream(rawNotifyXml.getBytes()));

            LOG.debug("About to perform notification message node xpath query");

            msgNodes = (NodeList) xpath.evaluate("//*[local-name()='Notify']/*[local-name()='NotificationMessage']",
                    inputSource, XPathConstants.NODESET);

            if ((msgNodes != null) && (msgNodes.getLength() > 0)) {

                LOG.debug("Message node list was not null/empty");

                for (int i = 0; i < msgNodes.getLength(); i++) {
                    Node childNode = msgNodes.item(i);
                    if (childNode != null) {
                        String nodeName = childNode.getLocalName();
                        LOG.debug("Node name: " + nodeName);
                    }
                }
            } else {
                LOG.debug("Message node or first child was null");
            }
        } catch (XPathExpressionException ex) {
            LOG.error("XPathExpressionException exception encountered loading the notify message body: " + ex.getMessage(), ex);
        }

        return msgNodes;
    }

    /**
     * Audit the request from the adapter.
     *
     * @param request The request to be audited
     * @param assertion The assertion to be audited
     */
    private void auditEntityRequest(Notify request, AssertionType assertion) {

        LOG.debug("Begin StandardOutboundHiemNotify.auditEntityRequest");

        try {
            AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

            NotifyRequestType message = new NotifyRequestType();
            message.setAssertion(assertion);
            message.setNotify(request);

            LogEventRequestType auditLogMsg = auditLogger.logNhinNotifyRequest(message,
                    NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ENTITY_INTERFACE);

            if (auditLogMsg != null) {
                AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
                AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
                proxy.auditLog(auditLogMsg, assertion);
            }
        } catch (Throwable t) {
            LOG.error("Error logging subscribe message: " + t.getMessage(), t);
        }

        LOG.debug("End StandardOutboundHiemNotify.auditEntityRequest");
    }

    /**
     * Audit the response from the adapter.
     *
     * @param ack The ack to be audited
     * @param assertion The assertion to be audited
     */
    private void auditEntityResponse(AcknowledgementType ack, AssertionType assertion) {

        LOG.debug("Begin StandardOutboundHiemNotify.auditEntityResponse");

        try {
            AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

            EntityNotifyResponseMessageType message = new EntityNotifyResponseMessageType();
            message.setAssertion(assertion);
            message.setAck(ack);

            LogEventRequestType auditLogMsg = auditLogger.logEntityNotifyResponse(message,
                    NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ENTITY_INTERFACE);

            if (auditLogMsg != null) {
                AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
                AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
                proxy.auditLog(auditLogMsg, assertion);
            }
        } catch (Throwable t) {
            LOG.error("Error logging subscribe message: " + t.getMessage(), t);
        }

        LOG.debug("End StandardOutboundHiemNotify.auditEntityResponse");
    }

    /**
     * Send subscription response to target.
     *
     * @param request The subscribe to send.
     * @param assertion The assertion to send
     * @param targetCommunitites The targets to be sent to
     * @return the response from the foreign entity
     */
    private AcknowledgementType sendRequestToTarget(Notify request, SoapMessageElements referenceParameters, AssertionType assertion,
            NhinTargetSystemType targetSystem) {

        AcknowledgementType ack = new AcknowledgementType();

        if (isPolicyValid(request, assertion)) {

            LOG.info("Policy check successful");

            // send request to nhin proxy
            try {
                ack = sendToNhinProxy(request, referenceParameters, assertion, targetSystem);
            } catch (Exception e) {
                // TODO nhinResponse = createFailedNhinSendResponse(hcid);
                String message = "Fault encountered sending notification message to target " + targetSystem.getHomeCommunity().getHomeCommunityId();
                ack.setMessage(message);
                LOG.error(message, e);
            }
        } else {
            String message = "Notification message failed policy check";
            ack.setMessage(message);
            LOG.error(message);
        }

        return ack;
    }

    /**
     * Sends the request to the nhin proxy.
     *
     * @param request the request to be sent
     * @param referenceParameters the reference parameters to be used
     * @param assertion the assertion to be used
     * @param nhinTargetSystem the nhin target system to be used
     */
    private AcknowledgementType sendToNhinProxy(Notify request, SoapMessageElements referenceParameters, AssertionType assertion,
            NhinTargetSystemType nhinTargetSystem) {

        AcknowledgementType ack = new AcknowledgementType();

        OutboundNotifyDelegate dsDelegate = new OutboundNotifyDelegate();
        OutboundNotifyOrchestratable dsOrchestratable = createOrchestratable(dsDelegate, request, referenceParameters,
                assertion, nhinTargetSystem);
        OutboundOrchestratable outboundOrchestratable = dsDelegate.process(dsOrchestratable);

        if (outboundOrchestratable != null) {
            ack.setMessage("Notification message successfully sent to target " + nhinTargetSystem.getHomeCommunity().getHomeCommunityId());
        } else {
            ack.setMessage("Notification message failure to target " + nhinTargetSystem.getHomeCommunity().getHomeCommunityId());
        }

        return ack;
    }

    /**
     * Create a notify orchestratable.
     *
     * @param delegate The delegate to be used by the orchestratable
     * @param request The request to be added to the orchestrtable
     * @param referenceParameters The reference parameters to be added to the orchestratable
     * @param assertion The assertion to be added to the orchestratable
     * @param nhinTargetSystem The target system to be added to the orchestratable
     * @return the orchestratable object
     */
    private OutboundNotifyOrchestratable createOrchestratable(OutboundNotifyDelegate delegate, Notify request,
            SoapMessageElements referenceParameters, AssertionType assertion, NhinTargetSystemType nhinTargetSystem) {

        OutboundNotifyOrchestratable dsOrchestratable = new OutboundNotifyOrchestratable(delegate);
        dsOrchestratable.setAssertion(assertion);
        dsOrchestratable.setRequest(request);
        dsOrchestratable.setReferenceParameters(referenceParameters);
        dsOrchestratable.setTarget(nhinTargetSystem);

        return dsOrchestratable;
    }

    /**
     * Check if policy for message is valid.
     *
     * @param notify The message to be checked.
     * @param assertion The assertion to be checked.
     * @return
     */
    private boolean isPolicyValid(Notify notifyRequest, AssertionType assertion) {

        LOG.debug("In HiemNotifyImpl.checkPolicy");

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

        LOG.debug("Finished HiemNotifyImpl.checkPolicy - valid: " + policyIsValid);

        return policyIsValid;
    }

    /**
     * Check if there is a valid target to send the request to.
     *
     * @param targetCommunity The community object to check for targets
     * @return true if there is a valid target
     */
    protected boolean hasNhinTargetHomeCommunityId(NhinTargetCommunityType targetCommunity) {

        if (targetCommunity != null && targetCommunity.getHomeCommunity() != null
                && NullChecker.isNotNullish(targetCommunity.getHomeCommunity().getHomeCommunityId())) {
            return true;
        }

        return false;
    }

    /**
     *
     * @param notificationMessage
     * @param subscriptionItem
     * @param ack
     */
    private void writeNotificationItem(Element notificationMessageElement, HiemSubscriptionItem subscriptionItem,
            AcknowledgementType ack) {

        NotificationMessageMarshaller notificationMessageMarshaller = new NotificationMessageMarshaller();
        NotificationMessageHolderType notificationMessage = notificationMessageMarshaller.unmarshal(notificationMessageElement);

        // Determine notification status from acknowledgment message
        String notificationStatus = HiemProcessorConstants.NOTIFICATION_STATUS_FAILED;
        if (ack != null && ack.getMessage() != null) {
            if (ack.getMessage().toUpperCase().contains("SUCCESS")) {
                notificationStatus = HiemProcessorConstants.NOTIFICATION_STATUS_SENT;
            }
        } else {
            notificationStatus = HiemProcessorConstants.NOTIFICATION_STATUS_ERROR;
        }

        NotificationStorageItemService storageService = new NotificationStorageItemService();
        storageService.saveStorageObject(notificationMessage, subscriptionItem, ack, notificationStatus);

    }

}
