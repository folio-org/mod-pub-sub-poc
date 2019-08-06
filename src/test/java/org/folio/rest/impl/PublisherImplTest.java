package org.folio.rest.impl;

import org.folio.rest.TestBase;
import org.folio.rest.jaxrs.model.TestRequest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class PublisherImplTest extends TestBase {

  @Test
  @Ignore("This test requires Kafka to be running on configured host/port")
  public void test() throws InterruptedException, JsonProcessingException {
    sendPublishRequest("order_created");
    sendPublishRequest("order_updated");
    sendPublishRequest("order_deleted");
    Thread.sleep(4000);
  }

  private void sendPublishRequest(String type) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    String request = mapper.writeValueAsString(getTestRequest(type));
    postWithStatus("/publish", request, 204);
  }

  private TestRequest getTestRequest(String type) {
    return new TestRequest()
      .withValue(type + " payload")
      .withEventType(type);
  }
}
