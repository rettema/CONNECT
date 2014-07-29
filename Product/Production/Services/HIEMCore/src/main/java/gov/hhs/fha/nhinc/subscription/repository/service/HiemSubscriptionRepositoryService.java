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
package gov.hhs.fha.nhinc.subscription.repository.service;

import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.EndpointReferenceMarshaller;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.NotificationMessageMarshaller;
import gov.hhs.fha.nhinc.hiem.processor.common.HiemProcessorConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.subscription.filters.documentfilter.DocumentFilterStrategy;
import gov.hhs.fha.nhinc.subscription.repository.data.HiemSubscriptionItem;
import gov.hhs.fha.nhinc.subscription.repository.data.SubscriptionStorageItem;
import gov.hhs.fha.nhinc.subscription.repository.roottopicextractor.RootTopicExtractor;
import gov.hhs.fha.nhinc.subscription.repository.topicfilter.ITopicFilterStrategy;
import gov.hhs.fha.nhinc.subscription.repository.topicfilter.TopicFilterFactory;
import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.w3._2005._08.addressing.EndpointReferenceType;
import org.w3c.dom.Element;

/**
 * Data service for subscription items
 *
 * @author Neil Webb
 * @author richard.ettema
 */
public class HiemSubscriptionRepositoryService {

    private static final Logger LOG = Logger.getLogger(HiemSubscriptionRepositoryService.class);
    public static final String REFERENCE_PARAMETER_SUBSCRIPTION_ID_NAMESPACE = "http://www.hhs.gov/healthit/nhin";
    public static final String REFERENCE_PARAMETER_SUBSCRIPTION_ID_ELEMENT_NAME = "SubscriptionId";

    /**
     * Save a subscription to a remote gateway or adapter
     *
     * @param subscriptionItem
     * @throws SubscriptionRepositoryException
     */
    public void saveSubscriptionToExternal(HiemSubscriptionItem subscriptionItem) throws SubscriptionRepositoryException {

        LOG.debug("In saveSubscriptionToExternal");

        SubscriptionStorageItem storageItem = loadStorageObject(subscriptionItem);
        if (storageItem != null) {
            SubscriptionStorageItemService storageService = new SubscriptionStorageItemService();
            LOG.debug("Calling SubscriptionStorageItemService.save");
            storageService.save(storageItem);
        } else {
            throw new SubscriptionRepositoryException("Subscription item was null");
        }
    }

    /**
     * Save a subscription that a remote system (gateway or adapter) initiates to the CONNECT gateway. A subscription reference
     * is created and populated in the subscription item that is passed in for storage.
     *
     * @param subscriptionItem
     * @return Subscription Reference
     * @throws Exception
     */
    public EndpointReferenceType saveSubscriptionToConnect(HiemSubscriptionItem subscriptionItem)
            throws Exception {

        EndpointReferenceType subRef = null;
        LOG.debug("In saveSubscriptionToConnect");

        SubscriptionStorageItem storageItem = loadStorageObject(subscriptionItem);
        if (storageItem != null) {

            String subscriptionId = SubscriptionIdHelper.extractSubscriptionIdFromSubscriptionReferenceXml(subscriptionItem.getSubscriptionReferenceXML());
            if (NullChecker.isNullish(subscriptionId)) {
                // Generate subscription id
                subscriptionId = generateSubscriptionId();
            }

            storageItem.setSubscriptionId(subscriptionId);

            // Create subscription reference
            ConnectSubscriptionReferenceHelper connectSubscriptionReferenceHelper = new ConnectSubscriptionReferenceHelper();
            subRef = connectSubscriptionReferenceHelper.createSubscriptionReference(subscriptionId);
            String subRefXml = marshalSubscriptionReference(subRef);
            storageItem.setSubscriptionReferenceXML(subRefXml);

            // Set status and creation date time
            storageItem.setSubscriptionStatus(HiemProcessorConstants.SUBSCRIPTION_STATUS_SUBSCRIBED);
            storageItem.setCreationTime(subscriptionItem.getCreationTime());

            SubscriptionStorageItemService storageService = new SubscriptionStorageItemService();

            LOG.debug("Calling SubscriptionStorageItemService.save");
            storageService.save(storageItem);
        } else {
            throw new SubscriptionRepositoryException("Subscription item was null");
        }
        return subRef;
    }

