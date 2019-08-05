package org.folio.rest.impl;

import static org.folio.kafka.KafkaUtil.getTopicName;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.folio.rest.jaxrs.model.TestRequest;
import org.folio.rest.jaxrs.resource.Publish;
import org.folio.rest.tools.utils.TenantTool;
import org.folio.spring.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.impl.KafkaProducerRecordImpl;

public class PublishImpl implements Publish {
  private static final Logger LOG = LoggerFactory.getLogger(PublishImpl.class);
  @Autowired
  private KafkaProducer<String, String> producer;

  public PublishImpl() {
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
  }

  @Override
  public void postPublish(TestRequest entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) {
    String tenantId = TenantTool.tenantId(okapiHeaders);
    producer.write(new KafkaProducerRecordImpl<>(getTopicName(tenantId, entity.getEventType()), entity.getValue()));
    asyncResultHandler.handle(Future.succeededFuture(PublishImpl.PostPublishResponse.respond204()));
  }
}
