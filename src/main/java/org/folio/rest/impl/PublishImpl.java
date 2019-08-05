package org.folio.rest.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.folio.rest.jaxrs.model.TestRequest;
import org.folio.rest.jaxrs.resource.Publish;
import org.folio.rest.tools.utils.TenantTool;
import org.folio.spring.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class PublishImpl implements Publish {
  private static final Logger LOG = LoggerFactory.getLogger(PublishImpl.class);
  @Autowired
  private KafkaTemplate<Integer, String> kafkaTemplate;

  public PublishImpl() {
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
  }

  @Override
  public void postPublish(TestRequest entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    String tenantId = TenantTool.tenantId(okapiHeaders);
    kafkaTemplate.send( tenantId + "." + entity.getEventType(), entity.getValue());
    asyncResultHandler.handle(Future.succeededFuture(PublishImpl.PostPublishResponse.respond204()));
  }
}
