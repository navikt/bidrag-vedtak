package no.nav.bidrag.vedtak.persistence.repository

import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlag
import no.nav.bidrag.vedtak.persistence.entity.EngangsbelopGrunnlagPK
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface EngangsbelopGrunnlagRepository : CrudRepository<EngangsbelopGrunnlag, EngangsbelopGrunnlagPK?>{

  @Query(
    "select ebg from EngangsbelopGrunnlag ebg " +
        "where ebg.engangsbelop.engangsbelopId = :engangsbelopId and ebg.grunnlag.grunnlagId = :grunnlagId")
  fun hentEngangsbelopGrunnlag(engangsbelopId: Int, grunnlagId: Int): EngangsbelopGrunnlag

  @Query(
    "select ebg from EngangsbelopGrunnlag ebg where ebg.engangsbelop.engangsbelopId = :engangsbelopId")
  fun hentAlleGrunnlagForEngangsbelop(engangsbelopId: Int): List<EngangsbelopGrunnlag>

}
