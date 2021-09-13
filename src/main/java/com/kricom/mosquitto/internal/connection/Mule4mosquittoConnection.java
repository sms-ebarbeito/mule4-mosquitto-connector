package com.kricom.mosquitto.internal.connection;


import com.kricom.mosquitto.internal.Mule4mosquittoConfiguration;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class Mule4mosquittoConnection {

  private final Logger LOGGER = LoggerFactory.getLogger(Mule4mosquittoConnection.class);

  private MqttClient mqttClient = null;

  String host;
  int port;
  String userName;
  String password;
  String clientId;

  public Mule4mosquittoConnection(String host, int port, String userName, String password, String clientId) {
    this.host = host;
    this.port = port;
    this.userName = userName;
    this.password = password;
    this.clientId = clientId;
    this.connect();
  }

  public String getId() {
    return clientId;
  }

  public void invalidate() {
    disconnect();
  }

  private MqttClient connect() {
    String brokerUrl ="tcp://" + host + ":" + port;
    MemoryPersistence persistence = new MemoryPersistence();
    try {
      mqttClient = new MqttClient(brokerUrl, clientId, persistence);
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setUserName(userName);
      connOpts.setPassword(password.toCharArray());
      connOpts.setCleanSession(true);
      connOpts.setAutomaticReconnect(true);
      connOpts.setKeepAliveInterval(100);
      connOpts.setConnectionTimeout(300);
      LOGGER.debug("checking");
      LOGGER.info("Mqtt Connecting to broker: " + brokerUrl);
      mqttClient.connect(connOpts);
      LOGGER.info("Mqtt Connected");
    } catch (MqttException me) {
      System.out.println(me);
    }
    return mqttClient;
  }

  public MqttClient reconnect(){
    synchronized (this) {
      if (mqttClient == null) {
        return connect();
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

  private void disconnect(){
    try {
      mqttClient.disconnect();
      LOGGER.info("Disconnected");
    } catch (MqttException me) {
      LOGGER.error("" + me.getLocalizedMessage());
    }
  }

  public Boolean isConnected(){
    if (mqttClient == null)
      return false;
    return mqttClient.isConnected();
  }

  public MqttClient getClient() {
    if (!this.isConnected()) {
      this.reconnect();
    }
    return mqttClient;
  }
}
