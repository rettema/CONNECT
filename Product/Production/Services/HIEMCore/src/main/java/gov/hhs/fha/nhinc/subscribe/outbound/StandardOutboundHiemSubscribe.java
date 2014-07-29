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
package gov.hhs.fha.nhinc.subscribe.outbound;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import gov.hhs.fha.nhinc.aspect.OutboundProcessingEvent;
import gov.hhs.fha.nhinc.auditrepository.AuditRepositoryLogger;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxy;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxyObjectFactory;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.eventcommon.SubscribeEventType;
import gov.hhs.fha.nhinc.common.hiemauditlog.SubscribeResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyResponseType;
import gov.hhs.fha.nhinc.common.nhinccommoninternalorch.SubscribeRequestType;
import gov.hhs.fha.nhinc.hiem.configuration.ConfigurationManager;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.WsntSubscribeMarshaller;
import gov.hhs.fha.nhinc.hiem.processor.common.HiemProcessorConstants;
import gov.hhs.fha.nhinc.hiem.processor.common.SubscriptionStorage;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.policyengine.PolicyEngineChecker;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxy;
import gov.hhs.fha.nhinc.policyengine.adapter.proxy.PolicyEngineProxyObjectFactory;
import gov.hhs.fha.nhinc.subscribe.aspect.SubscribeRequestTransformingBuilder;
import gov.hhs.fha.nhinc.subscribe.aspect.SubscribeResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.subscribe.entity.OutboundSubscribeDelegate;
import gov.hhs.fha.nhinc.subscribe.entity.OutboundSubscribeOrchestratable;
import gov.hhs.fha.nhinc.subscription.repository.data.HiemSubscriptionItem;
import gov.hhs.fha.nhinc.subscription.repository.roottopicextractor.RootTopicExtractor;
import gov.hhs.fha.nhinc.transform.marshallers.JAXBContextHandler;
import gov.hhs.fha.nhinc.util.HomeCommunityMap;
import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;

import oasis.names.tc.xacml._2_0.context.schema.os.DecisionType;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeCreationFailedFaultType;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.w3._2005._08.addressing.EndpointReferenceType;
import org.w3c.dom.Element;

/**
 * HIEM Subscribe outbound standard implementation
 *
 * @author richard.ettema
 */
public class StandardOutboundHiemSubscribe implements OutboundHiemSubscribe {

	private static final Logger LOG = Logger.getLogger(StandardOutboundHiemSubscribe.class);

	/**
	 * This method performs the entity orchestration for a subscribe at the entity.
	 *
	 * @param subscribe - This request
	 * @param assertion - The assertion of the message
	 * @param targetCommunitites - The target of the request
	 * @return a subscription response of success or fail
	 * @throws SubscribeCreationFailedFault
	 */
    @OutboundProcessingEvent(beforeBuilder = SubscribeRequestTransformingBuilder.class,
            afterReturningBuilder = SubscribeResponseDescriptionBuilder.class, serviceType = "HIEM Subscribe",
            version = "2.0")
	@Override
	public SubscribeResponse processSubscribe(Subscribe subscribe, AssertionType assertion,
			NhinTargetCommunitiesType targetCommunities) throws SubscribeCreationFailedFault {

		LOG.debug("Begin StandardOutboundHiemSubscribe.processSubscribe");

		SubscribeResponse response = null;

		auditEntityRequest(subscribe, assertion);

        // update subscription request with consumer notify end point
		updateSubscribeNotificationConsumerEndpointAddress(subscribe);

		if (isPolicyValid(subscribe, assertion)) {
			LOG.info("Policy check successful");

			for (NhinTargetCommunityType targetCommunity : targetCommunities.getNhinTargetCommunity()) {
				// send request to nhin proxy
				response = getResponseFromTarget(subscribe, assertion, targetCommunity);

				// check for valid response
				if (response != null && response.getSubscriptionReference() != null) {
					// save subscription
					storeSubscription(subscribe, response, assertion, targetCommunity);

				} else {
					String messageText = "Subscribe response failure from target "
							+ targetCommunity.getHomeCommunity().getHomeCommunityId() + ".";
					LOG.error(messageText);
	                SubscribeCreationFailedFaultType faultInfo = null;
	                throw new SubscribeCreationFailedFault(messageText, faultInfo);
				}
			}

		} else {
			String messageText = "Failed policy check.";
			LOG.error(messageText);
            SubscribeCreationFailedFaultType faultInfo = null;
            throw new SubscribeCreationFailedFault(messageText, faultInfo);
		}

		auditEntityResponse(response, assertion);

		LOG.debug("End StandardOutboundHiemSubscribe.processSubscribe");

		return response;
	}

