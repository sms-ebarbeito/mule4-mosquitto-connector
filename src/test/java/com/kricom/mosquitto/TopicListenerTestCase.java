package com.kricom.mosquitto;

import com.kricom.mosquitto.internal.connection.MosquittoConnection;
import com.kricom.mosquitto.internal.connection.MosquittoConnectionMock;
import com.kricom.mosquitto.internal.connection.MosquittoConnectionProviderMock;
import com.kricom.mosquitto.internal.sources.TopicListener;
import com.kricom.mosquitto.mock.MqttClientMock;
import com.kricom.mosquitto.mock.PollContextMock;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;


import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TopicListenerTestCase {

  private MosquittoConnectionProviderMock provider;
  private MosquittoConnectionMock connection;
  private TopicListener listener;

  private PollContextMock<InputStream, Map<String, Object>> context;

  @Before
  public void prepareListener() throws Exception {
    provider = new MosquittoConnectionProviderMock<MosquittoConnection>();
    connection = provider.connect();

    listener = new TopicListener();
    Class<?> listenerClass = listener.getClass(); //Class.forName("com.kricom.mosquitto.internal.sources.TopicListener");

    Field topicField = listenerClass.getDeclaredField("topic");
    Field connectionProviderField = listenerClass.getDeclaredField("connectionProvider");
    Field connectionField = listenerClass.getDeclaredField("connection");
    topicField.setAccessible(true);
    topicField.set(listener, "mule-test");
    connectionProviderField.setAccessible(true);
    connectionProviderField.set(listener, provider);
    connectionField.setAccessible(true);
    connectionField.set(listener, connection);
    Method doStart = listenerClass.getDeclaredMethod("doStart");
    doStart.setAccessible(true);
    doStart.invoke(listener);

    context = new PollContextMock<InputStream, Map<String, Object>>();
  }

  private MqttMessage getMessageFromConsumer(Consumer consumer) throws Exception {
    Class<?> consumerClass = consumer.getClass();
    Field consumerGetLambdaField = consumerClass.getDeclaredField("arg$2");
    consumerGetLambdaField.setAccessible(true);
    return (MqttMessage) consumerGetLambdaField.get(context.getConsumer());
  }

  @Test
  public void textTopicListenerOperation() throws Exception {

    String originalMessage = "Hello from JUnit test";

    MqttCallback callback = ((MqttClientMock)connection.getClient()).getCallback();
    MqttMessage msg = new MqttMessage();
    msg.setQos(2);
    msg.setId(1);
    msg.setPayload(originalMessage.getBytes());
    callback.messageArrived("mule-test", msg);

    listener.poll(context);

    MqttMessage mqttMessage = getMessageFromConsumer(context.getConsumer());
    assertThat(new String(mqttMessage.getPayload()), is("Hello from JUnit test"));
  }

  @Test
  public void jsonTopicListenerOperation() throws Exception {

    String originalMessage = "{ \"message\": \"Hello world from json mqtt\" }";

    MqttCallback callback = ((MqttClientMock)connection.getClient()).getCallback();
    MqttMessage msg = new MqttMessage();
    msg.setQos(2);
    msg.setId(1);
    msg.setPayload(originalMessage.getBytes());
    callback.messageArrived("mule-test", msg);

    listener.poll(context);

    MqttMessage mqttMessage = getMessageFromConsumer(context.getConsumer());
    assertThat(new String(mqttMessage.getPayload()), is(originalMessage));
  }

  @Test
  public void xmlTopicListenerOperation() throws Exception {

    String originalMessage = "<message><item>Hello world from xml mqtt</item></message>";

    MqttCallback callback = ((MqttClientMock)connection.getClient()).getCallback();
    MqttMessage msg = new MqttMessage();
    msg.setQos(2);
    msg.setId(1);
    msg.setPayload(originalMessage.getBytes());
    callback.messageArrived("mule-test", msg);

    listener.poll(context);

    MqttMessage mqttMessage = getMessageFromConsumer(context.getConsumer());
    assertThat(new String(mqttMessage.getPayload()), is(originalMessage));
  }

}