    /**
     * Update the subscription record defined by subscriptionId with the passed newStatus value.
     *
     * @param subscriptionId
     * @param newStatus
     * @throws Exception
     */
    public void updateSubscriptionItemStatus(String subscriptionId, String subscriptionRole, String newStatus) throws Exception {

        LOG.debug("Begin updateSubscriptionItemStatus");

        try {
            SubscriptionStorageItemService storageService = new SubscriptionStorageItemService();

            List<SubscriptionStorageItem> storageItemList = storageService.findBySubscriptionIdRole(subscriptionId, subscriptionRole);

            if (NullChecker.isNotNullish(storageItemList)) {
                SubscriptionStorageItem itemToUpdate = storageItemList.get(0);
                itemToUpdate.setSubscriptionStatus(newStatus);

                LOG.debug("Calling SubscriptionStorageItemService.save");
                storageService.save(itemToUpdate);
            }
        } catch (Exception ex) {
            LOG.error("Failed to update status of subscription reference " + subscriptionId, ex);
            throw new SubscriptionRepositoryException(ex);
        }

        LOG.debug("End updateSubscriptionItemStatus");
    }

    public HiemSubscriptionItem retrieveByLocalSubscriptionReferenceParameters(SoapMessageElements referenceParametersElements)
            throws SubscriptionRepositoryException {

        HiemSubscriptionItem subscriptionItem = null;
        SubscriptionStorageItemService storageService = new SubscriptionStorageItemService();
        SubscriptionStorageItem storageItem = storageService.retrieveByLocalSubscriptionReferenceParameters(referenceParametersElements);

        if (storageItem != null) {
            subscriptionItem = loadDataObject(storageItem);
        }

        return subscriptionItem;
    }

    public HiemSubscriptionItem retrieveBySubscriptionId(String subscriptionId) throws SubscriptionRepositoryException {
        HiemSubscriptionItem subscriptionItem = null;
        SubscriptionStorageItemService storageService = new SubscriptionStorageItemService();
        List<SubscriptionStorageItem> storageItemList = storageService.findBySubscriptionId(subscriptionId);
        if (NullChecker.isNotNullish(storageItemList)) {
            subscriptionItem = loadDataObject(storageItemList.get(0));
        }
        return subscriptionItem;
    }

    public HiemSubscriptionItem retrieveByLocalSubscriptionReference(String subscriptionReferenceXML)
            throws SubscriptionRepositoryException {

        HiemSubscriptionItem subscriptionItem = null;
        SubscriptionStorageItemService storageService = new SubscriptionStorageItemService();
        SubscriptionStorageItem storageItem = storageService.retrieveByLocalSubscriptionReference(subscriptionReferenceXML);

        if (storageItem != null) {
            subscriptionItem = loadDataObject(storageItem);
        }

        return subscriptionItem;
    }

    public HiemSubscriptionItem retrieveBySubscriptionReference(EndpointReferenceType subscriptionReference,
            String subscriptionRole) throws SubscriptionRepositoryException {

        LOG.debug("retrieveBySubscriptionReference [subscriptionRole=" + subscriptionRole + "] [" + subscriptionReference + "]");

        String subscriptionId = SubscriptionIdHelper.extractSubscriptionIdFromEndpointReferenceType(subscriptionReference);

        SubscriptionStorageItemService storageService = new SubscriptionStorageItemService();
        List<SubscriptionStorageItem> subscriptionStorageItems = storageService.findBySubscriptionIdRole(subscriptionId, subscriptionRole);
        List<HiemSubscriptionItem> subscriptionItems = loadDataObjects(subscriptionStorageItems);

        HiemSubscriptionItem hiemSubscriptionItem = null;

        if (subscriptionItems != null) {

            LOG.debug("Retrieve by subscriptionId and subscriptionRole found " + subscriptionItems.size() + " record(s).");

            if (!subscriptionItems.isEmpty()) {

                LOG.debug("Returning first record.");

                hiemSubscriptionItem = subscriptionItems.get(0);
            }
        }

        return hiemSubscriptionItem;
    }

