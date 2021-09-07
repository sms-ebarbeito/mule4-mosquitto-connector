package com.kricom.mosquitto.internal;

import com.kricom.mosquitto.internal.connection.Mule4mosquittoConnectionProvider;
import com.kricom.mosquitto.internal.operations.PublishOperation;
import com.kricom.mosquitto.internal.sources.MqttTopicListener;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations({PublishOperation.class})
@Sources({MqttTopicListener.class})
@ConnectionProviders(Mule4mosquittoConnectionProvider.class)
public class Mule4mosquittoConfiguration {

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

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getClientId() {
    return clientId;
  }
}
