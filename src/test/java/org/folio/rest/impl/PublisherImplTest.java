package org.folio.rest.impl;

import org.folio.rest.TestBase;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class PublisherImplTest extends TestBase {

  @Test
  public void test() {
    String testRequest = "{\n" +
      "  \"value\" : \"test payload\"\n" +
      ", \"eventType\": \"order_created\"}";
    postWithStatus("/publish", testRequest, 204);
  }
}