    public boolean isSubscriptionReferenceMatch(String xml1, String xml2) {

        SubscribeReferenceMatcher matcher = new SubscribeReferenceMatcher();
        return matcher.isSubscriptionReferenceMatch(xml2, xml2);

    }

    private SubscriptionStorageItem loadStorageObject(HiemSubscriptionItem subscriptionItem) {

        LOG.debug("In loadStorageObject");

        SubscriptionStorageItem storageItem = null;

        if (subscriptionItem != null) {
            storageItem = new SubscriptionStorageItem();

            storageItem.setSubscriptionRole(subscriptionItem.getRole());
            storageItem.setTopic(subscriptionItem.getTopic());
            storageItem.setDialect(subscriptionItem.getDialect());
            storageItem.setConsumer(subscriptionItem.getConsumer());
            storageItem.setProducer(subscriptionItem.getProducer());
            storageItem.setCreationTime(subscriptionItem.getCreationTime());
            storageItem.setSubscribeXML(subscriptionItem.getSubscribeXML());

            storageItem.setTargets(subscriptionItem.getTargets());
        }

        return storageItem;
    }

    private List<HiemSubscriptionItem> loadDataObjects(List<SubscriptionStorageItem> storageItems) {

        List<HiemSubscriptionItem> subscriptionItems = new ArrayList<HiemSubscriptionItem>();

        for (SubscriptionStorageItem storageItem : storageItems) {
            HiemSubscriptionItem subscriptionItem = loadDataObject(storageItem);
            subscriptionItems.add(subscriptionItem);
        }

        return subscriptionItems;
    }

    private HiemSubscriptionItem loadDataObject(SubscriptionStorageItem storageItem) {

        HiemSubscriptionItem subscriptionItem = null;

        if (storageItem != null) {
            subscriptionItem = new HiemSubscriptionItem();
            subscriptionItem.setSubscribeXML(storageItem.getSubscribeXML());
            subscriptionItem.setSubscriptionReferenceXML(storageItem.getSubscriptionReferenceXML());
            subscriptionItem.setTopic(storageItem.getTopic());
            subscriptionItem.setConsumer(storageItem.getConsumer());
            subscriptionItem.setProducer(storageItem.getProducer());
            subscriptionItem.setTargets(storageItem.getTargets());
            subscriptionItem.setCreationTime(storageItem.getCreationTime());
            subscriptionItem.setStorageObject(storageItem);
        }

        return subscriptionItem;
    }

