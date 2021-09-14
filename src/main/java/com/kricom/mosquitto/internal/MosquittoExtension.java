package com.kricom.mosquitto.internal;

import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "mule4-mosquitto")
@Extension(name = "Mosquitto", vendor = "ar.kricom", category = Category.COMMUNITY)
@Configurations(MosquittoConfiguration.class)

public class MosquittoExtension {

}
