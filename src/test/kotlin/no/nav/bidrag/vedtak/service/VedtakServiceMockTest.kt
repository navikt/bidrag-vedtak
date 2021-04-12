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
import java.time.LocalDate

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

    Mockito.`when`(persistenceServiceMock.opprettNyttVedtak(MockitoHelper.capture(vedtakDtoCaptor)))
      .thenReturn(byggVedtakDto())
    Mockito.`when`(persistenceServiceMock.opprettNyStonadsendring(MockitoHelper.capture(stonadsendringDtoCaptor)))
      .thenReturn(byggStonadsendringDto())
    Mockito.`when`(persistenceServiceMock.opprettNyPeriode(MockitoHelper.capture(periodeDtoCaptor)))
      .thenReturn(byggPeriodeDto())
    Mockito.`when`(persistenceServiceMock.opprettNyttGrunnlag(MockitoHelper.capture(grunnlagDtoCaptor)))
      .thenReturn(byggGrunnlagDto())
    Mockito.`when`(persistenceServiceMock.opprettNyttPeriodeGrunnlag(MockitoHelper.capture(periodeGrunnlagDtoCaptor)))
      .thenReturn(byggPeriodeGrunnlagDto())

    val nyttKomplettVedtakOpprettet = vedtakService.opprettKomplettVedtak(byggKomplettVedtakRequest())

    val vedtakDto = vedtakDtoCaptor.value
    val stonadsendringDtoListe = stonadsendringDtoCaptor.allValues
    val periodeDtoListe = periodeDtoCaptor.allValues
    val grunnlagDtoListe = grunnlagDtoCaptor.allValues
    val periodeGrunnlagDtoListe = periodeGrunnlagDtoCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettNyttVedtak(MockitoHelper.any(VedtakDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettNyStonadsendring(MockitoHelper.any(StonadsendringDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(4)).opprettNyPeriode(MockitoHelper.any(PeriodeDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(4)).opprettNyttGrunnlag(MockitoHelper.any(GrunnlagDto::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(11)).opprettNyttPeriodeGrunnlag(MockitoHelper.any(PeriodeGrunnlagDto::class.java))

    assertAll(
      Executable { assertThat(nyttKomplettVedtakOpprettet).isNotNull() },
      Executable { assertThat(nyttKomplettVedtakOpprettet.vedtakId).isNotNull() },

      // Sjekk VedtakDto
      Executable { assertThat(vedtakDto).isNotNull() },
      Executable { assertThat(vedtakDto.enhetId).isEqualTo("4812") },
      Executable { assertThat(vedtakDto.saksbehandlerId).isEqualTo("X123456") },

      // Sjekk StonadsendringDto
      Executable { assertThat(stonadsendringDtoListe).isNotNull() },
      Executable { assertThat(stonadsendringDtoListe.size).isEqualTo(2) },
      Executable { assertThat(stonadsendringDtoListe[0].stonadType).isEqualTo("BIDRAG") },
      Executable { assertThat(stonadsendringDtoListe[0].sakId).isEqualTo("SAK-001") },
      Executable { assertThat(stonadsendringDtoListe[0].behandlingId).isEqualTo("Fritekst") },
      Executable { assertThat(stonadsendringDtoListe[0].skyldnerId).isEqualTo("01018011111") },
      Executable { assertThat(stonadsendringDtoListe[0].kravhaverId).isEqualTo("01010511111") },
      Executable { assertThat(stonadsendringDtoListe[0].mottakerId).isEqualTo("01018211111") },

      Executable { assertThat(stonadsendringDtoListe[1].stonadType).isEqualTo("SAERTILSKUDD") },
      Executable { assertThat(stonadsendringDtoListe[1].sakId).isEqualTo("SAK-001") },
      Executable { assertThat(stonadsendringDtoListe[1].behandlingId).isEqualTo("Fritekst") },
      Executable { assertThat(stonadsendringDtoListe[1].skyldnerId).isEqualTo("01018011111") },
      Executable { assertThat(stonadsendringDtoListe[1].kravhaverId).isEqualTo("01010511111") },
      Executable { assertThat(stonadsendringDtoListe[1].mottakerId).isEqualTo("01018211111") },

      // Sjekk PeriodeDto
      Executable { assertThat(periodeDtoListe).isNotNull() },
      Executable { assertThat(periodeDtoListe.size).isEqualTo(4) },
      Executable { assertThat(periodeDtoListe[0].periodeFomDato).isEqualTo(LocalDate.parse("2019-01-01")) },
      Executable { assertThat(periodeDtoListe[0].periodeTilDato).isEqualTo(LocalDate.parse("2019-07-01")) },
      Executable { assertThat(periodeDtoListe[0].belop).isEqualTo(BigDecimal.valueOf(3490)) },
      Executable { assertThat(periodeDtoListe[0].valutakode).isEqualTo("NOK") },
      Executable { assertThat(periodeDtoListe[0].resultatkode).isEqualTo("KOSTNADSBEREGNET_BIDRAG") },

      Executable { assertThat(periodeDtoListe[1].periodeFomDato).isEqualTo(LocalDate.parse("2019-07-01")) },
      Executable { assertThat(periodeDtoListe[1].periodeTilDato).isEqualTo(LocalDate.parse("2020-01-01")) },
      Executable { assertThat(periodeDtoListe[1].belop).isEqualTo(BigDecimal.valueOf(3520)) },
      Executable { assertThat(periodeDtoListe[1].valutakode).isEqualTo("NOK") },
      Executable { assertThat(periodeDtoListe[1].resultatkode).isEqualTo("KOSTNADSBEREGNET_BIDRAG") },

      Executable { assertThat(periodeDtoListe[2].periodeFomDato).isEqualTo(LocalDate.parse("2019-06-01")) },
      Executable { assertThat(periodeDtoListe[2].periodeTilDato).isEqualTo(LocalDate.parse("2019-07-01")) },
      Executable { assertThat(periodeDtoListe[2].belop).isEqualTo(BigDecimal.valueOf(4240)) },
      Executable { assertThat(periodeDtoListe[2].valutakode).isEqualTo("NOK") },
      Executable { assertThat(periodeDtoListe[2].resultatkode).isEqualTo("SAERTILSKUDD_INNVILGET") },

      Executable { assertThat(periodeDtoListe[3].periodeFomDato).isEqualTo(LocalDate.parse("2019-08-01")) },
      Executable { assertThat(periodeDtoListe[3].periodeTilDato).isEqualTo(LocalDate.parse("2019-09-01")) },
      Executable { assertThat(periodeDtoListe[3].belop).isEqualTo(BigDecimal.valueOf(3410)) },
      Executable { assertThat(periodeDtoListe[3].valutakode).isEqualTo("NOK") },
      Executable { assertThat(periodeDtoListe[3].resultatkode).isEqualTo("SAERTILSKUDD_INNVILGET") },

      // Sjekk GrunnlagDto
      Executable { assertThat(grunnlagDtoListe).isNotNull() },
      Executable { assertThat(grunnlagDtoListe.size).isEqualTo(4) },
      Executable { assertThat(grunnlagDtoListe[0].grunnlagReferanse).isEqualTo("BM-LIGS-19") },
      Executable { assertThat(grunnlagDtoListe[0].grunnlagType).isEqualTo("INNTEKT") },
      Executable { assertThat(grunnlagDtoListe[0].grunnlagInnhold).isEmpty() },

      Executable { assertThat(grunnlagDtoListe[1].grunnlagReferanse).isEqualTo("BM-LIGN-19") },
      Executable { assertThat(grunnlagDtoListe[1].grunnlagType).isEqualTo("INNTEKT") },
      Executable { assertThat(grunnlagDtoListe[1].grunnlagInnhold).isEmpty() },

      Executable { assertThat(grunnlagDtoListe[2].grunnlagReferanse).isEqualTo("BP-SKATTEKLASSE-19") },
      Executable { assertThat(grunnlagDtoListe[2].grunnlagType).isEqualTo("SKATTEKLASSE") },
      Executable { assertThat(grunnlagDtoListe[2].grunnlagInnhold).isEmpty() },

      Executable { assertThat(grunnlagDtoListe[3].grunnlagReferanse).isEqualTo("SJAB-REF001") },
      Executable { assertThat(grunnlagDtoListe[3].grunnlagType).isEqualTo("SJABLON") },
      Executable { assertThat(grunnlagDtoListe[3].grunnlagInnhold).isEmpty() },

      // Sjekk PeriodeGrunnlagDto
      Executable { assertThat(periodeGrunnlagDtoListe).isNotNull() },
      Executable { assertThat(periodeGrunnlagDtoListe.size).isEqualTo(11) }
    )
  }

  object MockitoHelper {
    // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
    fun <T> any(type: Class<T>): T = Mockito.any(type)
  }
}
