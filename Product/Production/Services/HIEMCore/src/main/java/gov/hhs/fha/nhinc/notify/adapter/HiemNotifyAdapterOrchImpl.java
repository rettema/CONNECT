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
package gov.hhs.fha.nhinc.notify.adapter;

import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.NotificationMessageMarshaller;
import gov.hhs.fha.nhinc.hiem.processor.common.HiemProcessorConstants;
import gov.hhs.fha.nhinc.subscription.repository.data.HiemSubscriptionItem;
import gov.hhs.fha.nhinc.subscription.repository.service.HiemSubscriptionRepositoryService;
import gov.hhs.fha.nhinc.subscription.repository.service.NotificationStorageItemService;
import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;

import java.util.List;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3c.dom.Element;

/**
 * HIEM Notify Adapter Orchestration implementation
 *
 * @author richard.ettema
 */
public class HiemNotifyAdapterOrchImpl {

    private static final Logger LOG = Logger.getLogger(HiemNotifyAdapterOrchImpl.class);

    /**
     *
     * @param notifyElement
     * @param assertion
     * @return
     * @throws Exception
     */
    public AcknowledgementType adapterNotify(Notify notify) throws Exception {

        LOG.debug("Begin HiemNotifyAdapterOrchImpl.adapterNotify");

        AcknowledgementType ack = new AcknowledgementType();

		if (notify != null) {

			HiemSubscriptionRepositoryService repositoryService = new HiemSubscriptionRepositoryService();
			NotificationMessageMarshaller notificationMessageMarshaller = new NotificationMessageMarshaller();

			// Get list of all notifications from request message
			List<NotificationMessageHolderType> notificationMessageList = notify.getNotificationMessage();

			if (notificationMessageList != null) {

				LOG.debug("Notifcation message count: " + notificationMessageList.size());

				// Process each notification message
				for (NotificationMessageHolderType notificationMessage : notificationMessageList) {

					Element notificationMessageElement = notificationMessageMarshaller.marshal(notificationMessage);

					LOG.debug("Notification message: " + XmlUtility.serializeElementIgnoreFaults(notificationMessageElement));

					// Get list of all applicable subscriptions for this notification
					List<HiemSubscriptionItem> subscriptionItems = repositoryService.RetrieveByNotificationMessage(
							notificationMessageElement, HiemProcessorConstants.SUBSCRIPTION_ROLE_CONSUMER);

					if (subscriptionItems != null) {

						LOG.debug("Subscription item list count: " + subscriptionItems.size());

						for (HiemSubscriptionItem subscriptionItem : subscriptionItems) {

							// TODO Implement notification message framework to handle secondary processing of notifications

							// Default Success acknowledgment message
							ack.setMessage("Notification message successfully received");

							// Write notification record
							writeNotificationItem(notificationMessage, subscriptionItem, ack);
						}
					} else {
						ack.setMessage("No subscriptions found for this notification.");
						LOG.debug("Subscription item list was null");
					}
				}
			} else {
				ack.setMessage("No notifications found in request.");
				LOG.info("Notification message list was null in notify message");
			}
		} else {
			ack.setMessage("No notify message found in request.");
			LOG.info("Notify message was null");
		}

        LOG.debug("End HiemNotifyAdapterOrchImpl.adapterNotify");

        return ack;

    }

    /**
     *
     * @param notificationMessage
     * @param subscriptionItem
     * @param ack
     */
    private void writeNotificationItem(NotificationMessageHolderType notificationMessage, HiemSubscriptionItem subscriptionItem,
            AcknowledgementType ack) {

        NotificationStorageItemService storageService = new NotificationStorageItemService();
        storageService.saveStorageObject(notificationMessage, subscriptionItem, ack, HiemProcessorConstants.NOTIFICATION_STATUS_RECEIVED);

    }

}
