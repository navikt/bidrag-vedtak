package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.StonadRepository
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
  private lateinit var stonadRepository: StonadRepository

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    stonadRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal opprette nytt vedtak`() {
    // Oppretter nytt vedtak
    val nyttVedtakRequest = NyttVedtakRequest("TEST", "1111")
    val nyttVedtakOpprettet = vedtakService.opprettNyttVedtak(nyttVedtakRequest)

    assertAll(
      Executable { assertThat(nyttVedtakOpprettet).isNotNull() },
      Executable { assertThat(nyttVedtakOpprettet.opprettetAv).isEqualTo(nyttVedtakRequest.opprettetAv) },
      Executable { assertThat(nyttVedtakOpprettet.enhetsnummer).isEqualTo(nyttVedtakRequest.enhetsnummer) }
    )
  }

  @Test
  fun `skal finne data for ett vedtak`() {
    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Finner vedtaket som akkurat ble opprettet
    val vedtakFunnet = vedtakService.finnEttVedtak(nyttVedtakOpprettet.vedtakId)

    assertAll(
      Executable { assertThat(vedtakFunnet).isNotNull() },
      Executable { assertThat(vedtakFunnet.vedtakId).isEqualTo(nyttVedtakOpprettet.vedtakId) },
      Executable { assertThat(vedtakFunnet.opprettetAv).isEqualTo(nyttVedtakOpprettet.opprettetAv) },
      Executable { assertThat(vedtakFunnet.enhetsnummer).isEqualTo(nyttVedtakOpprettet.enhetsnummer) }
    )
  }

  @Test
  fun `skal finne data for alle vedtak`() {
    // Oppretter nye vedtak
    val nyttVedtakOpprettet1 = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))
    val nyttVedtakOpprettet2 = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "2222"))

    // Finner begge vedtakene som akkurat ble opprettet
    val vedtakFunnet = vedtakService.finnAlleVedtak()

    assertAll(
      Executable { assertThat(vedtakFunnet).isNotNull() },
      Executable { assertThat(vedtakFunnet.alleVedtak).isNotNull() },
      Executable { assertThat(vedtakFunnet.alleVedtak.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.alleVedtak[0]).isNotNull() },
      Executable { assertThat(vedtakFunnet.alleVedtak[0].vedtakId).isEqualTo(nyttVedtakOpprettet1.vedtakId) },
      Executable { assertThat(vedtakFunnet.alleVedtak[0].opprettetAv).isEqualTo(nyttVedtakOpprettet1.opprettetAv) },
      Executable { assertThat(vedtakFunnet.alleVedtak[0].enhetsnummer).isEqualTo(nyttVedtakOpprettet1.enhetsnummer) },
      Executable { assertThat(vedtakFunnet.alleVedtak[1]).isNotNull() },
      Executable { assertThat(vedtakFunnet.alleVedtak[1].vedtakId).isEqualTo(nyttVedtakOpprettet2.vedtakId) },
      Executable { assertThat(vedtakFunnet.alleVedtak[1].opprettetAv).isEqualTo(nyttVedtakOpprettet2.opprettetAv) },
      Executable { assertThat(vedtakFunnet.alleVedtak[1].enhetsnummer).isEqualTo(nyttVedtakOpprettet2.enhetsnummer) }
    )
  }
}
