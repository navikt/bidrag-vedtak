package no.nav.bidrag.vedtak.hendelser

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.transport.behandling.vedtak.VedtakHendelse
import no.nav.bidrag.transport.behandling.vedtak.VedtaksforslagHendelse
import org.springframework.kafka.core.KafkaTemplate

interface VedtakKafkaEventProducer {
    fun publishVedtak(vedtakHendelse: VedtakHendelse)
    fun publishVedtaksforslag(vedtaksforslagHendelse: VedtaksforslagHendelse)
}

class DefaultVedtakKafkaEventProducer(
    private val kafkaTemplate: KafkaTemplate<String?, String?>?,
    private val objectMapper: ObjectMapper,
    private val topicVedtak: String,
    private val topicVedtaksforslag: String,
) : VedtakKafkaEventProducer {

    override fun publishVedtak(vedtakHendelse: VedtakHendelse) {
        try {
            kafkaTemplate?.send(
                topicVedtak,
                vedtakHendelse.id.toString(),
                objectMapper.writeValueAsString(vedtakHendelse),
            )?.get()
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e.message, e)
        }
    }

    override fun publishVedtaksforslag(vedtaksforslagHendelse: VedtaksforslagHendelse) {
        try {
            kafkaTemplate?.send(
                topicVedtaksforslag,
                vedtaksforslagHendelse.vedtaksid.toString(),
                objectMapper.writeValueAsString(vedtaksforslagHendelse),
            )?.get()
        } catch (e: JsonProcessingException) {
            throw IllegalStateException(e.message, e)
        }
    }
}
