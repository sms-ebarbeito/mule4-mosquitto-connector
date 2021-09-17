package com.kricom.mosquitto.internal.sources;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
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
import java.util.Map;

public class SourcesHandler {

    private static SourcesHandler instance = new SourcesHandler();

    /**
     * Map with SourcesCallback from each topic listener
     */
    protected Map<String, SourceCallback<InputStream, Map<String, Object>>> sourcesCallBacks = new HashMap<String, SourceCallback<InputStream, Map<String, Object>>>();

    public static SourcesHandler getInstance() {
        return instance;
    }

    private SourcesHandler() { }

    public MqttCallback getCallback() {
        return callback;
    }

    /**
     * onStart topic listener may call this method to create the entry on map with the Mule SourceCallBack
     * @param topic
     * @param sourceCallback
     */
    public void addSource(String topic, SourceCallback<InputStream, Map<String, Object>> sourceCallback) {
        sourcesCallBacks.put(topic, sourceCallback);
    }

    /**
     * push a mqtt message into the sourceCallBack, this trigger a new payload into the output
     * @param topic
     * @param message
     * @throws Exception
     */
    protected void pushPayload(String topic, MqttMessage message) throws Exception {
        Result<InputStream, Map<String, Object>> result = read(topic, message);
        sourcesCallBacks.get(topic).handle(result);
    }

    /**
     * Transform mqtt message to mule result to run outside connector as payload
     * @param message
     * @return
     */
    private static Result<InputStream, Map<String, Object>> read(String topic, MqttMessage message) {
        InputStream payload = new ByteArrayInputStream(message.getPayload());
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("topic", topic);
        attributes.put("qos", new Integer(message.getQos()));
        attributes.put("id", message.getId());
        attributes.put("isDuplicated", message.isDuplicate());
        attributes.put("isRetained", message.isRetained());

        //TODO: ONLY JSON AND XML IS CHECKED, need a method to check payload mediatype

        //By default ANY is set
        org.mule.runtime.api.metadata.MediaType media = org.mule.runtime.api.metadata.MediaType.ANY;
        String payloadString = new String(message.getPayload());

        //Check if payload could be a Json
        try {
            JSONObject json = new JSONObject(payloadString);
            media = org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON;
        } catch (JSONException e) {
            //Check if payload could be a xml
            try {
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document document = docBuilder.parse(new InputSource(new StringReader(payloadString)));
                media = org.mule.runtime.api.metadata.MediaType.APPLICATION_XML;
            } catch (ParserConfigurationException e1) {
            } catch (IOException ioException) {
            } catch (SAXException saxException) {
            }
        }

        return Result.<InputStream, Map<String, Object>>builder()
                .output(payload)
                .attributes(attributes)
                .mediaType(media)
                .build();
    }


    /**********************************************************************
     * MQTT Callback class
     **********************************************************************/
    MqttCallback callback = new MqttCallback() {

        private final Logger LOGGER = LoggerFactory.getLogger(MqttCallback.class);

        public SourceCallback<InputStream, Map<String, Object>> sourceCallback;

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            if (message == null) { //If null there is no new messages!
                return;
            }

            pushPayload(topic, message);

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            LOGGER.info("Token: " + token.toString());
        }

        @Override
        public void connectionLost(Throwable cause) {
            LOGGER.error("Conection LOST!!");
            cause.printStackTrace();
        }

    };
}
