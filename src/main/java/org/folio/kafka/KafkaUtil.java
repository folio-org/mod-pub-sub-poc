package org.folio.kafka;

public class KafkaUtil {

  private KafkaUtil() {}

  public static String getTopicName(String tenant, String eventType) {
    return tenant + "." + eventType;
  }

}
