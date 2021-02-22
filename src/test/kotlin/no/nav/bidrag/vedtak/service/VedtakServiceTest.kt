package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@DisplayName("VedtakServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VedtakServiceTest {

  @Autowired
  private lateinit var vedtakService: VedtakService

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal opprette nytt vedtak`() {
    // Oppretter nytt vedtak
    val nyttVedtakRequest = NyttVedtakRequest("TEST", "1111")
    val nyttVedtakOpprettet = vedtakService.oprettNyttVedtak(nyttVedtakRequest)

    assertAll(
      Executable { assertThat(nyttVedtakOpprettet).isNotNull() },
      Executable { assertThat(nyttVedtakOpprettet.opprettet_av).isEqualTo(nyttVedtakRequest.opprettet_av) },
      Executable { assertThat(nyttVedtakOpprettet.enhetsnummer).isEqualTo(nyttVedtakRequest.enhetsnummer) }
    )
  }

  @Test
  fun `skal finne data for vedtak`() {
    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.lagreVedtak(VedtakDto(opprettet_av = "TEST", enhetsnummer = "1111"))

    // Finner vedtaket som akkurat ble opprettet
    val vedtakFunnet = vedtakService.finnVedtak(nyttVedtakOpprettet.vedtak_id)

    assertAll(
      Executable { assertThat(vedtakFunnet).isNotNull() },
      Executable { assertThat(vedtakFunnet.vedtak_id).isEqualTo(nyttVedtakOpprettet.vedtak_id) },
      Executable { assertThat(vedtakFunnet.opprettet_av).isEqualTo(nyttVedtakOpprettet.opprettet_av) },
      Executable { assertThat(vedtakFunnet.enhetsnummer).isEqualTo(nyttVedtakOpprettet.enhetsnummer) }
    )
  }
}
