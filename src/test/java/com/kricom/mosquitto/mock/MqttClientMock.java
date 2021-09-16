package com.kricom.mosquitto.mock;

import org.eclipse.paho.client.mqttv3.*;

import java.util.concurrent.ScheduledExecutorService;

/**
 * This is a Mock service for MqqtClient
 */
public class MqttClientMock extends MqttClient {

    private String serverURI;
    private String clientId;
    private MqttClientPersistence persistance;
    private ScheduledExecutorService executorServvice;

    private MqttConnectOptions options;

    private String topic;

    MqttCallback callback;

    private Boolean connected = false;

    public MqttClientMock(String serverURI, String clientId) throws MqttException {
        super(serverURI, clientId);
        this.serverURI = serverURI;
        this.clientId = clientId;
    }

    public MqttClientMock(String serverURI, String clientId, MqttClientPersistence persistence) throws MqttException {
        super(serverURI, clientId, persistence);
        this.serverURI = serverURI;
        this.clientId = clientId;
        this.persistance = persistence;
    }

    public MqttClientMock(String serverURI, String clientId, MqttClientPersistence persistence, ScheduledExecutorService executorService) throws MqttException {
        super(serverURI, clientId, persistence, executorService);
        this.serverURI = serverURI;
        this.clientId = clientId;
        this.persistance = persistence;
        this.executorServvice = executorService;
    }

    @Override
    public void connect(MqttConnectOptions options) throws MqttException {
        this.options = options;
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void reconnect() throws MqttException {
        connected = true;
    }

    @Override
    public void disconnect() throws MqttException {
        connected = false;
    }

    @Override
    public void setCallback(MqttCallback callback) {
        this.callback = callback;
    }

    public MqttCallback getCallback() {
        return callback;
    }

    @Override
    public void publish(String topic, MqttMessage message) throws MqttException {
        if (callback == null) {
            return;
        }
        IMqttDeliveryToken token = new MqttDeliveryToken();
        callback.deliveryComplete(token);
    }

    @Override
    public void subscribe(String topicFilter) throws MqttException {
        this.topic = topicFilter;
    }

}
