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
package gov.hhs.fha.nhinc.subscribe.adapter;

import java.util.Date;
import java.util.GregorianCalendar;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.hiem.configuration.topicconfiguration.TopicConfigurationEntry;
import gov.hhs.fha.nhinc.hiem.configuration.topicconfiguration.TopicConfigurationManager;
import gov.hhs.fha.nhinc.hiem.processor.common.HiemProcessorConstants;
import gov.hhs.fha.nhinc.hiem.processor.common.SubscriptionStorage;
import gov.hhs.fha.nhinc.hiem.processor.faults.ConfigurationException;
import gov.hhs.fha.nhinc.hiem.processor.faults.SoapFaultFactory;
import gov.hhs.fha.nhinc.subscription.repository.data.HiemSubscriptionItem;
import gov.hhs.fha.nhinc.subscription.repository.roottopicextractor.RootTopicExtractor;
import gov.hhs.fha.nhinc.util.HomeCommunityMap;
import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.w3._2005._08.addressing.EndpointReferenceType;
import org.w3c.dom.Element;

/**
 * HIEM Subscribe Adapter Orchestration implementation
 *
 * @author richard.ettema
 */
public class HiemSubscribeAdapterOrchImpl {

    private static final Logger LOG = Logger.getLogger(HiemSubscribeAdapterOrchImpl.class);
    private SubscriptionStorage subscriptionStorage;

    public HiemSubscribeAdapterOrchImpl() {
        super();
        subscriptionStorage = new SubscriptionStorage();
    }

    /**
     *
     * @param subscribe
     * @param assertion
     * @return
     * @throws TopicNotSupportedFault
     * @throws InvalidTopicExpressionFault
     * @throws SubscribeCreationFailedFault
     */
    public SubscribeResponse adapterSubscribe(Element subscribe, AssertionType assertion)
            throws TopicNotSupportedFault,
            InvalidTopicExpressionFault,
            SubscribeCreationFailedFault {

        LOG.debug("Begin HiemSubscribeAdapterOrchImpl.adapterSubscribe");

        SubscribeResponse response = null;

        TopicConfigurationEntry topicConfig;
        try {
            LOG.debug("determine topic configuration");
            topicConfig = getTopicConfiguration(subscribe);
            LOG.debug("getTopicConfiguration complete.  isnull=" + (topicConfig == null));

            // Validate that the subscription topic is defined
            if (topicConfig == null) {
                throw new SoapFaultFactory().getUnknownTopic(null);
            }

            // Validate that subscription topic is supported
            if (!topicConfig.isSupported()) {
                throw new SoapFaultFactory().getKnownTopicNotSupported(subscribe);
            }

        } catch (ConfigurationException ex) {
            throw new SoapFaultFactory().getTopicConfigurationException(ex);
        }

        // Extract HCIDs for Producer (local) and Consumer (remote)
        LOG.debug("Extract HCIDs for Producer (local) and Consumer (remote)");
        String producerHCID = HomeCommunityMap.getHomeCommunityIdWithPrefix(HomeCommunityMap.getLocalHomeCommunityId());
        String consumerHCID = HomeCommunityMap.getHomeCommunityIdWithPrefix(HomeCommunityMap.getCommunityIdFromAssertion(assertion));

        // Create subscription item
        LOG.debug("Calling createSubscriptionItem");
        HiemSubscriptionItem subscription = createSubscriptionItem(subscribe, producerHCID, consumerHCID, HiemProcessorConstants.SUBSCRIPTION_ROLE_PRODUCER);

        // Store subscription
        LOG.debug("Calling storeSubscriptionItem");
        EndpointReferenceType subRef = storeSubscriptionItem(subscription);

        response = new SubscribeResponse();
        response.setSubscriptionReference(subRef);

        try {
            GregorianCalendar gCal = new GregorianCalendar();
            gCal.setTime(subscription.getCreationTime());
            XMLGregorianCalendar xmlGCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
            response.setCurrentTime(xmlGCal);
        } catch (Exception e) {
            LOG.error("Exception setting Subscribe response current time: ", e);
        }

        LOG.debug("End HiemSubscribeAdapterOrchImpl.adapterSubscribe");

        return response;
    }

    /**
     *
     * @param subscriptionItem
     * @return
     */
    private EndpointReferenceType storeSubscriptionItem(HiemSubscriptionItem subscriptionItem) {
        LOG.debug("In storeSubscriptionItem");
        return subscriptionStorage.storeSubscriptionItem(subscriptionItem);
    }

    /**
     *
     * @param subscribe
     * @param producer
     * @param consumer
     * @return
     * @throws SubscribeCreationFailedFault
     */
    private HiemSubscriptionItem createSubscriptionItem(Element subscribe, String producer, String consumer, String role)
            throws SubscribeCreationFailedFault {

        // Convert Nhin/Adapter Subscription (subscribe) to XML and extract topic and dialect
        String subscribeXml = null;
        String topic = null;
        String dialect = null;
        try {
            subscribeXml = XmlUtility.serializeElement(subscribe);

            RootTopicExtractor rootTopicExtrator = new RootTopicExtractor();
            Element topicExpression = rootTopicExtrator.extractTopicExpressionElementFromSubscribeElement(subscribe);
            topic = rootTopicExtrator.extractRootTopicFromSubscribeXml(subscribeXml);
            dialect = rootTopicExtrator.getDialectFromTopicExpression(topicExpression);
        } catch (Exception ex) {
            LOG.error("failed to process nhin/adapter subscribe xml", ex);
            throw new SoapFaultFactory().getMalformedSubscribe("Unable to serialize subscribe element", ex);
        }

        HiemSubscriptionItem subscription = null;
        if (subscribe != null) {
            LOG.debug("Creating subscription item");
            subscription = new HiemSubscriptionItem();

            subscription.setRole(role);
            subscription.setTopic(topic);
            subscription.setDialect(dialect);
            subscription.setConsumer(consumer);
            subscription.setProducer(producer);
            subscription.setSubscribeXML(subscribeXml);
            subscription.setCreationTime(new Date());
            LOG.debug("Finished creating subscription item");
        } else {
            LOG.debug("Subscribe message was null in createSubscriptionItem");
        }
        return subscription;
    }

    /**
     *
     * @param subscribeElement
     * @return
     * @throws TopicNotSupportedFault
     * @throws InvalidTopicExpressionFault
     * @throws ConfigurationException
     */
    private TopicConfigurationEntry getTopicConfiguration(Element subscribeElement) throws TopicNotSupportedFault,
            InvalidTopicExpressionFault, ConfigurationException {
        RootTopicExtractor rootTopicExtractor = new RootTopicExtractor();

        Element topic;
        try {
            LOG.debug("finding topic from message");
            topic = rootTopicExtractor.extractTopicExpressionElementFromSubscribeElement(subscribeElement);
            LOG.debug("complete with finding topic.  found=" + (topic != null));
        } catch (XPathExpressionException ex) {
            throw new SoapFaultFactory().getUnableToParseTopicExpressionFromSubscribeFault(ex);
        }

        TopicConfigurationEntry topicConfig;
        topicConfig = TopicConfigurationManager.getInstance().getTopicConfiguration(topic);

        if (topicConfig == null) {
            throw new SoapFaultFactory().getUnknownTopic(topic);
        }
        if (!topicConfig.isSupported()) {
            throw new SoapFaultFactory().getKnownTopicNotSupported(topic);
        }

        return topicConfig;
    }

}
