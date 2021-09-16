package com.kricom.mosquitto.internal.connection;


import com.kricom.mosquitto.mock.MqttClientMock;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public class MosquittoConnectionMock extends MosquittoConnection {

  private MqttClientMock mqttClient = null;

  public MosquittoConnectionMock(String host, int port, String userName, String password, String clientId) {
    super(host, port, userName, password, clientId);
    connect();
  }

  public String getId() {
    return clientId;
  }

  public void invalidate() {
    disconnect();
  }

  @Override
  protected MqttClient connect() {
    String brokerUrl ="tcp://" + host + ":" + port;
    MemoryPersistence persistence = new MemoryPersistence();
    try {
      mqttClient = new MqttClientMock(brokerUrl, clientId, persistence);
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setUserName(userName);
      if(password != null) {
        connOpts.setPassword(password.toCharArray());
      }
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
    return mqttClient;
  }
}
