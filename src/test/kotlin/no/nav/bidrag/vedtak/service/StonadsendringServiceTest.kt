package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.NyStonadsendringRequest
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
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
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var periodeRepository: PeriodeRepository

  @Autowired
  private lateinit var stonadsendringRepository: StonadsendringRepository

  @Autowired
  private lateinit var vedtakRepository: VedtakRepository

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal opprette ny stonadsendring`() {
    // Oppretter nytt vedtak
    val nyttVedtakRequest = NyttVedtakRequest("TEST", "1111")
    val nyttVedtakOpprettet = vedtakService.opprettNyttVedtak(nyttVedtakRequest)

    // Oppretter ny stønadsendring
    val nyStonadsendringRequest = NyStonadsendringRequest(
      "BIDRAG",
      nyttVedtakOpprettet.vedtakId,
      "1111",
      "1111",
      "1111",
      "1111"
    )
    val nyStonadsendringOpprettet = stonadsendringService.opprettNyStonadsendring(nyStonadsendringRequest)

    assertAll(
      Executable { assertThat(nyStonadsendringOpprettet).isNotNull() },
      Executable { assertThat(nyStonadsendringOpprettet.stonadType).isEqualTo(nyStonadsendringRequest.stonadType) },
      Executable { assertThat(nyStonadsendringOpprettet.vedtakId).isEqualTo(nyStonadsendringRequest.vedtakId) },
      Executable { assertThat(nyStonadsendringOpprettet.behandlingId).isEqualTo(nyStonadsendringRequest.behandlingId) }
    )
  }

  @Test
  fun `skal finne data for en stonadsendring`() {
    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny stønadsendring
    val nyStonadsendringOpprettet = persistenceService.opprettNyStonadsendring(
      StonadsendringDto(
        stonadType = "BIDRAG",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        behandlingId = "1111",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111"
      )
    )

    // Finner stønadsendringen som akkurat ble opprettet
    val stonadsendringFunnet = stonadsendringService.finnEnStonadsendring(nyStonadsendringOpprettet.stonadsendringId)

    assertAll(
      Executable { assertThat(stonadsendringFunnet).isNotNull() },
      Executable { assertThat(stonadsendringFunnet.stonadsendringId).isEqualTo(nyStonadsendringOpprettet.stonadsendringId) },
      Executable { assertThat(stonadsendringFunnet.stonadType).isEqualTo(nyStonadsendringOpprettet.stonadType) },
      Executable { assertThat(stonadsendringFunnet.vedtakId).isEqualTo(nyStonadsendringOpprettet.vedtakId) },
      Executable { assertThat(stonadsendringFunnet.behandlingId).isEqualTo(nyStonadsendringOpprettet.behandlingId) }
    )
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal finne alle stonadsendringer for et vedtak`() {

    // Oppretter nytt vedtak
    val nyttVedtakOpprettet1 = persistenceService.opprettNyttVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))
    val nyttVedtakOpprettet2 = persistenceService.opprettNyttVedtak(VedtakDto(17, saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter nye stønadsendringer
    val nyStonadsendringDtoListe = mutableListOf<StonadsendringDto>()

    nyStonadsendringDtoListe.add(
      persistenceService.opprettNyStonadsendring(
        StonadsendringDto(
          stonadType = "BIDRAG",
          vedtakId = nyttVedtakOpprettet1.vedtakId,
          behandlingId = "1111",
          skyldnerId = "1111",
          kravhaverId = "1111",
          mottakerId = "1111"
        )
      )
    )

    nyStonadsendringDtoListe.add(
      persistenceService.opprettNyStonadsendring(
        StonadsendringDto(
          stonadType = "BIDRAG",
          vedtakId = nyttVedtakOpprettet1.vedtakId,
          behandlingId = "2222",
          skyldnerId = "2222",
          kravhaverId = "2222",
          mottakerId = "2222"
        )
      )
    )

    // Legger til en ekstra stonadsendring som ikke skal bli funnet pga annen vedtakId
    nyStonadsendringDtoListe.add(
      persistenceService.opprettNyStonadsendring(
        StonadsendringDto(
          stonadType = "BIDRAG",
          vedtakId = nyttVedtakOpprettet2.vedtakId,
          behandlingId = "9999",
          skyldnerId = "9999",
          kravhaverId = "9999",
          mottakerId = "9999"
        )
      )
    )

    // Finner begge stønadsendringene som akkurat ble opprettet
    val vedtakId = nyttVedtakOpprettet1.vedtakId
    val stonadsendringFunnet = stonadsendringService.finnAlleStonadsendringerForVedtak(vedtakId)

    assertAll(
      Executable { assertThat(stonadsendringFunnet).isNotNull() },
      Executable { assertThat(stonadsendringFunnet.alleStonadsendringerForVedtak).isNotNull() },
      Executable { assertThat(stonadsendringFunnet.alleStonadsendringerForVedtak.size).isEqualTo(2) },
      Executable {
        stonadsendringFunnet.alleStonadsendringerForVedtak.forEachIndexed { index, stonadsendring ->
          assertAll(
            Executable { assertThat(stonadsendring.stonadsendringId).isEqualTo(nyStonadsendringDtoListe[index].stonadsendringId) },
            Executable { assertThat(stonadsendring.stonadType).isEqualTo(nyStonadsendringDtoListe[index].stonadType) },
            Executable { assertThat(stonadsendring.vedtakId).isEqualTo(nyStonadsendringDtoListe[index].vedtakId) },
            Executable { assertThat(stonadsendring.behandlingId).isEqualTo(nyStonadsendringDtoListe[index].behandlingId) }
          )
        }
      }
    )
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }
}
