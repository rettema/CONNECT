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
package gov.hhs.fha.nhinc.subscription.repository.roottopicextractor;

import gov.hhs.fha.nhinc.xmlCommon.XmlUtility;
import gov.hhs.fha.nhinc.xmlCommon.XpathHelper;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.log4j.Logger;

/**
 * Oasis topic extraction helper
 *
 * @author rayj
 * @author richard.ettema
 */
public class RootTopicExtractorReverseCompat {

    private static final Logger LOG = Logger.getLogger(RootTopicExtractorReverseCompat.class);

    public Element buildTopicExpressionFromSubscribe(Element message) {
        String documentSubscribeXpathQuery = "//*[local-name()='Subscribe']/*[local-name()='AdhocQuery']";

        Element topicExpression = null;
        if (xpathCheckForTopic(documentSubscribeXpathQuery, message)) {
            topicExpression = buildDocumentTopicExpression();
        }
        return topicExpression;
    }

    public Element buildTopicFromNotify(Element message) {
        String documentNotifyXpathQuery = "//*[local-name()='NotificationMessage']/*[local-name()='Message']/*[local-name()='RetrieveDocumentSetRequest']";

        Element topic = null;

        if (xpathCheckForTopic(documentNotifyXpathQuery, message)) {
            topic = buildDocumentTopic();
        }
        return topic;
    }

    private Element buildDocumentTopicExpression() {
        String topicExpression = "<wsnt:TopicExpression xmlns:wsnt='http://docs.oasis-open.org/wsn/b-2' Dialect='http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple' xmlns:nhinc='urn:gov.hhs.fha.nhinc.hiemtopic'>nhinc:document</wsnt:TopicExpression>";
        Element element = null;
        try {
            element = XmlUtility.convertXmlToElement(topicExpression);
        } catch (Exception ex) {
            LOG.warn("unable to handle reverse compat topic", ex);
        }
        return element;
    }

    private Element buildDocumentTopic() {
        String topicExpression = "<wsnt:Topic xmlns:wsnt='http://docs.oasis-open.org/wsn/b-2' Dialect='http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple' xmlns:nhinc='urn:gov.hhs.fha.nhinc.hiemtopic'>nhinc:document</wsnt:Topic>";
        Element element = null;
        try {
            element = XmlUtility.convertXmlToElement(topicExpression);
        } catch (Exception ex) {
            LOG.warn("unable to handle reverse compat topic", ex);
        }
        return element;
    }

    private boolean xpathCheckForTopic(String xpathQuery, Element message) {
        boolean result = false;
        Node match = null;
        try {
            match = XpathHelper.performXpathQuery(message, xpathQuery);
        } catch (XPathExpressionException ex) {
            LOG.debug("Failed to perform xpath query against message.  Assume that this is not a match");
        }
        result = (match != null);
        return result;
    }

}
