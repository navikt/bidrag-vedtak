package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.periode.OpprettKomplettPeriodeRequest
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettKomplettStonadsendringRequest
import no.nav.bidrag.vedtak.api.vedtak.OpprettKomplettVedtakRequest
import no.nav.bidrag.vedtak.hendelser.VedtakKafkaEventProducer
import no.nav.bidrag.vedtak.model.VedtakHendelse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.kotlin.anyOrNull
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

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
    hendelserService.opprettHendelse(OpprettKomplettVedtakRequest())

    verify(vedtakEventProducerMock, never()).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette en hendelser når en stønadsendring er del av request`() {
    hendelserService.opprettHendelse(OpprettKomplettVedtakRequest(stonadsendringListe = listOf(
      OpprettKomplettStonadsendringRequest()
    )))

    verify(vedtakEventProducerMock).publish(anyOrNull())
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette en hendelser med skyldner id`() {
    hendelserService.opprettHendelse(OpprettKomplettVedtakRequest(stonadsendringListe = listOf(
      OpprettKomplettStonadsendringRequest(
        skyldnerId = "1"
      )
    )))

    verify(vedtakEventProducerMock).publish(VedtakHendelse(skyldnerId = "1"))
  }
}
