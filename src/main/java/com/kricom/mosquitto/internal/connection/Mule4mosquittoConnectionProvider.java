package com.kricom.mosquitto.internal.connection;

import com.kricom.mosquitto.internal.Mule4mosquittoConfiguration;
import com.kricom.mosquitto.internal.utils.MosquittoUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class (as it's name implies) provides connection instances and the funcionality to disconnect and validate those
 * connections.
 * <p>
 * All connection related parameters (values required in order to create a connection) must be
 * declared in the connection providers.
 * <p>
 * This particular example is a {@link PoolingConnectionProvider} which declares that connections resolved by this provider
 * will be pooled and reused. There are other implementations like {@link CachedConnectionProvider} which lazily creates and
 * caches connections or simply {@link ConnectionProvider} if you want a new connection each time something requires one.
 */
public class Mule4mosquittoConnectionProvider implements PoolingConnectionProvider<Mule4mosquittoConnection> {

  private final Logger LOGGER = LoggerFactory.getLogger(Mule4mosquittoConnectionProvider.class);

  @Config
  private Mule4mosquittoConfiguration config;

  @Override
  public Mule4mosquittoConnection connect() throws ConnectionException {
//    MosquittoUtils mutils = MosquittoUtils.getInstance();
//    mutils.connect(config);
    return new Mule4mosquittoConnection("bla bla bla");
  }

  @Override
  public void disconnect(Mule4mosquittoConnection connection) {
    try {
      connection.invalidate();
    } catch (Exception e) {
      LOGGER.error("Error while disconnecting [" + connection.getId() + "]: " + e.getMessage(), e);
    }
  }

  @Override
  public ConnectionValidationResult validate(Mule4mosquittoConnection connection) {
    return ConnectionValidationResult.success();
  }
}
