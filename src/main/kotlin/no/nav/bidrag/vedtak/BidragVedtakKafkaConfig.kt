package no.nav.bidrag.vedtak.no.nav.bidrag.vedtak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.vedtak.BidragVedtakProfiles.LOCAL
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.KafkaTemplate

@Configuration
@Profile(
  "!$LOCAL"
)
class BidragVedtakKafkaConfig {
  @Bean
  fun vedtakKafkaEventProducer(
    kafkaTemplate: KafkaTemplate<String?, String?>?,
    objectMapper: ObjectMapper,
    @Value("\${TOPIC_VEDTAK}") topic: String
  ): VedtakKafkaEventProducer {
    return VedtakKafkaEventProducer(kafkaTemplate, objectMapper, topic)
  }
}