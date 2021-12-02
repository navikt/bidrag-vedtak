package no.nav.bidrag.vedtak.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.vedtak.BidragVedtakLocal
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.engangsbelopgrunnlag.OpprettEngangsbelopGrunnlagRequest
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.api.vedtak.OpprettVedtakRequest
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import no.nav.bidrag.vedtak.persistence.repository.BehandlingsreferanseRepository
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbelopRepository
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

@DisplayName("EngangsbelopGrunnlagServiceTest")
@ActiveProfiles(BidragVedtakLocal.TEST_PROFILE)
@SpringBootTest(classes = [BidragVedtakLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EngangsbelopGrunnlagServiceTest {

  @Autowired
  private lateinit var engangsbelopGrunnlagService: EngangsbelopGrunnlagService

  @Autowired
  private lateinit var grunnlagService: GrunnlagService

  @Autowired
  private lateinit var engangsbelopService: EngangsbelopService

  @Autowired
  private lateinit var vedtakService: VedtakService

  @Autowired
  private lateinit var behandlingsreferanseRepository: BehandlingsreferanseRepository

  @Autowired
  private lateinit var engangsbelopGrunnlagRepository: EngangsbelopGrunnlagRepository

  @Autowired
  private lateinit var periodeGrunnlagRepository: PeriodeGrunnlagRepository

  @Autowired
  private lateinit var grunnlagRepository: GrunnlagRepository

  @Autowired
  private lateinit var engangsbelopRepository: EngangsbelopRepository

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
    behandlingsreferanseRepository.deleteAll()
    engangsbelopGrunnlagRepository.deleteAll()
    periodeGrunnlagRepository.deleteAll()
    engangsbelopRepository.deleteAll()
    grunnlagRepository.deleteAll()
    periodeRepository.deleteAll()
    stonadsendringRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal opprette nytt engangsbelopGrunnlag`() {
    // Oppretter nytt vedtak
    val nyttVedtakRequest = OpprettVedtakRequest("TEST", "1111")
    val nyttVedtakOpprettet = vedtakService.opprettVedtak(nyttVedtakRequest)

    // Oppretter nytt engangsbelop
    val nyttEngangsbelopRequest = OpprettEngangsbelopRequest(
      nyttVedtakOpprettet.vedtakId,
      1,
      null,
      "SAERTILSKUDD",
      "1111",
      "1111",
      "1111",
      BigDecimal.valueOf(999.0),
      "NOK",
      "SAERBIDRAG_BEREGNET"
    )
    val nyttEngangsbelopOpprettet = engangsbelopService.opprettEngangsbelop(nyttEngangsbelopRequest)

    // Oppretter nytt grunnlag
    val mapper = ObjectMapper()
    val jsonString = """{"Grunnlag 1":"Verdi 1","Grunnlag 2":"Verdi 2"}"""

    val nyttGrunnlagRequest = OpprettGrunnlagRequest(
      grunnlagReferanse = "",
      vedtakId = nyttVedtakOpprettet.vedtakId,
      grunnlagType = "Beregnet Inntekt",
      grunnlagInnhold = mapper.readTree(jsonString)
    )
    val grunnlagOpprettet = grunnlagService.opprettGrunnlag(nyttGrunnlagRequest)

    // Oppretter nytt engangsbelopgrunnlag
    val nyttEngangsbelopGrunnlagRequest = OpprettEngangsbelopGrunnlagRequest(
      nyttEngangsbelopOpprettet.engangsbelopId,
      grunnlagOpprettet.grunnlagId
    )
    val nyttEngangsbelopGrunnlagOpprettet = engangsbelopGrunnlagService.opprettEngangsbelopGrunnlag(nyttEngangsbelopGrunnlagRequest)

    assertAll(
      Executable { assertThat(nyttEngangsbelopGrunnlagOpprettet).isNotNull() },
      Executable { assertThat(nyttEngangsbelopGrunnlagOpprettet.engangsbelopId).isEqualTo(nyttEngangsbelopGrunnlagRequest.engangsbelopId) },
      Executable { assertThat(nyttEngangsbelopGrunnlagOpprettet.grunnlagId).isEqualTo(nyttEngangsbelopGrunnlagRequest.grunnlagId) }
    )
  }

  @Test
  fun `skal hente data for et engangsbelopgrunnlag`() {
    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    // Oppretter ny stønadsendring
    val nyEngangsbelopOpprettet = persistenceService.opprettEngangsbelop(
      EngangsbelopDto(
        vedtakId = nyttVedtakOpprettet.vedtakId,
        lopenr = 1,
        endrerEngangsbelopId = null,
        type = "SAERBIDRAG",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        belop = BigDecimal.valueOf(666.0),
        valutakode = "NOK",
        resultatkode = "SAERBIDRAG_BEREGNET"
      )
    )

    // Oppretter nytt grunnlag
    val nyttGrunnlagOpprettet = persistenceService.opprettGrunnlag(
      GrunnlagDto(
        grunnlagReferanse = "",
        vedtakId = nyttVedtakOpprettet.vedtakId,
        grunnlagType = "Beregnet Inntekt",
        grunnlagInnhold = "100"
      )
    )

    // Oppretter nytt engangsbelopgrunnlag
    val nyttEngangsbelopGrunnlagOpprettet = persistenceService.opprettEngangsbelopGrunnlag(
      EngangsbelopGrunnlagDto(
        nyEngangsbelopOpprettet.engangsbelopId,
        nyttGrunnlagOpprettet.grunnlagId
      )
    )

    // Henter engangsbelopgrunnlag som akkurat ble opprettet
    val engangsbelopGrunnlagFunnet = engangsbelopGrunnlagService.hentEngangsbelopGrunnlag(
      nyttEngangsbelopGrunnlagOpprettet.engangsbelopId, nyttEngangsbelopGrunnlagOpprettet.grunnlagId)

    assertAll(
      Executable { assertThat(engangsbelopGrunnlagFunnet).isNotNull() },
      Executable { assertThat(engangsbelopGrunnlagFunnet.engangsbelopId).isEqualTo(nyttEngangsbelopGrunnlagOpprettet.engangsbelopId) },
      Executable { assertThat(engangsbelopGrunnlagFunnet.grunnlagId).isEqualTo(nyttEngangsbelopGrunnlagOpprettet.grunnlagId) }
    )
    engangsbelopGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    engangsbelopRepository.deleteAll()
    vedtakRepository.deleteAll()
  }

  @Test
  fun `skal hente alle engangsbelopgrunnlag for et engangsbelop`() {

    // Oppretter nytt vedtak
    val nyttVedtakOpprettet = persistenceService.opprettVedtak(VedtakDto(saksbehandlerId = "TEST", enhetId = "1111"))

    val nyttEngangsbelopOpprettet = persistenceService.opprettEngangsbelop(
      EngangsbelopDto(
        vedtakId = nyttVedtakOpprettet.vedtakId,
        lopenr = 1,
        endrerEngangsbelopId = null,
        type = "SAERBIDRAG",
        skyldnerId = "1111",
        kravhaverId = "1111",
        mottakerId = "1111",
        belop = BigDecimal.valueOf(666.0),
        valutakode = "NOK",
        resultatkode = "SAERBIDRAG_BEREGNET"
      )
    )

    // Oppretter nye grunnlag
    val nyttGrunnlagDtoListe = mutableListOf<GrunnlagDto>()

    nyttGrunnlagDtoListe.add(
      persistenceService.opprettGrunnlag(
        GrunnlagDto(
          grunnlagReferanse = "",
          vedtakId = nyttVedtakOpprettet.vedtakId,
          grunnlagType = "Beregnet Inntekt",
          grunnlagInnhold = "100")
      )
    )

    nyttGrunnlagDtoListe.add(
      persistenceService.opprettGrunnlag(
        GrunnlagDto(
          grunnlagReferanse = "",
          vedtakId = nyttVedtakOpprettet.vedtakId,
          grunnlagType = "Beregnet Skatt",
          grunnlagInnhold = "10")
      )
    )

    // Oppretter nye engangsbelopgrunnlag
    val nyttEngangsbelopgrunnlagtoListe = mutableListOf<EngangsbelopGrunnlagDto>()

    nyttEngangsbelopgrunnlagtoListe.add(
      persistenceService.opprettEngangsbelopGrunnlag(
        EngangsbelopGrunnlagDto(
          nyttEngangsbelopOpprettet.engangsbelopId,
          nyttGrunnlagDtoListe[0].grunnlagId
        )
      )
    )

    nyttEngangsbelopgrunnlagtoListe.add(
      persistenceService.opprettEngangsbelopGrunnlag(
        EngangsbelopGrunnlagDto(
          nyttEngangsbelopOpprettet.engangsbelopId,
          nyttGrunnlagDtoListe[1].grunnlagId
        )
      )
    )

    // Henter begge engangsbelopgrunnlagene som akkurat ble opprettet
    val engangsbelopId = nyttEngangsbelopOpprettet.engangsbelopId
    val engangsbelopgrunnlagFunnet = engangsbelopGrunnlagService.hentAlleGrunnlagForEngangsbelop(engangsbelopId)

    assertAll(
      Executable { assertThat(engangsbelopgrunnlagFunnet).isNotNull() },
      Executable { assertThat(engangsbelopgrunnlagFunnet).isNotNull() },
      Executable { assertThat(engangsbelopgrunnlagFunnet.size).isEqualTo(2) },
      Executable {
        engangsbelopgrunnlagFunnet.forEachIndexed { index, engangsbelopGrunnlag ->
          assertAll(
            Executable { assertThat(engangsbelopGrunnlag.engangsbelopId).isEqualTo(nyttEngangsbelopgrunnlagtoListe[index].engangsbelopId) },
            Executable { assertThat(engangsbelopGrunnlag.grunnlagId).isEqualTo(nyttEngangsbelopgrunnlagtoListe[index].grunnlagId) }
          )
        }
      }
    )
    engangsbelopGrunnlagRepository.deleteAll()
    grunnlagRepository.deleteAll()
    engangsbelopRepository.deleteAll()
    vedtakRepository.deleteAll()
  }
}