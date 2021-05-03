package no.nav.bidrag.vedtak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagReferanseRequest
import no.nav.bidrag.vedtak.api.grunnlag.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.periode.OpprettPeriodeRequest
import no.nav.bidrag.vedtak.api.stonadsendring.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.api.vedtak.OpprettKomplettVedtakRequest
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

    fun byggKomplettVedtakRequest() = OpprettKomplettVedtakRequest(
      saksbehandlerId = "X123456",
      enhetId = "4812",
      grunnlagListe = byggGrunnlagListe(),
      stonadsendringListe = byggStonadsendringListe()
    )

    private fun byggGrunnlagListe() = listOf(
      OpprettGrunnlagRequest(
        grunnlagReferanse = "BM-LIGS-19",
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
        sakId = "SAK-001",
        behandlingId = "Fritekst",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        periodeListe = listOf(
          OpprettPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-01-01"),
            periodeTilDato = LocalDate.parse("2019-07-01"),
            belop = BigDecimal.valueOf(3490),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            grunnlagReferanseListe = listOf(
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = true
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGN-19",
                grunnlagValgt = false
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              )
            )
          ),
          OpprettPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-07-01"),
            periodeTilDato = LocalDate.parse("2020-01-01"),
            belop = BigDecimal.valueOf(3520),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            grunnlagReferanseListe = listOf(
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = false
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGN-19",
                grunnlagValgt = true
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BP-SKATTEKLASSE-19",
                grunnlagValgt = true
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              ),
            )
          )
        )
      ),
      OpprettStonadsendringRequest(
        stonadType = "SAERTILSKUDD",
        sakId = "SAK-001",
        behandlingId = "Fritekst",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        periodeListe = listOf(
          OpprettPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-06-01"),
            periodeTilDato = LocalDate.parse("2019-07-01"),
            belop = BigDecimal.valueOf(4240),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            grunnlagReferanseListe = listOf(
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = true
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              )
            )
          ),
          OpprettPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-08-01"),
            periodeTilDato = LocalDate.parse("2019-09-01"),
            belop = BigDecimal.valueOf(3410),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            grunnlagReferanseListe = listOf(
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = false
              ),
              OpprettGrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              ),
            )
          )
        )
      )
    )


    fun byggVedtakDto(
      vedtakId: Int = (1..100).random(),
      enhetId: String = "4812",
      saksbehandlerId: String = "X123456",
      opprettetTimestamp: LocalDateTime? = LocalDateTime.now()
    ) = VedtakDto(
      vedtakId = vedtakId,
      enhetId = enhetId,
      saksbehandlerId = saksbehandlerId,
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
      grunnlagId: Int = (1..100).random(),
      grunnlagValgt: Boolean = true
    ) = PeriodeGrunnlagDto(
      periodeId = periodeId,
      grunnlagId = grunnlagId,
      grunnlagValgt = grunnlagValgt
    )
  }
}