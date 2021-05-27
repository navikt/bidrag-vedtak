package no.nav.bidrag.vedtak.service

import no.nav.bidrag.vedtak.TestUtil.Companion.byggGrunnlagDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggKomplettVedtakRequest
import no.nav.bidrag.vedtak.TestUtil.Companion.byggPeriodeDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggPeriodeGrunnlagDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggStonadsendringDto
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakDto
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
  private lateinit var persistenceServiceMock: PersistenceService

  @Captor
  private lateinit var vedtakDtoCaptor: ArgumentCaptor<VedtakDto>

  @Captor
  private lateinit var stonadsendringDtoCaptor: ArgumentCaptor<StonadsendringDto>

  @Captor
  private lateinit var periodeDtoCaptor: ArgumentCaptor<PeriodeDto>

  @Captor
  private lateinit var grunnlagDtoCaptor: ArgumentCaptor<GrunnlagDto>

  @Captor
  private lateinit var periodeGrunnlagDtoCaptor: ArgumentCaptor<PeriodeGrunnlagDto>

  @Test
  fun `skal opprette nytt komplett vedtak`() {

    Mockito.`when`(persistenceServiceMock.opprettVedtak(MockitoHelper.capture(vedtakDtoCaptor)))
      .thenReturn(byggVedtakDto())
    Mockito.`when`(persistenceServiceMock.opprettStonadsendring(MockitoHelper.capture(stonadsendringDtoCaptor)))
      .thenReturn(byggStonadsendringDto())
    Mockito.`when`(persistenceServiceMock.opprettPeriode(MockitoHelper.capture(periodeDtoCaptor)))
      .thenReturn(byggPeriodeDto())
    Mockito.`when`(persistenceServiceMock.opprettGrunnlag(MockitoHelper.capture(grunnlagDtoCaptor)))
      .thenReturn(byggGrunnlagDto())
    Mockito.`when`(persistenceServiceMock.opprettPeriodeGrunnlag(MockitoHelper.capture(periodeGrunnlagDtoCaptor)))
      .thenReturn(byggPeriodeGrunnlagDto())

    val komplettVedtak = byggKomplettVedtakRequest()
    val nyttKomplettVedtakOpprettet = vedtakService.opprettKomplettVedtak(komplettVedtak)

    val vedtakDto = vedtakDtoCaptor.value
    val stonadsendringDtoListe = stonadsendringDtoCaptor.allValues
    val periodeDtoListe = periodeDtoCaptor.allValues
    val grunnlagDtoListe = grunnlagDtoCaptor.allValues
    val periodeGrunnlagDtoListe = periodeGrunnlagDtoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettVedtak(MockitoHelper.any(VedtakDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettStonadsendring(MockitoHelper.any(StonadsendringDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(4)).opprettPeriode(MockitoHelper.any(PeriodeDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(4)).opprettGrunnlag(MockitoHelper.any(GrunnlagDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(11)).opprettPeriodeGrunnlag(MockitoHelper.any(PeriodeGrunnlagDto::class.java))

    assertAll(
      Executable { assertThat(nyttKomplettVedtakOpprettet).isNotNull() },

      // Sjekk VedtakDto
      Executable { assertThat(vedtakDto).isNotNull() },
      Executable { assertThat(vedtakDto.enhetId).isEqualTo(komplettVedtak.enhetId) },
      Executable { assertThat(vedtakDto.saksbehandlerId).isEqualTo(komplettVedtak.saksbehandlerId) },

      // Sjekk StonadsendringDto
      Executable { assertThat(stonadsendringDtoListe).isNotNull() },
      Executable { assertThat(stonadsendringDtoListe.size).isEqualTo(2) },

      Executable { assertThat(stonadsendringDtoListe[0].stonadType).isEqualTo(komplettVedtak.stonadsendringListe!![0].stonadType) },
      Executable { assertThat(stonadsendringDtoListe[0].sakId).isEqualTo(komplettVedtak.stonadsendringListe!![0].sakId) },
      Executable { assertThat(stonadsendringDtoListe[0].behandlingId).isEqualTo(komplettVedtak.stonadsendringListe!![0].behandlingId) },
      Executable { assertThat(stonadsendringDtoListe[0].skyldnerId).isEqualTo(komplettVedtak.stonadsendringListe!![0].skyldnerId) },
      Executable { assertThat(stonadsendringDtoListe[0].kravhaverId).isEqualTo(komplettVedtak.stonadsendringListe!![0].kravhaverId) },
      Executable { assertThat(stonadsendringDtoListe[0].mottakerId).isEqualTo(komplettVedtak.stonadsendringListe!![0].mottakerId) },

      Executable { assertThat(stonadsendringDtoListe[1].stonadType).isEqualTo(komplettVedtak.stonadsendringListe!![1].stonadType) },
      Executable { assertThat(stonadsendringDtoListe[1].sakId).isEqualTo(komplettVedtak.stonadsendringListe!![1].sakId) },
      Executable { assertThat(stonadsendringDtoListe[1].behandlingId).isEqualTo(komplettVedtak.stonadsendringListe!![1].behandlingId) },
      Executable { assertThat(stonadsendringDtoListe[1].skyldnerId).isEqualTo(komplettVedtak.stonadsendringListe!![1].skyldnerId) },
      Executable { assertThat(stonadsendringDtoListe[1].kravhaverId).isEqualTo(komplettVedtak.stonadsendringListe!![1].kravhaverId) },
      Executable { assertThat(stonadsendringDtoListe[1].mottakerId).isEqualTo(komplettVedtak.stonadsendringListe!![1].mottakerId) },

      // Sjekk PeriodeDto
      Executable { assertThat(periodeDtoListe).isNotNull() },
      Executable { assertThat(periodeDtoListe.size).isEqualTo(4) },

      Executable { assertThat(periodeDtoListe[0].periodeFomDato).isEqualTo(komplettVedtak.stonadsendringListe!![0].periodeListe[0].periodeFomDato) },
      Executable { assertThat(periodeDtoListe[0].periodeTilDato).isEqualTo(komplettVedtak.stonadsendringListe!![0].periodeListe[0].periodeTilDato) },
      Executable { assertThat(periodeDtoListe[0].belop).isEqualTo(komplettVedtak.stonadsendringListe!![0].periodeListe[0].belop) },
      Executable { assertThat(periodeDtoListe[0].valutakode).isEqualTo(komplettVedtak.stonadsendringListe!![0].periodeListe[0].valutakode) },
      Executable { assertThat(periodeDtoListe[0].resultatkode).isEqualTo(komplettVedtak.stonadsendringListe!![0].periodeListe[0].resultatkode) },

      Executable { assertThat(periodeDtoListe[1].periodeFomDato).isEqualTo(komplettVedtak.stonadsendringListe!![0].periodeListe[1].periodeFomDato) },
      Executable { assertThat(periodeDtoListe[1].periodeTilDato).isEqualTo(komplettVedtak.stonadsendringListe!![0].periodeListe[1].periodeTilDato) },
      Executable { assertThat(periodeDtoListe[1].belop).isEqualTo(komplettVedtak.stonadsendringListe!![0].periodeListe[1].belop) },
      Executable { assertThat(periodeDtoListe[1].valutakode).isEqualTo(komplettVedtak.stonadsendringListe!![0].periodeListe[1].valutakode) },
      Executable { assertThat(periodeDtoListe[1].resultatkode).isEqualTo(komplettVedtak.stonadsendringListe!![0].periodeListe[1].resultatkode) },

      Executable { assertThat(periodeDtoListe[2].periodeFomDato).isEqualTo(komplettVedtak.stonadsendringListe!![1].periodeListe[0].periodeFomDato) },
      Executable { assertThat(periodeDtoListe[2].periodeTilDato).isEqualTo(komplettVedtak.stonadsendringListe!![1].periodeListe[0].periodeTilDato) },
      Executable { assertThat(periodeDtoListe[2].belop).isEqualTo(komplettVedtak.stonadsendringListe!![1].periodeListe[0].belop) },
      Executable { assertThat(periodeDtoListe[2].valutakode).isEqualTo(komplettVedtak.stonadsendringListe!![1].periodeListe[0].valutakode) },
      Executable { assertThat(periodeDtoListe[2].resultatkode).isEqualTo(komplettVedtak.stonadsendringListe!![1].periodeListe[0].resultatkode) },

      Executable { assertThat(periodeDtoListe[3].periodeFomDato).isEqualTo(komplettVedtak.stonadsendringListe!![1].periodeListe[1].periodeFomDato) },
      Executable { assertThat(periodeDtoListe[3].periodeTilDato).isEqualTo(komplettVedtak.stonadsendringListe!![1].periodeListe[1].periodeTilDato) },
      Executable { assertThat(periodeDtoListe[3].belop).isEqualTo(komplettVedtak.stonadsendringListe!![1].periodeListe[1].belop) },
      Executable { assertThat(periodeDtoListe[3].valutakode).isEqualTo(komplettVedtak.stonadsendringListe!![1].periodeListe[1].valutakode) },
      Executable { assertThat(periodeDtoListe[3].resultatkode).isEqualTo(komplettVedtak.stonadsendringListe!![1].periodeListe[1].resultatkode) },

      // Sjekk GrunnlagDto
      Executable { assertThat(grunnlagDtoListe).isNotNull() },
      Executable { assertThat(grunnlagDtoListe.size).isEqualTo(4) },

      Executable { assertThat(grunnlagDtoListe[0].grunnlagReferanse).isEqualTo(komplettVedtak.grunnlagListe[0].grunnlagReferanse) },
      Executable { assertThat(grunnlagDtoListe[0].grunnlagType).isEqualTo(komplettVedtak.grunnlagListe[0].grunnlagType) },
      Executable { assertThat(grunnlagDtoListe[0].grunnlagInnhold).isEqualTo(komplettVedtak.grunnlagListe[0].grunnlagInnhold.toString()) },

      Executable { assertThat(grunnlagDtoListe[1].grunnlagReferanse).isEqualTo(komplettVedtak.grunnlagListe[1].grunnlagReferanse) },
      Executable { assertThat(grunnlagDtoListe[1].grunnlagType).isEqualTo(komplettVedtak.grunnlagListe[1].grunnlagType) },
      Executable { assertThat(grunnlagDtoListe[1].grunnlagInnhold).isEqualTo(komplettVedtak.grunnlagListe[1].grunnlagInnhold.toString()) },

      Executable { assertThat(grunnlagDtoListe[2].grunnlagReferanse).isEqualTo(komplettVedtak.grunnlagListe[2].grunnlagReferanse) },
      Executable { assertThat(grunnlagDtoListe[2].grunnlagType).isEqualTo(komplettVedtak.grunnlagListe[2].grunnlagType) },
      Executable { assertThat(grunnlagDtoListe[2].grunnlagInnhold).isEqualTo(komplettVedtak.grunnlagListe[2].grunnlagInnhold.toString()) },

      Executable { assertThat(grunnlagDtoListe[3].grunnlagReferanse).isEqualTo(komplettVedtak.grunnlagListe[3].grunnlagReferanse) },
      Executable { assertThat(grunnlagDtoListe[3].grunnlagType).isEqualTo(komplettVedtak.grunnlagListe[3].grunnlagType) },
      Executable { assertThat(grunnlagDtoListe[3].grunnlagInnhold).isEqualTo(komplettVedtak.grunnlagListe[3].grunnlagInnhold.toString()) },

      // Sjekk PeriodeGrunnlagDto
      Executable { assertThat(periodeGrunnlagDtoListe).isNotNull() },
      Executable { assertThat(periodeGrunnlagDtoListe.size).isEqualTo(11) }
    )
  }

  @Test
  fun `skal hente komplett vedtak`() {

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

    val komplettVedtakFunnet = vedtakService.hentKomplettVedtak(1)

    assertAll(
      Executable { assertThat(komplettVedtakFunnet).isNotNull() },
      Executable { assertThat(komplettVedtakFunnet.vedtakId).isEqualTo(1) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe.size).isEqualTo(2) },
      Executable { assertThat(komplettVedtakFunnet.grunnlagListe[0].grunnlagReferanse).isEqualTo("REF1") },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe.size).isEqualTo(2) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].behandlingId).isEqualTo("BEHID1") },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe.size).isEqualTo(2) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(100)) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
      Executable { assertThat(komplettVedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0].grunnlagReferanse).isEqualTo("REF1") }
    )
  }

  object MockitoHelper {

    // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

    fun <T> any(type: Class<T>): T = Mockito.any(type)
  }
}
