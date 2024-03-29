package no.nav.bidrag.vedtak.hendelser

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.transport.behandling.vedtak.VedtakHendelse
import org.springframework.kafka.core.KafkaTemplate

interface VedtakKafkaEventProducer {
    fun publish(vedtakHendelse: VedtakHendelse)
}

class DefaultVedtakKafkaEventProducer(
    private val kafkaTemplate: KafkaTemplate<String?, String?>?,
    private val objectMapper: ObjectMapper,
    private val topic: String,
) : VedtakKafkaEventProducer {

    override fun publish(vedtakHendelse: VedtakHendelse) {
        try {
            kafkaTemplate?.send(
                topic,
                vedtakHendelse.id.toString(),
                objectMapper.writeValueAsString(vedtakHendelse),
            )?.get()
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e.message, e)
        }
    }
}
