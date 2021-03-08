package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Stonad
import org.springframework.data.repository.CrudRepository

interface StonadRepository : CrudRepository<Stonad, Int?>
