package com.kricom.mosquitto;

import com.kricom.mosquitto.internal.connection.MosquittoConnection;
import com.kricom.mosquitto.internal.connection.MosquittoConnectionMock;
import com.kricom.mosquitto.internal.connection.MosquittoConnectionProviderMock;
import com.kricom.mosquitto.internal.sources.TopicListener;
import com.kricom.mosquitto.mock.MqttClientMock;
import com.kricom.mosquitto.mock.SourceCallBackMock;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import org.mule.runtime.api.metadata.MediaType;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TopicListenerTestCase {

  private MosquittoConnectionProviderMock provider;
  private MosquittoConnectionMock connection;
  private TopicListener listener;

  private SourceCallBackMock<InputStream, Map<String, Object>> sourceCallback;

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
    sourceCallback = new SourceCallBackMock<InputStream, Map<String, Object>>();
    listener.onStart(sourceCallback);
  }

  private String getMessageFromResult() throws Exception {
      return new BufferedReader(
              new InputStreamReader(sourceCallback.getResult().getOutput(), StandardCharsets.UTF_8))
              .lines().collect(Collectors.joining(""));
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

    Map<String, Object> attrib = sourceCallback.getResult().getAttributes().get();
    assertThat(attrib.get("topic"), is("mule-test"));
    assertThat(attrib.get("qos"), is(2));

    assertThat(new String(getMessageFromResult()), is("Hello from JUnit test"));

    MediaType mediaType = sourceCallback.getResult().getMediaType().get();
    assertThat(mediaType, is(MediaType.ANY));
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

    Map<String, Object> attrib = sourceCallback.getResult().getAttributes().get();
    assertThat(attrib.get("topic"), is("mule-test"));
    assertThat(attrib.get("qos"), is(2));

    assertThat(new String(getMessageFromResult()), is(originalMessage));

    MediaType mediaType = sourceCallback.getResult().getMediaType().get();
    assertThat(mediaType, is(MediaType.APPLICATION_JSON));
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

    Map<String, Object> attrib = sourceCallback.getResult().getAttributes().get();
    assertThat(attrib.get("topic"), is("mule-test"));
    assertThat(attrib.get("qos"), is(2));

    assertThat(new String(getMessageFromResult()), is(originalMessage));

    MediaType mediaType = sourceCallback.getResult().getMediaType().get();
    assertThat(mediaType, is(MediaType.APPLICATION_XML));
  }

}
