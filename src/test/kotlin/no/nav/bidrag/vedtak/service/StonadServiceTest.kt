package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.NyStonadRequest
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.StonadDto
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

@DisplayName("StonadServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StonadServiceTest {

  @Autowired
  private lateinit var stonadService: StonadService

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
  fun `skal opprette ny stonad`() {
    // Oppretter nytt vedtak
    val nyttVedtakRequest = NyttVedtakRequest("TEST", "1111")
    val nyttVedtakOpprettet = vedtakService.opprettNyttVedtak(nyttVedtakRequest)

    // Oppretter ny stonad
    val nyStonadRequest = NyStonadRequest("BIDRAG", nyttVedtakOpprettet.vedtakId, "1111", "1111", "1111", "1111", "TEST", 1111)
    val nyStonadOpprettet = stonadService.opprettNyStonad(nyStonadRequest)

    assertAll(
      Executable { assertThat(nyStonadOpprettet).isNotNull() },
      Executable { assertThat(nyStonadOpprettet.stonadType).isEqualTo(nyStonadRequest.stonadType) },
      Executable { assertThat(nyStonadOpprettet.vedtakId).isEqualTo(nyStonadRequest.vedtakId) },
      Executable { assertThat(nyStonadOpprettet.behandlingId).isEqualTo(nyStonadRequest.behandlingId) },
      Executable { assertThat(nyStonadOpprettet.opprettetAv).isEqualTo(nyStonadRequest.opprettetAv) },
      Executable { assertThat(nyStonadOpprettet.enhetsnummer).isEqualTo(nyStonadRequest.enhetsnummer) }
    )
  }

  @Test
  fun `skal finne data for en stonad`() {
    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter ny stonad
    val nyStonadOpprettet = persistenceService.opprettNyStonad(
      StonadDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        opprettetAv = "TEST",
        enhetsnummer = 1111
      )
    )

    // Finner stonaden som akkurat ble opprettet
    val stonadFunnet = stonadService.finnEnStonad(nyStonadOpprettet.stonadId)

    assertAll(
      Executable { assertThat(stonadFunnet).isNotNull() },
      Executable { assertThat(stonadFunnet.stonadId).isEqualTo(nyStonadOpprettet.stonadId) },
      Executable { assertThat(stonadFunnet.stonadType).isEqualTo(nyStonadOpprettet.stonadType) },
      Executable { assertThat(stonadFunnet.vedtakId).isEqualTo(nyStonadOpprettet.vedtakId) },
      Executable { assertThat(stonadFunnet.behandlingId).isEqualTo(nyStonadOpprettet.behandlingId) },
      Executable { assertThat(stonadFunnet.opprettetAv).isEqualTo(nyStonadOpprettet.opprettetAv) },
      Executable { assertThat(stonadFunnet.enhetsnummer).isEqualTo(nyStonadOpprettet.enhetsnummer) }
    )
  }

  @Test
  fun `skal finne data for alle stonader`() {
    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter nye stønader
    val nyStonadDtoListe = mutableListOf<StonadDto>()

    nyStonadDtoListe.add(
      persistenceService.opprettNyStonad(
        StonadDto(
          stonadType = "BIDRAG",
          vedtakId = nyttVedtakOpprettet.vedtakId,
          behandlingId = "1111",
          skyldnerId = "1111",
          kravhaverId = "1111",
          mottakerId = "1111",
          opprettetAv = "TEST",
          enhetsnummer = 1111
        )
      )
    )

    nyStonadDtoListe.add(
      persistenceService.opprettNyStonad(
        StonadDto(
          stonadType = "BIDRAG",
          vedtakId = nyttVedtakOpprettet.vedtakId,
          behandlingId = "2222",
          skyldnerId = "2222",
          kravhaverId = "2222",
          mottakerId = "2222",
          opprettetAv = "TEST",
          enhetsnummer = 2222
        )
      )
    )

    // Finner begge stonadene som akkurat ble opprettet
    val stonadFunnet = stonadService.finnAlleStonader()

    assertAll(
      Executable { assertThat(stonadFunnet).isNotNull() },
      Executable { assertThat(stonadFunnet.alleStonader).isNotNull() },
      Executable { assertThat(stonadFunnet.alleStonader.size).isEqualTo(2) },
      Executable {
        stonadFunnet.alleStonader.forEachIndexed { index, stonad ->
          assertAll(
            Executable { assertThat(stonad.stonadId).isEqualTo(nyStonadDtoListe[index].stonadId) },
            Executable { assertThat(stonad.stonadType).isEqualTo(nyStonadDtoListe[index].stonadType) },
            Executable { assertThat(stonad.vedtakId).isEqualTo(nyStonadDtoListe[index].vedtakId) },
            Executable { assertThat(stonad.behandlingId).isEqualTo(nyStonadDtoListe[index].behandlingId) },
            Executable { assertThat(stonad.opprettetAv).isEqualTo(nyStonadDtoListe[index].opprettetAv) },
            Executable { assertThat(stonad.enhetsnummer).isEqualTo(nyStonadDtoListe[index].enhetsnummer) }
          )
        }
      }
    )
  }
}
