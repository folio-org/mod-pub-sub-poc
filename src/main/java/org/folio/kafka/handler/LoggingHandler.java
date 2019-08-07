package org.folio.kafka.handler;

import org.springframework.stereotype.Component;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;

@Component
public class LoggingHandler implements Handler<KafkaConsumerRecord<String, String>> {

  private static final Logger LOG = LoggerFactory.getLogger(LoggingHandler.class);
  @Override
  public void handle(KafkaConsumerRecord<String, String> record) {
    LOG.info(record.value());
  }
}
