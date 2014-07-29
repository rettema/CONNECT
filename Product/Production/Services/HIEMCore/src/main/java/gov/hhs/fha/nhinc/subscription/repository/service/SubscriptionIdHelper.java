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
import gov.hhs.fha.nhinc.hiem.dte.EndpointReferenceMarshaller;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;
import gov.hhs.fha.nhinc.xmlCommon.XpathHelper;

import org.w3._2005._08.addressing.EndpointReferenceType;
import org.w3c.dom.Element;
import org.apache.log4j.Logger;

/**
 * Subscription Id Helper
 *
 * @author rayj
 * @author richard.ettema
 */
public class SubscriptionIdHelper {

    private static final Logger LOG = Logger.getLogger(SubscriptionIdHelper.class);

    public static String extractSubscriptionIdFromEndpointReferenceType(EndpointReferenceType subscriptionReference) {

        LOG.debug("Begin SubscriptionIdHelper.extractSubscriptionIdFromEndpointReferenceType");

        String subscriptionId = null;

        if (subscriptionReference != null) {
            try {
                LOG.debug("Attempting to extract subscription id from EndpointReferenceType");

                Element subscriptionReferenceElement = EndpointReferenceMarshaller.marshal(subscriptionReference);

                String subscriptionReferenceXml = XmlUtility.serializeElementIgnoreFaults(subscriptionReferenceElement);

                subscriptionId = extractSubscriptionIdFromSubscriptionReferenceXml(subscriptionReferenceXml);

            } catch (Throwable t) {
                LOG.error("Error extracting subscription id from EndpointReferenceType: " + t.getMessage(), t);
            }
        }

        LOG.debug("End SubscriptionIdHelper.extractSubscriptionIdFromEndpointReferenceType - Subscription id = '" + subscriptionId + "'");

        return subscriptionId;
    }

    /**
     * Extract the SubscriptionId element value from the XML representation of an EndpointReferenceType
     *
     * @param subscriptionReferenceXml
     * @return SubscriptionId
     */
    public static String extractSubscriptionIdFromSubscriptionReferenceXml(String subscriptionReferenceXml) {

        LOG.debug("Begin SubscriptionIdHelper.extractSubscriptionIdFromSubscriptionReferenceXml");

        String subscriptionId = null;

        if (subscriptionReferenceXml != null) {
            try {
                LOG.debug("Attempting to extract subscription id from subscription reference xml: " + subscriptionReferenceXml);

                Element subscriptionIdElement = (Element) XpathHelper.performXpathQuery(subscriptionReferenceXml,
                        "//*[local-name()='ReferenceParameters']/*[local-name()='SubscriptionId']");
                subscriptionId = XmlUtility.getNodeValue(subscriptionIdElement);

                if (NullChecker.isNullish(subscriptionId)) {
                    subscriptionIdElement = (Element) XpathHelper
                            .performXpathQuery(subscriptionReferenceXml,
                                    "/*[local-name()='EndpointReference']/*[local-name()='ReferenceParameters']/*[local-name()='SubscriptionId']");
                }

                if (subscriptionId != null) {
                    subscriptionId = subscriptionId.trim();
                }

                LOG.debug("The value for subscription id was: " + ((subscriptionId == null) ? "null" : subscriptionId));
            } catch (Throwable t) {
                LOG.error("Error looking up subscription id: " + t.getMessage(), t);
            }
        }

        LOG.debug("End SubscriptionIdHelper.extractSubscriptionIdFromSubscriptionReferenceXml - Subscription id = '" + subscriptionId + "'");

        return subscriptionId;
    }

    /**
     * Extract the SubscriptionId from the SOAP Header message elements
     *
     * @param referenceParametersElements
     * @return SubscriptionId
     */
    public static String extractSubscriptionIdFromReferenceParametersElements(SoapMessageElements referenceParametersElements) {

        LOG.debug("Begin SubscriptionIdHelper.extractSubscriptionIdFromReferenceParametersElements");

        String subscriptionId = null;

        if (referenceParametersElements != null) {

            LOG.debug("looking for subscription id");

            for (Element consumerReferenceElement : referenceParametersElements.getElements()) {

                if (consumerReferenceElement.getLocalName().contentEquals("SubscriptionId")) {
                    LOG.debug("subscriptionId element: " + XmlUtility.formatElementForLogging(null, consumerReferenceElement));
                    subscriptionId = XmlUtility.getNodeValue(consumerReferenceElement);
                    break;
                }
            }
        }

        LOG.debug("End SubscriptionIdHelper.extractSubscriptionIdFromReferenceParametersElements - Subscription id = '" + subscriptionId + "'");

        return subscriptionId;
    }

}
