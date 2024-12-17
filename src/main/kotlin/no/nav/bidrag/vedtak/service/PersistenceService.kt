package no.nav.bidrag.vedtak.service

import io.micrometer.core.annotation.Timed
import no.nav.bidrag.transport.behandling.vedtak.request.HentVedtakForStønadRequest
import no.nav.bidrag.vedtak.SECURE_LOGGER
import no.nav.bidrag.vedtak.bo.EngangsbeløpGrunnlagBo
import no.nav.bidrag.vedtak.bo.PeriodeGrunnlagBo
import no.nav.bidrag.vedtak.bo.StønadsendringGrunnlagBo
import no.nav.bidrag.vedtak.persistence.entity.Behandlingsreferanse
import no.nav.bidrag.vedtak.persistence.entity.Engangsbeløp
import no.nav.bidrag.vedtak.persistence.entity.EngangsbeløpGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Periode
import no.nav.bidrag.vedtak.persistence.entity.PeriodeGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Stønadsendring
import no.nav.bidrag.vedtak.persistence.entity.StønadsendringGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import no.nav.bidrag.vedtak.persistence.repository.BehandlingsreferanseRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbeløpGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbeløpRepository
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import no.nav.bidrag.vedtak.persistence.repository.StønadsendringGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.StønadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import no.nav.bidrag.vedtak.util.VedtakUtil.Companion.tilJson
import org.springframework.stereotype.Service

