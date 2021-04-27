package no.nav.bidrag.vedtak.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.api.NyStonadsendringRequest
import no.nav.bidrag.vedtak.api.NyttGrunnlagRequest
import no.nav.bidrag.vedtak.api.NyttPeriodeGrunnlagRequest
import no.nav.bidrag.vedtak.api.NyttVedtakRequest
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
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
import java.math.BigDecimal
import java.time.LocalDate

@DisplayName("PeriodeGrunnlagServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PeriodeGrunnlagServiceTest {

  @Autowired
  private lateinit var periodeGrunnlagService: PeriodeGrunnlagService

  @Autowired
  private lateinit var grunnlagService: GrunnlagService

  @Autowired
  private lateinit var periodeService: PeriodeService

  @Autowired
  private lateinit var stonadsendringService: StonadsendringService

  @Autowired
  private lateinit var vedtakService: VedtakService

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

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
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal opprette nytt periodeGrunnlag`() {
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

    // Oppretter nytt grunnlag
    val mapper = ObjectMapper()
    val jsonString = """{"Grunnlag 1":"Verdi 1","Grunnlag 2":"Verdi 2"}"""

    val nyttGrunnlagRequest = NyttGrunnlagRequest(
      grunnlagReferanse = "",
      vedtakId = nyttVedtakOpprettet.vedtakId,
      grunnlagType = "Beregnet Inntekt",
      grunnlagInnhold = mapper.readTree(jsonString)
    )
    val grunnlagOpprettet = grunnlagService.opprettNyttGrunnlag(nyttGrunnlagRequest)

    // Oppretter ny periode
    val nyPeriodeRequest = NyPeriodeRequest(
      LocalDate.now(), LocalDate.now(), nyStonadsendringOpprettet.stonadsendringId,
      BigDecimal.valueOf(17), "NOK", "RESULTATKODE_TEST"
    )
    val nyPeriodeOpprettet = periodeService.opprettNyPeriode(nyPeriodeRequest)

    // Oppretter nytt periodegrunnlag
    val nyttPeriodeGrunnlagRequest = NyttPeriodeGrunnlagRequest(
      nyPeriodeOpprettet.periodeId,
      grunnlagOpprettet.grunnlagId,
      true
    )
    val nyttPeriodeGrunnlagOpprettet = periodeGrunnlagService.opprettNyttPeriodeGrunnlag(nyttPeriodeGrunnlagRequest)

    assertAll(
      Executable { assertThat(nyttPeriodeGrunnlagOpprettet).isNotNull() },
      Executable { assertThat(nyttPeriodeGrunnlagOpprettet.periodeId).isEqualTo(nyttPeriodeGrunnlagRequest.periodeId) },
      Executable { assertThat(nyttPeriodeGrunnlagOpprettet.grunnlagId).isEqualTo(nyttPeriodeGrunnlagRequest.grunnlagId) },
      Executable { assertThat(nyttPeriodeGrunnlagOpprettet.grunnlagValgt).isEqualTo(nyttPeriodeGrunnlagRequest.grunnlagValgt) }
    )
  }

  @Test
  fun `skal finne data for et periodegrunnlag`() {
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

    // Oppretter ny periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST"
      )
    )

    // Oppretter nytt grunnlag
    val nyttGrunnlagOpprettet = persistenceService.opprettNyttGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        grunnlagType = "Beregnet Inntekt",
        grunnlagInnhold = "100"
      )
    )

    // Oppretter nytt periodegrunnlag
    val nyttPeriodeGrunnlagOpprettet = persistenceService.opprettNyttPeriodeGrunnlag(
      PeriodeGrunnlagDto(
        nyPeriodeOpprettet.periodeId,
        nyttGrunnlagOpprettet.grunnlagId,
        true
      )
    )

    // Finner periodegrunnlag som akkurat ble opprettet
    val periodeGrunnlagFunnet = periodeGrunnlagService.hentPeriodeGrunnlag(
      nyttPeriodeGrunnlagOpprettet.periodeId, nyttPeriodeGrunnlagOpprettet.grunnlagId)

    assertAll(
      Executable { assertThat(periodeGrunnlagFunnet).isNotNull() },
      Executable { assertThat(periodeGrunnlagFunnet.periodeId).isEqualTo(nyttPeriodeGrunnlagOpprettet.periodeId) },
      Executable { assertThat(periodeGrunnlagFunnet.grunnlagId).isEqualTo(nyttPeriodeGrunnlagOpprettet.grunnlagId) },
      Executable { assertThat(periodeGrunnlagFunnet.grunnlagValgt).isEqualTo(nyttPeriodeGrunnlagOpprettet.grunnlagValgt) }
    )
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal finne alle periodegrunnlag for en periode`() {

    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettNyttVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

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

    // Oppretter ny periode
    val nyPeriodeOpprettet = persistenceService.opprettNyPeriode(
      PeriodeDto(
        periodeFomDato = LocalDate.now(),
        periodeTilDato = LocalDate.now(),
        stonadsendringId = nyStonadsendringOpprettet.stonadsendringId,
        belop = BigDecimal.valueOf(17.01),
        valutakode = "NOK",
        resultatkode = "RESULTATKODE_TEST"
      )
    )

    // Oppretter nye grunnlag
    val nyttGrunnlagDtoListe = mutableListOf<GrunnlagDto>()

    nyttGrunnlagDtoListe.add(
      persistenceService.opprettNyttGrunnlag(
        GrunnlagDto(
          grunnlagReferanse = "",
          vedtakId = nyttVedtakOpprettet.vedtakId,
          grunnlagType = "Beregnet Inntekt",
          grunnlagInnhold = "100")
      )
    )

    nyttGrunnlagDtoListe.add(
      persistenceService.opprettNyttGrunnlag(
        GrunnlagDto(
          grunnlagReferanse = "",
          vedtakId = nyttVedtakOpprettet.vedtakId,
          grunnlagType = "Beregnet Skatt",
          grunnlagInnhold = "10")
      )
    )

    // Oppretter nye periodegrunnlag
    val nyttPeriodegrunnlagtoListe = mutableListOf<PeriodeGrunnlagDto>()

    nyttPeriodegrunnlagtoListe.add(
      persistenceService.opprettNyttPeriodeGrunnlag(
        PeriodeGrunnlagDto(
          nyPeriodeOpprettet.periodeId,
          nyttGrunnlagDtoListe[0].grunnlagId,
          true)
      )
    )

    nyttPeriodegrunnlagtoListe.add(
      persistenceService.opprettNyttPeriodeGrunnlag(
        PeriodeGrunnlagDto(
          nyPeriodeOpprettet.periodeId,
          nyttGrunnlagDtoListe[1].grunnlagId,
          true)
      )
    )

    // Finner begge periodegrunnlagene som akkurat ble opprettet
    val periodeId = nyPeriodeOpprettet.periodeId
    val periodegrunnlagFunnet = periodeGrunnlagService.hentAlleGrunnlagForPeriode(periodeId)

    assertAll(
      Executable { assertThat(periodegrunnlagFunnet).isNotNull() },
      Executable { assertThat(periodegrunnlagFunnet.alleGrunnlagForPeriode).isNotNull() },
      Executable { assertThat(periodegrunnlagFunnet.alleGrunnlagForPeriode.size).isEqualTo(2) },
      Executable {
        periodegrunnlagFunnet.alleGrunnlagForPeriode.forEachIndexed { index, periodeGrunnlag ->
          assertAll(
            Executable { assertThat(periodeGrunnlag.periodeId).isEqualTo(nyttPeriodegrunnlagtoListe[index].periodeId) },
            Executable { assertThat(periodeGrunnlag.grunnlagId).isEqualTo(nyttPeriodegrunnlagtoListe[index].grunnlagId) },
            Executable { assertThat(periodeGrunnlag.grunnlagValgt).isEqualTo(nyttPeriodegrunnlagtoListe[index].grunnlagValgt) },
          )
        }
      }
    )
    periodeGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }
}