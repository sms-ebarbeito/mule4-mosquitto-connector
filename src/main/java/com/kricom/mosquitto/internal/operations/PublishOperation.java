package com.kricom.mosquitto.internal.operations;

import com.kricom.mosquitto.internal.Mule4mosquittoConfiguration;
import com.kricom.mosquitto.internal.connection.Mule4mosquittoConnection;
import com.kricom.mosquitto.internal.utils.MosquittoUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class PublishOperation {

  private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PublishOperation.class);

  @MediaType(value = ANY, strict = false)
  public String publish(@Config Mule4mosquittoConfiguration config,
                        @Connection Mule4mosquittoConnection connection,
                        InputStream payload,
                        String topic,
                        @Optional(defaultValue = "2") int qos) throws Exception {


    MosquittoUtils mutils = MosquittoUtils.getInstance();
    if (!mutils.isConnected()) {
      LOGGER.debug("Not connected --> Reconect!");
      mutils.reconnect(config);
    }

    LOGGER.debug("Publishing message");
    MqttMessage message = new MqttMessage(IOUtils.toByteArray(payload));
    message.setQos(qos);
    mutils.getClient().publish(topic, message);
    LOGGER.debug("Message published");
    return "Message sent";

  }

}
