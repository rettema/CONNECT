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
package gov.hhs.fha.nhinc.notify.aspect;

import gov.hhs.fha.nhinc.common.nhinccommonentity.NotifyRequestType;
import gov.hhs.fha.nhinc.event.AssertionEventDescriptionBuilder;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Notify;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * HIEM Notify event logging request description builder
 *
 * @author richard.ettema
 */
public class NotifyRequestDescriptionBuilder extends AssertionEventDescriptionBuilder {

    private static final Logger LOG = Logger.getLogger(NotifyRequestDescriptionBuilder.class);

    private Optional<Notify> notifyRequest;
    private Optional<NotifyRequestType> notifyRequestType;

    public NotifyRequestDescriptionBuilder() {
        this.notifyRequest = Optional.absent();
        this.notifyRequestType = Optional.absent();
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
        if (this.notifyRequest.isPresent()) {
            setPayLoadTypes(ImmutableList.of(this.notifyRequest.get().getClass().getSimpleName()));
        } else if (this.notifyRequestType.isPresent()) {
            setPayLoadTypes(ImmutableList.of(this.notifyRequestType.get().getClass().getSimpleName()));
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
        Optional<Notify> notifyRequest = extractNotify(arguments);
        if (notifyRequest.isPresent()) {
            LOG.info("org.oasis_open.docs.wsn.b_2.Notify argument found");
            this.notifyRequest = notifyRequest;
        }
        Optional<NotifyRequestType> notifyRequestType = extractNotifyRequestType(arguments);
        if (notifyRequestType.isPresent()) {
            LOG.info("gov.hhs.fha.nhinc.common.nhinccommonentity.NotifyRequestType argument found");
            this.notifyRequestType = notifyRequestType;
        }

        extractAssertion(arguments);
    }

    @Override
    public void setReturnValue(Object returnValue) {
        // return value not dealt with by request builder
    }

    private Optional<Notify> extractNotify(Object[] arguments) {
        if (arguments != null && arguments.length > 0 && arguments[0] instanceof Notify) {
            return Optional.of((Notify) arguments[0]);
        }
        return Optional.absent();
    }

    private Optional<NotifyRequestType> extractNotifyRequestType(Object[] arguments) {
        if (arguments != null && arguments.length > 0 && arguments[0] instanceof NotifyRequestType) {
            return Optional.of((NotifyRequestType) arguments[0]);
        }
        return Optional.absent();
    }

}
