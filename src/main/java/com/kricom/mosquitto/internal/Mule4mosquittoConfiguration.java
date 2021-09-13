package com.kricom.mosquitto.internal;

import com.kricom.mosquitto.internal.connection.Mule4mosquittoConnectionProvider;
import com.kricom.mosquitto.internal.operations.PublishOperation;
import com.kricom.mosquitto.internal.sources.TopicListener;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations({PublishOperation.class})
@Sources({TopicListener.class})
@ConnectionProviders(Mule4mosquittoConnectionProvider.class)
public class Mule4mosquittoConfiguration {


}
