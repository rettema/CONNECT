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
package gov.hhs.fha.nhinc.unsubscribe.nhin.proxy;

import gov.hhs.fha.nhinc.aspect.NwhinInvocationEvent;
import gov.hhs.fha.nhinc.auditrepository.AuditRepositoryLogger;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxy;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxyObjectFactory;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.hiemauditlog.UnsubscribeResponseMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommoninternalorch.UnsubscribeRequestType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.hiem.consumerreference.ReferenceParametersHelper;
import gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements;
import gov.hhs.fha.nhinc.messaging.client.CONNECTCXFClientFactory;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClient;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants.GATEWAY_API_LEVEL;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.unsubscribe.aspect.UnsubscribeRequestTransformingBuilder;
import gov.hhs.fha.nhinc.unsubscribe.aspect.UnsubscribeResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.unsubscribe.nhin.proxy.service.NhinHiemUnsubscribeServicePortDescriptor;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;

import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.SubscriptionManager;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

/**
 *
 * @author rayj
 * @author richard.ettema
 */
public class NhinHiemUnsubscribeWebServiceProxy implements NhinHiemUnsubscribeProxy {

    private static final Logger LOG = Logger.getLogger(NhinHiemUnsubscribeWebServiceProxy.class);

    protected CONNECTClient<SubscriptionManager> getCONNECTClientSecured(
            ServicePortDescriptor<SubscriptionManager> portDescriptor, String url, AssertionType assertion,
            String subscriptionId) {

        return CONNECTCXFClientFactory.getInstance().getCONNECTClientSecured(portDescriptor, url, assertion,
                subscriptionId);
    }

    /* (non-Javadoc)
     * @see gov.hhs.fha.nhinc.unsubscribe.nhin.proxy.NhinHiemUnsubscribeProxy#unsubscribe(org.oasis_open.docs.wsn.b_2.Unsubscribe, gov.hhs.fha.nhinc.hiem.consumerreference.SoapMessageElements, gov.hhs.fha.nhinc.common.nhinccommon.AssertionType, gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType, java.lang.String)
     */
    @Override
    @NwhinInvocationEvent(beforeBuilder = UnsubscribeRequestTransformingBuilder.class,
        afterReturningBuilder = UnsubscribeResponseDescriptionBuilder.class, serviceType = "HIEM Unsubscribe",
        version = "2.0")
    public UnsubscribeResponse unsubscribe(Unsubscribe unsubscribe, SoapMessageElements referenceParametersElements,
            AssertionType assertion, NhinTargetSystemType target, String subscriptionId) throws ResourceUnknownFault,
            UnableToDestroySubscriptionFault, Exception {

        UnsubscribeResponse response = null;

        try {
            // Audit the input message
            auditInputMessage(unsubscribe, assertion);

            String url = ConnectionManagerCache.getInstance().getEndpointURLFromNhinTarget(target,
                    NhincConstants.HIEM_SUBSCRIPTION_MANAGER_SERVICE_NAME);

            if (NullChecker.isNullish(url)) {
                LOG.error("Error: Failed to retrieve url for service: " + NhincConstants.HIEM_SUBSCRIPTION_MANAGER_SERVICE_NAME);
            } else if (target == null) {
                LOG.error("Target system passed into the proxy is null");
            } else {

                String wsAddressingTo = ReferenceParametersHelper.getWsAddressingTo(referenceParametersElements);
                if (wsAddressingTo == null) {
                    wsAddressingTo = url;
                }

                ServicePortDescriptor<SubscriptionManager> portDescriptor = new NhinHiemUnsubscribeServicePortDescriptor();

                CONNECTClient<SubscriptionManager> client = getCONNECTClientSecured(portDescriptor, wsAddressingTo, assertion,
                        subscriptionId);

                WebServiceProxyHelper wsHelper = new WebServiceProxyHelper();

                wsHelper.addTargetApiLevel((BindingProvider) client.getPort(), GATEWAY_API_LEVEL.LEVEL_g0);
                wsHelper.addServiceName((BindingProvider) client.getPort(),
                        NhincConstants.HIEM_SUBSCRIPTION_MANAGER_SERVICE_NAME);

                response = (UnsubscribeResponse) client.invokePort(SubscriptionManager.class, "unsubscribe", unsubscribe);

                // Audit the response message
                auditResponseMessage(response, assertion);
            }

        } catch (Exception ex) {
            LOG.error("Failed to send unsubscribe to remote gateway.", ex);
        }

        return response;
    }

    /**
     * Audit the Unsubscribe (Nhin) request.
     *
     * @param request The request to be audited
     * @param assertion The assertion to be audited
     */
    private void auditInputMessage(Unsubscribe unsubscribe, AssertionType assertion) {

        LOG.debug("Begin NhinHiemUnsubscribeWebServiceProxy.auditInputMessage");

        try {
            AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

            UnsubscribeRequestType message = new UnsubscribeRequestType();
            message.setAssertion(assertion);
            message.setUnsubscribe(unsubscribe);

            LogEventRequestType auditLogMsg = auditLogger.logNhinUnsubscribeRequest(message,
                    NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION, NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

            if (auditLogMsg != null) {
                AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
                AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
                proxy.auditLog(auditLogMsg, assertion);
            }
        } catch (Throwable t) {
            LOG.error("Error logging unsubscribe message: " + t.getMessage(), t);
        }

        LOG.debug("End NhinHiemUnsubscribeWebServiceProxy.auditInputMessage");

    }

    /**
     * Audit the Unsubscribe (Nhin) response.
     *
     * @param response The response to be audited
     * @param assertion The assertion to be audited
     */
    private void auditResponseMessage(UnsubscribeResponse response, AssertionType assertion) {

        LOG.debug("Begin NhinHiemUnsubscribeWebServiceProxy.auditResponseMessage");

        try {
            AuditRepositoryLogger auditLogger = new AuditRepositoryLogger();

            UnsubscribeResponseMessageType message = new UnsubscribeResponseMessageType();
            message.setAssertion(assertion);
            message.setUnsubscribeResponse(response);

            LogEventRequestType auditLogMsg = auditLogger.logUnsubscribeResponse(message,
                    NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_NHIN_INTERFACE);

            if (auditLogMsg != null) {
                AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
                AuditRepositoryProxy proxy = auditRepoFactory.getAuditRepositoryProxy();
                proxy.auditLog(auditLogMsg, assertion);
            }
        } catch (Throwable t) {
            LOG.error("Error logging unsubscribe response: " + t.getMessage(), t);
        }

        LOG.debug("End NhinHiemUnsubscribeWebServiceProxy.auditResponseMessage");

    }

}
