package no.nav.bidrag.vedtak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettBehandlingsreferanseRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettEngangsbelopRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettGrunnlagRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettStonadsendringRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakPeriodeRequestDto
import no.nav.bidrag.behandling.felles.dto.vedtak.OpprettVedtakRequestDto
import no.nav.bidrag.behandling.felles.enums.BehandlingsrefKilde
import no.nav.bidrag.behandling.felles.enums.EngangsbelopType
import no.nav.bidrag.behandling.felles.enums.GrunnlagType
import no.nav.bidrag.behandling.felles.enums.Innkreving
import no.nav.bidrag.behandling.felles.enums.StonadType
import no.nav.bidrag.behandling.felles.enums.VedtakKilde
import no.nav.bidrag.behandling.felles.enums.VedtakType
import no.nav.bidrag.vedtak.bo.EngangsbelopGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import no.nav.bidrag.vedtak.persistence.entity.Engangsbelop
import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {

  companion object {

    fun byggVedtakRequest() = OpprettVedtakRequestDto(
      kilde = VedtakKilde.MANUELT,
      type= VedtakType.ALDERSJUSTERING,
      opprettetAv = "X123456",
      dato = LocalDate.parse("2021-11-01"),
      enhetId = "4812",
      eksternReferanse = "eksternReferanse1",
      utsattTilDato = LocalDate.now(),
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
        type = StonadType.BIDRAG,
        sakId = "SAK-001",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        indeksreguleringAar = "2024",
        innkreving = Innkreving.JA,
        periodeListe = listOf(
          OpprettVedtakPeriodeRequestDto(
            fomDato = LocalDate.parse("2019-01-01"),
            tilDato = LocalDate.parse("2019-07-01"),
            belop = BigDecimal.valueOf(3490),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            referanse = "referanse1",
            grunnlagReferanseListe = listOf(
                "BM-LIGS-19",
                "BM-LIGN-19",
               "SJAB-REF001")
          )
          ,
          OpprettVedtakPeriodeRequestDto(
            fomDato = LocalDate.parse("2019-07-01"),
            tilDato = LocalDate.parse("2020-01-01"),
            belop = BigDecimal.valueOf(3520),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            referanse = "referanse2",
            grunnlagReferanseListe = listOf(
              "BM-LIGS-19",
              "BM-LIGN-19",
              "BP-SKATTEKLASSE-19",
              "SJAB-REF001")
          )
        )
      ),
      OpprettStonadsendringRequestDto(
        type = StonadType.BIDRAG,
        sakId = "SAK-001",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        indeksreguleringAar = "2024",
        innkreving = Innkreving.JA,
        periodeListe = listOf(
          OpprettVedtakPeriodeRequestDto(
            fomDato = LocalDate.parse("2019-06-01"),
            tilDato = LocalDate.parse("2019-07-01"),
            belop = BigDecimal.valueOf(4240),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            referanse = "referanse3",
            grunnlagReferanseListe = listOf(
              "BM-LIGS-19",
              "SJAB-REF001")
          )
          ,
          OpprettVedtakPeriodeRequestDto(
            fomDato = LocalDate.parse("2019-08-01"),
            tilDato = LocalDate.parse("2019-09-01"),
            belop = BigDecimal.valueOf(3410),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            referanse = "referanse4",
            grunnlagReferanseListe = listOf(
              "BM-LIGS-19",
              "SJAB-REF001")
          )
        )
      )
    )

    private fun byggEngangsbelopListe() = listOf(
      OpprettEngangsbelopRequestDto(
        endrerId = null,
        type = EngangsbelopType.SAERTILSKUDD,
        sakId = "SAK-101",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        belop = BigDecimal.valueOf(3490),
        valutakode = "NOK",
        resultatkode = "SAERTILSKUDD BEREGNET",
        referanse = "referanse1",
        innkreving = Innkreving.JA,
        grunnlagReferanseListe = listOf(
          "BM-LIGS-19",
          "BM-LIGN-19",
          "SJAB-REF001")
      ),
      OpprettEngangsbelopRequestDto(
        endrerId = 1,
        type = EngangsbelopType.SAERTILSKUDD,
        sakId = "SAK-101",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        belop = BigDecimal.valueOf(2990),
        valutakode = "NOK",
        resultatkode = "SAERTILSKUDD BEREGNET",
        referanse = "referanse2",
        innkreving = Innkreving.JA,
        grunnlagReferanseListe = listOf(
          "BM-LIGS-19",
          "BM-LIGN-19",
          "SJAB-REF001")
      )
    )

    private fun byggBehandlingsreferanseListe() = listOf(
      OpprettBehandlingsreferanseRequestDto(
        kilde = BehandlingsrefKilde.BISYS_SOKNAD,
        referanse = "Bisysreferanse01"
        ),
      OpprettBehandlingsreferanseRequestDto(
        kilde = BehandlingsrefKilde.BISYS_SOKNAD,
        referanse = "Bisysreferanse02"
      )
    )


    fun byggVedtak(
      vedtakId: Int = (1..100).random(),
      kilde: String = VedtakKilde.MANUELT.toString(),
      type: String = VedtakType.ALDERSJUSTERING.toString(),
      enhetId: String = "4812",
      dato: LocalDate = LocalDate.now(),
      opprettetAv: String = "X123456",
      opprettetTimestamp: LocalDateTime = LocalDateTime.now(),
      eksternReferanse: String = "eksternReferanse1",
      utsattTilDato: LocalDate = LocalDate.now()
    ) = Vedtak(
      id = vedtakId,
      kilde = kilde,
      type = type,
      enhetId = enhetId,
      dato = dato,
      opprettetAv = opprettetAv,
      opprettetTimestamp = opprettetTimestamp,
      eksternReferanse = eksternReferanse,
      utsattTilDato = utsattTilDato,
    )

    fun byggStonadsendring(
      stonadsendringId: Int = (1..100).random(),
      type: String = StonadType.BIDRAG.toString(),
      sakId: String = "SAK-001",
      skyldnerId: String = "01018011111",
      kravhaverId: String = "01010511111",
      mottakerId: String = "01018211111",
      innkreving: String = Innkreving.JA.toString()
    ) = Stonadsendring(
      id = stonadsendringId,
      type = type,
      vedtak = byggVedtak(),
      sakId = sakId,
      skyldnerId = skyldnerId,
      kravhaverId = kravhaverId,
      mottakerId = mottakerId,
      innkreving = innkreving
    )

    fun byggPeriode(
      periodeId: Int = (1..100).random(),
      fomDato: LocalDate = LocalDate.parse("2019-07-01"),
      tilDato: LocalDate? = LocalDate.parse("2020-01-01"),
      belop: BigDecimal = BigDecimal.valueOf(3520),
      valutakode: String = "NOK",
      resultatkode: String = "KOSTNADSBEREGNET_BIDRAG",
      referanse: String = "referanse1",
      ) = Periode(
      id = periodeId,
      fomDato = fomDato,
      tilDato = tilDato,
      stonadsendring = byggStonadsendring(),
      belop = belop,
      valutakode = valutakode,
      resultatkode = resultatkode,
      referanse = referanse
    )

    fun byggGrunnlag(
      grunnlagId: Int = (1..100).random(),
      grunnlagReferanse: String = "BM-LIGN-19",
      vedtak: Vedtak = byggVedtak(),
      type: String = GrunnlagType.INNTEKT.toString(),
      innhold: String =
          """{
          "rolle": "BIDRAGSMOTTAKER",
          "datoFom": "2021-01-01",
            "datoTil": null,
         "sivilstandKode": "sivilstandkode1"
        }"""

    ) = Grunnlag(
      id = grunnlagId,
      referanse = grunnlagReferanse,
      vedtak = vedtak,
      type = type,
      innhold = innhold
    )

    fun byggPeriodeGrunnlagBo(
      periodeId: Int = byggPeriode().id,
      grunnlagId: Int = byggGrunnlag().id
    ) = PeriodeGrunnlagBo(
      periodeId = periodeId,
      grunnlagId = grunnlagId
    )

    fun byggPeriodeGrunnlag(
      periode: Periode = byggPeriode(),
      grunnlag: Grunnlag = byggGrunnlag()
    ) = PeriodeGrunnlag(
      periode = periode,
      grunnlag = grunnlag
    )

    fun byggEngangsbelop(
      engangsbelopId: Int = (1..100).random(),
      lopenr: Int = (1..100).random(),
      endrerId: Int? = null,
      type: String = "SAERTILSKUDD",
      sakId: String = "SAK-101",
      skyldnerId: String = "01018011111",
      kravhaverId: String = "01010511111",
      mottakerId: String = "01018211111",
      belop: BigDecimal = BigDecimal.valueOf(3490),
      valutakode: String = "NOK",
      resultatkode: String = "SAERTILSKUDD BEREGNET",
      referanse: String = "referanse1",
      innkreving: String = "JA"
      ) = Engangsbelop(
      id = engangsbelopId,
      vedtak = byggVedtak(),
      lopenr = lopenr,
      endrerId = endrerId,
      type = type,
      sakId = sakId,
      skyldnerId = skyldnerId,
      kravhaverId = kravhaverId,
      mottakerId = mottakerId,
      belop = belop,
      valutakode = valutakode,
      resultatkode = resultatkode,
      referanse = referanse,
      innkreving = innkreving
    )

    fun byggEngangsbelopGrunnlagBo(
      engangsbelopId: Int = (1..100).random(),
      grunnlagId: Int = (1..100).random()
    ) = EngangsbelopGrunnlagBo(
      engangsbelopId = engangsbelopId,
      grunnlagId = grunnlagId
    )

    fun byggEngangsbelopGrunnlag(
      engangsbelop: Engangsbelop = byggEngangsbelop(),
      grunnlag: Grunnlag = byggGrunnlag(),
    ) = EngangsbelopGrunnlag(
      engangsbelop = engangsbelop,
      grunnlag = grunnlag
    )

    fun byggBehandlingsreferanse(
      behandlingsreferanseId: Int = (1..100).random(),
      vedtak: Vedtak = byggVedtak(),
      kilde: String = "BISYS_SOKNAD",
      referanse: String = "Bisysreferanse01"
    ) = Behandlingsreferanse(
      id = behandlingsreferanseId,
      vedtak = vedtak,
      kilde = kilde,
      referanse = referanse
    )
  }
}