package com.kricom.mosquitto.internal.sources;

import com.kricom.mosquitto.internal.connection.Mule4mosquittoConnection;
import com.kricom.mosquitto.internal.connection.Mule4mosquittoConnectionProvider;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.connector.ConnectException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.PollContext;
import org.mule.runtime.extension.api.runtime.source.PollingSource;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
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
public class TopicListener extends PollingSource<InputStream, Map<String, Object>>  {

    private final Logger LOGGER = LoggerFactory.getLogger(TopicListener.class);

    protected static Map<String, Queue<MqttMessage>> incommingMessages = new HashMap<String, Queue<MqttMessage>>();

    @Parameter
    @Example("Topic to subscribe client and listener to mqtt messages")
    @Optional(defaultValue = "topic")
    @Placement(order = 1)
    private String topic;

    @Connection
    ConnectionProvider<Mule4mosquittoConnection> connectionProvider;

    Mule4mosquittoConnection connection;

    @Override
    protected void doStart() throws MuleException {
        connection = connectionProvider.connect();

        synchronized (this){
            try {
                    connection.getClient().subscribe(topic);
                    //Create Object Store
                    this.createQueue(topic);
                    connection.getClient().setCallback(callback);
            } catch (MqttException e) {
                LOGGER.error("Could not subscribe to topic");
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void doStop() {
        LOGGER.debug("MqttTopicListener --> doStop()");
        try {
            connection.getClient().unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        connectionProvider.disconnect(connection);
    }

    @Override
    public void poll(PollContext<InputStream, Map<String, Object>> pollContext) {
        MqttMessage message = pollMessage(topic);
        if (message == null) { //If null there is no new messages!
            return;
        }
        LOGGER.debug("Message extracted from Queue: " + message);
        //Add message to payload
        pollContext.accept(item -> {
            item.setResult(read(message));
        });
    }

    @Override
    public void onRejectedItem(Result<InputStream, Map<String, Object>> result, SourceCallbackContext sourceCallbackContext) {
        // TODO: What need to do this????
    }

    /**
     * Transform mqtt message to mule result to run outside connector as payload
     * @param message
     * @return
     */
    private Result<InputStream, Map<String, Object>> read(MqttMessage message) {
        InputStream payload = new ByteArrayInputStream(message.getPayload());
        Map<String, Object> attributes = new HashMap<String, Object>();
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

    /*****************************************************************************************************
     * TODO: Transform to Object Store
     *****************************************************************************************************/
    /**
     * Add a new message on Store
     * @param topic
     * @param message
     */
    protected void addMessage(String topic, MqttMessage message) {
        synchronized (this){
            Queue<MqttMessage> messages = this.incommingMessages.get(topic);
            if (messages != null){
                messages.add(message);
            }
        }
    }

    /**
     * Creates a new entry on MAP to store Messages
     * @param topic
     */
    private void createQueue(String topic) {
        synchronized (this) {
            if (incommingMessages.get(topic) == null) {
                incommingMessages.put(topic, new LinkedList<MqttMessage>());
            }
        }

    }

    /**
     * Extract the first message on the Queue
     * @return
     */
    protected MqttMessage pollMessage(String topic) {
        synchronized (this){
            Queue<MqttMessage> messages = this.incommingMessages.get(topic);
            if (messages != null){
                return messages.poll();
            }
            return null;
        }
    }


    /**********************************************************************
     * MQTT Callback class
     **********************************************************************/

    MqttCallback callback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
//            LOGGER.debug("Topic: " + topic + " - Message: " + message.toString());
            addMessage(topic, message);
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
