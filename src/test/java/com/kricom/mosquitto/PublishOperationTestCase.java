package com.kricom.mosquitto;

import com.kricom.mosquitto.internal.MosquittoConfiguration;
import com.kricom.mosquitto.internal.connection.MosquittoConnection;
import com.kricom.mosquitto.internal.connection.MosquittoConnectionMock;
import com.kricom.mosquitto.internal.connection.MosquittoConnectionProvider;
import com.kricom.mosquitto.internal.connection.MosquittoConnectionProviderMock;
import com.kricom.mosquitto.internal.operations.PublishOperation;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.powermock.api.mockito.PowerMockito.mock;

public class PublishOperationTestCase {


  @Test
  public void executePublishOperation() throws Exception {
    MosquittoConnectionProviderMock provider = new MosquittoConnectionProviderMock<MosquittoConnection>();
    MosquittoConnectionMock connection = provider.connect();

    int qos = 2;
    String topic = "mule-test";
    InputStream payload = new ByteArrayInputStream("This is a message to send across mqtt".getBytes());

    PublishOperation publishOp = new PublishOperation();
    String result = publishOp.publish(
            new MosquittoConfiguration(),
            connection,
            payload,
            topic,
            qos
    );

    assertThat(result, is("Message sent"));

  }

}
