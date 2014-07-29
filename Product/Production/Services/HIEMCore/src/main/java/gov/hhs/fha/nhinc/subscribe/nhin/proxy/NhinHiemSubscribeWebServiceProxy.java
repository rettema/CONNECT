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
package gov.hhs.fha.nhinc.subscribe.nhin.proxy;

import javax.xml.ws.BindingProvider;

import gov.hhs.fha.nhinc.aspect.NwhinInvocationEvent;
import gov.hhs.fha.nhinc.auditrepository.AuditRepositoryLogger;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxy;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxyObjectFactory;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.hiemauditlog.SubscribeResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommoninternalorch.SubscribeRequestType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.messaging.client.CONNECTCXFClientFactory;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClient;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants.GATEWAY_API_LEVEL;
import gov.hhs.fha.nhinc.subscribe.aspect.SubscribeRequestTransformingBuilder;
import gov.hhs.fha.nhinc.subscribe.aspect.SubscribeResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.subscribe.nhin.proxy.service.NhinHiemSubscribeServicePortDescriptor;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;

/**
 *
 * @author Jon Hoppesch
 * @author richard.ettema
 */
public class NhinHiemSubscribeWebServiceProxy implements NhinHiemSubscribeProxy {

    private static final Logger LOG = Logger.getLogger(NhinHiemSubscribeWebServiceProxy.class);

    protected CONNECTClient<NotificationProducer> getCONNECTClientSecured(
            ServicePortDescriptor<NotificationProducer> portDescriptor, String url, AssertionType assertion) {

        return CONNECTCXFClientFactory.getInstance().getCONNECTClientSecured(portDescriptor, url, assertion);
    }

    /* (non-Javadoc)
     * @see gov.hhs.fha.nhinc.subscribe.nhin.proxy.NhinHiemSubscribeProxy#subscribe(org.oasis_open.docs.wsn.b_2.Subscribe, gov.hhs.fha.nhinc.common.nhinccommon.AssertionType, gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType)
     */
    @Override
    @NwhinInvocationEvent(beforeBuilder = SubscribeRequestTransformingBuilder.class,
        afterReturningBuilder = SubscribeResponseDescriptionBuilder.class, serviceType = "HIEM Subscribe",
        version = "2.0")
    public SubscribeResponse subscribe(Subscribe subscribe, AssertionType assertion, NhinTargetSystemType target)
            throws Exception {

        SubscribeResponse response = null;

        try {
            // Audit the input message
            auditInputMessage(subscribe, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
                    NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

            String url = ConnectionManagerCache.getInstance().getEndpointURLFromNhinTarget(target,
                    NhincConstants.HIEM_SUBSCRIBE_SERVICE_NAME);

            if (NullChecker.isNullish(url)) {
                LOG.error("The URL for service: " + NhincConstants.HIEM_SUBSCRIBE_SERVICE_NAME + " is null");
            } else if (target == null) {
                LOG.error("Target system passed into the proxy is null");
            } else {
                ServicePortDescriptor<NotificationProducer> portDescriptor = new NhinHiemSubscribeServicePortDescriptor();

                CONNECTClient<NotificationProducer> client = getCONNECTClientSecured(portDescriptor, url, assertion);

                WebServiceProxyHelper wsHelper = new WebServiceProxyHelper();
                wsHelper.addTargetCommunity((BindingProvider) client.getPort(), target);
                wsHelper.addTargetApiLevel((BindingProvider) client.getPort(), GATEWAY_API_LEVEL.LEVEL_g0);
                wsHelper.addServiceName((BindingProvider) client.getPort(), NhincConstants.HIEM_SUBSCRIBE_SERVICE_NAME);

                response = (SubscribeResponse) client.invokePort(NotificationProducer.class, "subscribe", subscribe);

                // Audit the response message
                auditResponseMessage(response, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
                        NhincConstants.AUDIT_LOG_NHIN_INTERFACE);
            }

        } catch (Exception ex) {
            LOG.error("Error sending subscribe to remote gateway.", ex);
        }

        return response;
    }

    /**
     * Audit the Subscribe (Nhin) request
     *
     * @param subscribe
     * @param assertion
     * @param direction
     * @param logInterface
     */
    private void auditInputMessage(Subscribe subscribe, AssertionType assertion, String direction, String logInterface) {

        LOG.debug("Begin NhinHiemSubscribeWebServiceProxy.auditInputMessage");

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

        LOG.debug("End NhinHiemSubscribeWebServiceProxy.auditInputMessage");

    }

    /**
     * Audit the Subscribe (Nhin) response.
     *
     * @param response
     * @param assertion
     * @param direction
     * @param logInterface
     */
    private void auditResponseMessage(SubscribeResponse response, AssertionType assertion, String direction, String logInterface) {

        LOG.debug("Begin NhinHiemSubscribeWebServiceProxy.auditResponseMessage");

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

        LOG.debug("End NhinHiemSubscribeWebServiceProxy.auditResponseMessage");

    }

}
