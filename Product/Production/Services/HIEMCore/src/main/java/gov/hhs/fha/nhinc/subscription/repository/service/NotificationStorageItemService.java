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

import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.NhincCommonAcknowledgementMarshaller;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.NotificationMessageMarshaller;
import gov.hhs.fha.nhinc.hiem.processor.common.HiemProcessorConstants;
import gov.hhs.fha.nhinc.subscription.repository.dao.NotificationStorageItemDao;
import gov.hhs.fha.nhinc.subscription.repository.data.HiemSubscriptionItem;
import gov.hhs.fha.nhinc.subscription.repository.data.NotificationStorageItem;
import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;

/**
 * NotificationStorageItem persistence service
 *
 * @author richard.ettema
 */
public class NotificationStorageItemService {

    private static Log log = LogFactory.getLog(NotificationStorageItemService.class);

    /**
     * Save a notification storage item
     *
     * @param notificationItem Object to save
     */
    public void save(NotificationStorageItem notificationItem) {
        NotificationStorageItemDao dao = new NotificationStorageItemDao();
        dao.save(notificationItem);
    }

    /**
     * Retrieve notification storage items by basic search criteria
     *
     * @param startCreationDate
     * @param stopCreationDate
     * @param notificationStatus
     * @return Retrieved notifications
     */
    public List<NotificationStorageItem> findByCriteria(Date startNotificationDate, Date stopNotificationDate, String notificationStatus) {
        NotificationStorageItemDao dao = new NotificationStorageItemDao();
        return dao.findByCriteria(startNotificationDate, stopNotificationDate, notificationStatus);
    }

    /**
     * Retrieve a notification storage item by identifier
     *
     * @param notificationId Notification identifier
     * @return Retrieved notification
     */
    public NotificationStorageItem findById(Long notificationId) {
        NotificationStorageItemDao dao = new NotificationStorageItemDao();
        return dao.findById(notificationId);
    }

    /**
     * Retrieve a notification storage item by subscription identifier
     *
     * @param subscriptionId Subscription identifier
     * @return Retrieved notifications
     */
    public List<NotificationStorageItem> findBySubscriptionId(String subscriptionId) {
        NotificationStorageItemDao dao = new NotificationStorageItemDao();
        return dao.findBySubscriptionId(subscriptionId);
    }

    /**
     * Delete a notification storage item
     *
     * @param notificationItem Notification storage item to delete
     */
    public void delete(NotificationStorageItem notificationItem) {
        NotificationStorageItemDao dao = new NotificationStorageItemDao();
        dao.delete(notificationItem);
    }

    /**
     * Save a notification storage item generated from an inbound Notify message
     *
     * @param notificationMessage
     * @param subscriptionItem
     * @param ack
     */
    public void saveStorageObject(NotificationMessageHolderType notificationMessage, HiemSubscriptionItem subscriptionItem,
            AcknowledgementType ack, String notificationStatus) {

        NotificationStorageItem storageItem = loadStorageObject(notificationMessage, subscriptionItem, ack, notificationStatus);

        if (storageItem != null) {
            save(storageItem);
        } else {
            log.error("NotificationStorageItemService.saveStorageObject() - ERROR: storage object is null");
        }
    }

    private NotificationStorageItem loadStorageObject(NotificationMessageHolderType notificationMessage,
            HiemSubscriptionItem subscriptionItem, AcknowledgementType ack, String notificationStatus) {

        log.debug("NotificationStorageItemService.loadStorageObject() - START");

        NotificationStorageItem storageItem = null;

        if (subscriptionItem != null) {
            storageItem = new NotificationStorageItem();

            Long subscriptionId = subscriptionItem.getStorageObject().getId();
            storageItem.setSubscriptionId(subscriptionId);

            if (ack != null) {
                NhincCommonAcknowledgementMarshaller acknowledgementMarshaller = new NhincCommonAcknowledgementMarshaller();
                storageItem.setAcknowledgementMessage(XmlUtility.serializeElementIgnoreFaults(acknowledgementMarshaller
                        .marshal(ack)));
                storageItem.setNotificationStatus(notificationStatus);
            } else {
                storageItem.setNotificationStatus(HiemProcessorConstants.NOTIFICATION_STATUS_ERROR);
            }

            if (notificationMessage != null) {
                NotificationMessageMarshaller notificationMessageMarshaller = new NotificationMessageMarshaller();
                storageItem.setNotificationMessage(XmlUtility.serializeElementIgnoreFaults(notificationMessageMarshaller
                        .marshal(notificationMessage)));

                if (notificationMessage.getTopic() != null) {
                    if (notificationMessage.getTopic().getContent() != null
                            && !notificationMessage.getTopic().getContent().isEmpty()) {
                        storageItem.setTopic(notificationMessage.getTopic().getContent().get(0).toString());
                    }
                    if (notificationMessage.getTopic().getDialect() != null) {
                        storageItem.setDialect(notificationMessage.getTopic().getDialect());
                    }
                }
            }

            storageItem.setNotificationTime(new Timestamp(new Date().getTime()));
        }

        return storageItem;
    }

}
