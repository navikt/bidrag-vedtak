package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Vedtak
import org.springframework.data.repository.CrudRepository

interface VedtakRepository : CrudRepository<Vedtak, Int?>
