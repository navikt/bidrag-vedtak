package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.EngangsbeløpGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.EngangsbeløpGrunnlagPK
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface EngangsbeløpGrunnlagRepository : CrudRepository<EngangsbeløpGrunnlag, EngangsbeløpGrunnlagPK?> {

    @Query(
        "select ebg from EngangsbeløpGrunnlag ebg " +
            "where ebg.engangsbeløp.id = :engangsbeløpsid and ebg.grunnlag.id = :grunnlagsid",
    )
    fun hentEngangsbeløpGrunnlag(engangsbeløpsid: Int, grunnlagsid: Int): EngangsbeløpGrunnlag

    @Query(
        "select ebg from EngangsbeløpGrunnlag ebg where ebg.engangsbeløp.id = :engangsbeløpsid order by ebg.grunnlag.id",
    )
    fun hentAlleGrunnlagForEngangsbeløp(engangsbeløpsid: Int): List<EngangsbeløpGrunnlag>

    @Modifying
    @Query(
        "delete from EngangsbeløpGrunnlag er where er.engangsbeløp.vedtak.id = :vedtaksid",
    )
    fun deleteByEngangsbeløpVedtakId(@Param("vedtaksid") vedtaksid: Int): Int
}
