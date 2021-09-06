package com.kricom.mosquitto.internal.operations;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import com.kricom.mosquitto.internal.Mule4mosquittoConfiguration;
import com.kricom.mosquitto.internal.connection.Mule4mosquittoConnection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class ProduceOperation {

  @MediaType(value = ANY, strict = false)
  public String publish(@Config Mule4mosquittoConfiguration configuration, @Connection Mule4mosquittoConnection connection) {
    return "ok";
  }

}
