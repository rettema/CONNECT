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
package gov.hhs.fha.nhinc.subscription.repository.data;

import java.io.Serializable;
import java.util.Date;

/**
 * Data storage object used for database interactions
 *
 * @author Neil Webb
 * @author richard.ettema
 */
public class SubscriptionStorageItem implements Serializable {

    private static final long serialVersionUID = -6997779397856060877L;

    private Long id;
    private String subscriptionId;
    private String subscriptionStatus;
    private String subscriptionRole;
    private String topic;
    private String dialect;
    private String consumer;
    private String producer;
    private String patientId;
    private String patientAssigningAuthority;
    private Date creationTime;
    private String targets;
    private String subscribeXML;
    private String subscriptionReferenceXML;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSubscriptionRole() {
        return subscriptionRole;
    }

    public void setSubscriptionRole(String subscriptionRole) {
        this.subscriptionRole = subscriptionRole;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientAssigningAuthority() {
        return patientAssigningAuthority;
    }

    public void setPatientAssigningAuthority(String patientAssigningAuthority) {
        this.patientAssigningAuthority = patientAssigningAuthority;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }

    public String getSubscribeXML() {
        return subscribeXML;
    }

    public void setSubscribeXML(String subscribeXML) {
        this.subscribeXML = subscribeXML;
    }

    public String getSubscriptionReferenceXML() {
        return subscriptionReferenceXML;
    }

    public void setSubscriptionReferenceXML(String subscriptionReferenceXML) {
        this.subscriptionReferenceXML = subscriptionReferenceXML;
    }

    @Override
    public int hashCode() {
        return subscriptionId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return ((obj == this) || ((obj instanceof SubscriptionStorageItem) && subscriptionId
                .equals(((SubscriptionStorageItem) obj).getSubscriptionId())));
    }

}
