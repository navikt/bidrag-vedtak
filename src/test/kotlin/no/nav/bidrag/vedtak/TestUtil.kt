package no.nav.bidrag.vedtak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.vedtak.api.behandlingsreferanse.OpprettBehandlingsreferanseRequest
import no.nav.bidrag.vedtak.api.engangsbelop.OpprettEngangsbelopRequest
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagReferanseRequest
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.periode.OpprettPeriodeRequest
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.api.vedtak.OpprettVedtakRequest
import no.nav.bidrag.vedtak.dto.BehandlingsreferanseDto
import no.nav.bidrag.vedtak.dto.EngangsbelopDto
import no.nav.bidrag.vedtak.dto.EngangsbelopGrunnlagDto
import no.nav.bidrag.vedtak.dto.GrunnlagDto
import no.nav.bidrag.vedtak.dto.PeriodeDto
import no.nav.bidrag.vedtak.dto.PeriodeGrunnlagDto
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.dto.VedtakDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {

  companion object {

    fun byggVedtakRequest() = OpprettVedtakRequest(
      vedtakType = "Manuelt",
      opprettetAv = "X123456",
      vedtakDato = LocalDate.parse("2021-11-01"),
      enhetId = "4812",
      grunnlagListe = byggGrunnlagListe(),
      stonadsendringListe = byggStonadsendringListe(),
      engangsbelopListe = byggEngangsbelopListe(),
      behandlingsreferanseListe = byggBehandlingsreferanseListe()
    )

    private fun byggGrunnlagListe() = listOf(
      OpprettGrunnlagRequest(
        grunnlagReferanse = "BM-LIGS-19",
        vedtakId = 0,
        grunnlagType = "INNTEKT",
        grunnlagInnhold =  ObjectMapper().readTree(
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
      OpprettGrunnlagRequest(
        grunnlagReferanse = "BM-LIGN-19",
        vedtakId = 0,
        grunnlagType = "INNTEKT",
        grunnlagInnhold = ObjectMapper().readTree(
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
      OpprettGrunnlagRequest(
        grunnlagReferanse = "BP-SKATTEKLASSE-19",
        vedtakId = 0,
        grunnlagType = "SKATTEKLASSE",
        grunnlagInnhold = ObjectMapper().readTree(
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
      OpprettGrunnlagRequest(
        grunnlagReferanse = "SJAB-REF001",
        vedtakId = 0,
        grunnlagType = "SJABLON",
        grunnlagInnhold = ObjectMapper().readTree(
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
      OpprettStonadsendringRequest(
        stonadType = "BIDRAG",
        vedtakId = 0,
        sakId = "SAK-001",
        behandlingId = "Fritekst",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        periodeListe = listOf(
          OpprettPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-01-01"),
            periodeTilDato = LocalDate.parse("2019-07-01"),
            stonadsendringId = 0,
            belop = BigDecimal.valueOf(3490),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            grunnlagReferanseListe = listOf(
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19"
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGN-19"
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001"
              )
            )
          ),
          OpprettPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-07-01"),
            periodeTilDato = LocalDate.parse("2020-01-01"),
            stonadsendringId = 0,
            belop = BigDecimal.valueOf(3520),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            grunnlagReferanseListe = listOf(
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19"
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGN-19"
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BP-SKATTEKLASSE-19"
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001"
              )
            )
          )
        )
      ),
      OpprettStonadsendringRequest(
        stonadType = "FORSKUDD",
        vedtakId = 0,
        sakId = "SAK-001",
        behandlingId = "Fritekst",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        periodeListe = listOf(
          OpprettPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-06-01"),
            periodeTilDato = LocalDate.parse("2019-07-01"),
            stonadsendringId = 0,
            belop = BigDecimal.valueOf(4240),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            grunnlagReferanseListe = listOf(
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19"
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001"
              )
            )
          ),
          OpprettPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-08-01"),
            periodeTilDato = LocalDate.parse("2019-09-01"),
            stonadsendringId = 0,
            belop = BigDecimal.valueOf(3410),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            grunnlagReferanseListe = listOf(
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19"
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001"
              ))
          )
        )
      )
    )

    private fun byggEngangsbelopListe() = listOf(
      OpprettEngangsbelopRequest(
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
          OpprettGrunnlagReferanseRequest(
            grunnlagReferanse = "BM-LIGS-19"
          ),
          OpprettGrunnlagReferanseRequest(
            grunnlagReferanse = "BM-LIGN-19"
          ),
          OpprettGrunnlagReferanseRequest(
            grunnlagReferanse = "SJAB-REF001"
          )
        )
      ),
      OpprettEngangsbelopRequest(
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
          OpprettGrunnlagReferanseRequest(
            grunnlagReferanse = "BM-LIGS-19"
          ),
          OpprettGrunnlagReferanseRequest(
            grunnlagReferanse = "BM-LIGN-19"
          ),
          OpprettGrunnlagReferanseRequest(
            grunnlagReferanse = "SJAB-REF001"
          )
        )
      )
    )

    private fun byggBehandlingsreferanseListe() = listOf(
      OpprettBehandlingsreferanseRequest(
        vedtakId = 0,
        kilde = "Bisys",
        referanse = "Bisysreferanse01"
        ),
      OpprettBehandlingsreferanseRequest(
        vedtakId = 0,
        kilde = "Bisys",
        referanse = "Bisysreferanse02"
      )
    )


    fun byggVedtakDto(
      vedtakId: Int = (1..100).random(),
      vedtakType: String = "Manuelt",
      enhetId: String = "4812",
      opprettetAv: String = "X123456",
      opprettetTimestamp: LocalDateTime? = LocalDateTime.now()
    ) = VedtakDto(
      vedtakId = vedtakId,
      vedtakType = vedtakType,
      enhetId = enhetId,
      opprettetAv = opprettetAv,
      opprettetTimestamp = opprettetTimestamp!!
    )

    fun byggStonadsendringDto(
      stonadsendringId: Int = (1..100).random(),
      stonadType: String = "BIDRAG",
      vedtakId: Int = (1..100).random(),
      sakId: String = "SAK-001",
      behandlingId: String = "Fritekst",
      skyldnerId: String = "01018011111",
      kravhaverId: String = "01010511111",
      mottakerId: String = "01018211111"
    ) = StonadsendringDto(
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
    ) = PeriodeDto(
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
      grunnlagType: String = "INNTEKT",
      grunnlagInnhold: String = "Innhold"
    ) = GrunnlagDto(
      grunnlagId = grunnlagId,
      grunnlagReferanse = grunnlagReferanse,
      vedtakId = vedtakId,
      grunnlagType = grunnlagType,
      grunnlagInnhold = grunnlagInnhold
    )

    fun byggPeriodeGrunnlagDto(
      periodeId: Int = (1..100).random(),
      grunnlagId: Int = (1..100).random()
    ) = PeriodeGrunnlagDto(
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
    ) = EngangsbelopDto(
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
    ) = EngangsbelopGrunnlagDto(
      engangsbelopId = engangsbelopId,
      grunnlagId = grunnlagId
    )

    fun byggBehandlingsreferanseDto(
      behandlingsreferanseId: Int = (1..100).random(),
      vedtakId: Int = (1..100).random(),
      kilde: String = "Bisys",
      referanse: String = "Bisysreferanse01"
    ) = BehandlingsreferanseDto(
      behandlingsreferanseId = behandlingsreferanseId,
      vedtakId = vedtakId,
      kilde = kilde,
      referanse = referanse
    )
  }
}