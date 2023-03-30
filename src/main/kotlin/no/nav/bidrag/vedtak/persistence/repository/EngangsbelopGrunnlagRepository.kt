package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlagPK
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface EngangsbelopGrunnlagRepository : CrudRepository<EngangsbelopGrunnlag, EngangsbelopGrunnlagPK?> {

    @Query(
        "select ebg from EngangsbelopGrunnlag ebg " +
            "where ebg.engangsbelop.id = :engangsbelopId and ebg.grunnlag.id = :grunnlagId"
    )
    fun hentEngangsbelopGrunnlag(engangsbelopId: Int, grunnlagId: Int): EngangsbelopGrunnlag

    @Query(
        "select ebg from EngangsbelopGrunnlag ebg where ebg.engangsbelop.id = :engangsbelopId order by ebg.grunnlag.id"
    )
    fun hentAlleGrunnlagForEngangsbelop(engangsbelopId: Int): List<EngangsbelopGrunnlag>
}
