package org.folio.kafka.listener;

import org.folio.rest.impl.PublishImpl;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Component
public class Listener {
  private static final Logger LOG = LoggerFactory.getLogger(PublishImpl.class);

  @KafkaListener(id = "foo", topicPattern= ".*", groupId = "pub-sub")
  public void listen(String data, MessageHeaders headers) {
    LOG.info(data);
  }

}
