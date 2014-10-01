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
package gov.hhs.fha.nhinc.adapter.hiem.gui;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
//import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.Tab;
import com.sun.webui.jsf.component.TabSet;
import com.sun.webui.jsf.component.TextField;

import gov.hhs.fha.nhinc.adapter.hiem.gui.servicefacade.EventMessagingFacade;
import gov.hhs.fha.nhinc.subscription.repository.data.NotificationStorageItem;
import gov.hhs.fha.nhinc.subscription.repository.data.SubscriptionStorageItem;
import gov.hhs.fha.nhinc.util.Format;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.FacesException;

import org.apache.log4j.Logger;

/**
 * <p>
 * Page bean that corresponds to a similarly named JSP page. This class contains component definitions (and initialization code)
 * for all components that you have defined on this page, as well as lifecycle methods and event handlers where you may add
 * behavior to respond to incoming events.
 * </p>
 *
 * @version ManageEventMessaging.java
 * @version Created on May 15, 2014, 08:22:53 PM
 *
 * @author richard.ettema
 */
public class ManageEventMessaging extends AbstractPageBean {

    private static final Logger LOG = Logger.getLogger(ManageEventMessaging.class);

    private TabSet processTabSet = new TabSet();

    private Tab processSubscriptionsTab = new Tab();
    private TextField subscriptionStartDate = new TextField();
    private TextField subscriptionStopDate = new TextField();
    private DropDown subscriptionRoleDD = new DropDown();
    private DropDown subscriptionStatusDD = new DropDown();

    private Tab processNotificationsTab = new Tab();
    private StaticText subscriptionId = new StaticText();
    private StaticText subscriptionTopic = new StaticText();
    private StaticText subscriptionConsumer = new StaticText();
    private StaticText subscriptionProducer = new StaticText();
    private TextField notificationStartDate = new TextField();
    private TextField notificationStopDate = new TextField();
    private DropDown notificationStatusDD = new DropDown();

    private StaticText errorMessages = new StaticText();
    private String errors;

    private void _init() throws Exception {
    }

    public TabSet getProcessTabSet() {
        return processTabSet;
    }

    public void setProcessTabSet(TabSet processTabSet) {
        this.processTabSet = processTabSet;
    }

    public Tab getProcessSubscriptionsTab() {
        return processSubscriptionsTab;
    }

    public void setProcessSubscriptionsTab(Tab processSubscriptionsTab) {
        this.processSubscriptionsTab = processSubscriptionsTab;
    }

    public TextField getSubscriptionStartDate() {
        return subscriptionStartDate;
    }

    public void setSubscriptionStartDate(TextField subscriptionStartDate) {
        this.subscriptionStartDate = subscriptionStartDate;
    }

    public TextField getSubscriptionStopDate() {
        return subscriptionStopDate;
    }

    public void setSubscriptionStopDate(TextField subscriptionStopDate) {
        this.subscriptionStopDate = subscriptionStopDate;
    }

    public DropDown getSubscriptionRoleDD() {
        return subscriptionRoleDD;
    }

    public void setSubscriptionRoleDD(DropDown subscriptionRoleDD) {
        this.subscriptionRoleDD = subscriptionRoleDD;
    }

    public DropDown getSubscriptionStatusDD() {
        return subscriptionStatusDD;
    }

    public void setSubscriptionStatusDD(DropDown subscriptionStatusDD) {
        this.subscriptionStatusDD = subscriptionStatusDD;
    }

    public Tab getProcessNotificationsTab() {
        return processNotificationsTab;
    }

    public void setProcessNotificationsTab(Tab processNotificationsTab) {
        this.processNotificationsTab = processNotificationsTab;
    }

