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
package gov.hhs.fha.nhinc.adapter.hiem.gui.servicefacade;

import com.sun.webui.jsf.model.Option;

import gov.hhs.fha.nhinc.subscription.repository.data.NotificationStorageItem;
import gov.hhs.fha.nhinc.subscription.repository.data.SubscriptionStorageItem;
import gov.hhs.fha.nhinc.subscription.repository.service.NotificationStorageItemService;
import gov.hhs.fha.nhinc.subscription.repository.service.SubscriptionStorageItemService;
import gov.hhs.fha.nhinc.util.EventMessagingGUIConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * HIEM Re-implementation - 2014-06-19
 *
 * @author richard.ettema
 */
public class EventMessagingFacade implements EventMessagingGUIConstants {

    private static final Logger LOG = Logger.getLogger(EventMessagingFacade.class);

    /**
     * Return a list of Notification records based on the passed in criteria.
     *
     * @param queryCriteria
     * @return List<NotificationStorageItem>
     */
    public List<NotificationStorageItem> queryNotificationRecords(Date startDate, Date stopDate, String notificationStatus) {
        LOG.debug("EventMessagingFacade.queryNotificationRecords");

        NotificationStorageItemService notificationService = new NotificationStorageItemService();
        List<NotificationStorageItem> notificationRecs = notificationService.findByCriteria(startDate, stopDate, notificationStatus);

        return notificationRecs;
    }

    /**
     * Return a list of Subscription records based on the passed in criteria.
     *
     * @param queryCriteria
     * @return List<SubscriptionStorageItem>
     */
    public List<SubscriptionStorageItem> querySubscriptionRecords(Date startDate, Date stopDate, String subscriptionRole, String subscriptionStatus) {
        LOG.debug("EventMessagingFacade.querySubscriptionRecords");

        SubscriptionStorageItemService subscriptionService = new SubscriptionStorageItemService();
        List<SubscriptionStorageItem> subscriptionRecs = subscriptionService.findByCriteria(startDate, stopDate, subscriptionRole, subscriptionStatus);

        return subscriptionRecs;
    }

    public List<Option> queryForNotificationStatuses() {
        LOG.debug("EventMessagingFacade.queryForNotificationStatuses");

        List<Option> optionList = new ArrayList<Option>();

        optionList.add(new Option("Select...", ""));
        optionList.add(new Option(NOTIFY_STATUS_RECEIVED, NOTIFY_STATUS_RECEIVED));
        optionList.add(new Option(NOTIFY_STATUS_SENT, NOTIFY_STATUS_SENT));

        return optionList;
    }

    public List<Option> queryForSubscriptionRoles() {
        LOG.debug("EventMessagingFacade.queryForSubscriptionRoles");

        List<Option> optionList = new ArrayList<Option>();

        optionList.add(new Option("Select...", ""));
        optionList.add(new Option(SUBSCRIBE_ROLE_CONSUMER, SUBSCRIBE_ROLE_CONSUMER));
        optionList.add(new Option(SUBSCRIBE_ROLE_PRODUCER, SUBSCRIBE_ROLE_PRODUCER));

        return optionList;
    }

    public List<Option> queryForSubscriptionStatuses() {
        LOG.debug("EventMessagingFacade.queryForSubscriptionStatuses");

        List<Option> optionList = new ArrayList<Option>();

        optionList.add(new Option("Select...", ""));
        optionList.add(new Option(SUBSCRIBE_STATUS_SUBSCRIBED, SUBSCRIBE_STATUS_SUBSCRIBED));
        optionList.add(new Option(SUBSCRIBE_STATUS_UNSUBSCRIBED, SUBSCRIBE_STATUS_UNSUBSCRIBED));

        return optionList;
    }

}
