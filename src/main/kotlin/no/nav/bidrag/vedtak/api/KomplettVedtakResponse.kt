package no.nav.bidrag.vedtak.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.nav.bidrag.vedtak.dto.StonadsendringDto
import no.nav.bidrag.vedtak.persistence.entity.Grunnlag
import no.nav.bidrag.vedtak.persistence.entity.Stonadsendring
import java.time.LocalDateTime

@ApiModel
data class KomplettVedtakResponse(

  @ApiModelProperty(value = "Vedtak-id")
  var vedtakId: Int = 0,

  @ApiModelProperty(value = "Id til saksbehandler som oppretter vedtaket")
  var saksbehandlerId: String = "",

  @ApiModelProperty(value = "Id til enheten som er ansvarlig for vedtaket")
  var enhetId: String = "",

  @ApiModelProperty(value = "Opprettet timestamp")
  var opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @ApiModelProperty(value = "Liste over alle grunnlag som inngår i vedtaket")
  var grunnlagListe: List<Grunnlag> = emptyList(),

  @ApiModelProperty(value = "Liste over alle stønadsendringer som inngår i vedtaket")
  var stonadsendringListe: List<Stonadsendring> = emptyList()
)