package no.nav.bidrag.vedtak

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.domene.enums.BehandlingsrefKilde
import no.nav.bidrag.domene.enums.Beslutningstype
import no.nav.bidrag.domene.enums.Engangsbeløptype
import no.nav.bidrag.domene.enums.GrunnlagType
import no.nav.bidrag.domene.enums.Innkrevingstype
import no.nav.bidrag.domene.enums.Stønadstype
import no.nav.bidrag.domene.enums.Vedtakskilde
import no.nav.bidrag.domene.enums.Vedtakstype
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.domene.streng.Enhetsnummer
import no.nav.bidrag.domene.streng.Saksnummer
import no.nav.bidrag.domene.tid.Datoperiode
import no.nav.bidrag.domene.tid.ÅrMånedsperiode
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettBehandlingsreferanseRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettEngangsbeløpRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettGrunnlagRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettStønadsendringRequestDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettPeriodeRequestDto
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
            opprettetAvNavn = "Saksbehandler1",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = null,
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringListe(),
            engangsbeløpListe = byggEngangsbeløpListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe()
        )

        private fun byggGrunnlagListe() = listOf(
            OpprettGrunnlagRequestDto(
                referanse = "BM-LIGS-19",
                type = GrunnlagType.INNTEKT,
                innhold = ObjectMapper().readTree(
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

        private fun byggStønadsendringListe() = listOf(
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                Beslutningstype.ENDRING,
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
                        grunnlagReferanseListe = listOf(
                            "BM-LIGS-19",
                            "BM-LIGN-19",
                            "SJAB-REF001"
                        )
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
                            "SJAB-REF001"
                        )
                    )
                )
            ),
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                Beslutningstype.ENDRING,
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
                        grunnlagReferanseListe = listOf(
                            "BM-LIGS-19",
                            "SJAB-REF001"
                        )
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-08-01"), LocalDate.parse("2019-09-01")),
                        beløp = BigDecimal.valueOf(3410),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId4",
                        grunnlagReferanseListe = listOf(
                            "BM-LIGS-19",
                            "SJAB-REF001"
                        )
                    )
                )
            )
        )

        private fun byggEngangsbeløpListe() = listOf(
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SAERTILSKUDD,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(3490),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse1",
                delytelseId = "delytelseId1",
                eksternReferanse = "EksternRef1",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001"
                )
            ),
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SAERTILSKUDD,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(2990),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse2",
                delytelseId = "delytelseId2",
                eksternReferanse = "EksternRef2",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001"
                )
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

        fun byggVedtakRequestUtenGrunnlag() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            opprettetAvNavn = "Saksbehandler1",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = null,
            grunnlagListe = emptyList(),
            stønadsendringListe = byggStønadsendringUtenGrunnlagListe(),
            engangsbeløpListe = byggEngangsbeløpUtenGrunnlagListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe()
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
                        grunnlagReferanseListe = emptyList()
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                        beløp = BigDecimal.valueOf(3520),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = "delytelseId2",
                        grunnlagReferanseListe = emptyList()

                    )
                )
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
                        grunnlagReferanseListe = emptyList()
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-08-01"), LocalDate.parse("2019-09-01")),
                        beløp = BigDecimal.valueOf(3410),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId4",
                        grunnlagReferanseListe = emptyList()
                    )
                )
            )
        )

        private fun byggEngangsbeløpUtenGrunnlagListe() = listOf(
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SAERTILSKUDD,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(3490),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse1",
                delytelseId = "delytelseId1",
                eksternReferanse = "EksternRef1",
                grunnlagReferanseListe = emptyList()
            ),
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SAERTILSKUDD,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(2990),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse2",
                delytelseId = "delytelseId2",
                eksternReferanse = "EksternRef2",
                grunnlagReferanseListe = emptyList()
            )
        )

        fun byggOppdaterVedtakMedMismatchVedtak() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            opprettetAvNavn = "Saksbehandler2",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = null,
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringListe(),
            engangsbeløpListe = byggEngangsbeløpListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe()
        )

        fun byggOppdaterVedtakMedMismatchStønadsendring() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            opprettetAvNavn = "Saksbehandler1",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = null,
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringMedMismatchListe(),
            engangsbeløpListe = byggEngangsbeløpListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe()
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
                Beslutningstype.ENDRING,
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
                            "SJAB-REF001"
                        )
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
                            "SJAB-REF001"
                        )
                    )
                )
            )
        )

        fun byggOppdaterVedtakMedMismatchPeriode() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            opprettetAvNavn = "Saksbehandler1",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = null,
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringMedMismatchPeriodeListe(),
            engangsbeløpListe = byggEngangsbeløpListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe()
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
                Beslutningstype.ENDRING,
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
                        grunnlagReferanseListe = emptyList()
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-07-01"), LocalDate.parse("2020-01-01")),
                        beløp = BigDecimal.valueOf(3520),
                        valutakode = "NOK",
                        resultatkode = "KOSTNADSBEREGNET_BIDRAG",
                        delytelseId = "delytelseId2",
                        grunnlagReferanseListe = emptyList()

                    )
                )
            ),
            OpprettStønadsendringRequestDto(
                type = Stønadstype.BIDRAG,
                sak = Saksnummer("SAK-001"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                førsteIndeksreguleringsår = 2024,
                innkreving = Innkrevingstype.MED_INNKREVING,
                Beslutningstype.ENDRING,
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
                        grunnlagReferanseListe = emptyList()
                    ),
                    OpprettPeriodeRequestDto(
                        periode = ÅrMånedsperiode(LocalDate.parse("2019-08-01"), LocalDate.parse("2019-09-01")),
                        beløp = BigDecimal.valueOf(3410),
                        valutakode = "NOK",
                        resultatkode = "SAERTILSKUDD_INNVILGET",
                        delytelseId = "delytelseId4",
                        grunnlagReferanseListe = emptyList()
                    )
                )
            )
        )

        fun byggOppdaterVedtakMedMismatchEngangsbeløp() = OpprettVedtakRequestDto(
            kilde = Vedtakskilde.MANUELT,
            type = Vedtakstype.ALDERSJUSTERING,
            opprettetAv = "X123456",
            opprettetAvNavn = "Saksbehandler1",
            vedtakstidspunkt = LocalDateTime.parse("2020-01-01T23:34:55.869121094"),
            enhetsnummer = Enhetsnummer("4812"),
            innkrevingUtsattTilDato = LocalDate.now(),
            fastsattILand = null,
            grunnlagListe = byggGrunnlagListe(),
            stønadsendringListe = byggStønadsendringListe(),
            engangsbeløpListe = byggEngangsbeløpMedFeilListe(),
            behandlingsreferanseListe = byggBehandlingsreferanseListe()
        )

        private fun byggEngangsbeløpMedFeilListe() = listOf(
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SAERTILSKUDD,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(3491),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse1",
                delytelseId = "delytelseId1",
                eksternReferanse = "EksternRef1",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001"
                )
            ),
            OpprettEngangsbeløpRequestDto(
                type = Engangsbeløptype.SAERTILSKUDD,
                sak = Saksnummer("SAK-101"),
                skyldner = Personident("01018011111"),
                kravhaver = Personident("01010511111"),
                mottaker = Personident("01018211111"),
                beløp = BigDecimal.valueOf(2990),
                valutakode = "NOK",
                resultatkode = "SAERTILSKUDD BEREGNET",
                innkreving = Innkrevingstype.MED_INNKREVING,
                Beslutningstype.ENDRING,
                omgjørVedtakId = 400,
                referanse = "referanse2",
                delytelseId = "delytelseId2",
                eksternReferanse = "EksternRef2",
                grunnlagReferanseListe = listOf(
                    "BM-LIGS-19",
                    "BM-LIGN-19",
                    "SJAB-REF001"
                )
            )
        )

        fun byggVedtak(
            vedtakId: Int = (1..100).random(),
            kilde: String = Vedtakskilde.MANUELT.toString(),
            type: String = Vedtakstype.ALDERSJUSTERING.toString(),
            enhetsnummer: String = "4812",
            vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
            opprettetAv: String = "X123456",
            opprettetAvNavn: String = "Saksbehandler1",
            opprettetTimestamp: LocalDateTime = LocalDateTime.now(),
            innkrevingUtsattTilDato: LocalDate = LocalDate.now()
        ) = Vedtak(
            id = vedtakId,
            kilde = kilde,
            type = type,
            enhetsnummer = enhetsnummer,
            vedtakstidspunkt = vedtakstidspunkt,
            opprettetAv = opprettetAv,
            opprettetAvNavn = opprettetAvNavn,
            opprettetTimestamp = opprettetTimestamp,
            innkrevingUtsattTilDato = innkrevingUtsattTilDato
        )

        fun byggStønadsendring(
            stønadsendringId: Int = (1..100).random(),
            type: String = Stønadstype.BIDRAG.toString(),
            sak: String = "SAK-001",
            skyldner: String = "01018011111",
            kravhaver: String = "01010511111",
            mottaker: String = "01018211111",
            innkreving: String = Innkrevingstype.MED_INNKREVING.toString(),
            beslutning: Beslutningstype = Beslutningstype.ENDRING,
            eksternReferanse: String = "eksternRef1"
        ) = Stønadsendring(
            id = stønadsendringId,
            type = type,
            vedtak = byggVedtak(),
            sak = sak,
            skyldner = skyldner,
            kravhaver = kravhaver,
            mottaker = mottaker,
            innkreving = innkreving,
            beslutning = beslutning.toString(),
            eksternReferanse = eksternReferanse
        )

        fun byggPeriode(
            periodeId: Int = (1..100).random(),
            fomDato: LocalDate = LocalDate.parse("2019-07-01"),
            tilDato: LocalDate? = LocalDate.parse("2020-01-01"),
            beløp: BigDecimal = BigDecimal.valueOf(3520),
            valutakode: String = "NOK",
            resultatkode: String = "KOSTNADSBEREGNET_BIDRAG",
            delytelseId: String = "delytelseId1"
        ) = Periode(
            id = periodeId,
            fomDato = fomDato,
            tilDato = tilDato,
            stønadsendring = byggStønadsendring(),
            beløp = beløp,
            valutakode = valutakode,
            resultatkode = resultatkode,
            delytelseId = delytelseId
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
            innkreving: String = "JA",
            beslutning: Beslutningstype = Beslutningstype.ENDRING,
            omgjørVedtakId: Int = 123,
            referanse: String = "referanse5",
            delytelseId: String = "delytelseId5",
            eksternReferanse: String = "eksternReferanse5"
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
            innkreving = innkreving,
            beslutning = beslutning.toString(),
            omgjørVedtakId = omgjørVedtakId,
            referanse = referanse,
            delytelseId = delytelseId,
            eksternReferanse = eksternReferanse
        )

        fun byggEngangsbeløpGrunnlagBo(
            engangsbeløpId: Int = (1..100).random(),
            grunnlagId: Int = (1..100).random()
        ) = EngangsbeløpGrunnlagBo(
            engangsbeløpId = engangsbeløpId,
            grunnlagId = grunnlagId
        )

        fun byggEngangsbeløpGrunnlag(
            engangsbeløp: Engangsbeløp = byggEngangsbeløp(),
            grunnlag: Grunnlag = byggGrunnlag()
        ) = EngangsbeløpGrunnlag(
            engangsbeløp = engangsbeløp,
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
