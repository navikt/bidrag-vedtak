package no.nav.bidrag.vedtak.service

import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.vedtak.TestUtil.Companion.byggBehandlingsreferanseDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggEngangsbelopDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggEngangsbelopGrunnlagDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggGrunnlagDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggPeriodeDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggPeriodeGrunnlagDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggStonadsendringDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakDto
import no.nav.bidrag.vedtak.dto.BehandlingsreferanseDto
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal

@DisplayName("VedtakServiceMockTest")
@ExtendWith(MockitoExtension::class)
class VedtakServiceMockTest {

  @InjectMocks
  private lateinit var vedtakService: VedtakService

  @Mock
  private lateinit var hendelserService: HendelserService

  @Mock
  private lateinit var persistenceServiceMock: PersistenceService

  @Captor
  private lateinit var vedtakDtoCaptor: ArgumentCaptor<VedtakDto>

  @Captor
  private lateinit var stonadsendringDtoCaptor: ArgumentCaptor<StonadsendringDto>

  @Captor
  private lateinit var engangsbelopDtoCaptor: ArgumentCaptor<EngangsbelopDto>

  @Captor
  private lateinit var periodeDtoCaptor: ArgumentCaptor<PeriodeDto>

  @Captor
  private lateinit var grunnlagDtoCaptor: ArgumentCaptor<GrunnlagDto>

  @Captor
  private lateinit var periodeGrunnlagDtoCaptor: ArgumentCaptor<PeriodeGrunnlagDto>

  @Captor
  private lateinit var engangsbelopGrunnlagDtoCaptor: ArgumentCaptor<EngangsbelopGrunnlagDto>

  @Captor
  private lateinit var behandlingsreferanseDtoCaptor: ArgumentCaptor<BehandlingsreferanseDto>


