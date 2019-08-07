package org.folio.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.folio.kafka.PubSubConsumerConfig;
import org.folio.kafka.handler.LoggingHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;

@Configuration
@ComponentScan(basePackages = {"org.folio.kafka.handler"})
@PropertySource("classpath:application.properties")
public class ApplicationConfig {

  @Bean
  public PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public List<KafkaConsumer<String, String>> kafkaConsumers(@Value("${topic.tenant}") String tenant,
                                                            @Value("#{'${topic.types}'.split(',')}") List<String> eventTypes,
                                                            @Value("${KAFKA_HOST}") String kafkaHost,
                                                            @Value("${KAFKA_PORT}") String kafkaPort,
                                                            LoggingHandler loggingHandler, Vertx vertx) {
    return eventTypes
      .stream().map(eventType ->
      {
        PubSubConsumerConfig pubSubConfig = new PubSubConsumerConfig(tenant, eventType);
        return KafkaConsumer.<String, String>create(vertx, consumerConfig(getUrl(kafkaHost, kafkaPort), pubSubConfig.getGroupId()))
          .subscribe(pubSubConfig.getTopicName())
          .handler(loggingHandler);
      })
      .collect(Collectors.toList());
  }

  @Bean
  public KafkaProducer kafkaProducer(@Value("${KAFKA_HOST}") String kafkaHost,
                                     @Value("${KAFKA_PORT}") String kafkaPort) {
    Map<String, String> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getUrl(kafkaHost, kafkaPort));
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    return KafkaProducer.createShared(Vertx.vertx(), "pub-sub-producer", props);
  }

  @Bean
  public AdminClient adminClient(@Value("${topic.tenant}") String tenant,
                                 @Value("#{'${topic.types}'.split(',')}") List<String> eventTypes,
                                 @Value("${KAFKA_HOST}") String kafkaHost,
                                 @Value("${KAFKA_PORT}") String kafkaPort) {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getUrl(kafkaHost, kafkaPort));
    AdminClient adminClient = AdminClient.create(configs);

    int numPartitions = 1;
    short replicationFactor = 1;

    List<NewTopic> topics = eventTypes.stream()
      .map(eventType -> new NewTopic(new PubSubConsumerConfig(tenant, eventType).getTopicName(), numPartitions, replicationFactor))
      .collect(Collectors.toList());

    adminClient.createTopics(topics);

    return adminClient;
  }

  private Map<String, String> consumerConfig(String bootstrapServerUrl, String groupId) {
    Map<String, String> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServerUrl);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    return props;
  }

  private String getUrl(String kafkaHost, String kafkaPort) {
    return kafkaHost + ":" + kafkaPort;
  }
}
