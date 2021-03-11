package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import org.springframework.data.repository.CrudRepository

interface StonadsendringRepository : CrudRepository<Stonadsendring, Int?>
