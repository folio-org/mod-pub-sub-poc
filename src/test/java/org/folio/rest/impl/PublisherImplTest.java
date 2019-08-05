package org.folio.rest.impl;

import org.folio.rest.TestBase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class PublisherImplTest extends TestBase {

  @Test
  @Ignore("This test requires Kafka to be running on configured host/port")
  public void test() throws InterruptedException {
    String testRequest = "{\n" +
      "  \"value\" : \"test payload\"\n" +
      ", \"eventType\": \"order_created\"}";
    postWithStatus("/publish", testRequest, 204);
    Thread.sleep(4000);
  }
}
