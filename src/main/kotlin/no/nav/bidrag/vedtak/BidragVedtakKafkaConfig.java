package no.nav.bidrag.vedtak;

import static no.nav.bidrag.vedtak.BidragVedtakProfiles.LOCAL;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@Profile("!" + LOCAL)
public class BidragVedtakKafkaConfig {

  @Bean
  public VedtakKafkaEventProducer vedtakKafkaEventProducer(
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      @Value("${TOPIC_VEDTAK}") String topic
  ) {
    return new VedtakKafkaEventProducer(kafkaTemplate, objectMapper, topic);
  }
}
