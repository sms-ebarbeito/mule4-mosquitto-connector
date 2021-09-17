package com.kricom.mosquitto.internal.sources;

import com.kricom.mosquitto.internal.connection.MosquittoConnection;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

@Alias("Listener")
@Summary("Suscribe to Topic and listen for incoming messages")
@MediaType(value = ANY, strict = false)
public class TopicListener extends Source<InputStream, Map<String, Object>> {

    private final Logger LOGGER = LoggerFactory.getLogger(TopicListener.class);


    @Parameter
    @Example("Topic to subscribe client and listener to mqtt messages")
    @Optional(defaultValue = "topic")
    @Placement(order = 1)
    protected String topic;

    @Connection
    ConnectionProvider<MosquittoConnection> connectionProvider;

    MosquittoConnection connection;

    SourcesHandler sourcesHandler = SourcesHandler.getInstance();

    @Override
    public void onStart(SourceCallback<InputStream, Map<String, Object>> sourceCallback) throws MuleException {
        connection = connectionProvider.connect();
        LOGGER.info("onStart --> Thread: " + Thread.currentThread());
        synchronized (this){
            try {
                    sourcesHandler.addSource(topic, sourceCallback);
                    connection.getClient().subscribe(topic);
                    connection.getClient().setCallback(sourcesHandler.getCallback());
            } catch (MqttException e) {
                LOGGER.error("Could not subscribe to topic");
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onStop() {
        LOGGER.debug("MqttTopicListener --> doStop()");
        try {
            connection.getClient().unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        connectionProvider.disconnect(connection);
    }

}