	/**
	 * Audit the entity (initiating) request.
	 *
	 * @param request The request to be audited
	 * @param assertion The assertion to be audited
	 */
	private void auditEntityRequest(Subscribe subscribe, AssertionType assertion) {
		LOG.debug("In EntitysubscribeOrchImpl.auditEntityRequest");

		try {
			AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

			SubscribeRequestType message = new SubscribeRequestType();
			message.setAssertion(assertion);
			message.setSubscribe(subscribe);

			LogEventRequestType auditLogMsg = auditLogger.logNhinSubscribeRequest(message,
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
	private void auditEntityResponse(SubscribeResponse response, AssertionType assertion) {
		LOG.debug("In EntitysubscribeOrchImpl.auditEntityResponse");
		try {
			AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

			SubscribeResponseMessageType message = new SubscribeResponseMessageType();
			message.setAssertion(assertion);
			message.setSubscribeResponse(response);

			LogEventRequestType auditLogMsg = auditLogger.logSubscribeResponse(message,
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
	private SubscribeResponse getResponseFromTarget(Subscribe request, AssertionType assertion,
			NhinTargetCommunityType targetCommunity) {

		SubscribeResponse nhinResponse = null;
		if (hasNhinTargetHomeCommunityId(targetCommunity)) {
			try {
				nhinResponse = sendToNhinProxy(request, assertion, targetCommunity);
			} catch (Exception e) {
				String hcid = targetCommunity.getHomeCommunity().getHomeCommunityId();
				LOG.error("Fault encountered while trying to send message to the nhin " + hcid, e);
			}
		} else {
			LOG.warn("The request to the Nhin did not contain a target home community id.");
		}

		return nhinResponse;
	}

	/**
	 *
	 * @param subscribe
	 * @param assertion
	 * @param nhinTargetCommunity
	 * @return
	 */
	private SubscribeResponse sendToNhinProxy(Subscribe subscribe, AssertionType assertion,
			NhinTargetCommunityType nhinTargetCommunity) {

		NhinTargetSystemType nhinTargetSystem = new NhinTargetSystemType();
		nhinTargetSystem.setHomeCommunity(nhinTargetCommunity.getHomeCommunity());

		OutboundSubscribeDelegate dsDelegate = new OutboundSubscribeDelegate();
		OutboundSubscribeOrchestratable dsOrchestratable = createOrchestratable(dsDelegate, subscribe, assertion,
				nhinTargetSystem);
		SubscribeResponse response = ((OutboundSubscribeOrchestratable) dsDelegate.process(dsOrchestratable)).getResponse();

		return response;
	}

	private OutboundSubscribeOrchestratable createOrchestratable(OutboundSubscribeDelegate delegate, Subscribe request,
			AssertionType assertion, NhinTargetSystemType nhinTargetSystem) {

		OutboundSubscribeOrchestratable dsOrchestratable = new OutboundSubscribeOrchestratable(delegate);
		dsOrchestratable.setAssertion(assertion);
		dsOrchestratable.setRequest(request);
		dsOrchestratable.setTarget(nhinTargetSystem);

		return dsOrchestratable;
	}

	/**
	 * Check if policy for message is valid.
	 *
	 * @param subscribe The message to be checked.
	 * @param assertion The assertion to be checked.
	 * @return
	 */
	private boolean isPolicyValid(Subscribe subscribe, AssertionType assertion) {
		LOG.debug("In EntitySubscribeOrchImpl.isPolicyValid");
		boolean policyIsValid = false;

		SubscribeEventType policyCheckReq = new SubscribeEventType();
		policyCheckReq.setDirection(NhincConstants.POLICYENGINE_OUTBOUND_DIRECTION);
		gov.hhs.fha.nhinc.common.eventcommon.SubscribeMessageType request = new gov.hhs.fha.nhinc.common.eventcommon.SubscribeMessageType();
		request.setAssertion(assertion);
		request.setSubscribe(subscribe);
		policyCheckReq.setMessage(request);

		PolicyEngineChecker policyChecker = new PolicyEngineChecker();
		CheckPolicyRequestType policyReq = policyChecker.checkPolicySubscribe(policyCheckReq);
		policyReq.setAssertion(assertion);
		PolicyEngineProxyObjectFactory policyEngFactory = new PolicyEngineProxyObjectFactory();
		PolicyEngineProxy policyProxy = policyEngFactory.getPolicyEngineProxy();
		CheckPolicyResponseType policyResp = policyProxy.checkPolicy(policyReq, assertion);

		if (policyResp.getResponse() != null && NullChecker.isNotNullish(policyResp.getResponse().getResult())
				&& policyResp.getResponse().getResult().get(0).getDecision() == DecisionType.PERMIT) {
			policyIsValid = true;
		}

		LOG.debug("Finished NhinHiemSubscribeWebServiceProxy.checkPolicy - valid: " + policyIsValid);
		return policyIsValid;
	}

	/**
	 * Check if there is a valid target to send the request to.
	 *
	 * @param targetCommunities The communities object to check for targets
	 * @return true if there is a valid target
	 */
	private boolean hasNhinTargetHomeCommunityId(NhinTargetCommunityType targetCommunity) {

		if (targetCommunity != null && targetCommunity.getHomeCommunity() != null
				&& NullChecker.isNotNullish(targetCommunity.getHomeCommunity().getHomeCommunityId())) {
			return true;
		}

		return false;
	}

	private EndpointReferenceType storeSubscription(Subscribe subscribe, SubscribeResponse response, AssertionType assertion,
			NhinTargetCommunityType targetCommunity) {

		EndpointReferenceType endpointReference = null;

		// ******Convert Subscription Reference (response) to XML
		String subscriptionReference = null;

		// Use reflection to get the correct subscription reference object
		Object subRef = getSubscriptionReference(response);
		if (subRef != null) {
			if (subRef.getClass().isAssignableFrom(EndpointReferenceType.class)) {
				subscriptionReference = serializeEndpointReferenceType((EndpointReferenceType) subRef);
			} else if (subRef.getClass().isAssignableFrom(W3CEndpointReference.class)) {
				subscriptionReference = serializeW3CEndpointReference((W3CEndpointReference) subRef);
			} else {
				LOG.error("Unknown subscription reference type: " + subRef.getClass().getName());
			}
		} else {
			LOG.error("Subscription reference was null");
		}

		// Convert Entity Subscription (subscribe) to XML and extract topic and dialect
		String subscribeXml = null;
		String topic = null;
		String dialect = null;
		try {
			WsntSubscribeMarshaller marshaller = new WsntSubscribeMarshaller();
			Element subscribeElement = marshaller.marshalSubscribeRequest(subscribe);
			subscribeXml = XmlUtility.serializeElement(subscribeElement);

			RootTopicExtractor rootTopicExtrator = new RootTopicExtractor();
			Element topicExpression = rootTopicExtrator.extractTopicExpressionElementFromSubscribeElement(subscribeElement);
			topic = rootTopicExtrator.extractRootTopicFromSubscribeXml(subscribeXml);
			dialect = rootTopicExtrator.getDialectFromTopicExpression(topicExpression);
		} catch (Exception ex) {
			LOG.error("failed to process entity subscribe xml", ex);
			subscribeXml = null;
			topic = null;
			dialect = null;
		}

		String targetCommunityXml = serializeTargetCommunity(targetCommunity);

		// Extract HCIDs for Consumer (local) and Producer (remote)
		LOG.debug("Extract HCIDs for Consumer (local) and Producer (remote)");
		String consumerHCID = HomeCommunityMap.getHomeCommunityIdWithPrefix(HomeCommunityMap.getLocalHomeCommunityId());
		String producerHCID = HomeCommunityMap.getHomeCommunityIdWithPrefix(HomeCommunityMap
				.getCommunityIdFromTargetCommunity(targetCommunity));

		HiemSubscriptionItem subscriptionItem = new HiemSubscriptionItem();

		subscriptionItem.setRole(HiemProcessorConstants.SUBSCRIPTION_ROLE_CONSUMER);
		subscriptionItem.setTopic(topic);
		subscriptionItem.setDialect(dialect);
		subscriptionItem.setConsumer(consumerHCID);
		subscriptionItem.setProducer(producerHCID);
		subscriptionItem.setSubscribeXML(subscribeXml);
		subscriptionItem.setSubscriptionReferenceXML(subscriptionReference);
		subscriptionItem.setTargets(targetCommunityXml);

		if (response.getCurrentTime() != null) {
			try {
				subscriptionItem.setCreationTime(response.getCurrentTime().toGregorianCalendar().getTime());
			} catch (Exception e) {
				LOG.error("Exception getting Subscribe response current time: ", e);
			}
		}

		SubscriptionStorage storage = new SubscriptionStorage();

		endpointReference = storage.storeSubscriptionItem(subscriptionItem);

		return endpointReference;
	}

	private String serializeTargetCommunity(NhinTargetCommunityType targetCommunity) {
		String targetCommunityXml = null;

		if (targetCommunity != null) {
			try {
				gov.hhs.fha.nhinc.common.nhinccommon.ObjectFactory ncCommonObjFact = new gov.hhs.fha.nhinc.common.nhinccommon.ObjectFactory();
				JAXBContextHandler oHandler = new JAXBContextHandler();
				JAXBContext jc = oHandler.getJAXBContext("gov.hhs.fha.nhinc.common.nhinccommon");
				Marshaller marshaller = jc.createMarshaller();
				StringWriter swXML = new StringWriter();
				LOG.debug("Calling marshal");
				marshaller.marshal(ncCommonObjFact.createNhinTargetCommunity(targetCommunity), swXML);
				targetCommunityXml = swXML.toString();
				LOG.debug("Marshaled subscription reference: " + targetCommunityXml);
			} catch (JAXBException ex) {
				LOG.error("Error serializing the target community: " + ex.getMessage(), ex);
			}
		}

		return targetCommunityXml;
	}

	private String serializeEndpointReferenceType(EndpointReferenceType endpointRefernece) {
		String endpointReferenceXml = null;
		if (endpointRefernece != null) {
			try {
				org.w3._2005._08.addressing.ObjectFactory wsaObjFact = new org.w3._2005._08.addressing.ObjectFactory();
				JAXBContextHandler oHandler = new JAXBContextHandler();
				JAXBContext jc = oHandler.getJAXBContext("org.w3._2005._08.addressing");
				Marshaller marshaller = jc.createMarshaller();
				StringWriter swXML = new StringWriter();
				LOG.debug("Calling marshal");
				marshaller.marshal(wsaObjFact.createEndpointReference(endpointRefernece), swXML);
				endpointReferenceXml = swXML.toString();
				LOG.debug("Marshaled endpoint reference: " + endpointReferenceXml);
			} catch (JAXBException ex) {
				LOG.error("Error serializing the endpoint reference: " + ex.getMessage(), ex);
			}
		}
		return endpointReferenceXml;
	}

	private void updateSubscribeNotificationConsumerEndpointAddress(Subscribe subscribe) {
		try {
			ConfigurationManager configurationManager = new ConfigurationManager();

			String notificationConsumerEndpointAddress = configurationManager.getNhinNotificationConsumerAddress();

			subscribe.getConsumerReference().getAddress().setValue(notificationConsumerEndpointAddress);

		} catch (Exception ex) {
			LOG.error("Error retrieving the notification consumer endpoint address: " + ex.getMessage(), ex);
		}
	}

	private Object getSubscriptionReference(SubscribeResponse subscribeResponse) {
		Object o = null;
		if (subscribeResponse != null) {
			Method[] methods = subscribeResponse.getClass().getDeclaredMethods();
			if (methods != null) {
				LOG.debug("Method count: " + methods.length);
				for (Method m : methods) {
					LOG.debug("Looking at method: " + m.getName());
					if (m.getName().equals("getSubscriptionReference")) {
						try {
							LOG.debug("Return type of getSubscriptionReference method: " + m.getReturnType().getName());
							Object[] params = {};
							o = m.invoke(subscribeResponse, params);
							break;
						} catch (IllegalAccessException ex) {
							LOG.error("IllegalAccessException calling getSubscriptionReference method: " + ex.getMessage(), ex);
						} catch (IllegalArgumentException ex) {
							LOG.error("IllegalArgumentException calling getSubscriptionReference method: " + ex.getMessage(),
									ex);
						} catch (InvocationTargetException ex) {
							LOG.error("InvocationTargetException calling getSubscriptionReference method: " + ex.getMessage(),
									ex);
						}
					}
				}
			} else {
				LOG.debug("Methods were null");
			}
		}
		return o;
	}

	private String serializeW3CEndpointReference(W3CEndpointReference endpointRefernece) {
		String endpointReferenceXml = null;
		if (endpointRefernece != null) {
			try {
				JAXBContextHandler oHandler = new JAXBContextHandler();
				JAXBContext jc = oHandler.getJAXBContext("javax.xml.ws.wsaddressing");
				Marshaller marshaller = jc.createMarshaller();
				StringWriter swXML = new StringWriter();
				LOG.debug("Calling marshal");
				marshaller.marshal(endpointRefernece, swXML);
				endpointReferenceXml = swXML.toString();
				LOG.debug("Marshaled W3C endpoint reference: " + endpointReferenceXml);
			} catch (JAXBException ex) {
				LOG.error("Error serializing the W3C endpoint reference: " + ex.getMessage(), ex);
			}
		}
		return endpointReferenceXml;
	}

}
