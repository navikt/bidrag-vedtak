package no.nav.bidrag.vedtak.service

import io.micrometer.core.annotation.Timed
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
import no.nav.bidrag.vedtak.persistence.repository.BehandlingsreferanseRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbeløpGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.EngangsbeløpRepository
import no.nav.bidrag.vedtak.persistence.repository.GrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.PeriodeRepository
import no.nav.bidrag.vedtak.persistence.repository.StønadsendringGrunnlagRepository
import no.nav.bidrag.vedtak.persistence.repository.StønadsendringRepository
import no.nav.bidrag.vedtak.persistence.repository.VedtakRepository
import org.slf4j.LoggerFactory
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
    val behandlingsreferanseRepository: BehandlingsreferanseRepository
) {

    @Timed
    fun opprettVedtak(vedtak: Vedtak): Vedtak {
        return vedtakRepository.save(vedtak)
    }

    @Timed
    fun hentVedtak(id: Int): Vedtak {
        return vedtakRepository.findById(id).orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", id)) }
    }

    fun opprettStønadsendring(stønadsendring: Stønadsendring): Stønadsendring {
        vedtakRepository.findById(stønadsendring.vedtak.id)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", stønadsendring.vedtak.id)) }
        return stønadsendringRepository.save(stønadsendring)
    }

    fun hentAlleStønadsendringerForVedtak(id: Int): List<Stønadsendring> {
        return stønadsendringRepository.hentAlleStønadsendringerForVedtak(id)
    }

    fun opprettPeriode(periode: Periode): Periode {
        stønadsendringRepository.findById(periode.stønadsendring.id)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke stønadsendring med id %d i databasen", periode.stønadsendring.id)) }
        return periodeRepository.save(periode)
    }

    fun hentAllePerioderForStønadsendring(id: Int): List<Periode> {
        return periodeRepository.hentAllePerioderForStønadsendring(id)
    }

    fun hentAlleGrunnlagForStønadsendring(stønadsendringId: Int): List<StønadsendringGrunnlag> {
        return stønadsendringGrunnlagRepository.hentAlleGrunnlagForStønadsendring(stønadsendringId)
    }

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

    fun hentAlleGrunnlagForVedtak(id: Int): List<Grunnlag> {
        return grunnlagRepository.hentAlleGrunnlagForVedtak(id)
    }

    fun slettAlleGrunnlagForVedtak(vedtakId: Int): Int {
        return grunnlagRepository.slettAlleGrunnlagForVedtak(vedtakId)
    }

    fun opprettPeriodeGrunnlag(periodeGrunnlagBo: PeriodeGrunnlagBo): PeriodeGrunnlag {
        val eksisterendePeriode = periodeRepository.findById(periodeGrunnlagBo.periodeId)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke periode med id %d i databasen", periodeGrunnlagBo.periodeId)) }
        val eksisterendeGrunnlag = grunnlagRepository.findById(periodeGrunnlagBo.grunnlagId)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", periodeGrunnlagBo.grunnlagId)) }
        val nyttPeriodeGrunnlag = PeriodeGrunnlag(eksisterendePeriode, eksisterendeGrunnlag)
//    SECURE_LOGGER.info("bidrag-vedtak - nyttPeriodeGrunnlag: $nyttPeriodeGrunnlag")
        return periodeGrunnlagRepository.save(nyttPeriodeGrunnlag)
    }

    fun hentAlleGrunnlagForPeriode(periodeId: Int): List<PeriodeGrunnlag> {
        return periodeGrunnlagRepository.hentAlleGrunnlagForPeriode(periodeId)
    }

    fun opprettEngangsbeløp(engangsbeløp: Engangsbeløp): Engangsbeløp {
        vedtakRepository.findById(engangsbeløp.vedtak.id)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", engangsbeløp.vedtak.id)) }
        return engangsbeløpRepository.save(engangsbeløp)
    }

    fun hentAlleEngangsbeløpForVedtak(id: Int): List<Engangsbeløp> {
        return engangsbeløpRepository.hentAlleEngangsbeløpForVedtak(id)
    }

    fun opprettEngangsbeløpGrunnlag(engangsbeløpGrunnlagBo: EngangsbeløpGrunnlagBo): EngangsbeløpGrunnlag {
        val eksisterendeEngangsbeløp = engangsbeløpRepository.findById(engangsbeløpGrunnlagBo.engangsbeløpId)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke engangsbeløp med id %d i databasen", engangsbeløpGrunnlagBo.engangsbeløpId)) }
        val eksisterendeGrunnlag = grunnlagRepository.findById(engangsbeløpGrunnlagBo.grunnlagId)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke grunnlag med id %d i databasen", engangsbeløpGrunnlagBo.grunnlagId)) }
        val nyttEngangsbeløpGrunnlag = EngangsbeløpGrunnlag(eksisterendeEngangsbeløp, eksisterendeGrunnlag)
//    SECURE_LOGGER.info("nyttEngangsbeløpGrunnlag: $nyttEngangsbeløpGrunnlag")
        return engangsbeløpGrunnlagRepository.save(nyttEngangsbeløpGrunnlag)
    }

    fun hentAlleGrunnlagForEngangsbeløp(engangsbeløpId: Int): List<EngangsbeløpGrunnlag> {
        return engangsbeløpGrunnlagRepository.hentAlleGrunnlagForEngangsbeløp(engangsbeløpId)
    }

    fun opprettBehandlingsreferanse(behandlingsreferanse: Behandlingsreferanse): Behandlingsreferanse {
        vedtakRepository.findById(behandlingsreferanse.vedtak.id)
            .orElseThrow { IllegalArgumentException(String.format("Fant ikke vedtak med id %d i databasen", behandlingsreferanse.vedtak.id)) }
        return behandlingsreferanseRepository.save(behandlingsreferanse)
    }

    fun hentAlleBehandlingsreferanserForVedtak(id: Int): List<Behandlingsreferanse> {
        return behandlingsreferanseRepository.hentAlleBehandlingsreferanserForVedtak(id)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PersistenceService::class.java)
    }
}
