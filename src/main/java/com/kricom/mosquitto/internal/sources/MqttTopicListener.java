package com.kricom.mosquitto.internal.sources;

import com.kricom.mosquitto.internal.Mule4mosquittoConfiguration;
import com.kricom.mosquitto.internal.utils.MosquittoUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.PollContext;
import org.mule.runtime.extension.api.runtime.source.PollingSource;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;


@Alias("Listener")
@Summary("Suscribe to Topic and listen for incoming messages")
@MediaType(value = ANY, strict = false)
public class MqttTopicListener extends PollingSource<InputStream, String> {

    private final Logger LOGGER = LoggerFactory.getLogger(MqttTopicListener.class);

    protected static Map<String, Queue<MqttMessage>> incommingMessages = new HashMap<String, Queue<MqttMessage>>();

    @Config
    private Mule4mosquittoConfiguration config;

    MosquittoUtils mutils = MosquittoUtils.getInstance();

    @Parameter
    @Optional(defaultValue = "topic-test")
    @Placement(order = 1)
    private String topic;


    @Override
    protected void doStart() throws MuleException {
        LOGGER.info("MqttTopicListener --> doStart()");
        if (!mutils.isConnected()) {
            LOGGER.info("Not connected --> Reconect!");
            mutils.reconnect(config);
        }
        try {
            synchronized (this){
                createQueue(topic);
                mutils.getClient().subscribe(topic);
                mutils.getClient().setCallback(callback);
            }
        } catch (MqttException e) {
            LOGGER.error("Could not subscribe to topic");
        }

        LOGGER.info("Queue Actual status");
        for (Map.Entry<String, Queue<MqttMessage>> queue: incommingMessages.entrySet()) {
            LOGGER.info("|--> " + queue.getKey());
        }
    }

    @Override
    protected void doStop() {
        LOGGER.debug("MqttTopicListener --> doStop()");
        if (!mutils.isConnected()) {
            LOGGER.error("Not connected --> Reconect!");
            mutils.reconnect(config);
        }
        try {
            mutils.getClient().unsubscribe(topic);

        } catch (MqttException e) {
            LOGGER.error("Could not unsubscribe to topic");
        }
    }

    @Override
    public void poll(PollContext<InputStream, String> pollContext) {
        if (pollContext.isSourceStopping()) {
            return;
        }
        MqttMessage message = pollMessage(topic);
        if (message == null) {
//            LOGGER.info("No new messages...");
            return;
        }
//        LOGGER.debug("Message extracted from Queue: " + message);
        //Add message to payload
        pollContext.accept(item -> {
                    item.setResult(read(message));
        });
    }

    /**
     * Transform mqtt message to mule result to run outside connector as payload
     * @param message
     * @return
     */
    private Result<InputStream, String> read(MqttMessage message) {
        InputStream payload = new ByteArrayInputStream(message.getPayload());
        //TODO: Replace string attribute to Object and complete with message information
        String attributes = "Qos: " + message.getQos();
        //TODO: JSON IS HARDCODED, need a method to check payload mediatype
        org.mule.runtime.api.metadata.MediaType media = org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON;
        return Result.<InputStream, String>builder()
                .output(payload)
                .attributes(attributes)
                .mediaType(media)
                .build();
    }

    @Override
    public void onRejectedItem(Result<InputStream, String> result, SourceCallbackContext sourceCallbackContext) {

    }

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

