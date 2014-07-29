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
package gov.hhs.fha.nhinc.unsubscribe.aspect;

import gov.hhs.fha.nhinc.common.nhinccommonentity.UnsubscribeRequestType;
import gov.hhs.fha.nhinc.event.AssertionEventDescriptionBuilder;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * HIEM Unsubscribe event logging request description builder
 *
 * @author richard.ettema
 */
public class UnsubscribeRequestDescriptionBuilder extends AssertionEventDescriptionBuilder {

    private static final Logger LOG = Logger.getLogger(UnsubscribeRequestDescriptionBuilder.class);

    private Optional<Unsubscribe> unsubscribeRequest;
    private Optional<UnsubscribeRequestType> unsubscribeRequestType;

    public UnsubscribeRequestDescriptionBuilder() {
        this.unsubscribeRequest = Optional.absent();
        this.unsubscribeRequestType = Optional.absent();
    }

    @Override
    public void buildTimeStamp() {
        // time stamp not available from request
    }

    @Override
    public void buildStatuses() {
        // status not a relevant field for requests
    }

    @Override
    public void buildRespondingHCIDs() {
        // responding HCID not relevant for request object
    }

    @Override
    public void buildPayloadTypes() {
        if (this.unsubscribeRequest.isPresent()) {
            setPayLoadTypes(ImmutableList.of(this.unsubscribeRequest.get().getClass().getSimpleName()));
        } else if (this.unsubscribeRequestType.isPresent()) {
            setPayLoadTypes(ImmutableList.of(this.unsubscribeRequestType.get().getClass().getSimpleName()));
        }
    }

    @Override
    public void buildPayloadSizes() {
        // payload size not available in request
    }

    @Override
    public void buildErrorCodes() {
        // error codes not available in request
    }

    @Override
    public void setArguments(Object... arguments) {
        Optional<Unsubscribe> unsubscribeRequest = extractUnsubscribe(arguments);
        if (unsubscribeRequest.isPresent()) {
            LOG.info("org.oasis_open.docs.wsn.b_2.Unsubscribe argument found");
            this.unsubscribeRequest = unsubscribeRequest;
        }
        Optional<UnsubscribeRequestType> unsubscribeRequestType = extractUnsubscribeRequestType(arguments);
        if (unsubscribeRequestType.isPresent()) {
            LOG.info("gov.hhs.fha.nhinc.common.nhinccommonentity.UnsubscribeRequestType argument found");
            this.unsubscribeRequestType = unsubscribeRequestType;
        }

        extractAssertion(arguments);
    }

    @Override
    public void setReturnValue(Object returnValue) {
        // return value not dealt with by request builder
    }

    private Optional<Unsubscribe> extractUnsubscribe(Object[] arguments) {
        if (arguments != null && arguments.length > 0 && arguments[0] instanceof Unsubscribe) {
            return Optional.of((Unsubscribe) arguments[0]);
        }
        return Optional.absent();
    }

    private Optional<UnsubscribeRequestType> extractUnsubscribeRequestType(Object[] arguments) {
        if (arguments != null && arguments.length > 0 && arguments[0] instanceof UnsubscribeRequestType) {
            return Optional.of((UnsubscribeRequestType) arguments[0]);
        }
        return Optional.absent();
    }

}
