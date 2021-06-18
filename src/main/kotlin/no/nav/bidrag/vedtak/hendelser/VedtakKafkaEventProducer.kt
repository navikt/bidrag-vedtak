package no.nav.bidrag.vedtak.hendelser

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.vedtak.model.VedtakHendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class VedtakKafkaEventProducer(
  private val kafkaTemplate: KafkaTemplate<String, String>,
  private val objectMapper: ObjectMapper,
 @Value("TOPIC_VEDTAK") private val topic: String
) {

  fun publish(vedtakHendelse: VedtakHendelse) {
    try {
      kafkaTemplate.send(
        topic,
        vedtakHendelse.stonadType,
        objectMapper.writeValueAsString(vedtakHendelse)
      )
    } catch (e: JsonProcessingException) {
      throw IllegalStateException(e.message, e)
    }
  }

}