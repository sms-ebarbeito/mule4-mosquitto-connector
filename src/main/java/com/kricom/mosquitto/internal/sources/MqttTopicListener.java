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

import java.util.*;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;


@Alias("Listener")
@Summary("Suscribe to Topic and listen for incoming messages")
@MediaType(value = ANY, strict = false)
public class MqttTopicListener extends PollingSource<String, Object> {

    private final Logger LOGGER = LoggerFactory.getLogger(MqttTopicListener.class);

    protected static Map<String, Queue<String>> incommingMessages = new HashMap<String, Queue<String>>();

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
            createQueue(topic);
            mutils.getClient().subscribe(topic);
            mutils.getClient().setCallback(callback);
        } catch (MqttException e) {
            LOGGER.error("Could not subscribe to topic");
        }

        LOGGER.info("Actual status of Queue");
        for (Map.Entry<String, Queue<String>> queue: incommingMessages.entrySet()) {
            LOGGER.info("|--> " + queue.getKey());
        }
    }

    @Override
    protected void doStop() {
        LOGGER.info("MqttTopicListener --> doStop()");
        if (!mutils.isConnected()) {
            LOGGER.info("Not connected --> Reconect!");
            mutils.reconnect(config);
        }
        try {
            mutils.getClient().unsubscribe(topic);

        } catch (MqttException e) {
            LOGGER.error("Could not unsubscribe to topic");
        }
    }

    @Override
    public void poll(PollContext<String, Object> pollContext) {
        if (pollContext.isSourceStopping()) {
            return;
        }
        String message = pollMessage(topic);
        if (message == null) {
//            LOGGER.info("No new messages...");
            return;
        }
        LOGGER.info("Message extracted from Queue: " + message);
        //Add message to payload
        pollContext.accept(item -> {
                    item.setResult(Result.<String, Object>builder()
                    .output(message)
                    .build());
        });
    }

    @Override
    public void onRejectedItem(Result<String, Object> result, SourceCallbackContext sourceCallbackContext) {

    }

    /**
     * Add a new message on Store
     * @param topic
     * @param message
     */
    protected void addMessage(String topic, String message) {
        synchronized (this){
            Queue<String> messages = this.incommingMessages.get(topic);
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
        if (this.incommingMessages.get(topic) == null) {
            this.incommingMessages.put(topic, new LinkedList<String>());
        }

    }

    /**
     * Extract the first message on the Queue
     * @return
     */
    protected String pollMessage(String topic) {
        synchronized (this){
            Queue<String> messages = this.incommingMessages.get(topic);
            if (messages != null){
                return messages.poll();
            }
            return null;
        }
    }


    MqttCallback callback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            LOGGER.info("Topic: " + topic + " - Message: " + message.toString());
            addMessage(topic, message.toString());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            LOGGER.info("Token: " + token.toString());
        }

        @Override
        public void connectionLost(Throwable cause) {
            LOGGER.error("Conectio LOST!!");
            cause.printStackTrace();
        }
    };

}

