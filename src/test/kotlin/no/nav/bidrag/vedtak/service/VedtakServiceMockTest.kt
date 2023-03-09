package no.nav.bidrag.vedtak.service

import no.nav.bidrag.behandling.felles.enums.EngangsbelopType
import no.nav.bidrag.behandling.felles.enums.VedtakKilde
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.vedtak.TestUtil.Companion.byggBehandlingsreferanse
import no.nav.bidrag.vedtak.TestUtil.Companion.byggEngangsbelop
import no.nav.bidrag.vedtak.TestUtil.Companion.byggEngangsbelopGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggPeriode
import no.nav.bidrag.vedtak.TestUtil.Companion.byggPeriodeGrunnlag
import no.nav.bidrag.vedtak.TestUtil.Companion.byggStonadsendring
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtak
import no.nav.bidrag.vedtak.TestUtil.Companion.byggVedtakRequest
import no.nav.bidrag.vedtak.bo.EngangsbelopGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
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
  private lateinit var vedtakCaptor: ArgumentCaptor<Vedtak>

  @Captor
  private lateinit var stonadsendringCaptor: ArgumentCaptor<Stonadsendring>

  @Captor
  private lateinit var engangsbelopCaptor: ArgumentCaptor<Engangsbelop>

  @Captor
  private lateinit var periodeCaptor: ArgumentCaptor<Periode>

  @Captor
  private lateinit var grunnlagCaptor: ArgumentCaptor<Grunnlag>

  @Captor
  private lateinit var periodeGrunnlagBoCaptor: ArgumentCaptor<PeriodeGrunnlagBo>

  @Captor
  private lateinit var engangsbelopGrunnlagBoCaptor: ArgumentCaptor<EngangsbelopGrunnlagBo>

  @Captor
  private lateinit var behandlingsreferanseCaptor: ArgumentCaptor<Behandlingsreferanse>


  @Test
  fun `skal opprette nytt vedtak`() {

    Mockito.`when`(persistenceServiceMock.opprettVedtak(MockitoHelper.capture(vedtakCaptor)))
      .thenReturn(byggVedtak())
    Mockito.`when`(persistenceServiceMock.opprettStonadsendring(MockitoHelper.capture(stonadsendringCaptor)))
      .thenReturn(byggStonadsendring())
    Mockito.`when`(persistenceServiceMock.opprettEngangsbelop(MockitoHelper.capture(engangsbelopCaptor)))
      .thenReturn(byggEngangsbelop())
    Mockito.`when`(persistenceServiceMock.opprettPeriode(MockitoHelper.capture(periodeCaptor)))
      .thenReturn(byggPeriode())
    Mockito.`when`(persistenceServiceMock.opprettGrunnlag(MockitoHelper.capture(grunnlagCaptor)))
      .thenReturn(byggGrunnlag())
    Mockito.`when`(persistenceServiceMock.opprettPeriodeGrunnlag(MockitoHelper.capture(periodeGrunnlagBoCaptor)))
      .thenReturn(byggPeriodeGrunnlag())
    Mockito.`when`(persistenceServiceMock.opprettEngangsbelopGrunnlag(MockitoHelper.capture(engangsbelopGrunnlagBoCaptor)))
      .thenReturn(byggEngangsbelopGrunnlag())
    Mockito.`when`(persistenceServiceMock.opprettBehandlingsreferanse(MockitoHelper.capture(behandlingsreferanseCaptor)))
      .thenReturn(byggBehandlingsreferanse())

    val vedtak = byggVedtakRequest()
    val nyttVedtakOpprettet = vedtakService.opprettVedtak(vedtak)

    val vedtakDto = vedtakCaptor.value
    val stonadsendringDtoListe = stonadsendringCaptor.allValues
    val engangsbelopDtoListe = engangsbelopCaptor.allValues
    val periodeDtoListe = periodeCaptor.allValues
    val grunnlagDtoListe = grunnlagCaptor.allValues
    val periodeGrunnlagBoListe = periodeGrunnlagBoCaptor.allValues
    val engangsbelopGrunnlagBoListe = engangsbelopGrunnlagBoCaptor.allValues
    val behandlingsreferanseListe = behandlingsreferanseCaptor.allValues

    Mockito.verify(persistenceServiceMock, Mockito.times(1)).opprettVedtak(MockitoHelper.any(Vedtak::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettStonadsendring(MockitoHelper.any(Stonadsendring::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettEngangsbelop(MockitoHelper.any(Engangsbelop::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(4)).opprettPeriode(MockitoHelper.any(Periode::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(4)).opprettGrunnlag(MockitoHelper.any(Grunnlag::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(11)).opprettPeriodeGrunnlag(MockitoHelper.any(PeriodeGrunnlagBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(6)).opprettEngangsbelopGrunnlag(MockitoHelper.any(EngangsbelopGrunnlagBo::class.java))
    Mockito.verify(persistenceServiceMock, Mockito.times(2)).opprettBehandlingsreferanse(MockitoHelper.any(Behandlingsreferanse::class.java))

    assertAll(
      Executable { assertThat(nyttVedtakOpprettet).isNotNull() },

      // Sjekk VedtakDto
      Executable { assertThat(vedtakDto).isNotNull() },
      Executable { assertThat(vedtakDto.type).isEqualTo(vedtak.type.toString()) },
      Executable { assertThat(vedtakDto.enhetId).isEqualTo(vedtak.enhetId) },
      Executable { assertThat(vedtakDto.opprettetAv).isEqualTo(vedtak.opprettetAv) },
      Executable { assertThat(vedtakDto.vedtakTidspunkt).isEqualTo(vedtak.vedtakTidspunkt) },
      Executable { assertThat(vedtakDto.utsattTilDato).isEqualTo(vedtak.utsattTilDato) },

      // Sjekk StonadsendringDto
      Executable { assertThat(stonadsendringDtoListe).isNotNull() },
      Executable { assertThat(stonadsendringDtoListe.size).isEqualTo(2) },

      Executable { assertThat(stonadsendringDtoListe[0].type).isEqualTo(vedtak.stonadsendringListe!![0].type.toString()) },
      Executable { assertThat(stonadsendringDtoListe[0].sakId).isEqualTo(vedtak.stonadsendringListe!![0].sakId) },
      Executable { assertThat(stonadsendringDtoListe[0].skyldnerId).isEqualTo(vedtak.stonadsendringListe!![0].skyldnerId) },
      Executable { assertThat(stonadsendringDtoListe[0].kravhaverId).isEqualTo(vedtak.stonadsendringListe!![0].kravhaverId) },
      Executable { assertThat(stonadsendringDtoListe[0].mottakerId).isEqualTo(vedtak.stonadsendringListe!![0].mottakerId) },
      Executable { assertThat(stonadsendringDtoListe[0].innkreving).isEqualTo(vedtak.stonadsendringListe!![0].innkreving.toString()) },
      Executable { assertThat(stonadsendringDtoListe[0].endring).isEqualTo(vedtak.stonadsendringListe!![0].endring) },

      Executable { assertThat(stonadsendringDtoListe[1].type).isEqualTo(vedtak.stonadsendringListe!![1].type.toString()) },
      Executable { assertThat(stonadsendringDtoListe[1].sakId).isEqualTo(vedtak.stonadsendringListe!![1].sakId) },
      Executable { assertThat(stonadsendringDtoListe[1].skyldnerId).isEqualTo(vedtak.stonadsendringListe!![1].skyldnerId) },
      Executable { assertThat(stonadsendringDtoListe[1].kravhaverId).isEqualTo(vedtak.stonadsendringListe!![1].kravhaverId) },
      Executable { assertThat(stonadsendringDtoListe[1].mottakerId).isEqualTo(vedtak.stonadsendringListe!![1].mottakerId) },
      Executable { assertThat(stonadsendringDtoListe[1].innkreving).isEqualTo(vedtak.stonadsendringListe!![1].innkreving.toString()) },
      Executable { assertThat(stonadsendringDtoListe[1].endring).isEqualTo(vedtak.stonadsendringListe!![1].endring) },

      // Sjekk EngangsbelopDto
      Executable { assertThat(engangsbelopDtoListe).isNotNull() },
      Executable { assertThat(engangsbelopDtoListe.size).isEqualTo(2) },

      Executable { assertThat(engangsbelopDtoListe[0].type).isEqualTo(vedtak.engangsbelopListe!![0].type.toString()) },
      Executable { assertThat(engangsbelopDtoListe[0].sakId).isEqualTo(vedtak.engangsbelopListe!![0].sakId) },
      Executable { assertThat(engangsbelopDtoListe[0].skyldnerId).isEqualTo(vedtak.engangsbelopListe!![0].skyldnerId) },
      Executable { assertThat(engangsbelopDtoListe[0].kravhaverId).isEqualTo(vedtak.engangsbelopListe!![0].kravhaverId) },
      Executable { assertThat(engangsbelopDtoListe[0].mottakerId).isEqualTo(vedtak.engangsbelopListe!![0].mottakerId) },
      Executable { assertThat(engangsbelopDtoListe[0].belop).isEqualTo(vedtak.engangsbelopListe!![0].belop) },
      Executable { assertThat(engangsbelopDtoListe[0].valutakode).isEqualTo(vedtak.engangsbelopListe!![0].valutakode) },
      Executable { assertThat(engangsbelopDtoListe[0].resultatkode).isEqualTo(vedtak.engangsbelopListe!![0].resultatkode) },
      Executable { assertThat(engangsbelopDtoListe[0].referanse).isEqualTo(vedtak.engangsbelopListe!![0].referanse) },
      Executable { assertThat(engangsbelopDtoListe[0].innkreving).isEqualTo(vedtak.engangsbelopListe!![0].innkreving.toString()) },
      Executable { assertThat(engangsbelopDtoListe[0].endring).isEqualTo(vedtak.engangsbelopListe!![0].endring) },

      Executable { assertThat(engangsbelopDtoListe[1].type).isEqualTo(vedtak.engangsbelopListe!![1].type.toString()) },
      Executable { assertThat(engangsbelopDtoListe[1].sakId).isEqualTo(vedtak.engangsbelopListe!![1].sakId) },
      Executable { assertThat(engangsbelopDtoListe[1].skyldnerId).isEqualTo(vedtak.engangsbelopListe!![1].skyldnerId) },
      Executable { assertThat(engangsbelopDtoListe[1].kravhaverId).isEqualTo(vedtak.engangsbelopListe!![1].kravhaverId) },
      Executable { assertThat(engangsbelopDtoListe[1].mottakerId).isEqualTo(vedtak.engangsbelopListe!![1].mottakerId) },
      Executable { assertThat(engangsbelopDtoListe[1].belop).isEqualTo(vedtak.engangsbelopListe!![1].belop) },
      Executable { assertThat(engangsbelopDtoListe[1].valutakode).isEqualTo(vedtak.engangsbelopListe!![1].valutakode) },
      Executable { assertThat(engangsbelopDtoListe[1].resultatkode).isEqualTo(vedtak.engangsbelopListe!![1].resultatkode) },
      Executable { assertThat(engangsbelopDtoListe[1].referanse).isEqualTo(vedtak.engangsbelopListe!![1].referanse) },
      Executable { assertThat(engangsbelopDtoListe[1].innkreving).isEqualTo(vedtak.engangsbelopListe!![1].innkreving.toString()) },
      Executable { assertThat(engangsbelopDtoListe[1].endring).isEqualTo(vedtak.engangsbelopListe!![1].endring) },

      // Sjekk PeriodeDto
      Executable { assertThat(periodeDtoListe).isNotNull() },
      Executable { assertThat(periodeDtoListe.size).isEqualTo(4) },

      Executable { assertThat(periodeDtoListe[0].fomDato).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[0].fomDato) },
      Executable { assertThat(periodeDtoListe[0].tilDato).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[0].tilDato) },
      Executable { assertThat(periodeDtoListe[0].belop).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[0].belop) },
      Executable { assertThat(periodeDtoListe[0].valutakode).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[0].valutakode) },
      Executable { assertThat(periodeDtoListe[0].resultatkode).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[0].resultatkode) },

      Executable { assertThat(periodeDtoListe[1].fomDato).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[1].fomDato) },
      Executable { assertThat(periodeDtoListe[1].tilDato).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[1].tilDato) },
      Executable { assertThat(periodeDtoListe[1].belop).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[1].belop) },
      Executable { assertThat(periodeDtoListe[1].valutakode).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[1].valutakode) },
      Executable { assertThat(periodeDtoListe[1].resultatkode).isEqualTo(vedtak.stonadsendringListe!![0].periodeListe[1].resultatkode) },

      Executable { assertThat(periodeDtoListe[2].fomDato).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[0].fomDato) },
      Executable { assertThat(periodeDtoListe[2].tilDato).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[0].tilDato) },
      Executable { assertThat(periodeDtoListe[2].belop).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[0].belop) },
      Executable { assertThat(periodeDtoListe[2].valutakode).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[0].valutakode) },
      Executable { assertThat(periodeDtoListe[2].resultatkode).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[0].resultatkode) },

      Executable { assertThat(periodeDtoListe[3].fomDato).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[1].fomDato) },
      Executable { assertThat(periodeDtoListe[3].tilDato).isEqualTo(vedtak.stonadsendringListe!![1].periodeListe[1].tilDato) },
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
      Executable { assertThat(periodeGrunnlagBoListe).isNotNull() },
      Executable { assertThat(periodeGrunnlagBoListe.size).isEqualTo(11) },

      // Sjekk EngangsbelopGrunnlagDto
      Executable { assertThat(engangsbelopGrunnlagBoListe).isNotNull() },
      Executable { assertThat(engangsbelopGrunnlagBoListe.size).isEqualTo(6) },

      // Sjekk BehandlingsreferanseDto
      Executable { assertThat(behandlingsreferanseListe).isNotNull() },
      Executable { assertThat(behandlingsreferanseListe.size).isEqualTo(2) }


    )
  }

  @Test
  fun `skal hente vedtak`() {

    // Hent vedtak
    Mockito.`when`(persistenceServiceMock.hentVedtak(MockitoHelper.any(Int::class.java))).thenReturn(byggVedtak(vedtakId = 1))
    Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForVedtak(MockitoHelper.any(Int::class.java)))
      .thenReturn(
        listOf(
          byggGrunnlag(grunnlagId = 1, vedtak = byggVedtak(), grunnlagReferanse = "REF1"),
          byggGrunnlag(grunnlagId = 2, vedtak = byggVedtak(), grunnlagReferanse = "REF2")
        )
      )
    Mockito.`when`(persistenceServiceMock.hentAlleStonadsendringerForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
      listOf(
        byggStonadsendring(stonadsendringId = 1),
        byggStonadsendring(stonadsendringId = 2)
      )
    )
    Mockito.`when`(persistenceServiceMock.hentAlleEngangsbelopForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
      listOf(
        byggEngangsbelop(engangsbelopId =  1, type = "SAERTILSKUDD", sakId = "SAK-101", skyldnerId = "01018011111",
          kravhaverId = "01010511111", mottakerId = "01018211111", belop = BigDecimal.valueOf(3490), valutakode = "NOK",
          resultatkode = "SAERTILSKUDD BEREGNET", innkreving = "JA", endring = true, omgjorVedtakId = 123, referanse = "Referanse1",
          delytelseId = "delytelseId1", eksternReferanse = "EksternRef1")
      )
    )
    Mockito.`when`(persistenceServiceMock.hentAllePerioderForStonadsendring(MockitoHelper.any(Int::class.java)))
      .thenReturn(
        listOf(
          byggPeriode(periodeId = 1, belop = BigDecimal.valueOf(100)),
          byggPeriode(periodeId = 2, belop = BigDecimal.valueOf(200))
        )
      )
    Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForPeriode(MockitoHelper.any(Int::class.java))).thenReturn(
      listOf(byggPeriodeGrunnlag(periode = byggPeriode(periodeId = 1), grunnlag = byggGrunnlag(grunnlagId = 1)),
      byggPeriodeGrunnlag(periode = byggPeriode(periodeId = 2), grunnlag = byggGrunnlag(grunnlagId = 2)))
    )
    Mockito.`when`(persistenceServiceMock.hentGrunnlag(MockitoHelper.any(Int::class.java))).thenReturn(
      byggGrunnlag(grunnlagId = 1, vedtak = byggVedtak(), grunnlagReferanse = "REF1")
    )
    Mockito.`when`(persistenceServiceMock.hentAlleGrunnlagForEngangsbelop(MockitoHelper.any(Int::class.java))).thenReturn(
      listOf(byggEngangsbelopGrunnlag(engangsbelop = byggEngangsbelop(engangsbelopId = 1), grunnlag = byggGrunnlag(grunnlagId = 1)),
      byggEngangsbelopGrunnlag(engangsbelop = byggEngangsbelop(engangsbelopId = 2), grunnlag = byggGrunnlag(grunnlagId = 2)))
    )
    Mockito.`when`(persistenceServiceMock.hentAlleBehandlingsreferanserForVedtak(MockitoHelper.any(Int::class.java))).thenReturn(
      listOf(byggBehandlingsreferanse(kilde = "BISYS_SOKNAD", referanse = "Bisys-01"),
        byggBehandlingsreferanse(kilde = "BISYS_SOKNAD", referanse = "Bisys-02"))
    )

    val vedtakFunnet = vedtakService.hentVedtak(1)

    assertAll(
      Executable { assertThat(vedtakFunnet).isNotNull() },
      Executable { assertThat(vedtakFunnet.kilde).isEqualTo(VedtakKilde.MANUELT) },
      Executable { assertThat(vedtakFunnet.type).isEqualTo(VedtakType.ALDERSJUSTERING) },
      Executable { assertThat(vedtakFunnet.grunnlagListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.grunnlagListe[0].referanse).isEqualTo("REF1") },
      Executable { assertThat(vedtakFunnet.stonadsendringListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].belop).isEqualTo(BigDecimal.valueOf(100)) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe.size).isEqualTo(2) },
      Executable { assertThat(vedtakFunnet.stonadsendringListe[0].periodeListe[0].grunnlagReferanseListe[0]).isEqualTo("REF1") },
      Executable { assertThat(vedtakFunnet.engangsbelopListe.size).isEqualTo(1) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].type).isEqualTo(EngangsbelopType.SAERTILSKUDD) },
      Executable { assertThat(vedtakFunnet.engangsbelopListe[0].sakId).isEqualTo("SAK-101") },
      Executable { assertThat(vedtakFunnet.behandlingsreferanseListe.size).isEqualTo(2) },
    )
  }

  object MockitoHelper {

    // use this in place of captor.capture() if you are trying to capture an argument that is not nullable
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

    fun <T> any(type: Class<T>): T = Mockito.any(type)
  }
}
