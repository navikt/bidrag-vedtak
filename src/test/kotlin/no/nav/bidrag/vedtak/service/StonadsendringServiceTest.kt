package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.NyStonadsendringRequest
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.StonadsendringRepository
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

@DisplayName("StonadsendringServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StonadsendringServiceTest {

  @Autowired
  private lateinit var stonadsendringService: StonadsendringService

  @Autowired
  private lateinit var vedtakService: VedtakService

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal opprette ny stonadsendring`() {
    // Oppretter nytt vedtak
    val nyttVedtakRequest = NyttVedtakRequest("TEST", "1111")
    val nyttVedtakOpprettet = vedtakService.opprettNyttVedtak(nyttVedtakRequest)

    // Oppretter ny stønadsendring
    val nyStonadsendringRequest = NyStonadsendringRequest("BIDRAG", nyttVedtakOpprettet.vedtakId, "1111", "1111", "1111", "1111", "TEST")
    val nyStonadsendringOpprettet = stonadsendringService.opprettNyStonadsendring(nyStonadsendringRequest)

    assertAll(
      Executable { assertThat(nyStonadsendringOpprettet).isNotNull() },
      Executable { assertThat(nyStonadsendringOpprettet.stonadType).isEqualTo(nyStonadsendringRequest.stonadType) },
      Executable { assertThat(nyStonadsendringOpprettet.vedtakId).isEqualTo(nyStonadsendringRequest.vedtakId) },
      Executable { assertThat(nyStonadsendringOpprettet.behandlingId).isEqualTo(nyStonadsendringRequest.behandlingId) },
      Executable { assertThat(nyStonadsendringOpprettet.opprettetAv).isEqualTo(nyStonadsendringRequest.opprettetAv) }
    )
  }

  @Test
  fun `skal finne data for en stonadsendring`() {
    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter ny stønadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        opprettetAv = "TEST"
      )
    )

    // Finner stønadsendringen som akkurat ble opprettet
    val stonadsendringFunnet = stonadsendringService.finnEnStonadsendring(nyStonadsendringOpprettet.stonadsendringId)

    assertAll(
      Executable { assertThat(stonadsendringFunnet).isNotNull() },
      Executable { assertThat(stonadsendringFunnet.stonadsendringId).isEqualTo(nyStonadsendringOpprettet.stonadsendringId) },
      Executable { assertThat(stonadsendringFunnet.stonadType).isEqualTo(nyStonadsendringOpprettet.stonadType) },
      Executable { assertThat(stonadsendringFunnet.vedtakId).isEqualTo(nyStonadsendringOpprettet.vedtakId) },
      Executable { assertThat(stonadsendringFunnet.behandlingId).isEqualTo(nyStonadsendringOpprettet.behandlingId) },
      Executable { assertThat(stonadsendringFunnet.opprettetAv).isEqualTo(nyStonadsendringOpprettet.opprettetAv) }
    )
  }

  @Test
  fun `skal finne data for alle stonadsendringer`() {
    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(opprettetAv = "TEST", enhetsnummer = "1111"))

    // Oppretter nye stønadsendringer
    val nyStonadsendringDtoListe = mutableListOf<StonadsendringDto>()

    nyStonadsendringDtoListe.add(
      persistenceService.opprettNyStonadsendring(
        StonadsendringDto(
          stonadType = "BIDRAG",
          vedtakId = nyttVedtakOpprettet.vedtakId,
          behandlingId = "1111",
          skyldnerId = "1111",
          kravhaverId = "1111",
          mottakerId = "1111",
          opprettetAv = "TEST"
        )
      )
    )

    nyStonadsendringDtoListe.add(
      persistenceService.opprettNyStonadsendring(
        StonadsendringDto(
          stonadType = "BIDRAG",
          vedtakId = nyttVedtakOpprettet.vedtakId,
          behandlingId = "2222",
          skyldnerId = "2222",
          kravhaverId = "2222",
          mottakerId = "2222",
          opprettetAv = "TEST"
        )
      )
    )

    // Finner begge stønadsendringene som akkurat ble opprettet
    val stonadsendringFunnet = stonadsendringService.finnAlleStonadsendringer()

    assertAll(
      Executable { assertThat(stonadsendringFunnet).isNotNull() },
      Executable { assertThat(stonadsendringFunnet.alleStonadsendringer).isNotNull() },
      Executable { assertThat(stonadsendringFunnet.alleStonadsendringer.size).isEqualTo(2) },
      Executable {
        stonadsendringFunnet.alleStonadsendringer.forEachIndexed { index, stonadsendring ->
          assertAll(
            Executable { assertThat(stonadsendring.stonadsendringId).isEqualTo(nyStonadsendringDtoListe[index].stonadsendringId) },
            Executable { assertThat(stonadsendring.stonadType).isEqualTo(nyStonadsendringDtoListe[index].stonadType) },
            Executable { assertThat(stonadsendring.vedtakId).isEqualTo(nyStonadsendringDtoListe[index].vedtakId) },
            Executable { assertThat(stonadsendring.behandlingId).isEqualTo(nyStonadsendringDtoListe[index].behandlingId) },
            Executable { assertThat(stonadsendring.opprettetAv).isEqualTo(nyStonadsendringDtoListe[index].opprettetAv) }
          )
        }
      }
    )
  }
}
