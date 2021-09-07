package com.kricom.mosquitto.internal.connection;

import com.kricom.mosquitto.internal.Mule4mosquittoConfiguration;
import org.mule.runtime.api.connection.*;
import org.mule.runtime.extension.api.annotation.param.Config;
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
