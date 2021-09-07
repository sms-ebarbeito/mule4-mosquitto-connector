package com.kricom.mosquitto.internal.utils;

import com.kricom.mosquitto.internal.Mule4mosquittoConfiguration;
import com.kricom.mosquitto.internal.operations.PublishOperation;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.LoggerFactory;

public class MosquittoUtils {
    private static MosquittoUtils instance = new MosquittoUtils();
    private MqttClient mqttClient = null;
    private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MosquittoUtils.class);


    private MosquittoUtils() { }

    public static MosquittoUtils getInstance() {
        return instance;
    }

    public MqttClient getClient() {
        return mqttClient;
    }

    public MqttClient connect(Mule4mosquittoConfiguration config) {
        String brokerUrl ="tcp://" + config.getHost() + ":" + config.getPort();
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            mqttClient = new MqttClient(brokerUrl, config.getClientId(), persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName(config.getUserName());
            connOpts.setPassword(config.getPassword().toCharArray());
            connOpts.setCleanSession(true);
            LOGGER.info("checking");
            LOGGER.info("Mqtt Connecting to broker: " + brokerUrl);
            mqttClient.connect(connOpts);
            LOGGER.info("Mqtt Connected");
        } catch (MqttException me) {
            System.out.println(me);
        }
        return mqttClient;
    }

    public MqttClient reconnect(Mule4mosquittoConfiguration config){
        synchronized (this) {
            if (mqttClient == null) {
                return connect(config);
            } else {
                if(!mqttClient.isConnected()){
                    try {
                        mqttClient.reconnect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return mqttClient;
    }

    public void disconnect(){
        try {
            mqttClient.disconnect();
            LOGGER.info("Disconnected");
        } catch (MqttException me) {
            System.out.println(me);
        }
    }

    public Boolean isConnected(){
        if (mqttClient == null)
            return false;
        return mqttClient.isConnected();
    }
}