@Service
class PersistenceService(
    val vedtakRepository: VedtakRepository,
    val stønadsendringRepository: StønadsendringRepository,
    val stønadsendringGrunnlagRepository: StønadsendringGrunnlagRepository,
    val periodeRepository: PeriodeRepository,
    val grunnlagRepository: GrunnlagRepository,
    val periodeGrunnlagRepository: PeriodeGrunnlagRepository,
    val engangsbeløpRepository: EngangsbeløpRepository,
    val engangsbeløpGrunnlagRepository: EngangsbeløpGrunnlagRepository,
    val behandlingsreferanseRepository: BehandlingsreferanseRepository,
) {

    @Timed
    fun opprettVedtak(vedtak: Vedtak): Vedtak = vedtakRepository.save(vedtak)

    @Timed
    fun hentVedtak(id: Int): Vedtak =
        vedtakRepository.findById(id).orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", id)) }

    fun opprettStønadsendring(stønadsendring: Stønadsendring): Stønadsendring {
        vedtakRepository.findById(stønadsendring.vedtak.id)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", stønadsendring.vedtak.id)) }
        return stønadsendringRepository.save(stønadsendring)
    }

    fun hentAlleStønadsendringerForVedtak(id: Int): List<Stønadsendring> = stønadsendringRepository.hentAlleStønadsendringerForVedtak(id)

    fun opprettPeriode(periode: Periode): Periode {
        stønadsendringRepository.findById(periode.stønadsendring.id)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke stønadsendring med id %d i databasen", periode.stønadsendring.id)) }
        return periodeRepository.save(periode)
    }

    fun hentAllePerioderForStønadsendring(id: Int): List<Periode> = periodeRepository.hentAllePerioderForStønadsendring(id)

    fun hentAlleGrunnlagForStønadsendring(stønadsendringId: Int): List<StønadsendringGrunnlag> =
        stønadsendringGrunnlagRepository.hentAlleGrunnlagForStønadsendring(stønadsendringId)

    fun opprettGrunnlag(grunnlag: Grunnlag): Grunnlag {
        vedtakRepository.findById(grunnlag.vedtak.id)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", grunnlag.vedtak.id)) }
        return grunnlagRepository.save(grunnlag)
    }

    fun hentGrunnlag(id: Int): Grunnlag {
        val grunnlag = grunnlagRepository.findById(id)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", id)) }
        return grunnlag
    }

    fun hentAlleGrunnlagForVedtak(id: Int): List<Grunnlag> = grunnlagRepository.hentAlleGrunnlagForVedtak(id)

    fun slettAlleGrunnlagForVedtak(vedtakId: Int): Int = grunnlagRepository.slettAlleGrunnlagForVedtak(vedtakId)

    fun opprettStønadsendringGrunnlag(stønadsendringGrunnlagBo: StønadsendringGrunnlagBo): StønadsendringGrunnlag {
        val eksisterendeStønadsendring = stønadsendringRepository.findById(stønadsendringGrunnlagBo.stønadsendringsid)
            .orElseThrow {
                IllegalArgumentException(
                    String.format("Fant ikke stønadsendring med id %d i databasen", stønadsendringGrunnlagBo.stønadsendringsid),
                )
            }
        val eksisterendeGrunnlag = grunnlagRepository.findById(stønadsendringGrunnlagBo.grunnlagsid)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", stønadsendringGrunnlagBo.grunnlagsid)) }
        val nyStønadsendringGrunnlag = StønadsendringGrunnlag(eksisterendeStønadsendring, eksisterendeGrunnlag)
        SECURE_LOGGER.debug("nyStønadsendringGrunnlag: ${tilJson(nyStønadsendringGrunnlag)}")
        return stønadsendringGrunnlagRepository.save(nyStønadsendringGrunnlag)
    }

    fun opprettPeriodeGrunnlag(periodeGrunnlagBo: PeriodeGrunnlagBo): PeriodeGrunnlag {
        val eksisterendePeriode = periodeRepository.findById(periodeGrunnlagBo.periodeid)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", periodeGrunnlagBo.periodeid)) }
        val eksisterendeGrunnlag = grunnlagRepository.findById(periodeGrunnlagBo.grunnlagsid)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", periodeGrunnlagBo.grunnlagsid)) }
        val nyttPeriodeGrunnlag = PeriodeGrunnlag(eksisterendePeriode, eksisterendeGrunnlag)
        SECURE_LOGGER.debug("nyttPeriodeGrunnlag: ${tilJson(nyttPeriodeGrunnlag)}")
        return periodeGrunnlagRepository.save(nyttPeriodeGrunnlag)
    }

    fun hentAlleGrunnlagForPeriode(periodeId: Int): List<PeriodeGrunnlag> = periodeGrunnlagRepository.hentAlleGrunnlagForPeriode(periodeId)

    fun opprettEngangsbeløp(engangsbeløp: Engangsbeløp): Engangsbeløp {
        vedtakRepository.findById(engangsbeløp.vedtak.id)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", engangsbeløp.vedtak.id)) }
        return engangsbeløpRepository.save(engangsbeløp)
    }

    fun hentAlleEngangsbeløpForVedtak(id: Int): List<Engangsbeløp> = engangsbeløpRepository.hentAlleEngangsbeløpForVedtak(id)

    fun opprettEngangsbeløpGrunnlag(engangsbeløpGrunnlagBo: EngangsbeløpGrunnlagBo): EngangsbeløpGrunnlag {
        val eksisterendeEngangsbeløp = engangsbeløpRepository.findById(engangsbeløpGrunnlagBo.engangsbeløpsid)
            .orElseThrow {
                IllegalArgumentException(
                    String.format("Fant ikke engangsbeløp med id %d i databasen", engangsbeløpGrunnlagBo.engangsbeløpsid),
                )
            }
        val eksisterendeGrunnlag = grunnlagRepository.findById(engangsbeløpGrunnlagBo.grunnlagsid)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", engangsbeløpGrunnlagBo.grunnlagsid)) }
        val nyttEngangsbeløpGrunnlag = EngangsbeløpGrunnlag(eksisterendeEngangsbeløp, eksisterendeGrunnlag)
        SECURE_LOGGER.debug("nyttEngangsbeløpGrunnlag: ${tilJson(nyttEngangsbeløpGrunnlag)}")
        return engangsbeløpGrunnlagRepository.save(nyttEngangsbeløpGrunnlag)
    }

    fun hentAlleGrunnlagForEngangsbeløp(engangsbeløpId: Int): List<EngangsbeløpGrunnlag> =
        engangsbeløpGrunnlagRepository.hentAlleGrunnlagForEngangsbeløp(engangsbeløpId)

    fun opprettBehandlingsreferanse(behandlingsreferanse: Behandlingsreferanse): Behandlingsreferanse {
        vedtakRepository.findById(behandlingsreferanse.vedtak.id)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", behandlingsreferanse.vedtak.id)) }
        return behandlingsreferanseRepository.save(behandlingsreferanse)
    }

    fun hentAlleBehandlingsreferanserForVedtak(id: Int): List<Behandlingsreferanse> =
        behandlingsreferanseRepository.hentAlleBehandlingsreferanserForVedtak(id)

    fun referanseErUnik(vedtaksid: Int, referanse: String): Boolean = engangsbeløpRepository.sjekkReferanse(vedtaksid, referanse).isNullOrBlank()

    fun hentStønadsendringForStønad(request: HentVedtakForStønadRequest): List<Stønadsendring> = stønadsendringRepository.hentVedtakForStønad(
        request.sak.toString(),
        request.type.toString(),
        request.skyldner.verdi,
        request.kravhaver.verdi,
    )

    fun hentVedtaksidForBehandlingsreferanse(kilde: String, behandlingsreferanse: String): List<Int> {
        val vedtakFunnet = behandlingsreferanseRepository.hentVedtaksidForBehandlingsreferanse(kilde, behandlingsreferanse)
        return if (vedtakFunnet.isEmpty()) {
            emptyList()
        } else {
            vedtakFunnet.map { it.vedtak.id }
        }
    }
}
