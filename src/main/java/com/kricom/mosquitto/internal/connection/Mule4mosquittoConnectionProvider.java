package com.kricom.mosquitto.internal.connection;

import com.kricom.mosquitto.internal.Mule4mosquittoConfiguration;
import org.mule.runtime.api.connection.*;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


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

  @DisplayName("Mosquitto Host")
  @Optional(defaultValue = "localhost")
  @Parameter
  private String host;

  @DisplayName("Mosquitto Port")
  @Optional(defaultValue = "1883")
  @Parameter
  private int port;

  @DisplayName("User Name")
  @Optional(defaultValue = "mule")
  @Parameter
  private String userName;

  @DisplayName("Password")
  @Optional(defaultValue = "max")
  @Password
  @Parameter
  private String password;

  @DisplayName("Client Id")
  @Optional(defaultValue = "mule-mosquito-client")
  @Parameter
  private String clientId;

  @Override
  public Mule4mosquittoConnection connect() throws ConnectionException {
//    MosquittoUtils mutils = MosquittoUtils.getInstance();
//    mutils.connect(config);
    return new Mule4mosquittoConnection(host, port, userName, password, clientId);
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
      ConnectionValidationResult result;
      if(connection.isConnected()){
        result = ConnectionValidationResult.success();
      } else {
        result = ConnectionValidationResult.failure("Connection failed " + connection.getId(), new Exception());
      }

      return result;
  }
}
