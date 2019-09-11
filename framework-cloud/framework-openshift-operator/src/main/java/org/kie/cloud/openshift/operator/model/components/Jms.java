/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.cloud.openshift.operator.model.components;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Kie server JMS configuration.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Jms {

    private Boolean enableIntegration;
    private String amqSecretName;
    private String amqTruststoreName;
    private String amqTruststorePassword;
    private String amqKeystoreName;
    private String amqKeystorePassword;
    private Boolean executor;
    private String queueExecutor;
    private Boolean executorTransacted;
    private String queueRequest;
    private String queueResponse;
    private Boolean enableSignal;
    private String queueSignal;
    private Boolean enableAudit;
    private String queueAudit;
    private Boolean auditTransacted;
    private String username;
    private String password;
    private String amqQueues;
    private Boolean amqEnableSSL;

    public Boolean getEnableIntegration() {
        return enableIntegration;
    }

    public void setEnableIntegration(Boolean enableIntegration) {
        this.enableIntegration = enableIntegration;
    }

    public String getAmqKeystoreName() {
        return amqKeystoreName;
    }

    public void setAmqKeystoreName(String amqKeystoreName) {
        this.amqKeystoreName = amqKeystoreName;
    }

    public String getAmqKeystorePassword() {
        return amqKeystorePassword;
    }

    public void setAmqKeystorePassword(String amqKeystorePassword) {
        this.amqKeystorePassword = amqKeystorePassword;
    }

    public String getAmqSecretName() {
        return amqSecretName;
    }

    public void setAmqSecretName(String amqSecretName) {
        this.amqSecretName = amqSecretName;
    }

    public String getAmqTruststoreName() {
        return amqTruststoreName;
    }

    public void setAmqTruststoreName(String amqTruststoreName) {
        this.amqTruststoreName = amqTruststoreName;
    }

    public String getAmqTruststorePassword() {
        return amqTruststorePassword;
    }

    public void setAmqTruststorePassword(String amqTruststorePassword) {
        this.amqTruststorePassword = amqTruststorePassword;
    }

    public Boolean getAmqEnableSSL() {
        return amqEnableSSL;
    }

    public void setAmqEnableSSL(Boolean amqEnableSSL) {
        this.amqEnableSSL = amqEnableSSL;
    }

    public String getAmqQueues() {
        return amqQueues;
    }

    public void setAmqQueues(String amqQueues) {
        this.amqQueues = amqQueues;
    }

    public Boolean getAuditTransacted() {
        return auditTransacted;
    }

    public void setAuditTransacted(Boolean auditTransacted) {
        this.auditTransacted = auditTransacted;
    }

    public Boolean getEnableAudit() {
        return enableAudit;
    }

    public void setEnableAudit(Boolean enableAudit) {
        this.enableAudit = enableAudit;
    }

    public Boolean getEnableSignal() {
        return enableSignal;
    }

    public void setEnableSignal(Boolean enableSignal) {
        this.enableSignal = enableSignal;
    }

    public Boolean getExecutor() {
        return executor;
    }

    public void setExecutor(Boolean executor) {
        this.executor = executor;
    }

    public Boolean getExecutorTransacted() {
        return executorTransacted;
    }

    public void setExecutorTransacted(Boolean executorTransacted) {
        this.executorTransacted = executorTransacted;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQueueAudit() {
        return queueAudit;
    }

    public void setQueueAudit(String queueAudit) {
        this.queueAudit = queueAudit;
    }

    public String getQueueExecutor() {
        return queueExecutor;
    }

    public void setQueueExecutor(String queueExecutor) {
        this.queueExecutor = queueExecutor;
    }

    public String getQueueRequest() {
        return queueRequest;
    }

    public void setQueueRequest(String queueRequest) {
        this.queueRequest = queueRequest;
    }

    public String getQueueResponse() {
        return queueResponse;
    }

    public void setQueueResponse(String queueResponse) {
        this.queueResponse = queueResponse;
    }

    public String getQueueSignal() {
        return queueSignal;
    }

    public void setQueueSignal(String queueSignal) {
        this.queueSignal = queueSignal;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
