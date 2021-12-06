package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettKomplettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettKomplettStonadsendringRequest
import no.nav.bidrag.vedtak.api.vedtak.OpprettKomplettVedtakRequest
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.bidrag.vedtak.model.VedtakHendelse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DisplayName("HendelserServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class])
class HendelserServiceTest {

  @Autowired
  private lateinit var hendelserService: HendelserService

  @MockBean
  private lateinit var vedtakEventProducerMock: VedtakKafkaEventProducer

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal ikke opprette hendelser når ingen stønadsendringer er del av request`() {
    hendelserService.opprettHendelse(OpprettKomplettVedtakRequest(), 1, LocalDateTime.now())

    verify(vedtakEventProducerMock, never()).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette en hendelser når en stønadsendring er del av request`() {
    hendelserService.opprettHendelse(OpprettKomplettVedtakRequest(stonadsendringListe = listOf(
      OpprettKomplettStonadsendringRequest()
    )), 1, LocalDateTime.now())

    verify(vedtakEventProducerMock).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette en hendelser med skyldner-id`() {
    hendelserService.opprettHendelse(OpprettKomplettVedtakRequest(stonadsendringListe = listOf(
      OpprettKomplettStonadsendringRequest(
        skyldnerId = "1"
      )
    )), 0, LocalDateTime.parse("2021-07-06T09:31:25.007971200"))

    verify(vedtakEventProducerMock).publish(VedtakHendelse(
      skyldnerId = "1", opprettetTimestamp = LocalDateTime.parse("2021-07-06T09:31:25.007971200")))
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal ikke opprette hendelse ved engangsbeløp SAERTILSKUDD`() {
    hendelserService.opprettHendelse(OpprettKomplettVedtakRequest(engangsbelopListe = listOf(
      OpprettKomplettEngangsbelopRequest(
        type = "SAERTILSKUDD"
      )
    )), 1, LocalDateTime.now())
    verify(vedtakEventProducerMock, never()).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal kun opprette hendelse ved stønadsendring og ikke for engangsbeløp`() {
    hendelserService.opprettHendelse(OpprettKomplettVedtakRequest(engangsbelopListe = listOf(
      OpprettKomplettEngangsbelopRequest(
        type = "SAERTILSKUDD"
      )
    ), stonadsendringListe = listOf(
      OpprettKomplettStonadsendringRequest(
        skyldnerId = "1"
      ))), 0, LocalDateTime.parse("2021-07-06T09:31:25.007971200"))
    verify(vedtakEventProducerMock).publish(VedtakHendelse(
      skyldnerId = "1", opprettetTimestamp = LocalDateTime.parse("2021-07-06T09:31:25.007971200")))
  }

}