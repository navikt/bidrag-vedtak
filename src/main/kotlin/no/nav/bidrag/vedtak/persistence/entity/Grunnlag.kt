package no.nav.bidrag.vedtak.persistence.entity

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import no.nav.bidrag.domene.enums.grunnlag.Grunnlagstype
import no.nav.bidrag.transport.behandling.felles.grunnlag.GrunnlagDto
import no.nav.bidrag.transport.behandling.vedtak.request.OpprettGrunnlagRequestDto
import no.nav.bidrag.transport.felles.commonObjectmapper
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import kotlin.reflect.full.memberProperties

@Entity
data class Grunnlag(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grunnlagsid")
    val id: Int = 0,

    @Column(nullable = false, name = "referanse")
    val referanse: String = "",

    @ManyToOne
    @JoinColumn(name = "vedtaksid")
    val vedtak: Vedtak = Vedtak(),

    @Column(nullable = false, name = "type")
    val type: String = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, name = "innhold")
    val innhold: String = "",

    @Column
    val gjelderReferanse: String? = null,

    @Column(nullable = false)
    val grunnlagsreferanseListe: List<String> = emptyList(),
)

fun OpprettGrunnlagRequestDto.toGrunnlagEntity(vedtak: Vedtak) = with(::Grunnlag) {
    val propertiesByName = OpprettGrunnlagRequestDto::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                Grunnlag::id.name -> 0
                Grunnlag::vedtak.name -> vedtak
                Grunnlag::type.name -> type.toString()
                Grunnlag::innhold.name -> innhold.toString()
                Grunnlag::grunnlagsreferanseListe.name -> grunnlagsreferanseListe
                Grunnlag::gjelderReferanse.name -> gjelderReferanse
                else -> propertiesByName[parameter.name]?.get(this@toGrunnlagEntity)
            }
        },
    )
}

fun Grunnlag.toGrunnlagDto() = with(::GrunnlagDto) {
    val propertiesByName = Grunnlag::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                GrunnlagDto::type.name -> commonObjectmapper.readValue("\"$type\"", Grunnlagstype::class.java)
                GrunnlagDto::innhold.name -> stringTilJsonNode(innhold)
                GrunnlagDto::grunnlagsreferanseListe.name -> grunnlagsreferanseListe
                GrunnlagDto::gjelderReferanse.name -> gjelderReferanse
                else -> propertiesByName[parameter.name]?.get(this@toGrunnlagDto)
            }
        },
    )
}

private fun stringTilJsonNode(innhold: String): JsonNode {
    val mapper = ObjectMapper()
    return mapper.readTree(innhold)
}