  @Test
  fun `skal opprette nytt vedtak`() {

    Mockito.`when`(persistenceServiceMock.opprettVedtak(MockitoHelper.capture(vedtakDtoCaptor)))
      .thenReturn(byggVedtakDto())
    Mockito.`when`(persistenceServiceMock.opprettStonadsendring(MockitoHelper.capture(stonadsendringDtoCaptor)))
      .thenReturn(byggStonadsendringDto())
    Mockito.`when`(persistenceServiceMock.opprettEngangsbelop(MockitoHelper.capture(engangsbelopDtoCaptor)))
      .thenReturn(byggEngangsbelopDto())
    Mockito.`when`(persistenceServiceMock.opprettPeriode(MockitoHelper.capture(periodeDtoCaptor)))
      .thenReturn(byggPeriodeDto())
    Mockito.`when`(persistenceServiceMock.opprettGrunnlag(MockitoHelper.capture(grunnlagDtoCaptor)))
      .thenReturn(byggGrunnlagDto())
    Mockito.`when`(persistenceServiceMock.opprettPeriodeGrunnlag(MockitoHelper.capture(periodeGrunnlagDtoCaptor)))
      .thenReturn(byggPeriodeGrunnlagDto())
    Mockito.`when`(persistenceServiceMock.opprettEngangsbelopGrunnlag(MockitoHelper.capture(engangsbelopGrunnlagDtoCaptor)))
      .thenReturn(byggEngangsbelopGrunnlagDto())
    Mockito.`when`(persistenceServiceMock.opprettBehandlingsreferanse(MockitoHelper.capture(behandlingsreferanseDtoCaptor)))
      .thenReturn(byggBehandlingsreferanseDto())

    val vedtak = byggVedtakRequest()
    val nyttVedtakOpprettet = vedtakService.opprettVedtak(vedtak)

    val vedtakDto = vedtakDtoCaptor.value
    val stonadsendringDtoListe = stonadsendringDtoCaptor.allValues
    val engangsbelopDtoListe = engangsbelopDtoCaptor.allValues
    val periodeDtoListe = periodeDtoCaptor.allValues
    val grunnlagDtoListe = grunnlagDtoCaptor.allValues
    val periodeGrunnlagDtoListe = periodeGrunnlagDtoCaptor.allValues
    val engangsbelopGrunnlagDtoListe = engangsbelopGrunnlagDtoCaptor.allValues
    val behandlingsreferanseDtoListe = behandlingsreferanseDtoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettVedtak(MockitoHelper.any(VedtakDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettStonadsendring(MockitoHelper.any(StonadsendringDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettEngangsbelop(MockitoHelper.any(EngangsbelopDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(4)).opprettPeriode(MockitoHelper.any(PeriodeDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(4)).opprettGrunnlag(MockitoHelper.any(GrunnlagDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(11)).opprettPeriodeGrunnlag(MockitoHelper.any(PeriodeGrunnlagDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(6)).opprettEngangsbelopGrunnlag(MockitoHelper.any(EngangsbelopGrunnlagDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettBehandlingsreferanse(MockitoHelper.any(BehandlingsreferanseDto::class.java))

    assertAll(
      Executable { assertThat(nyttVedtakOpprettet).isNotNull() },

      // Sjekk VedtakDto
      Executable { assertThat(vedtakDto).isNotNull() },
      Executable { assertThat(vedtakDto.vedtakType).isEqualTo(vedtak.vedtakType.toString()) },
      Executable { assertThat(vedtakDto.enhetId).isEqualTo(vedtak.enhetId) },
      Executable { assertThat(vedtakDto.opprettetAv).isEqualTo(vedtak.opprettetAv) },

      // Sjekk StonadsendringDto
      Executable { assertThat(stonadsendringDtoListe).isNotNull() },
      Executable { assertThat(stonadsendringDtoListe.size).isEqualTo(2) },

      Executable { assertThat(stonadsendringDtoListe[0].stonadType).isEqualTo(vedtak.stonadsendringListe!![0].stonadType.toString()) },
      Executable { assertThat(stonadsendringDtoListe[0].sakId).isEqualTo(vedtak.stonadsendringListe!![0].sakId) },
      Executable { assertThat(stonadsendringDtoListe[0].behandlingId).isEqualTo(vedtak.stonadsendringListe!![0].behandlingId) },
      Executable { assertThat(stonadsendringDtoListe[0].skyldnerId).isEqualTo(vedtak.stonadsendringListe!![0].skyldnerId) },
      Executable { assertThat(stonadsendringDtoListe[0].kravhaverId).isEqualTo(vedtak.stonadsendringListe!![0].kravhaverId) },
      Executable { assertThat(stonadsendringDtoListe[0].mottakerId).isEqualTo(vedtak.stonadsendringListe!![0].mottakerId) },

      Executable { assertThat(stonadsendringDtoListe[1].stonadType).isEqualTo(vedtak.stonadsendringListe!![1].stonadType.toString()) },
      Executable { assertThat(stonadsendringDtoListe[1].sakId).isEqualTo(vedtak.stonadsendringListe!![1].sakId) },
      Executable { assertThat(stonadsendringDtoListe[1].behandlingId).isEqualTo(vedtak.stonadsendringListe!![1].behandlingId) },
      Executable { assertThat(stonadsendringDtoListe[1].skyldnerId).isEqualTo(vedtak.stonadsendringListe!![1].skyldnerId) },
      Executable { assertThat(stonadsendringDtoListe[1].kravhaverId).isEqualTo(vedtak.stonadsendringListe!![1].kravhaverId) },
      Executable { assertThat(stonadsendringDtoListe[1].mottakerId).isEqualTo(vedtak.stonadsendringListe!![1].mottakerId) },

      // Sjekk EngangsbelopDto
      Executable { assertThat(engangsbelopDtoListe).isNotNull() },
      Executable { assertThat(engangsbelopDtoListe.size).isEqualTo(2) },

      Executable { assertThat(engangsbelopDtoListe[0].lopenr).isEqualTo(vedtak.engangsbelopListe!![0].lopenr) },
      Executable { assertThat(engangsbelopDtoListe[0].endrerEngangsbelopId).isEqualTo(vedtak.engangsbelopListe!![0].endrerEngangsbelopId) },
      Executable { assertThat(engangsbelopDtoListe[0].type).isEqualTo(vedtak.engangsbelopListe!![0].type) },
      Executable { assertThat(engangsbelopDtoListe[0].skyldnerId).isEqualTo(vedtak.engangsbelopListe!![0].skyldnerId) },
      Executable { assertThat(engangsbelopDtoListe[0].kravhaverId).isEqualTo(vedtak.engangsbelopListe!![0].kravhaverId) },
      Executable { assertThat(engangsbelopDtoListe[0].mottakerId).isEqualTo(vedtak.engangsbelopListe!![0].mottakerId) },
      Executable { assertThat(engangsbelopDtoListe[0].belop).isEqualTo(vedtak.engangsbelopListe!![0].belop) },
      Executable { assertThat(engangsbelopDtoListe[0].valutakode).isEqualTo(vedtak.engangsbelopListe!![0].valutakode) },
      Executable { assertThat(engangsbelopDtoListe[0].resultatkode).isEqualTo(vedtak.engangsbelopListe!![0].resultatkode) },

      Executable { assertThat(engangsbelopDtoListe[1].lopenr).isEqualTo(vedtak.engangsbelopListe!![1].lopenr) },
      Executable { assertThat(engangsbelopDtoListe[1].endrerEngangsbelopId).isEqualTo(vedtak.engangsbelopListe!![1].endrerEngangsbelopId) },
      Executable { assertThat(engangsbelopDtoListe[1].type).isEqualTo(vedtak.engangsbelopListe!![1].type) },
      Executable { assertThat(engangsbelopDtoListe[1].skyldnerId).isEqualTo(vedtak.engangsbelopListe!![1].skyldnerId) },
      Executable { assertThat(engangsbelopDtoListe[1].kravhaverId).isEqualTo(vedtak.engangsbelopListe!![1].kravhaverId) },
      Executable { assertThat(engangsbelopDtoListe[1].mottakerId).isEqualTo(vedtak.engangsbelopListe!![1].mottakerId) },
      Executable { assertThat(engangsbelopDtoListe[1].belop).isEqualTo(vedtak.engangsbelopListe!![1].belop) },
      Executable { assertThat(engangsbelopDtoListe[1].valutakode).isEqualTo(vedtak.engangsbelopListe!![1].valutakode) },
      Executable { assertThat(engangsbelopDtoListe[1].resultatkode).isEqualTo(vedtak.engangsbelopListe!![1].resultatkode) },

      // Sjekk PeriodeDto
      Executable { assertThat(periodeDtoListe).isNotNull() },
      Executable { assertThat(periodeDtoListe.size).isEqualTo(4) },

      Executable { assertThat(periodeDtoListe[0].periodeFomDato).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[0].periodeFomDato) },
      Executable { assertThat(periodeDtoListe[0].periodeTilDato).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[0].periodeTilDato) },
      Executable { assertThat(periodeDtoListe[0].belop).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[0].belop) },
      Executable { assertThat(periodeDtoListe[0].valutakode).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[0].valutakode) },
      Executable { assertThat(periodeDtoListe[0].resultatkode).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[0].resultatkode) },

      Executable { assertThat(periodeDtoListe[1].periodeFomDato).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[1].periodeFomDato) },
      Executable { assertThat(periodeDtoListe[1].periodeTilDato).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[1].periodeTilDato) },
      Executable { assertThat(periodeDtoListe[1].belop).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[1].belop) },
      Executable { assertThat(periodeDtoListe[1].valutakode).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[1].valutakode) },
      Executable { assertThat(periodeDtoListe[1].resultatkode).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[1].resultatkode) },

      Executable { assertThat(periodeDtoListe[2].periodeFomDato).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[0].periodeFomDato) },
      Executable { assertThat(periodeDtoListe[2].periodeTilDato).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[0].periodeTilDato) },
      Executable { assertThat(periodeDtoListe[2].belop).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[0].belop) },
      Executable { assertThat(periodeDtoListe[2].valutakode).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[0].valutakode) },
      Executable { assertThat(periodeDtoListe[2].resultatkode).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[0].resultatkode) },

      Executable { assertThat(periodeDtoListe[3].periodeFomDato).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[1].periodeFomDato) },
      Executable { assertThat(periodeDtoListe[3].periodeTilDato).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[1].periodeTilDato) },
      Executable { assertThat(periodeDtoListe[3].belop).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[1].belop) },
      Executable { assertThat(periodeDtoListe[3].valutakode).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[1].valutakode) },
      Executable { assertThat(periodeDtoListe[3].resultatkode).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[1].resultatkode) },

      // Sjekk GrunnlagDto
      Executable { assertThat(grunnlagDtoListe).isNotNull() },
      Executable { assertThat(grunnlagDtoListe.size).isEqualTo(4) },

      Executable { assertThat(grunnlagDtoListe[0].referanse).isEqualTo(vedtak.grunnlagListe[0].referanse) },
      Executable { assertThat(grunnlagDtoListe[0].type).isEqualTo(vedtak.grunnlagListe[0].type.toString()) },
      Executable { assertThat(grunnlagDtoListe[0].innhold).isEqualTo(vedtak.grunnlagListe[0].innhold.toString()) },

      Executable { assertThat(grunnlagDtoListe[1].referanse).isEqualTo(vedtak.grunnlagListe[1].referanse) },
      Executable { assertThat(grunnlagDtoListe[1].type).isEqualTo(vedtak.grunnlagListe[1].type.toString()) },
      Executable { assertThat(grunnlagDtoListe[1].innhold).isEqualTo(vedtak.grunnlagListe[1].innhold.toString()) },

      Executable { assertThat(grunnlagDtoListe[2].referanse).isEqualTo(vedtak.grunnlagListe[2].referanse) },
      Executable { assertThat(grunnlagDtoListe[2].type).isEqualTo(vedtak.grunnlagListe[2].type.toString()) },
      Executable { assertThat(grunnlagDtoListe[2].innhold).isEqualTo(vedtak.grunnlagListe[2].innhold.toString()) },

      Executable { assertThat(grunnlagDtoListe[3].referanse).isEqualTo(vedtak.grunnlagListe[3].referanse) },
      Executable { assertThat(grunnlagDtoListe[3].type).isEqualTo(vedtak.grunnlagListe[3].type.toString()) },
      Executable { assertThat(grunnlagDtoListe[3].innhold).isEqualTo(vedtak.grunnlagListe[3].innhold.toString()) },

      // Sjekk PeriodeGrunnlagDto
      Executable { assertThat(periodeGrunnlagDtoListe).isNotNull() },
      Executable { assertThat(periodeGrunnlagDtoListe.size).isEqualTo(11) },

      // Sjekk EngangsbelopGrunnlagDto
      Executable { assertThat(engangsbelopGrunnlagDtoListe).isNotNull() },
      Executable { assertThat(engangsbelopGrunnlagDtoListe.size).isEqualTo(6) },

      // Sjekk BehandlingsreferanseDto
      Executable { assertThat(behandlingsreferanseDtoListe).isNotNull() },
      Executable { assertThat(behandlingsreferanseDtoListe.size).isEqualTo(2) }


    )
  }

  @Test
  fun `skal hente vedtak`() {

    // Hent vedtak
    Mockito.`when`(persistenceServiceMock.hentVedtak(MockitoHelper.any(Int::class.java))).thenReturn(byggVedtakDto(vedtakId = 1))
    Mockito.`when`(persistenceServiceMock.hentVedtak(MockitoHelper.any(Int::class.java))).thenReturn(byggVedtakDto(vedtakId = 1))
    Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForVedtak(MockitoHelper.any(Int::class.java)))
      .thenReturn(
        listOf(
          byggGrunnlagDto(grunnlagId = 1, vedtakId = 1, grunnlagReferanse = "REF1"),
          byggGrunnlagDto(grunnlagId = 2, vedtakId = 1, grunnlagReferanse = "REF2")
        )
      )
    Mockito.`when`(persistenceServiceMock.hentAlleStonadsendringerForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
      listOf(
        byggStonadsendringDto(stonadsendringId = 1, vedtakId = 1, behandlingId = "BEHID1"),
        byggStonadsendringDto(stonadsendringId = 2, vedtakId = 1, behandlingId = "BEHID2")
      )
    )
    Mockito.`when`(persistenceServiceMock.hentAlleEngangsbelopForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
      listOf(
        byggEngangsbelopDto(engangsbelopId =  1, vedtakId = 1, 1, null, "SAERTILSKUDD",
          "01018011111", "01010511111", "01018211111", BigDecimal.valueOf(3490),
          "NOK", "SAERTILSKUDD BEREGNET")
      )
    )
    Mockito.`when`(persistenceServiceMock.hentAllePerioderForStonadsendring(MockitoHelper.any(Int::class.java)))
      .thenReturn(
        listOf(
          byggPeriodeDto(periodeId = 1, stonadsendringId = 1, belop = BigDecimal.valueOf(100)),
          byggPeriodeDto(periodeId = 2, stonadsendringId = 2, belop = BigDecimal.valueOf(200))
        )
      )
    Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForPeriode(MockitoHelper.any(Int::class.java))).thenReturn(
      listOf(byggPeriodeGrunnlagDto(
        periodeId = 1,
        grunnlagId = 1
      ), byggPeriodeGrunnlagDto(periodeId = 2, grunnlagId = 2))
    )
    Mockito.`when`(persistenceServiceMock.hentGrunnlag(MockitoHelper.any(Int::class.java))).thenReturn(
      byggGrunnlagDto(grunnlagId = 1, vedtakId = 1, grunnlagReferanse = "REF1")
    )
    Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForEngangsbelop(MockitoHelper.any(Int::class.java))).thenReturn(
      listOf(byggEngangsbelopGrunnlagDto(engangsbelopId = 1,grunnlagId = 1),
        byggEngangsbelopGrunnlagDto(engangsbelopId = 2, grunnlagId = 2))
    )
    Mockito.`when`(persistenceServiceMock.hentAlleBehandlingsreferanserForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
      listOf(byggBehandlingsreferanseDto(kilde = "Bisys", referanse = "Bisys-01"),
        byggBehandlingsreferanseDto(kilde = "Bisys", referanse = "Bisys-02"))
    )

    val vedtakFunnet = vedtakService.hentVedtak(1)

    assertAll(
      Executable { assertThat(vedtakFunnet).isNotNull() },
      Executable { assertThat(vedtakFunnet.vedtakId).isEqualTo(1) },
      Executable { assertThat(vedtakFunnet.vedtakType).isEqualTo(VedtakType.MANUELT) },
      Executable { assertThat(vedtakFunnet.grunnlagListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[0].referanse).isEqualTo("REF1") },
      Executable { assertThat(vedtakFunnet.stonadsendringListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].behandlingId).isEqualTo("BEHID1") },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(100)) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo("REF1") },
      Executable { assertThat(vedtakFunnet.engangsbelopListe.size).isEqualTo(1) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].lopenr).isEqualTo(1) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].type).isEqualTo("SAERTILSKUDD") },
      Executable { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },
    )
  }

  object MockitoHelper {

    // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

    fun <T> any(type: Class<T>): T = Mockito.any(type)
  }
}
