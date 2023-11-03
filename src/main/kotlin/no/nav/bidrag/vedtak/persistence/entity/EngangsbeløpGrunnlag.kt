package no.nav.bidrag.vedtak.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.io.Serializable

@IdClass(EngangsbeløpGrunnlagPK::class)
@Entity
@Table(name = "engangsbeløpgrunnlag")
data class EngangsbeløpGrunnlag(

    @Id
    @ManyToOne
    @JoinColumn(name = "engangsbeløpsid")
    val engangsbeløp: Engangsbeløp = Engangsbeløp(),

    @Id
    @ManyToOne
    @JoinColumn(name = "grunnlagsid")
    val grunnlag: Grunnlag = Grunnlag()

)

data class EngangsbeløpGrunnlagPK(val engangsbeløp: Int = 0, val grunnlag: Int = 0) : Serializable