    private String generateSubscriptionId() {
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * Retrieve the number of subscriptions in the repository.
     *
     * @return Subscription count
     */
    public int subscriptionCount() {
        return new SubscriptionStorageItemService().subscriptionCount();
    }

    /**
     * Empty the subscription repository.
     */
    public void emptyRepository() {
        new SubscriptionStorageItemService().emptyRepository();
    }

    public String marshalSubscriptionReference(EndpointReferenceType subscriptionReference)
            throws SubscriptionRepositoryException {

        try {
            EndpointReferenceMarshaller marshaller = new EndpointReferenceMarshaller();
            Element subscriptionReferenceElement = marshaller.marshal(subscriptionReference);
            String subscriptionReferenceXml = XmlUtility.serializeElement(subscriptionReferenceElement);

            return subscriptionReferenceXml;
        } catch (Exception ex) {
            LOG.error("Failed to marshall subscription reference");
            throw new SubscriptionRepositoryException(ex);
        }
    }

    public EndpointReferenceType unmarshalSubscriptionReference(String subscriptionReferenceXml)
            throws SubscriptionRepositoryException {

        try {
            EndpointReferenceMarshaller marshaller = new EndpointReferenceMarshaller();
            Element subscriptionReferenceElement = XmlUtility.convertXmlToElement(subscriptionReferenceXml);
            EndpointReferenceType subscriptionReference = marshaller.unmarshal(subscriptionReferenceElement);

            return subscriptionReference;
        } catch (Exception ex) {
            LOG.error("Failed to unmarshall subscription reference");
            throw new SubscriptionRepositoryException(ex);
        }
    }

    public List<HiemSubscriptionItem> RetrieveByNotificationMessage(Element notificationMessageElement, String subscriptionRole)
            throws SubscriptionRepositoryException {

        List<HiemSubscriptionItem> subscriptionItems = null;

        NotificationMessageMarshaller notificationMessageMarshaller = new NotificationMessageMarshaller();
        NotificationMessageHolderType notificationMessage = notificationMessageMarshaller.unmarshal(notificationMessageElement);

        LOG.debug("checking to see if should retrieve by subscription reference or by topic");

        if (notificationMessage.getSubscriptionReference() != null) {

            LOG.debug("retrieve by subscription reference: [" + notificationMessage.getSubscriptionReference() + "]");

            HiemSubscriptionItem subscriptionItem = retrieveBySubscriptionReference(notificationMessage.getSubscriptionReference(), subscriptionRole);
            if (subscriptionItem != null) {
                subscriptionItems = new ArrayList<HiemSubscriptionItem>();
                subscriptionItems.add(subscriptionItem);
            }

        } else {
            LOG.debug("retrieve by topic");

            // get root topic from notificationMessage
            RootTopicExtractor rootTopicExtractor = new RootTopicExtractor();
            String rootTopic = rootTopicExtractor.extractRootTopicFromNotificationMessageElement(notificationMessageElement);

            LOG.debug("retrieve by root topic [" + rootTopic + "]");

            // retrieve by root topic, producer
            SubscriptionStorageItemService storageService = new SubscriptionStorageItemService();
            List<SubscriptionStorageItem> subscriptionStorageItems = storageService.findByRootTopic(rootTopic, subscriptionRole);
            subscriptionItems = loadDataObjects(subscriptionStorageItems);

            List<HiemSubscriptionItem> matchingSubscriptionItems = new ArrayList<HiemSubscriptionItem>();

            // find matching subscriptions by topic
            for (HiemSubscriptionItem subscriptionItem : subscriptionItems) {
                boolean match = true;

                try {
                    // check to see if topic passes
                    String subscribeXml = subscriptionItem.getSubscribeXML();
                    Element subscriptionElement = null;
                    try {
                        subscriptionElement = XmlUtility.convertXmlToElement(subscribeXml);
                    } catch (Exception ex) {
                        LOG.error("Failed to convert subscription to element", ex);
                    }

                    Element subscriptionTopicExpression = rootTopicExtractor
                            .extractTopicExpressionElementFromSubscribeXml(subscribeXml);

                    String dialect = rootTopicExtractor.getDialectFromTopicExpression(subscriptionTopicExpression);

                    ITopicFilterStrategy topicfilter = TopicFilterFactory.getTopicFilterStrategy(dialect);
                    match = topicfilter.MeetsCriteria(subscriptionTopicExpression, notificationMessageElement);

                    if (match && DocumentFilterStrategy.IsDocumentCentric(subscriptionTopicExpression)) {
                        DocumentFilterStrategy filter = new DocumentFilterStrategy();
                        match = filter.MeetsCriteria(subscriptionElement, notificationMessageElement);
                    }

                    if (match) {
                        matchingSubscriptionItems.add(subscriptionItem);
                    }
                } catch (XPathExpressionException ex) {
                    LOG.warn("Failed to extract subscription topic expression from subscription item's raw Subscribe XML", ex);
                    match = false;
                }
            }

            subscriptionItems = matchingSubscriptionItems;
        }

        // return subscription items
        return subscriptionItems;
    }

    public void deleteSubscription(HiemSubscriptionItem subscriptionItem) throws SubscriptionRepositoryException {

        SubscriptionStorageItemService storageService = new SubscriptionStorageItemService();
        storageService.delete(subscriptionItem.getStorageObject());

    }

}
