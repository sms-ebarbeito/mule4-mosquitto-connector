package com.kricom.mosquitto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.powermock.api.mockito.PowerMockito.mock;

import com.kricom.mosquitto.internal.connection.MosquittoConnection;
import com.kricom.mosquitto.internal.connection.MosquittoConnectionProvider;
import com.kricom.mosquitto.internal.connection.MosquittoConnectionProviderMock;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.junit.Test;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

public class FlowTestCase extends MuleArtifactFunctionalTestCase {

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @Test
  public void executePublishOperation() throws Exception {
    //TODO: Mock mqtt connection
    MosquittoConnectionProvider provider = mock(MosquittoConnectionProvider.class);
    //MosquittoConnection connection = provider.connect();

    System.out.println("Is connected?? : " + provider);

//    String payloadValue = ((String) flowRunner("publish").run()
//                                      .getMessage()
//                                      .getPayload()
//                                      .getValue());
//    assertThat(payloadValue, is("Message sent"));
  }

  @Test
  public void executeListener() throws Exception {
    //TODO: Mock mqtt connection
    String payloadValue = ((String) flowRunner("listener")
                                      .run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertThat(payloadValue, is("Using Configuration [configId] with Connection id [aValue:100]"));
  }
}
