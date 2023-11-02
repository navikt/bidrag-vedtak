package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.EngangsbeløpGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.EngangsbeløpGrunnlagPK
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface EngangsbeløpGrunnlagRepository : CrudRepository<EngangsbeløpGrunnlag, EngangsbeløpGrunnlagPK?> {

    @Query(
        "select ebg from EngangsbeløpGrunnlag ebg " +
            "where ebg.engangsbeløp.id = :engangsbeløpId and ebg.grunnlag.id = :grunnlagId"
    )
    fun hentEngangsbeløpGrunnlag(engangsbeløpId: Int, grunnlagId: Int): EngangsbeløpGrunnlag

    @Query(
        "select ebg from EngangsbeløpGrunnlag ebg where ebg.engangsbeløp.id = :engangsbeløpId order by ebg.grunnlag.id"
    )
    fun hentAlleGrunnlagForEngangsbeløp(engangsbeløpId: Int): List<EngangsbeløpGrunnlag>
}
