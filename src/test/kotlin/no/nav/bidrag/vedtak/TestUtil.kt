package no.nav.bidrag.vedtak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.domene.enums.vedtak.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.vedtak.Beslutningstype
import no.nav.bidrag.domene.enums.vedtak.Engangsbeløptype
import no.nav.bidrag.domene.enums.vedtak.Innkrevingstype
import no.nav.bidrag.domene.enums.vedtak.Stønadstype
import no.nav.bidrag.domene.enums.vedtak.Vedtakskilde
import no.nav.bidrag.domene.enums.vedtak.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.organisasjon.Enhetsnummer
import no.nav.bidrag.domene.sak.Saksnummer
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettBehandlingsreferanseRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettEngangsbeløpRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettGrunnlagRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettPeriodeRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettStønadsendringRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettVedtakRequestDto
import no.nav.bidrag.vedtak.bo.EngangsbeløpGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import no.nav.bidrag.vedtak.persistence.entity.Engangsbeløp
import no.nav.bidrag.vedtak.persistence.entity.EngangsbeløpGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Stønadsendring
import no.nav.bidrag.vedtak.persistence.entity.StønadsendringGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TestUtil {

    companion object {

        fun byggVedtakRequest() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            unikReferanse = "unikReferanse",
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = "NO",
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringListe(),
            engangsbeløpListe = byggEngangsbeløpListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe(),
        )

        private fun byggGrunnlagListe() = listOf(
            OpprettGrunnlagRequestDto(
                referanse = "BM-LIGS-19",
                type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                gjelderReferanse = "PERSON_BM",
                grunnlagsreferanseListe = listOf("innhentet_ainntekt_1", "innhentet_ainntekt_2"),
                innhold = ObjectMapper().readTree(
                    """
          {
            "inntektDatoFraTil": {
              "periodeDatoFra": "2019-01-01",
              "periodeDatoTil": "2020-01-01"
            },
            "inntektBelop": 400000,
            "inntektType": "SKATTEGRUNNLAG_SKE"
          }""",
                ),
            ),
            OpprettGrunnlagRequestDto(
                referanse = "BM-LIGN-19",
                type = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE,
                gjelderReferanse = "PERSON_BM",
                grunnlagsreferanseListe = listOf("innhentet_ainntekt_4", "innhentet_ainntekt_3"),
                innhold = ObjectMapper().readTree(
                    """
          {
            "inntektDatoFraTil": {
              "periodeDatoFra": "2019-01-01",
              "periodeDatoTil": "2020-01-01"
            },
            "inntektBelop": 400000,
            "inntektType": "LIGNING_SKE"
          }""",
                ),
            ),
            OpprettGrunnlagRequestDto(
                referanse = "BP-SKATTEKLASSE-19",
                type = Grunnlagstype.SKATTEKLASSE,
                innhold = ObjectMapper().readTree(
                    """
          {
            "skatteklasseDatoFraTil": {
              "periodeDatoFra": "2019-01-01",
              "periodeDatoTil": "2020-01-01"
            },
            "skatteKlasseId": 1
          }""",
                ),
            ),
            OpprettGrunnlagRequestDto(
                referanse = "SJAB-REF001",
                type = Grunnlagstype.SJABLON_SJABLONTALL,
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
          }""",
                ),
            ),
            OpprettGrunnlagRequestDto(
                referanse = "VIRKNINGSDATO-1",
                type = Grunnlagstype.VIRKNINGSTIDSPUNKT,
                innhold = ObjectMapper().readTree(
                    """
          {
            "virkningsdato": "2023-11-03"
          }""",
                ),
            ),
            OpprettGrunnlagRequestDto(
                referanse = "NOTAT-1",
                type = Grunnlagstype.NOTAT,
                innhold = ObjectMapper().readTree(
                    """
          {
            "notat": "Dette er et saksbehandlingsnotat"
          }""",
                ),
            ),
            OpprettGrunnlagRequestDto(
                referanse = "VIRKNINGSDATO-2",
                type = Grunnlagstype.VIRKNINGSTIDSPUNKT,
                innhold = ObjectMapper().readTree(
                    """
          {
            "virkningsdato": "2023-12-03"
          }""",
                ),
            ),
            OpprettGrunnlagRequestDto(
                referanse = "NOTAT-2",
                type = Grunnlagstype.NOTAT,
                innhold = ObjectMapper().readTree(
                    """
          {
            "notat": "Dette er enda et saksbehandlingsnotat"
          }""",
                ),
            ),
        )

        private fun byggStønadsendringListe() = listOf(
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 123,
                eksternReferanse = "eksternRef1",
                grunnlagReferanseListe = listOf(
                    "VIRKNINGSDATO-1",
                    "NOTAT-1",
                ),
                periodeListe = listOf(
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                        beløp = BigDecimal.valueOf(3490),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = "delytelseId1",
                        grunnlagReferanseListe = listOf(
                            "BM-LIGS-19",
                            "BM-LIGN-19",
                            "SJAB-REF001",
                        ),
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                        beløp = BigDecimal.valueOf(3520),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = "delytelseId2",
                        grunnlagReferanseListe = listOf(
                            "BM-LIGS-19",
                            "BM-LIGN-19",
                            "BP-SKATTEKLASSE-19",
                            "SJAB-REF001",
                        ),
                    ),
                ),
            ),
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 200,
                eksternReferanse = "eksternRef3",
                grunnlagReferanseListe = listOf(
                    "VIRKNINGSDATO-2",
                    "NOTAT-2",
                ),
                periodeListe = listOf(
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-06-01"), LocalDate.parse("2019-07-01")),
                        beløp = BigDecimal.valueOf(4240),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId3",
                        grunnlagReferanseListe = listOf(
                            "BM-LIGS-19",
                            "SJAB-REF001",
                        ),
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-08-01"), LocalDate.parse("2019-09-01")),
                        beløp = BigDecimal.valueOf(3410),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId4",
                        grunnlagReferanseListe = listOf(
                            "BM-LIGS-19",
                            "SJAB-REF001",
                        ),
                    ),
                ),
            ),
        )

        private fun byggEngangsbeløpListe() = listOf(
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(3490),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse3",
                delytelseId = "delytelseId1",
                eksternReferanse = "EksternRef1",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001",
                ),
            ),
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(2990),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = null,
                referanse = null,
                delytelseId = "delytelseId2",
                eksternReferanse = "EksternRef2",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001",
                ),
            ),
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(2345),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse1",
                delytelseId = "delytelseId2",
                eksternReferanse = "EksternRef2",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001",
                ),
            ),
        )

        private fun byggBehandlingsreferanseListe() = listOf(
            OpprettBehandlingsreferanseRequestDto(
                kilde = BehandlingsrefKilde.BISYS_SØKNAD,
                referanse = "Bisysreferanse01",
            ),
            OpprettBehandlingsreferanseRequestDto(
                kilde = BehandlingsrefKilde.BISYS_SØKNAD,
                referanse = "Bisysreferanse02",
            ),
        )

        fun byggVedtakMedDuplikateReferanserRequest() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = "NO",
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringListe(),
            engangsbeløpListe = byggEngangsbeløpListeMedDuplikateReferanser(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe(),
        )

        private fun byggEngangsbeløpListeMedDuplikateReferanser() = listOf(
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(3490),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse1",
                delytelseId = "delytelseId1",
                eksternReferanse = "EksternRef1",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001",
                ),
            ),
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(2990),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse1",
                delytelseId = "delytelseId2",
                eksternReferanse = "EksternRef2",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001",
                ),
            ),
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(2990),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse2",
                delytelseId = "delytelseId2",
                eksternReferanse = "EksternRef2",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001",
                ),
            ),
        )

        fun byggVedtakRequestUtenGrunnlag() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.FASTSETTELSE,
            opprettetAv = "X123456",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = "NO",
            grunnlagListe = emptyList(),
            stønadsendringListe = byggStønadsendringUtenGrunnlagListe(),
            engangsbeløpListe = byggEngangsbeløpUtenGrunnlagListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe(),
        )

        private fun byggStønadsendringUtenGrunnlagListe() = listOf(
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 123,
                eksternReferanse = "eksternRef1",
                grunnlagReferanseListe = emptyList(),
                periodeListe = listOf(
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                        beløp = BigDecimal.valueOf(3490),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = "delytelseId1",
                        grunnlagReferanseListe = emptyList(),
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                        beløp = BigDecimal.valueOf(3520),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = "delytelseId2",
                        grunnlagReferanseListe = emptyList(),

                    ),
                ),
            ),
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 200,
                eksternReferanse = "eksternRef3",
                grunnlagReferanseListe = emptyList(),
                periodeListe = listOf(
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-06-01"), LocalDate.parse("2019-07-01")),
                        beløp = BigDecimal.valueOf(4240),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId3",
                        grunnlagReferanseListe = emptyList(),
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-08-01"), LocalDate.parse("2019-09-01")),
                        beløp = BigDecimal.valueOf(3410),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId4",
                        grunnlagReferanseListe = emptyList(),
                    ),
                ),
            ),
        )

        private fun byggEngangsbeløpUtenGrunnlagListe() = listOf(
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(3490),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse1",
                delytelseId = "delytelseId1",
                eksternReferanse = "EksternRef1",
                grunnlagReferanseListe = emptyList(),
            ),
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(2990),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse2",
                delytelseId = "delytelseId2",
                eksternReferanse = "EksternRef2",
                grunnlagReferanseListe = emptyList(),
            ),
        )

        fun byggVedtakRequestMedInputparametre(
            vedtaksdato: LocalDate?,
            vedtakstype: Vedtakstype?,
            saksnummer: Saksnummer?,
            type: Stønadstype?,
            skyldner: Personident?,
            kravhaver: Personident?,
            innkreving: Innkrevingstype?,
            beslutning: Beslutningstype?,
        ) = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = vedtakstype ?: Vedtakstype.ENDRING,
            opprettetAv = "X123456",
            vedtakstidspunkt = if (vedtaksdato == null) {
                LocalDateTime.of(2020, 5, 7, 23, 34, 55, 869121094)
            } else {
                vedtaksdato.atStartOfDay()
            },
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = "NO",
            grunnlagListe = emptyList(),
            stønadsendringListe = byggStønadsendringMedInputparametreListe(
                saksnummer,
                type,
                skyldner,
                kravhaver,
                innkreving,
                beslutning,
            ),
            engangsbeløpListe = byggEngangsbeløpUtenGrunnlagListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe(),
        )

        private fun byggStønadsendringMedInputparametreListe(
            saksnummer: Saksnummer?,
            type: Stønadstype?,
            skyldner: Personident?,
            kravhaver: Personident?,
            innkreving: Innkrevingstype?,
            beslutning: Beslutningstype?,
        ) = listOf(
            OpprettStønadsendringRequestDto(
                type = type ?: Stønadstype.BIDRAG,
                sak = saksnummer ?: Saksnummer("SAK-001"),
                skyldner = skyldner ?: Personident("1"),
                kravhaver = kravhaver ?: Personident("2"),
                mottaker = Personident("00000000000"),
                førsteIndeksreguleringsår = null,
                innkreving = innkreving ?: Innkrevingstype.MED_INNKREVING,
                beslutning = beslutning ?: Beslutningstype.ENDRING,
                omgjørVedtakId = null,
                eksternReferanse = null,
                grunnlagReferanseListe = emptyList(),
                periodeListe = listOf(
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                        beløp = BigDecimal.valueOf(3490),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = null,
                        grunnlagReferanseListe = emptyList(),
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                        beløp = BigDecimal.valueOf(3520),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = null,
                        grunnlagReferanseListe = emptyList(),

                    ),
                ),
            ),
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 200,
                eksternReferanse = "eksternRef3",
                grunnlagReferanseListe = emptyList(),
                periodeListe = listOf(
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-06-01"), LocalDate.parse("2019-07-01")),
                        beløp = BigDecimal.valueOf(4240),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId3",
                        grunnlagReferanseListe = emptyList(),
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-08-01"), LocalDate.parse("2019-09-01")),
                        beløp = BigDecimal.valueOf(3410),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId4",
                        grunnlagReferanseListe = emptyList(),
                    ),
                ),
            ),
        )

        fun byggOppdaterVedtakMedMismatchVedtak() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = null,
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringListe(),
            engangsbeløpListe = byggEngangsbeløpListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe(),
        )

        fun byggOppdaterVedtakMedMismatchStønadsendring() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = null,
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringMedMismatchListe(),
            engangsbeløpListe = byggEngangsbeløpListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe(),
        )

        private fun byggStønadsendringMedMismatchListe() = listOf(
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 123,
                eksternReferanse = "eksternRef1",
                grunnlagReferanseListe = emptyList(),
                periodeListe = listOf(
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-01")),
                        beløp = BigDecimal.valueOf(3491),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = "delytelseId1",
                        grunnlagReferanseListe = listOf(
                            "BM-LIGS-19",
                            "BM-LIGN-19",
                            "SJAB-REF001",
                        ),
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                        beløp = BigDecimal.valueOf(3520),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = "delytelseId2",
                        grunnlagReferanseListe = listOf(
                            "BM-LIGS-19",
                            "BM-LIGN-19",
                            "BP-SKATTEKLASSE-19",
                            "SJAB-REF001",
                        ),
                    ),
                ),
            ),
        )

        fun byggOppdaterVedtakMedMismatchPeriode() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = null,
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringMedMismatchPeriodeListe(),
            engangsbeløpListe = byggEngangsbeløpListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe(),
        )

        private fun byggStønadsendringMedMismatchPeriodeListe() = listOf(
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 123,
                eksternReferanse = "eksternRef1",
                grunnlagReferanseListe = emptyList(),
                periodeListe = listOf(
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-01-01"), LocalDate.parse("2019-07-02")),
                        beløp = BigDecimal.valueOf(3490),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = "delytelseId1",
                        grunnlagReferanseListe = emptyList(),
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                        beløp = BigDecimal.valueOf(3520),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = "delytelseId2",
                        grunnlagReferanseListe = emptyList(),

                    ),
                ),
            ),
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 200,
                eksternReferanse = "eksternRef3",
                grunnlagReferanseListe = emptyList(),
                periodeListe = listOf(
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-06-01"), LocalDate.parse("2019-07-01")),
                        beløp = BigDecimal.valueOf(4240),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId3",
                        grunnlagReferanseListe = emptyList(),
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-08-01"), LocalDate.parse("2019-09-01")),
                        beløp = BigDecimal.valueOf(3410),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId4",
                        grunnlagReferanseListe = emptyList(),
                    ),
                ),
            ),
        )

        fun byggOppdaterVedtakMedMismatchEngangsbeløp() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = null,
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringListe(),
            engangsbeløpListe = byggEngangsbeløpMedFeilListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe(),
        )

        private fun byggEngangsbeløpMedFeilListe() = listOf(
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(3491),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse1",
                delytelseId = "delytelseId1",
                eksternReferanse = "EksternRef1",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001",
                ),
            ),
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(2990),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse2",
                delytelseId = "delytelseId2",
                eksternReferanse = "EksternRef2",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001",
                ),
            ),
        )

        fun byggVedtak(
            vedtaksid: Int = (1..100).random(),
            kilde: String = Vedtakskilde.MANUELT.toString(),
            type: String = Vedtakstype.ALDERSJUSTERING.toString(),
            enhetsnummer: String = Enhetsnummer("4812").toString(),
            vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
            opprettetAv: String = "X123456",
            opprettetAvNavn: String = "Saksbehandler1",
            opprettetTimestamp: LocalDateTime = LocalDateTime.now(),
            innkrevingUtsattTilDato: LocalDate = LocalDate.now(),
        ) = Vedtak(
            id = vedtaksid,
            kilde = kilde,
            type = type,
            enhetsnummer = enhetsnummer,
            vedtakstidspunkt = vedtakstidspunkt,
            opprettetAv = opprettetAv,
            opprettetAvNavn = opprettetAvNavn,
            opprettetTidspunkt = opprettetTimestamp,
            innkrevingUtsattTilDato = innkrevingUtsattTilDato,
        )

        fun byggStønadsendring(
            stønadsendringsid: Int = (1..100).random(),
            type: String = Stønadstype.BIDRAG.toString(),
            sak: String = Saksnummer("SAK-001").toString(),
            skyldner: String = "01018011111",
            kravhaver: String = "01010511111",
            mottaker: String = "01018211111",
            innkreving: String = Innkrevingstype.MED_INNKREVING.toString(),
            beslutning: String = Beslutningstype.ENDRING.toString(),
            eksternReferanse: String = "eksternRef1",
        ) = Stønadsendring(
            id = stønadsendringsid,
            type = type,
            vedtak = byggVedtak(),
            sak = sak,
            skyldner = skyldner,
            kravhaver = kravhaver,
            mottaker = mottaker,
            innkreving = innkreving,
            beslutning = beslutning,
            eksternReferanse = eksternReferanse,
        )

        fun byggPeriode(
            periodeid: Int = (1..100).random(),
            fomDato: LocalDate = LocalDate.parse("2019-07-01"),
            tilDato: LocalDate? = LocalDate.parse("2020-01-01"),
            beløp: BigDecimal = BigDecimal.valueOf(3520),
            valutakode: String = "NOK",
            resultatkode: String = "KOSTNADSBEREGNET_BIDRAG",
            delytelseId: String = "delytelseId1",
        ) = Periode(
            id = periodeid,
            fom = fomDato,
            til = tilDato,
            stønadsendring = byggStønadsendring(),
            beløp = beløp,
            valutakode = valutakode,
            resultatkode = resultatkode,
            delytelseId = delytelseId,
        )

        fun byggGrunnlag(
            grunnlagsid: Int = (1..100).random(),
            grunnlagReferanse: String = "BM-LIGN-19",
            vedtak: Vedtak = byggVedtak(),
            type: String = Grunnlagstype.INNTEKT_RAPPORTERING_PERIODE.toString(),
            innhold: String =
                """{
            "rolle": "BIDRAGSMOTTAKER",
            "datoFom": "2021-01-01",
            "datoTil": null,
            "sivilstandKode": "sivilstandkode1"
                }""",
        ) = Grunnlag(
            id = grunnlagsid,
            referanse = grunnlagReferanse,
            vedtak = vedtak,
            type = type,
            innhold = innhold,
        )

        fun byggPeriodeGrunnlagBo(periodeid: Int = byggPeriode().id, grunnlagsid: Int = byggGrunnlag().id) = PeriodeGrunnlagBo(
            periodeid = periodeid,
            grunnlagsid = grunnlagsid,
        )

        fun byggPeriodeGrunnlag(periode: Periode = byggPeriode(), grunnlag: Grunnlag = byggGrunnlag()) = PeriodeGrunnlag(
            periode = periode,
            grunnlag = grunnlag,
        )

        fun byggStønadsendringGrunnlag(stønadsendring: Stønadsendring = byggStønadsendring(), grunnlag: Grunnlag = byggGrunnlag()) = StønadsendringGrunnlag(
            stønadsendring = stønadsendring,
            grunnlag = grunnlag,
        )

        fun byggEngangsbeløp(
            engangsbeløpId: Int = (1..100).random(),
            type: String = "SAERTILSKUDD",
            sak: String = "SAK-101",
            skyldner: String = "01018011111",
            kravhaver: String = "01010511111",
            mottaker: String = "01018211111",
            beløp: BigDecimal = BigDecimal.valueOf(3490),
            valutakode: String = "NOK",
            resultatkode: String = "SAERTILSKUDD BEREGNET",
            innkreving: Innkrevingstype = Innkrevingstype.MED_INNKREVING,
            beslutning: Beslutningstype = Beslutningstype.ENDRING,
            omgjørVedtakId: Int = 123,
            referanse: String = "referanse5",
            delytelseId: String = "delytelseId5",
            eksternReferanse: String = "eksternReferanse5",
        ) = Engangsbeløp(
            id = engangsbeløpId,
            vedtak = byggVedtak(),
            type = type,
            sak = sak,
            skyldner = skyldner,
            kravhaver = kravhaver,
            mottaker = mottaker,
            beløp = beløp,
            valutakode = valutakode,
            resultatkode = resultatkode,
            innkreving = innkreving.toString(),
            beslutning = beslutning.toString(),
            omgjørVedtakId = omgjørVedtakId,
            referanse = referanse,
            delytelseId = delytelseId,
            eksternReferanse = eksternReferanse,
        )

        fun byggEngangsbeløpGrunnlagBo(engangsbeløpId: Int = (1..100).random(), grunnlagId: Int = (1..100).random()) = EngangsbeløpGrunnlagBo(
            engangsbeløpsid = engangsbeløpId,
            grunnlagsid = grunnlagId,
        )

        fun byggEngangsbeløpGrunnlag(engangsbeløp: Engangsbeløp = byggEngangsbeløp(), grunnlag: Grunnlag = byggGrunnlag()) = EngangsbeløpGrunnlag(
            engangsbeløp = engangsbeløp,
            grunnlag = grunnlag,
        )

        fun byggBehandlingsreferanse(
            behandlingsreferanseid: Int = (1..100).random(),
            vedtak: Vedtak = byggVedtak(),
            kilde: String = "BISYS_SØKNAD",
            referanse: String = "Bisysreferanse01",
        ) = Behandlingsreferanse(
            id = behandlingsreferanseid,
            vedtak = vedtak,
            kilde = kilde,
            referanse = referanse,
        )

        fun byggVedtakEngangsbeløpUtenReferanseRequest() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.KLAGE,
            opprettetAv = "X123456",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = "NO",
            grunnlagListe = emptyList(),
            stønadsendringListe = emptyList(),
            engangsbeløpListe = byggEngangsbeløpOmgøringsvedtakUtenReferanse(),
            behandlingsreferanseListe = emptyList(),
        )

        private fun byggEngangsbeløpOmgøringsvedtakUtenReferanse() = listOf(
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SÆRBIDRAG,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(3490),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                beslutning = Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = null,
                delytelseId = "delytelseId1",
                eksternReferanse = "EksternRef1",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001",
                ),
            ),
        )
    }
}
