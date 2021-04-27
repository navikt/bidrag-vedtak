package no.nav.bidrag.vedtak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.vedtak.api.GrunnlagReferanseRequest
import no.nav.bidrag.vedtak.api.NyPeriodeRequest
import no.nav.bidrag.vedtak.api.NyStonadsendringRequest
import no.nav.bidrag.vedtak.api.NyttGrunnlagRequest
import no.nav.bidrag.vedtak.api.NyttKomplettVedtakRequest
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

    fun byggKomplettVedtakRequest() = NyttKomplettVedtakRequest(
      saksbehandlerId = "X123456",
      enhetId = "4812",
      grunnlagListe = byggGrunnlagListe(),
      stonadsendringListe = byggStonadsendringListe()
    )

    private fun byggGrunnlagListe() = listOf(
      NyttGrunnlagRequest(
        grunnlagReferanse = "BM-LIGS-19",
        grunnlagType = "INNTEKT",
        grunnlagInnhold = ObjectMapper().readTree(
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
      NyttGrunnlagRequest(
        grunnlagReferanse = "BM-LIGN-19",
        grunnlagType = "INNTEKT"
      ),
      NyttGrunnlagRequest(
        grunnlagReferanse = "BP-SKATTEKLASSE-19",
        grunnlagType = "SKATTEKLASSE"
      ),
      NyttGrunnlagRequest(
        grunnlagReferanse = "SJAB-REF001",
        grunnlagType = "SJABLON"
      )
    )

    private fun byggStonadsendringListe() = listOf(
      NyStonadsendringRequest(
        stonadType = "BIDRAG",
        sakId = "SAK-001",
        behandlingId = "Fritekst",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        periodeListe = listOf(
          NyPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-01-01"),
            periodeTilDato = LocalDate.parse("2019-07-01"),
            belop = BigDecimal.valueOf(3490),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            grunnlagReferanseListe = listOf(
              GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = true
              ),
              GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGN-19",
                grunnlagValgt = false
              ),
              GrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              )
            )
          ),
          NyPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-07-01"),
            periodeTilDato = LocalDate.parse("2020-01-01"),
            belop = BigDecimal.valueOf(3520),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            grunnlagReferanseListe = listOf(
              GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = false
              ),
              GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGN-19",
                grunnlagValgt = true
              ),
              GrunnlagReferanseRequest(
                grunnlagReferanse = "BP-SKATTEKLASSE-19",
                grunnlagValgt = true
              ),
              GrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              ),
            )
          )
        )
      ),
      NyStonadsendringRequest(
        stonadType = "SAERTILSKUDD",
        sakId = "SAK-001",
        behandlingId = "Fritekst",
        skyldnerId = "01018011111",
        kravhaverId = "01010511111",
        mottakerId = "01018211111",
        periodeListe = listOf(
          NyPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-06-01"),
            periodeTilDato = LocalDate.parse("2019-07-01"),
            belop = BigDecimal.valueOf(4240),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            grunnlagReferanseListe = listOf(
              GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = true
              ),
              GrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              )
            )
          ),
          NyPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-08-01"),
            periodeTilDato = LocalDate.parse("2019-09-01"),
            belop = BigDecimal.valueOf(3410),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            grunnlagReferanseListe = listOf(
              GrunnlagReferanseRequest(
                grunnlagReferanse = "BM-LIGS-19",
                grunnlagValgt = false
              ),
              GrunnlagReferanseRequest(
                grunnlagReferanse = "SJAB-REF001",
                grunnlagValgt = true
              ),
            )
          )
        )
      )
    )


    fun byggVedtakDto() = VedtakDto(
      vedtakId = (1..100).random(),
      enhetId = "4812",
      saksbehandlerId = "X123456",
      opprettetTimestamp = LocalDateTime.now()
    )

    fun byggStonadsendringDto() = StonadsendringDto(
      stonadsendringId = (1..100).random(),
      stonadType = "BIDRAG",
      vedtakId = (1..100).random(),
      sakId = "SAK-001",
      behandlingId = "Fritekst",
      skyldnerId = "01018011111",
      kravhaverId = "01010511111",
      mottakerId = "01018211111"
    )

    fun byggPeriodeDto() = PeriodeDto(
      periodeId = (1..100).random(),
      periodeFomDato = LocalDate.parse("2019-07-01"),
      periodeTilDato = LocalDate.parse("2020-01-01"),
      stonadsendringId = (1..100).random(),
      belop = BigDecimal.valueOf(3520),
      valutakode = "NOK",
      resultatkode = "KOSTNADSBEREGNET_BIDRAG"
    )

    fun byggGrunnlagDto() = GrunnlagDto(
      grunnlagId = (1..100).random(),
      grunnlagReferanse = "BM-LIGN-19",
      vedtakId = (1..100).random(),
      grunnlagType = "INNTEKT",
      grunnlagInnhold = "Innhold"
    )

    fun byggPeriodeGrunnlagDto() = PeriodeGrunnlagDto(
      periodeId = (1..100).random(),
      grunnlagId = (1..100).random(),
      grunnlagValgt = true
    )
  }
}