package no.nav.bidrag.vedtak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettBehandlingsreferanseRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettEngangsbelopRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettGrunnlagRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettStonadsendringRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakRequestDto
import no.nav.bidrag.behandling.felles.enums.GrunnlagType
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.vedtak.bo.BehandlingsreferanseBo
import no.nav.bidrag.vedtak.bo.EngangsbelopBo
import no.nav.bidrag.vedtak.bo.EngangsbelopGrunnlagBo
import no.nav.bidrag.vedtak.bo.GrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.bo.StonadsendringBo
import no.nav.bidrag.vedtak.bo.VedtakBo
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {

  companion object {

    fun byggVedtakRequest() = OpprettVedtakRequestDto(
      vedtakType = VedtakType.MANUELT,
      opprettetAv = "X123456",
      vedtakDato = LocalDate.parse("2021-11-01"),
      enhetId = "4812",
      grunnlagListe = byggGrunnlagListe(),
      stonadsendringListe = byggStonadsendringListe(),
      engangsbelopListe = byggEngangsbelopListe(),
      behandlingsreferanseListe = byggBehandlingsreferanseListe()
    )

    private fun byggGrunnlagListe() = listOf(
      OpprettGrunnlagRequestDto(
        referanse = "BM-LIGS-19",
        type = GrunnlagType.INNTEKT,
        innhold =  ObjectMapper().readTree(
          """
          {
            "inntektDatoFraTil": {
              "periodeDatoFra": "2019-01-01",
              "periodeDatoTil": "2020-01-01"
            },
            "inntektBelop": 400000,
            "inntektType": "SKATTEGRUNNLAG_SKE"
          }"""
        )
      ),
      OpprettGrunnlagRequestDto(
        referanse = "BM-LIGN-19",
        type = GrunnlagType.INNTEKT,
        innhold = ObjectMapper().readTree(
          """
          {
            "inntektDatoFraTil": {
              "periodeDatoFra": "2019-01-01",
              "periodeDatoTil": "2020-01-01"
            },
            "inntektBelop": 400000,
            "inntektType": "LIGNING_SKE"
          }"""
        )
      ),
      OpprettGrunnlagRequestDto(
        referanse = "BP-SKATTEKLASSE-19",
        type = GrunnlagType.SKATTEKLASSE,
        innhold = ObjectMapper().readTree(
          """
          {
            "skatteklasseDatoFraTil": {
              "periodeDatoFra": "2019-01-01",
              "periodeDatoTil": "2020-01-01"
            },
            "skatteKlasseId": 1
          }"""
        )
      ),
      OpprettGrunnlagRequestDto(
        referanse = "SJAB-REF001",
        type = GrunnlagType.SJABLON,
        innhold = ObjectMapper().readTree(
          """
          {
            "sjablonListe": [
              {
                "sjablonNavn": "BoutgiftBeløp",
                "sjablonVerdi": 5981
              },
              {
                "sjablonNavn": "FordelSærfradragBeløp",
                "sjablonVerdi": 12977
              }
            ]
          }"""
        )
      )
    )

    private fun byggStonadsendringListe() = listOf(
      OpprettStonadsendringRequestDto(
        stonadType = StonadType.BIDRAG,
        sakId = "SAK-001",
        behandlingId = "Fritekst",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        periodeListe = listOf(
          OpprettVedtakPeriodeRequestDto(
            periodeFomDato = LocalDate.parse("2019-01-01"),
            periodeTilDato = LocalDate.parse("2019-07-01"),
            belop = BigDecimal.valueOf(3490),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            grunnlagReferanseListe = listOf(
                "BM-LIGS-19",
                "BM-LIGN-19",
               "SJAB-REF001")
          )
          ,
          OpprettVedtakPeriodeRequestDto(
            periodeFomDato = LocalDate.parse("2019-07-01"),
            periodeTilDato = LocalDate.parse("2020-01-01"),
            belop = BigDecimal.valueOf(3520),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            grunnlagReferanseListe = listOf(
              "BM-LIGS-19",
              "BM-LIGN-19",
              "BP-SKATTEKLASSE-19",
              "SJAB-REF001")
          )
        )
      ),
      OpprettStonadsendringRequestDto(
        stonadType = StonadType.BIDRAG,
        sakId = "SAK-001",
        behandlingId = "Fritekst",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        periodeListe = listOf(
          OpprettVedtakPeriodeRequestDto(
            periodeFomDato = LocalDate.parse("2019-06-01"),
            periodeTilDato = LocalDate.parse("2019-07-01"),
            belop = BigDecimal.valueOf(4240),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            grunnlagReferanseListe = listOf(
              "BM-LIGS-19",
              "SJAB-REF001")
          )
          ,
          OpprettVedtakPeriodeRequestDto(
            periodeFomDato = LocalDate.parse("2019-08-01"),
            periodeTilDato = LocalDate.parse("2019-09-01"),
            belop = BigDecimal.valueOf(3410),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            grunnlagReferanseListe = listOf(
              "BM-LIGS-19",
              "SJAB-REF001")
          )
        )
      )
    )

    private fun byggEngangsbelopListe() = listOf(
      OpprettEngangsbelopRequestDto(
        vedtakId = 0,
        lopenr = 1,
        endrerEngangsbelopId = null,
        type = "SAERTILSKUDD",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        belop = BigDecimal.valueOf(3490),
        valutakode = "NOK",
        resultatkode = "SAERTILSKUDD BEREGNET",
        grunnlagReferanseListe = listOf(
          "BM-LIGS-19",
          "BM-LIGN-19",
          "SJAB-REF001")
      ),
      OpprettEngangsbelopRequestDto(
        vedtakId = 0,
        lopenr = 2,
        endrerEngangsbelopId = 1,
        type = "SAERTILSKUDD",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        belop = BigDecimal.valueOf(2990),
        valutakode = "NOK",
        resultatkode = "SAERTILSKUDD BEREGNET",
        grunnlagReferanseListe = listOf(
          "BM-LIGS-19",
          "BM-LIGN-19",
          "SJAB-REF001")
      )
    )

    private fun byggBehandlingsreferanseListe() = listOf(
      OpprettBehandlingsreferanseRequestDto(
        vedtakId = 0,
        kilde = "Bisys",
        referanse = "Bisysreferanse01"
        ),
      OpprettBehandlingsreferanseRequestDto(
        vedtakId = 0,
        kilde = "Bisys",
        referanse = "Bisysreferanse02"
      )
    )


    fun byggVedtakDto(
      vedtakId: Int = (1..100).random(),
      vedtakType: String = VedtakType.MANUELT.toString(),
      enhetId: String = "4812",
      opprettetAv: String = "X123456",
      opprettetTimestamp: LocalDateTime? = LocalDateTime.now()
    ) = VedtakBo(
      vedtakId = vedtakId,
      vedtakType = vedtakType,
      enhetId = enhetId,
      opprettetAv = opprettetAv,
      opprettetTimestamp = opprettetTimestamp!!
    )

    fun byggStonadsendringDto(
      stonadsendringId: Int = (1..100).random(),
      stonadType: String = StonadType.BIDRAG.toString(),
      vedtakId: Int = (1..100).random(),
      sakId: String = "SAK-001",
      behandlingId: String = "Fritekst",
      skyldnerId: String = "01018011111",
      kravhaverId: String = "01010511111",
      mottakerId: String = "01018211111"
    ) = StonadsendringBo(
      stonadsendringId = stonadsendringId,
      stonadType = stonadType,
      vedtakId = vedtakId,
      sakId = sakId,
      behandlingId = behandlingId,
      skyldnerId = skyldnerId,
      kravhaverId = kravhaverId,
      mottakerId = mottakerId
    )

    fun byggPeriodeDto(
      periodeId: Int = (1..100).random(),
      periodeFomDato: LocalDate = LocalDate.parse("2019-07-01"),
      periodeTilDato: LocalDate? = LocalDate.parse("2020-01-01"),
      stonadsendringId: Int = (1..100).random(),
      belop: BigDecimal = BigDecimal.valueOf(3520),
      valutakode: String = "NOK",
      resultatkode: String = "KOSTNADSBEREGNET_BIDRAG"
    ) = PeriodeBo(
      periodeId = periodeId,
      periodeFomDato = periodeFomDato,
      periodeTilDato = periodeTilDato,
      stonadsendringId = stonadsendringId,
      belop = belop,
      valutakode = valutakode,
      resultatkode = resultatkode
    )

    fun byggGrunnlagDto(
      grunnlagId: Int = (1..100).random(),
      grunnlagReferanse: String = "BM-LIGN-19",
      vedtakId: Int = (1..100).random(),
      type: String = GrunnlagType.INNTEKT.toString(),
      innhold: String = "Innhold"
    ) = GrunnlagBo(
      grunnlagId = grunnlagId,
      referanse = grunnlagReferanse,
      vedtakId = vedtakId,
      type = type,
      innhold = innhold
    )

    fun byggPeriodeGrunnlagDto(
      periodeId: Int = (1..100).random(),
      grunnlagId: Int = (1..100).random()
    ) = PeriodeGrunnlagBo(
      periodeId = periodeId,
      grunnlagId = grunnlagId
    )

    fun byggEngangsbelopDto(
      engangsbelopId: Int = (1..100).random(),
      vedtakId: Int = (1..100).random(),
      lopenr: Int = (1..100).random(),
      endrerEngangsbelopId: Int? = null,
      type: String = "SAERTILSKUDD",
      skyldnerId: String = "01018011111",
      kravhaverId: String = "01010511111",
      mottakerId: String = "01018211111",
      belop: BigDecimal = BigDecimal.valueOf(3490),
      valutakode: String = "NOK",
      resultatkode: String = "SAERTILSKUDD BEREGNET"
    ) = EngangsbelopBo(
      engangsbelopId = engangsbelopId,
      vedtakId = vedtakId,
      lopenr = lopenr,
      endrerEngangsbelopId = endrerEngangsbelopId,
      type = type,
      skyldnerId = skyldnerId,
      kravhaverId = kravhaverId,
      mottakerId = mottakerId,
      belop = belop,
      valutakode = valutakode,
      resultatkode = resultatkode
    )

    fun byggEngangsbelopGrunnlagDto(
      engangsbelopId: Int = (1..100).random(),
      grunnlagId: Int = (1..100).random()
    ) = EngangsbelopGrunnlagBo(
      engangsbelopId = engangsbelopId,
      grunnlagId = grunnlagId
    )

    fun byggBehandlingsreferanseDto(
      behandlingsreferanseId: Int = (1..100).random(),
      vedtakId: Int = (1..100).random(),
      kilde: String = "Bisys",
      referanse: String = "Bisysreferanse01"
    ) = BehandlingsreferanseBo(
      behandlingsreferanseId = behandlingsreferanseId,
      vedtakId = vedtakId,
      kilde = kilde,
      referanse = referanse
    )
  }
}