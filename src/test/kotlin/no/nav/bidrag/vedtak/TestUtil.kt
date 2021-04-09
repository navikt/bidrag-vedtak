package no.nav.bidrag.vedtak

import no.nav.bidrag.vedtak.api.GrunnlagReferanseRequest
import no.nav.bidrag.vedtak.api.OpprettGrunnlagRequest
import no.nav.bidrag.vedtak.api.OpprettPeriodeRequest
import no.nav.bidrag.vedtak.api.OpprettStonadsendringRequest
import no.nav.bidrag.vedtak.api.OpprettVedtakRequest
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

    fun byggKomplettVedtakRequest() = OpprettVedtakRequest(
      saksbehandlerId = "X123456",
      enhetId = "4812",
      grunnlagListe = byggGrunnlagListe(),
      stonadsendringListe = byggStonadsendringListe()
    )

    private fun byggGrunnlagListe() = listOf(
      OpprettGrunnlagRequest(
        grunnlagReferanse = "BM-LIGS-19",
        grunnlagType = "INNTEKT"
      ),
      OpprettGrunnlagRequest(
        grunnlagReferanse = "BM-LIGN-19",
        grunnlagType = "INNTEKT"
      ),
      OpprettGrunnlagRequest(
        grunnlagReferanse = "BP-SKATTEKLASSE-19",
        grunnlagType = "SKATTEKLASSE"
      ),
      OpprettGrunnlagRequest(
        grunnlagReferanse = "SJAB-REF001",
        grunnlagType = "SJABLON"
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
            listOf(
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
          OpprettPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-07-01"),
            periodeTilDato = LocalDate.parse("2020-01-01"),
            belop = BigDecimal.valueOf(3520),
            valutakode = "NOK",
            resultatkode = "KOSTNADSBEREGNET_BIDRAG",
            listOf(
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
            listOf(
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
          OpprettPeriodeRequest(
            periodeFomDato = LocalDate.parse("2019-08-01"),
            periodeTilDato = LocalDate.parse("2019-09-01"),
            belop = BigDecimal.valueOf(3410),
            valutakode = "NOK",
            resultatkode = "SAERTILSKUDD_INNVILGET",
            listOf(
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