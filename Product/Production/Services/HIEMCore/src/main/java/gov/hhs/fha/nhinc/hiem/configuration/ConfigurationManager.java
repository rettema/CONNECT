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
package gov.hhs.fha.nhinc.hiem.configuration;

import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.hiem.processor.faults.ConfigurationException;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;

/**
 * Utility class with methods to return local entity and nhin notification endpoint addresses
 *
 * @author rayj
 * @author richard.ettema
 */
public class ConfigurationManager {

    /**
     *
     * @return URL string for the HIEM Entity Notify web service location
     * @throws ConfigurationException
     */
    public String getEntityNotificationConsumerAddress() throws ConfigurationException {

        String url = null;

        try {
            url = ConnectionManagerCache.getInstance().getInternalEndpointURLByServiceName(
                    NhincConstants.HIEM_NOTIFY_ENTITY_SERVICE_NAME);
        } catch (ConnectionManagerException ex) {
            throw new ConfigurationException("Unable to determine EntityNotificationConsumerAddress", ex);
        }

        return url;
    }

    /**
     *
     * @return URL string for the HIEM Nhin Notify web service location
     * @throws ConfigurationException
     */
    public String getNhinNotificationConsumerAddress() throws ConfigurationException {

        String url = null;

        try {
            url = ConnectionManagerCache.getInstance().getInternalEndpointURLByServiceName(
                    NhincConstants.HIEM_NOTIFY_SERVICE_NAME);
        } catch (ConnectionManagerException ex) {
            throw new ConfigurationException("Unable to determine NhinNotificationConsumerAddress", ex);
        }

        return url;
    }

}
