package org.folio.spring;

import static org.folio.kafka.KafkaUtil.getTopicName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.folio.kafka.handler.LoggingHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;

@Configuration
@ComponentScan(basePackages = {"org.folio.kafka.handler"})
public class ApplicationConfig {

  @Bean
  public PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
    configurer.setLocation(new ClassPathResource("application.properties"));
    return configurer;
  }

  @Bean
  public KafkaConsumer kafkaConsumer(@Value("${topic.tenant}") String tenant, @Value("${topic.type}") String eventType,
                                     @Value("${bootstrap.server.url}") String bootstrapServerUrl,
                                     @Value("${group.id}") String groupId,
                                     LoggingHandler loggingHandler, Vertx vertx) {
    Map<String, String> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerUrl);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = KafkaConsumer.create(vertx, props);
    return consumer
      .subscribe(getTopicName(tenant, eventType))
      .handler(loggingHandler);
  }

  @Bean
  public KafkaProducer kafkaProducer(@Value("${bootstrap.server.url}") String bootstrapServerUrl) {
    Map<String, String> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerUrl);
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    return KafkaProducer.createShared(Vertx.vertx(), "pub-sub-producer", props);
  }

  @Bean
  public AdminClient adminClient(@Value("${topic.tenant}") String tenant,
                                 @Value("${topic.type}") String eventType,
                                 @Value("${bootstrap.server.url}") String bootstrapServerUrl) {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerUrl);
    AdminClient adminClient = AdminClient.create(configs);

    String topicName = getTopicName(tenant, eventType);
    int numPartitions = 1;
    short replicationFactor = 1;
    adminClient.createTopics(Collections.singletonList(
      new NewTopic(topicName, numPartitions, replicationFactor)));

    return adminClient;
  }
}