    public StaticText getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(StaticText subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public StaticText getSubscriptionTopic() {
        return subscriptionTopic;
    }

    public void setSubscriptionTopic(StaticText subscriptionTopic) {
        this.subscriptionTopic = subscriptionTopic;
    }

    public StaticText getSubscriptionConsumer() {
        return subscriptionConsumer;
    }

    public void setSubscriptionConsumer(StaticText subscriptionConsumer) {
        this.subscriptionConsumer = subscriptionConsumer;
    }

    public StaticText getSubscriptionProducer() {
        return subscriptionProducer;
    }

    public void setSubscriptionProducer(StaticText subscriptionProducer) {
        this.subscriptionProducer = subscriptionProducer;
    }

    public TextField getNotificationStartDate() {
        return notificationStartDate;
    }

    public void setNotificationStartDate(TextField notificationStartDate) {
        this.notificationStartDate = notificationStartDate;
    }

    public TextField getNotificationStopDate() {
        return notificationStopDate;
    }

    public void setNotificationStopDate(TextField notificationStopDate) {
        this.notificationStopDate = notificationStopDate;
    }

    public DropDown getNotificationStatusDD() {
        return notificationStatusDD;
    }

    public void setNotificationStatusDD(DropDown notificationStatusDD) {
        this.notificationStatusDD = notificationStatusDD;
    }

    public StaticText getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(StaticText errorMessages) {
        this.errorMessages = errorMessages;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    /** Creates a new instance of ManageEventMessaging */
    public ManageEventMessaging() {
    }

    /**
     * <p>
     * Callback method that is called whenever a page is navigated to, either directly via a URL, or indirectly via page
     * navigation. Customize this method to acquire resources that will be needed for event handlers and lifecycle methods,
     * whether or not this page is performing post back processing.
     * </p>
     *
     * <p>
     * Note that, if the current request is a postback, the property values of the components do <strong>not</strong> represent
     * any values submitted with this request. Instead, they represent the property values that were saved for this view when it
     * was rendered.
     * </p>
     */
    @Override
    public void init() {
        // Perform initializations inherited from our superclass
        super.init();
        // Perform application initialization that must complete
        // *before* managed components are initialized
        // Add your own initialiation code here

        // <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
        // Initialize automatically managed components
        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("ManageEventMessaging Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e : new FacesException(e);
        }

        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // Add your own initialization code here

        // Call Subscriptions Tab Action to initialize drop down lists
        processSubscriptionsTab_action();
    }

    /**
     * <p>
     * Callback method that is called after the component tree has been restored, but before any event processing takes place.
     * This method will <strong>only</strong> be called on a postback request that is processing a form submit. Customize this
     * method to allocate resources that will be required in your event handlers.
     * </p>
     */
    @Override
    public void preprocess() {
    }

    /**
     * <p>
     * Callback method that is called just before rendering takes place. This method will <strong>only</strong> be called for
     * the page that will actually be rendered (and not, for example, on a page that handled a postback and then navigated to a
     * different page). Customize this method to allocate resources that will be required for rendering this page.
     * </p>
     */
    @Override
    public void prerender() {
    }

    /**
     * <p>
     * Callback method that is called after rendering is completed for this request, if <code>init()</code> was called
     * (regardless of whether or not this was the page that was actually rendered). Customize this method to release resources
     * acquired in the <code>init()</code>, <code>preprocess()</code>, or <code>prerender()</code> methods (or acquired during
     * execution of an event handler).
     * </p>
     */
    @Override
    public void destroy() {
    }

    public String processSubscriptionsTab_action() {
        // Process the action. Return value is a navigation
        // case name where null will return to the same page.

        this.errorMessages.setText("");

        EventMessagingFacade eventMessagingFacade = new EventMessagingFacade();

        UserSession userSession = (UserSession) getBean("UserSession");

        userSession.setSubscriptionRoleItems(null); // reset to null to force lazy load
        userSession.getSubscriptionRoleItems().addAll(eventMessagingFacade.queryForSubscriptionRoles());

        userSession.setSubscriptionStatusItems(null); // reset to null to force lazy load
        userSession.getSubscriptionStatusItems().addAll(eventMessagingFacade.queryForSubscriptionStatuses());

        return null;
    }

    public String processNotificationsTab_action() {
        // Process the action. Return value is a navigation
        // case name where null will return to the same page.

        this.errorMessages.setText("");

        EventMessagingFacade eventMessagingFacade = new EventMessagingFacade();

        UserSession userSession = (UserSession) getBean("UserSession");

        userSession.setNotificationStatusItems(null); // reset to null to force lazy load
        userSession.getNotificationStatusItems().addAll(eventMessagingFacade.queryForNotificationStatuses());

        return null;
    }

    public String retrieveSubscriptionsButton_action() throws Exception {
        // Process the action. Return value is a navigation
        // case name where null will return to the same page.

        LOG.info("Start ManageEventMessaging.retrieveSubscriptionsButton_action");

        this.errorMessages.setText("");

        Date startDate = null;
        Date stopDate = null;

        if (!isDateSearchCriteriaValid(subscriptionStartDate, subscriptionStopDate, startDate, stopDate)) {
            this.errorMessages.setText(errors);
            return null;
        }

        EventMessagingFacade eventMessagingFacade = new EventMessagingFacade();

        String subscriptionRole = (String) subscriptionRoleDD.getValue();
        String subscriptionStatus = (String) subscriptionStatusDD.getValue();

        List<SubscriptionStorageItem> subscriptionResults = eventMessagingFacade.querySubscriptionRecords(startDate, stopDate, subscriptionRole, subscriptionStatus);

        UserSession userSession = (UserSession) getBean("UserSession");
        userSession.setSubscriptionResults(null); // reset to null to force lazy load

        if (subscriptionResults == null || subscriptionResults.size() == 0) {
            this.errorMessages.setText("No records found.");

        } else {
            userSession.getSubscriptionResults().addAll(subscriptionResults);
        }

        LOG.info("End ManageEventMessaging.retrieveSubscriptionsButton_action");

        return null;
    }

    public String retrieveNotificationsButton_action() throws Exception {
        // Process the action. Return value is a navigation
        // case name where null will return to the same page.

        LOG.info("Start ManageEventMessaging.retrieveNotificationsButton_action");

        this.errorMessages.setText("");

        Date startDate = null;
        Date stopDate = null;

        if (!isDateSearchCriteriaValid(notificationStartDate, notificationStopDate, startDate, stopDate)) {
            this.errorMessages.setText(errors);
            return null;
        }

        EventMessagingFacade eventMessagingFacade = new EventMessagingFacade();

        String notificationStatus = (String) notificationStatusDD.getValue();

        List<NotificationStorageItem> notificationResults = eventMessagingFacade.queryNotificationRecords(startDate, stopDate, notificationStatus);

        UserSession userSession = (UserSession) getBean("UserSession");
        userSession.setNotificationResults(null); // reset to null to force lazy load

        if (notificationResults == null || notificationResults.size() == 0) {
            this.errorMessages.setText("No records found.");

        } else {
            userSession.getNotificationResults().addAll(notificationResults);
        }

        LOG.info("End ManageEventMessaging.retrieveNotificationsButton_action");

        return null;
    }

    /**
     *
     * @param startDateText
     * @param stopDateText
     * @param startDate
     * @param stopDate
     * @return isValid; true - valid, false - not valid
     */
    private boolean isDateSearchCriteriaValid(TextField startDateText, TextField stopDateText, Date startDate, Date stopDate) {

        StringBuffer message = new StringBuffer();
        boolean isValid = true;

        if (startDateText == null || stopDateText == null) {
            message.append("Start Date and Stop Date should not be null");
            isValid = false;
        } else {
            try {
                String startDateString = (String) startDateText.getText();
                String stopDateString = (String) stopDateText.getText();

                Calendar cal1 = Format.getCalendarInstance(Format.MMDDYYYYHHMMSS_DATEFORMAT, startDateString);
                Calendar cal2 = Format.getCalendarInstance(Format.MMDDYYYYHHMMSS_DATEFORMAT, stopDateString);

                startDate = cal1.getTime();
                stopDate = cal2.getTime();

                if (startDate.after(stopDate)) {
                    message.append("Start Date should not be after Stop Date");
                    isValid = false;
                }
            } catch (Exception e) {
                LOG.error("Unable to parse date values, please check the date values are entered with the correct format (MMDDYYYY HH:MM:SS)", e);

                message.append("Unable to parse date values, please check the date values are entered with the correct format (MMDDYYYY HH:MM:SS)");
                isValid = false;
            }
        }

        errors = message.toString();

        return isValid;
    }

}
