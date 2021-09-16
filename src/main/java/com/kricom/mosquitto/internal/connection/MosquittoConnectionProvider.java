package com.kricom.mosquitto.internal.connection;

import org.mule.runtime.api.connection.*;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
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
public class MosquittoConnectionProvider<Mule4mosquitoConnection> implements PoolingConnectionProvider<MosquittoConnection> {

  private final Logger LOGGER = LoggerFactory.getLogger(MosquittoConnectionProvider.class);

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

  private static int clientIdCount = 1;

  private MosquittoConnection connection;

  @Override
  public MosquittoConnection connect() throws ConnectionException {
    synchronized (this){
      if (connection == null){
        connection = new MosquittoConnection(host, port, userName, password, clientId+"-"+clientIdCount);
        LOGGER.info("PROVIDER connect() clientId counter = " + clientIdCount);
        clientIdCount++;
      }
    }
    return connection;
  }

  @Override
  public void disconnect(MosquittoConnection connection) {
    try {
      LOGGER.error("disconnect provider invoked [" + connection.getId() + "]: ");
      connection.invalidate();
    } catch (Exception e) {
      LOGGER.error("Error while disconnecting [" + connection.getId() + "]: " + e.getMessage(), e);
    }
  }

  @Override
  public ConnectionValidationResult validate(MosquittoConnection connection) {
      ConnectionValidationResult result;
      if(connection.isConnected()){
        result = ConnectionValidationResult.success();
      } else {
        result = ConnectionValidationResult.failure("Connection failed " + connection.getId(), new Exception());
      }

      return result;
  }
}
