package no.nav.bidrag.vedtak.hendelser

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.vedtak.model.VedtakHendelse
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

interface VedtakKafkaEventProducer{
  fun publish(vedtakHendelse: VedtakHendelse)
}

class DefaultVedtakKafkaEventProducer(
  private val kafkaTemplate: KafkaTemplate<String?, String?>?,
  private val objectMapper: ObjectMapper,
  private val topic: String
): VedtakKafkaEventProducer {

  override fun publish(vedtakHendelse: VedtakHendelse) {
    try {
      kafkaTemplate?.send(
        topic,
        vedtakHendelse.stonadType,
        objectMapper.writeValueAsString(vedtakHendelse)
      )
    } catch (e: JsonProcessingException) {
      throw IllegalStateException(e.message, e)
    }
  }
}