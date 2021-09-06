package com.kricom.mosquitto.internal;

import com.kricom.mosquitto.internal.connection.Mule4mosquittoConnectionProvider;
import com.kricom.mosquitto.internal.operations.ProduceOperation;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "mule4-mosquitto")
@Extension(name = "Mule4-mosquitto-", vendor = "ar.kricom", category = Category.COMMUNITY)
@Configurations(Mule4mosquittoConfiguration.class)
public class Mule4mosquittoExtension {

}
